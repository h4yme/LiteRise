"""
Test script for generate_game_content.php
Tests all 10 game types with a sample lesson.

Usage:
    python test_generate_game_content.py
    python test_generate_game_content.py --url https://yourdomain.com/api
    python test_generate_game_content.py --game minimal_pairs
"""

import argparse
import json
import time
import requests

# ── CONFIG ────────────────────────────────────────────────────────────────────
DEFAULT_BASE_URL = "http://localhost/api"   # change to your server URL

SAMPLE_LESSON = json.dumps({
    "topic": "CVCC Words - Blends and Clusters",
    "phonics_pattern": "CVCC",
    "grade_level": 3,
    "keywords": ["frog", "lamp", "milk", "drum", "stop", "fast", "jump", "log"],
    "example_sentences": [
        "The frog sat on a log.",
        "Maria lit the lamp at night.",
        "He drank cold milk at breakfast.",
        "The drum made a loud sound."
    ],
    "learning_objective": "Students can identify and read CVCC words correctly."
})

GAME_TYPES = [
    "minimal_pairs",
    "timed_trail",
    "picture_match",
    "story_sequencing",
    "synonym_sprint",
    "dialogue_reading",
    "fill_in_blanks",
    "sentence_scramble",
    "word_explosion",
    "word_hunt",
]

# Expected top-level keys in each game's content JSON
EXPECTED_KEYS = {
    "minimal_pairs":    ["pairs"],
    "timed_trail":      ["questions"],
    "picture_match":    ["items"],
    "story_sequencing": ["title", "events"],
    "synonym_sprint":   ["groups"],
    "dialogue_reading": ["lines"],
    "fill_in_blanks":   ["questions"],
    "sentence_scramble":["sentences"],
    "word_explosion":   ["categories"],
    "word_hunt":        ["words"],
}

# ── HELPERS ───────────────────────────────────────────────────────────────────
GREEN  = "\033[92m"
RED    = "\033[91m"
YELLOW = "\033[93m"
CYAN   = "\033[96m"
RESET  = "\033[0m"

def ok(msg):   print(f"  {GREEN}✓ {msg}{RESET}")
def fail(msg): print(f"  {RED}✗ {msg}{RESET}")
def info(msg): print(f"  {YELLOW}→ {msg}{RESET}")


def test_game(base_url: str, game_type: str, node_id: int = 101) -> bool:
    url = f"{base_url}/generate_game_content.php"
    payload = {
        "node_id": node_id,
        "game_type": game_type,
        "lesson_content": SAMPLE_LESSON,
    }

    print(f"\n{CYAN}▶ {game_type}{RESET}")
    try:
        t0 = time.time()
        r = requests.post(url, json=payload, timeout=60)
        elapsed = time.time() - t0

        info(f"HTTP {r.status_code}  ({elapsed:.1f}s)")

        if r.status_code != 200:
            fail(f"Non-200 response: {r.text[:300]}")
            return False

        data = r.json()

        # success flag
        if not data.get("success"):
            fail(f"success=false — {data.get('message', '(no message)')}")
            return False
        ok("success=true")

        # game_type echo
        if data.get("game_type") == game_type:
            ok(f"game_type='{game_type}'")
        else:
            fail(f"game_type mismatch: got '{data.get('game_type')}'")

        # from_cache
        info(f"from_cache={data.get('from_cache')}")

        # content structure
        content = data.get("content")
        if not isinstance(content, dict):
            fail(f"content is not a dict: {type(content)}")
            return False

        missing = [k for k in EXPECTED_KEYS[game_type] if k not in content]
        if missing:
            fail(f"content missing keys: {missing}")
            return False
        ok(f"content keys present: {EXPECTED_KEYS[game_type]}")

        # item count
        first_key  = EXPECTED_KEYS[game_type][0]
        first_list = content.get(first_key)
        if isinstance(first_list, list):
            ok(f"{first_key} has {len(first_list)} item(s)")
        elif isinstance(first_list, str):
            ok(f"{first_key}='{first_list[:40]}'")

        return True

    except requests.exceptions.ConnectionError:
        fail(f"Cannot connect to {url}")
        return False
    except requests.exceptions.Timeout:
        fail("Request timed out (60 s)")
        return False
    except Exception as exc:
        fail(f"Unexpected error: {exc}")
        return False


def test_invalid_inputs(base_url: str):
    url = f"{base_url}/generate_game_content.php"
    print(f"\n{CYAN}▶ Invalid-input tests{RESET}")
    cases = [
        ("missing node_id",       {"game_type": "picture_match", "lesson_content": SAMPLE_LESSON}),
        ("bad game_type",         {"node_id": 1, "game_type": "nonexistent", "lesson_content": SAMPLE_LESSON}),
        ("missing lesson_content",{"node_id": 1, "game_type": "picture_match"}),
        ("empty body",            {}),
    ]
    passed = 0
    for label, payload in cases:
        try:
            r = requests.post(url, json=payload, timeout=15)
            if r.status_code == 400:
                ok(f"{label} → 400 Bad Request")
                passed += 1
            else:
                fail(f"{label} → expected 400, got {r.status_code}")
        except Exception as exc:
            fail(f"{label} → {exc}")
    return passed == len(cases)


# ── MAIN ──────────────────────────────────────────────────────────────────────
def main():
    parser = argparse.ArgumentParser(description="Test generate_game_content.php")
    parser.add_argument("--url",  default=DEFAULT_BASE_URL,
                        help="Base API URL (default: %(default)s)")
    parser.add_argument("--game", default=None,
                        choices=GAME_TYPES,
                        help="Test a single game type only")
    args = parser.parse_args()

    base = args.url.rstrip("/")
    games_to_test = [args.game] if args.game else GAME_TYPES

    print(f"\n{'='*55}")
    print(f"  LiteRise — generate_game_content.php test suite")
    print(f"  Target: {base}")
    print(f"{'='*55}")

    results = {}

    for game in games_to_test:
        results[game] = test_game(base, game)
        time.sleep(0.5)   # gentle pacing so the API key isn't hammered

    if not args.game:
        results["_invalid_inputs"] = test_invalid_inputs(base)

    # ── Summary ──────────────────────────────────────────────────────────────
    total  = len(results)
    passed = sum(1 for v in results.values() if v)
    failed = total - passed

    print(f"\n{'='*55}")
    print(f"  Results: {GREEN}{passed} passed{RESET}  {RED}{failed} failed{RESET}  / {total} total")
    print(f"{'='*55}")

    if failed:
        print(f"\n{RED}Failed:{RESET}")
        for name, ok_flag in results.items():
            if not ok_flag:
                print(f"  • {name}")

    return 0 if failed == 0 else 1


if __name__ == "__main__":
    raise SystemExit(main())
