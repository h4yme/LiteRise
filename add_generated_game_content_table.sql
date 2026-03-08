-- ============================================================
-- GeneratedGameContent table
-- Caches AI-generated game content so the Anthropic API
-- is only called once per node+game_type combination.
-- ============================================================

IF NOT EXISTS (
    SELECT 1 FROM sys.tables WHERE name = 'GeneratedGameContent'
)
BEGIN
    CREATE TABLE GeneratedGameContent (
        ContentID   INT IDENTITY(1,1) PRIMARY KEY,
        NodeID      INT           NOT NULL,
        GameType    VARCHAR(40)   NOT NULL,   -- minimal_pairs | timed_trail | picture_match | story_sequencing | synonym_sprint
        ContentJSON NVARCHAR(MAX) NOT NULL,
        CreatedAt   DATETIME      NOT NULL DEFAULT GETDATE(),
        UpdatedAt   DATETIME      NOT NULL DEFAULT GETDATE(),
        CONSTRAINT UQ_GeneratedGameContent UNIQUE (NodeID, GameType)
    );

    CREATE INDEX IX_GeneratedGameContent_NodeGame
        ON GeneratedGameContent (NodeID, GameType);

    PRINT 'GeneratedGameContent table created.';
END
ELSE
BEGIN
    PRINT 'GeneratedGameContent table already exists – skipped.';
END
GO
