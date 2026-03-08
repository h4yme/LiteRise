-- ============================================================
-- LiteRise Badge Seed Data
-- Run this once against LiteRiseDB to populate the Badges table.
-- UnlockCondition format:
--   Module:N   -> complete all 13 nodes of Module N
--   XP:N       -> TotalXP >= N
--   Streak:N   -> LongestStreak >= N
--   Games:N    -> total games completed >= N
--   Lessons:N  -> total lessons completed >= N
--   Perfect:N  -> perfect-score games >= N
-- ============================================================

-- Guard: only insert if Badges table is empty
IF NOT EXISTS (SELECT 1 FROM Badges)
BEGIN

    -- ── Module completion badges (1 per module) ──────────────────
    INSERT INTO Badges (BadgeName, BadgeDescription, BadgeIconURL, UnlockCondition, XPReward, BadgeCategory)
    VALUES
    ('Grammar Starter',    'Completed Module 1: Grammar Fundamentals',   'badge_module1', 'Module:1', 100, 'module'),
    ('Word Wizard',        'Completed Module 2: Vocabulary Building',     'badge_module2', 'Module:2', 100, 'module'),
    ('Reading Champion',   'Completed Module 3: Reading Comprehension',   'badge_module3', 'Module:3', 100, 'module'),
    ('Writing Star',       'Completed Module 4: Writing Skills',          'badge_module4', 'Module:4', 100, 'module'),
    ('Speaking Hero',      'Completed Module 5: Listening & Speaking',    'badge_module5', 'Module:5', 100, 'module'),

    -- ── XP milestone badges ───────────────────────────────────────
    ('First Steps',        'Earned your first 100 XP',                   'badge_xp100',   'XP:100',   0, 'xp'),
    ('On a Roll',          'Earned 500 XP',                               'badge_xp500',   'XP:500',   0, 'xp'),
    ('XP Master',          'Earned 1000 XP',                              'badge_xp1000',  'XP:1000',  0, 'xp'),

    -- ── Streak badges ─────────────────────────────────────────────
    ('Hot Streak',         'Kept a 3-day learning streak',                'badge_streak3', 'Streak:3', 0, 'streak'),
    ('On Fire!',           'Kept a 7-day learning streak',                'badge_streak7', 'Streak:7', 0, 'streak'),

    -- ── Perfect score badges ──────────────────────────────────────
    ('Perfect!',           'Got a perfect score on a game',               'badge_perfect1','Perfect:1', 0, 'achievement'),
    ('Perfection Pro',     'Got 5 perfect scores',                        'badge_perfect5','Perfect:5', 0, 'achievement'),

    -- ── Lesson progress badges ────────────────────────────────────
    ('Learner',            'Completed 5 lessons',                         'badge_lessons5', 'Lessons:5',  0, 'progress'),
    ('Scholar',            'Completed 10 lessons',                        'badge_lessons10','Lessons:10', 0, 'progress'),
    ('Super Scholar',      'Completed 25 lessons',                        'badge_lessons25','Lessons:25', 0, 'progress');

END
GO
