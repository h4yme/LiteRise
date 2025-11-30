-- Fix Pronunciation Questions - Add AnswerChoices for MCQ format
-- This makes pronunciation questions show multiple choice options instead of requiring speech input

-- Very Easy Pronunciation Questions
UPDATE Items SET AnswerChoices = '["/kæt/", "/kɑt/", "/keɪt/", "/kat/"]' WHERE ItemID = 51;
UPDATE Items SET AnswerChoices = '["/deɪ/", "/daɪ/", "/diː/", "/dæ/"]' WHERE ItemID = 52;
UPDATE Items SET AnswerChoices = '["/ʃɪp/", "/ʃiːp/", "/sɪp/", "/ʃæp/"]' WHERE ItemID = 53;
UPDATE Items SET AnswerChoices = '["/sɪŋ/", "/siːŋ/", "/sæŋ/", "/saɪŋ/"]' WHERE ItemID = 54;
UPDATE Items SET AnswerChoices = '["/bed/", "/biːd/", "/bæd/", "/beɪd/"]' WHERE ItemID = 55;

-- Easy Pronunciation Questions
UPDATE Items SET AnswerChoices = '["/naɪt/", "/nɪt/", "/niːt/", "/næt/"]' WHERE ItemID = 56;
UPDATE Items SET AnswerChoices = '["/θɪŋk/", "/sɪŋk/", "/tɪŋk/", "/θæŋk/"]' WHERE ItemID = 57;
UPDATE Items SET AnswerChoices = '["/miːt/", "/met/", "/maɪt/", "/mɪt/"]' WHERE ItemID = 58;
UPDATE Items SET AnswerChoices = '["/bəˈnænə/", "/bæˈnænə/", "/bəˈnɑːnə/", "/ˈbænənə/"]' WHERE ItemID = 59;
UPDATE Items SET AnswerChoices = '["/sɪt/", "/siːt/", "/set/", "/saɪt/"]' WHERE ItemID = 60;

-- Medium Pronunciation Questions
UPDATE Items SET AnswerChoices = '["/ɪˈnʌf/", "/iˈnʌf/", "/ɪˈnɑf/", "/eˈnʌf/"]' WHERE ItemID = 61;
UPDATE Items SET AnswerChoices = '["/əˈbaʊt/", "/æˈbaʊt/", "/əˈbuːt/", "/aˈbaʊt/"]' WHERE ItemID = 62;
UPDATE Items SET AnswerChoices = '["/bɪˈɡɪn/", "/ˈbeɡɪn/", "/bəˈɡɪn/", "/biːˈɡɪn/"]' WHERE ItemID = 63;
UPDATE Items SET AnswerChoices = '["/beər/", "/bɪər/", "/bær/", "/biːr/"]' WHERE ItemID = 64;
UPDATE Items SET AnswerChoices = '["/klaɪm/", "/klɪm/", "/kliːm/", "/klaːm/"]' WHERE ItemID = 65;

-- Hard Pronunciation Questions
UPDATE Items SET AnswerChoices = '["/haʊs/", "/huːs/", "/hæʊs/", "/hoʊs/"]' WHERE ItemID = 66;
UPDATE Items SET AnswerChoices = '["/θɔːt/", "/tɔːt/", "/θaʊt/", "/θoːt/"]' WHERE ItemID = 67;
UPDATE Items SET AnswerChoices = '["/ˌʌndəˈstænd/", "/ˌʌndərˈstænd/", "/ˌʌnˈdəstænd/", "/ˌʊndəˈstænd/"]' WHERE ItemID = 68;
UPDATE Items SET AnswerChoices = '["/fəˈtɑɡrəfi/", "/ˈfɑtoʊɡræfi/", "/fɑˈtɑɡrəfi/", "/fəˈtoːɡrafi/"]' WHERE ItemID = 69;
UPDATE Items SET AnswerChoices = '["/ˈmeʒər/", "/ˈmiːʒər/", "/ˈmezər/", "/ˈmeɪʒər/"]' WHERE ItemID = 70;

-- Very Hard Pronunciation Questions
UPDATE Items SET AnswerChoices = '["/ˌɑntrəprəˈnɜːr/", "/ˌɑntrəˈprenər/", "/ˌentrəprəˈnɜːr/", "/ˌɑntrəˈprenʊr/"]' WHERE ItemID = 71;
UPDATE Items SET AnswerChoices = '["/bjʊˈrɑkrəsi/", "/ˈbjʊroʊkræsi/", "/bjuːˈrɑkrəsi/", "/bəˈrɑkrəsi/"]' WHERE ItemID = 72;
UPDATE Items SET AnswerChoices = '["/ˌkɑnʃiˈenʃəs/", "/ˌkɑnsiˈenʃəs/", "/kɑnˈʃenʃəs/", "/ˌkɑʃiˈenʃəs/"]' WHERE ItemID = 73;
UPDATE Items SET AnswerChoices = '["/ˌɑnəˌmætəˈpiːə/", "/ˌɑnoʊˈmætoʊpiə/", "/ˌɑnəˈmætoʊpiə/", "/ˌɑməˌtætoˈpiːə/"]' WHERE ItemID = 74;
UPDATE Items SET AnswerChoices = '["/njuːˈmoʊniə/", "/nuːˈmoʊniə/", "/pnjuːˈmoʊniə/", "/nəˈmoʊniə/"]' WHERE ItemID = 75;

-- Verify the updates
SELECT ItemID, ItemText, ItemType, AnswerChoices
FROM Items
WHERE ItemType = 'Pronunciation'
ORDER BY ItemID;
