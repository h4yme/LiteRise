USE [LiteRiseDB]
GO

-- Add Phonetic and Definition fields to Items table for Pronunciation support
-- Phonetic: IPA or simplified phonetic breakdown (e.g., "/ˈæp.əl/" or "AP-uhl")
-- Definition: Word definition to display when user clicks on the word

-- Check if columns don't exist before adding them
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'[dbo].[Items]') AND name = 'Phonetic')
BEGIN
    ALTER TABLE [dbo].[Items]
    ADD Phonetic NVARCHAR(200) NULL;
    PRINT 'Phonetic column added to Items table';
END
ELSE
BEGIN
    PRINT 'Phonetic column already exists in Items table';
END

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'[dbo].[Items]') AND name = 'Definition')
BEGIN
    ALTER TABLE [dbo].[Items]
    ADD Definition NVARCHAR(500) NULL;
    PRINT 'Definition column added to Items table';
END
ELSE
BEGIN
    PRINT 'Definition column already exists in Items table';
END

GO

-- Update sample pronunciation items with phonetic and definition data
-- Example: Update existing pronunciation items (if any)

-- Update pronunciation items if they exist
UPDATE [dbo].[Items]
SET
    Phonetic = CASE ItemText
        WHEN 'apple' THEN '/ˈæp.əl/'
        WHEN 'beautiful' THEN '/ˈbjuː.tɪ.fəl/'
        WHEN 'excellent' THEN '/ˈek.səl.ənt/'
        WHEN 'knowledge' THEN '/ˈnɒl.ɪdʒ/'
        WHEN 'library' THEN '/ˈlaɪ.brər.i/'
        WHEN 'necessary' THEN '/ˈnes.ə.ser.i/'
        WHEN 'recommend' THEN '/ˌrek.əˈmend/'
        WHEN 'restaurant' THEN '/ˈres.tər.ɑːnt/'
        WHEN 'definitely' THEN '/ˈdef.ɪ.nət.li/'
        WHEN 'environment' THEN '/ɪnˈvaɪ.rən.mənt/'
        ELSE Phonetic
    END,
    Definition = CASE ItemText
        WHEN 'apple' THEN 'A round fruit with red, green, or yellow skin and white flesh'
        WHEN 'beautiful' THEN 'Pleasing the senses or mind aesthetically'
        WHEN 'excellent' THEN 'Extremely good; outstanding'
        WHEN 'knowledge' THEN 'Facts, information, and skills acquired through experience or education'
        WHEN 'library' THEN 'A building or room containing collections of books for people to read or borrow'
        WHEN 'necessary' THEN 'Required to be done, achieved, or present; needed; essential'
        WHEN 'recommend' THEN 'To suggest that someone or something would be good or suitable for a particular purpose'
        WHEN 'restaurant' THEN 'A place where people pay to sit and eat meals that are cooked and served on the premises'
        WHEN 'definitely' THEN 'Without doubt; certainly'
        WHEN 'environment' THEN 'The surroundings or conditions in which a person, animal, or plant lives or operates'
        ELSE Definition
    END
WHERE ItemType = 'Pronunciation' AND ItemText IN (
    'apple', 'beautiful', 'excellent', 'knowledge', 'library',
    'necessary', 'recommend', 'restaurant', 'definitely', 'environment'
);

PRINT 'Sample pronunciation items updated with phonetic and definition data';
GO
