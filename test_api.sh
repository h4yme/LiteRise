#!/bin/bash

# LiteRise API Quick Test Script
# Tests all endpoints to verify functionality

BASE_URL="http://localhost/api"
STUDENT_EMAIL="maria.santos@student.com"
STUDENT_PASS="password123"

echo "======================================"
echo "🧪 LiteRise API Test Suite"
echo "======================================"

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Test 1: Database Connection
echo -e "\n${YELLOW}[1/7] Testing Database Connection...${NC}"
RESPONSE=$(curl -s "$BASE_URL/test_db.php")
if echo "$RESPONSE" | grep -q '"status":"success"'; then
    echo -e "${GREEN}✓ Database connection successful${NC}"
    echo "$RESPONSE" | jq -r '.statistics'
else
    echo -e "${RED}✗ Database connection failed${NC}"
    echo "$RESPONSE"
    exit 1
fi

# Test 2: Student Login
echo -e "\n${YELLOW}[2/7] Testing Student Login...${NC}"
LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/login.php" \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"$STUDENT_EMAIL\",\"password\":\"$STUDENT_PASS\"}")

if echo "$LOGIN_RESPONSE" | grep -q 'StudentID'; then
    STUDENT_ID=$(echo "$LOGIN_RESPONSE" | jq -r '.StudentID')
    STUDENT_NAME=$(echo "$LOGIN_RESPONSE" | jq -r '.FullName')
    echo -e "${GREEN}✓ Login successful${NC}"
    echo "  Student: $STUDENT_NAME (ID: $STUDENT_ID)"
else
    echo -e "${RED}✗ Login failed${NC}"
    echo "$LOGIN_RESPONSE"
    exit 1
fi

# Test 3: Create Test Session
echo -e "\n${YELLOW}[3/7] Creating Test Session...${NC}"
SESSION_RESPONSE=$(curl -s -X POST "$BASE_URL/create_session.php" \
  -H "Content-Type: application/json" \
  -d "{\"StudentID\":$STUDENT_ID,\"SessionType\":\"PreAssessment\"}")

if echo "$SESSION_RESPONSE" | grep -q 'SessionID'; then
    SESSION_ID=$(echo "$SESSION_RESPONSE" | jq -r '.SessionID')
    INITIAL_THETA=$(echo "$SESSION_RESPONSE" | jq -r '.InitialTheta')
    echo -e "${GREEN}✓ Session created${NC}"
    echo "  SessionID: $SESSION_ID"
    echo "  Initial Ability (θ): $INITIAL_THETA"
else
    echo -e "${RED}✗ Session creation failed${NC}"
    echo "$SESSION_RESPONSE"
    exit 1
fi

# Test 4: Get Assessment Items
echo -e "\n${YELLOW}[4/7] Fetching Assessment Items...${NC}"
ITEMS_RESPONSE=$(curl -s -X POST "$BASE_URL/get_preassessment_items.php")

ITEM_COUNT=$(echo "$ITEMS_RESPONSE" | jq '. | length')
if [ "$ITEM_COUNT" -gt 0 ]; then
    echo -e "${GREEN}✓ Retrieved $ITEM_COUNT items${NC}"
    echo "  First item type:" $(echo "$ITEMS_RESPONSE" | jq -r '.[0].ItemType')
    echo "  First item difficulty:" $(echo "$ITEMS_RESPONSE" | jq -r '.[0].Difficulty')

    # Get first two item IDs for testing
    ITEM1=$(echo "$ITEMS_RESPONSE" | jq -r '.[0].ItemID')
    ITEM2=$(echo "$ITEMS_RESPONSE" | jq -r '.[1].ItemID')
else
    echo -e "${RED}✗ Failed to retrieve items${NC}"
    echo "$ITEMS_RESPONSE"
    exit 1
fi

# Test 5: Submit Responses
echo -e "\n${YELLOW}[5/7] Submitting Sample Responses...${NC}"
SUBMIT_RESPONSE=$(curl -s -X POST "$BASE_URL/submit_responses.php" \
  -H "Content-Type: application/json" \
  -d "{
    \"StudentID\": $STUDENT_ID,
    \"SessionID\": $SESSION_ID,
    \"Responses\": [
      {\"ItemID\": $ITEM1, \"SelectedOption\": \"A\", \"Correct\": true, \"TimeTakenSec\": 15.5},
      {\"ItemID\": $ITEM2, \"SelectedOption\": \"B\", \"Correct\": true, \"TimeTakenSec\": 12.3}
    ]
  }")

if echo "$SUBMIT_RESPONSE" | grep -q '"success":true'; then
    FINAL_THETA=$(echo "$SUBMIT_RESPONSE" | jq -r '.FinalTheta')
    ACCURACY=$(echo "$SUBMIT_RESPONSE" | jq -r '.Accuracy')
    RELIABILITY=$(echo "$SUBMIT_RESPONSE" | jq -r '.Reliability')
    echo -e "${GREEN}✓ Responses submitted${NC}"
    echo "  Final Ability (θ): $FINAL_THETA"
    echo "  Accuracy: $ACCURACY%"
    echo "  Reliability: $RELIABILITY"
else
    echo -e "${RED}✗ Response submission failed${NC}"
    echo "$SUBMIT_RESPONSE"
    exit 1
fi

# Test 6: Get Student Progress
echo -e "\n${YELLOW}[6/7] Checking Student Progress...${NC}"
PROGRESS_RESPONSE=$(curl -s "$BASE_URL/get_student_progress.php?StudentID=$STUDENT_ID")

if echo "$PROGRESS_RESPONSE" | grep -q 'CurrentAbility'; then
    CURRENT_ABILITY=$(echo "$PROGRESS_RESPONSE" | jq -r '.CurrentAbility')
    TOTAL_XP=$(echo "$PROGRESS_RESPONSE" | jq -r '.TotalXP')
    TOTAL_SESSIONS=$(echo "$PROGRESS_RESPONSE" | jq -r '.TotalSessions')
    echo -e "${GREEN}✓ Progress retrieved${NC}"
    echo "  Current Ability: $CURRENT_ABILITY"
    echo "  Total XP: $TOTAL_XP"
    echo "  Total Sessions: $TOTAL_SESSIONS"
else
    echo -e "${RED}✗ Progress retrieval failed${NC}"
    echo "$PROGRESS_RESPONSE"
    exit 1
fi

# Test 7: Get Personalized Lessons
echo -e "\n${YELLOW}[7/7] Getting Personalized Lessons...${NC}"
LESSONS_RESPONSE=$(curl -s "$BASE_URL/get_lessons.php?StudentID=$STUDENT_ID")

LESSON_COUNT=$(echo "$LESSONS_RESPONSE" | jq '. | length')
if [ "$LESSON_COUNT" -gt 0 ]; then
    echo -e "${GREEN}✓ Retrieved $LESSON_COUNT personalized lessons${NC}"
    echo "  First lesson:" $(echo "$LESSONS_RESPONSE" | jq -r '.[0].LessonTitle')
else
    echo -e "${YELLOW}⚠ No lessons available yet${NC}"
fi

# Summary
echo -e "\n======================================"
echo -e "${GREEN}✓ All API Tests Passed!${NC}"
echo "======================================"
echo ""
echo "API is ready for Android app integration"
echo ""
echo "Test Summary:"
echo "  Student: $STUDENT_NAME"
echo "  Initial θ: $INITIAL_THETA"
echo "  Final θ: $FINAL_THETA"
echo "  Sessions: $TOTAL_SESSIONS"
echo "  XP: $TOTAL_XP"
echo ""
