-- ============================================================
-- populate_lesson_content.sql
-- Copies ContentJSON from Nodes into Lessons.LessonContent.
-- LessonID = NodeID (1:1 match for all 65 rows).
-- Run once on the SQL Server database.
-- ============================================================

-- Preview rows that will be updated (run SELECT first to verify)
SELECT
    L.LessonID,
    L.LessonTitle,
    LEN(N.ContentJSON) AS ContentJSONLength
FROM dbo.Lessons L
INNER JOIN dbo.Nodes N ON N.NodeID = L.LessonID
WHERE L.LessonContent IS NULL;

-- ============================================================
-- Actual UPDATE: copy Nodes.ContentJSON → Lessons.LessonContent
-- ============================================================
UPDATE L
SET L.LessonContent = N.ContentJSON
FROM dbo.Lessons L
INNER JOIN dbo.Nodes N ON N.NodeID = L.LessonID
WHERE L.LessonContent IS NULL;

-- Verify: should return 0 rows with NULL LessonContent
SELECT LessonID, LessonTitle
FROM dbo.Lessons
WHERE LessonContent IS NULL;

PRINT 'Done. LessonContent populated for all ' + CAST(@@ROWCOUNT AS VARCHAR) + ' rows.';
