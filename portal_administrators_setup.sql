-- =============================================================
-- LiteRise Portal — Administrators table + stored procedures
-- Run this once against the literisedb database before using
-- the Teacher / Admin portal.
-- =============================================================

-- ─── Table ───────────────────────────────────────────────────
IF NOT EXISTS (
    SELECT 1 FROM sys.tables WHERE name = 'Administrators'
)
BEGIN
    CREATE TABLE Administrators (
        AdminID      INT           IDENTITY(1,1) PRIMARY KEY,
        FirstName    NVARCHAR(100) NOT NULL,
        LastName     NVARCHAR(100) NOT NULL,
        Email        NVARCHAR(255) NOT NULL UNIQUE,
        PasswordHash NVARCHAR(255) NOT NULL,
        Role         NVARCHAR(20)  NOT NULL
                     CONSTRAINT CK_Admins_Role CHECK (Role IN ('Admin', 'Teacher')),
        SchoolID     INT           NULL,   -- Teacher's assigned school (NULL = all schools)
        IsActive     BIT           NOT NULL DEFAULT 1,
        LastLogin    DATETIME      NULL,
        CreatedAt    DATETIME      NOT NULL DEFAULT GETDATE()
    );
END
GO

-- ─── SP: Look up admin/teacher by email ──────────────────────
CREATE OR ALTER PROCEDURE SP_AdminLogin
    @Email NVARCHAR(255)
AS
BEGIN
    SET NOCOUNT ON;
    SELECT AdminID, FirstName, LastName, Email, PasswordHash, Role, SchoolID, IsActive
    FROM   Administrators
    WHERE  Email    = @Email
      AND  IsActive = 1;
END
GO

-- ─── SP: Stamp last login time ───────────────────────────────
CREATE OR ALTER PROCEDURE SP_UpdateAdminLastLogin
    @AdminID INT
AS
BEGIN
    SET NOCOUNT ON;
    UPDATE Administrators
    SET    LastLogin = GETDATE()
    WHERE  AdminID  = @AdminID;
END
GO

-- ─── Seed default accounts ───────────────────────────────────
-- Run portal/setup/seed_admin.php to insert real bcrypt hashes.
-- The placeholder hashes below will NOT work for login.
-- They are present only so the schema is self-documenting.
--
-- Default credentials (set in seed_admin.php):
--   admin@literise.com   / LiteRise@2025  (Role: Admin)
--   teacher@literise.com / LiteRise@2025  (Role: Teacher)
--
-- IMPORTANT: change passwords before deploying to production.
