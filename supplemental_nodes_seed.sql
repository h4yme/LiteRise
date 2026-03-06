-- =============================================================================
-- LiteRise Supplemental Nodes Seed
-- INTERVENTION nodes for all 60 CORE_LESSON nodes (101-112, 201-212, 301-312,
-- 401-412, 501-512) + SUPPLEMENTAL nodes for weak areas + ENRICHMENT for Q4.
-- Run after database_seed.sql and migrate_nodes.sql.
-- Safe to re-run: wrapped in TRY/CATCH.
-- =============================================================================

USE LiteRiseDB;
GO

-- Clear existing supplemental nodes so re-runs are idempotent
IF OBJECT_ID('StudentSupplementalProgress','U') IS NOT NULL
    DELETE FROM StudentSupplementalProgress;
IF OBJECT_ID('SupplementalNodes','U') IS NOT NULL
    DELETE FROM SupplementalNodes;
GO

-- =============================================================================
-- MODULE 1: PHONICS AND WORD STUDY (AfterNodeID 101-112)
-- =============================================================================
BEGIN TRY
INSERT INTO SupplementalNodes
    (NodeType, AfterNodeID, TriggerLogic, Title, ContentJSON, SkillCategory, EstimatedDuration, XPReward, IsActive, IsVisible, CreatedDate)
VALUES
-- Node 101: Sight Words and CVCC Patterns
('INTERVENTION', 101, 'score < 70',
 'CVCC Remediation: Blending Ending Consonants',
 '{"instruction":"CVCC words end with TWO consonant sounds. Practice blending them together.","steps":["1. Say the vowel sound","2. Add the first consonant: H-A-N","3. Add the last consonant: H-A-N-D","4. Blend smoothly: HAND"],"practiceWords":["hand","belt","milk","lamp","sand","mist","fast","dust"],"sightWords":["could","light","always","around","bright"],"tip":"Tap your fingers for each sound: H-A-N-D = 4 taps!"}',
 'phonics', 12, 30, 1, 1, GETDATE()),

-- Node 102: CCVC Patterns
('INTERVENTION', 102, 'score < 70',
 'CCVC Remediation: Beginning Consonant Clusters',
 '{"instruction":"CCVC words START with two consonant sounds together. Practice each cluster.","clusters":{"fr":["frog","free","from"],"st":["stop","star","step"],"pl":["plan","play","plug"],"dr":["drum","draw","drip"],"cl":["clip","clap","clay"]},"tip":"Say both consonants without a vowel between them: FR not FUH-R!","practiceActivity":"Read each word aloud and clap when you hear the blend at the start."}',
 'phonics', 12, 30, 1, 1, GETDATE()),

-- Node 103: VCV and VCCV Syllable Patterns
('INTERVENTION', 103, 'score < 70',
 'Syllable Division Remediation: VCV vs VCCV',
 '{"instruction":"Where you divide a word changes the vowel sound!","rules":{"VCV":"Divide AFTER the first vowel → first vowel is LONG (says its name)","VCCV":"Divide BETWEEN the two consonants → first vowel is SHORT"},"vcvPractice":["Ti-ger","Pa-per","Ro-bot","Ba-by","Mu-sic"],"vccvPractice":["Rab-bit","Nap-kin","Pup-py","Kit-ten","Muf-fin"],"trick":"Look for the pattern of vowels and consonants in the middle of the word."}',
 'phonics', 15, 30, 1, 1, GETDATE()),

-- Node 104: Q2 Community Sight Words
('INTERVENTION', 104, 'score < 70',
 'Community Sight Words Practice',
 '{"instruction":"These words are used when talking about helping in the community. Read them until they feel automatic!","sightWords":["carry","clean","drink","full","light","small","bright","wash","those","grow"],"activities":{"flashCards":"Look at each word for 3 seconds, cover it, then write it.","sentences":["I will help clean the park.","She can carry the basket.","Please wash the vegetables before cooking."]},"tip":"Practice reading these words every day until you do not need to sound them out."}',
 'phonics', 10, 30, 1, 1, GETDATE()),

-- Node 105: VCV Open Syllable
('INTERVENTION', 105, 'score < 70',
 'VCV Open Syllable Remediation: Long Vowel Rule',
 '{"instruction":"VCV rule: ONE consonant between two vowels → divide AFTER the first vowel → it says its name (LONG sound)!","examples":[{"word":"Tiger","division":"Ti-ger","vowelSound":"Ti = long I"},{"word":"Paper","division":"Pa-per","vowelSound":"Pa = long A"},{"word":"Silent","division":"Si-lent","vowelSound":"Si = long I"}],"practiceWords":["basin","pilot","tulip","melon","hotel","robot","music","open","evil","baby"],"selfCheck":"Does the first vowel say its NAME? Then you divided it correctly!"}',
 'phonics', 12, 30, 1, 1, GETDATE()),

-- Node 106: VCCV Closed Syllable
('INTERVENTION', 106, 'score < 70',
 'VCCV Closed Syllable Remediation: Short Vowel Rule',
 '{"instruction":"VCCV rule: TWO consonants between vowels → divide BETWEEN them → first vowel is SHORT!","examples":[{"word":"Basket","division":"Bas-ket","vowelSound":"Bas = short A"},{"word":"Dinner","division":"Din-ner","vowelSound":"Din = short I"},{"word":"Garden","division":"Gar-den","vowelSound":"Gar = short A (r-controlled)"}],"practiceWords":["market","canteen","jacket","happen","kitten","rabbit","puppy","muffin","napkin","tennis"],"selfCheck":"Do you see TWO consonants together? Divide between them!"}',
 'phonics', 12, 30, 1, 1, GETDATE()),

-- Node 107: Narrative Sight Words
('INTERVENTION', 107, 'score < 70',
 'Action Sight Words: Sentence Practice',
 '{"instruction":"These action words appear in stories. Learn them by using them in your own sentences!","words":["bring","carry","draw","drink","fall"],"sentenceFrames":["Please _____ the book to me.","Can you _____ water for us?","I like to _____ pictures of animals."],"gameIdea":"Make up a short story using all 5 words!","writingPrompt":"Write 5 sentences, one for each word: bring, carry, draw, drink, fall."}',
 'phonics', 10, 30, 1, 1, GETDATE()),

-- Node 108: Descriptive Sight Words
('INTERVENTION', 108, 'score < 70',
 'Descriptive Sight Words: Antonym Pairs',
 '{"instruction":"These words DESCRIBE things. Remember them by learning their opposites (antonyms)!","antonymPairs":[["far","near"],["hot","cold"],["full","empty"],["light","dark"],["eight","?? (eight has no antonym - it is a number!)"]]},"tip":"Use antonym pairs as memory hooks: FAR is the opposite of NEAR. Now you know both!","sentencePractice":["The sun is hot, but the water is cold.","The room is full of light.","The star is very far away."]}',
 'phonics', 10, 30, 1, 1, GETDATE()),

-- Node 109: Comparison Sight Words
('INTERVENTION', 109, 'score < 70',
 'Comparison Words Remediation: Better vs Best',
 '{"instruction":"BETTER compares TWO things. BEST compares THREE or MORE things.","rule":{"better":"use when comparing 2 things","best":"use when comparing 3+ things"},"examples":[{"correct":"This mango is better than that one.","why":"comparing 2 mangoes"},{"correct":"This is the best mango in the market.","why":"comparing many mangoes"},{"wrong":"This is the better mango in the market.","fix":"Should be BEST (comparing many)"}],"practiceWords":["better","best","both","clean","small"],"quiz":"Choose the correct word: This apple is ___ (better/best) than the banana."}',
 'phonics', 10, 30, 1, 1, GETDATE()),

-- Node 110: Abstract Sight Words
('INTERVENTION', 110, 'score < 70',
 'Abstract Sight Words Remediation: Meaning & Context',
 '{"instruction":"These tricky words have meanings that cannot be pictured easily. Learn them through sentences!","words":{"together":"with each other","because":"for the reason that","should":"it is a good idea to","through":"from one side to the other","thought":"past tense of think; an idea"},"contextSentences":["We did it together as a team.","I stayed home because I was sick.","You should drink water every day.","The ball rolled through the tunnel.","She thought about the answer carefully."],"memoryTrick":"Use each word in a sentence about YOUR own life!"}',
 'phonics', 12, 30, 1, 1, GETDATE()),

-- Node 111: Quantity and Time Words
('INTERVENTION', 111, 'score < 70',
 'Quantity Words Remediation: Enough, Several, Always',
 '{"instruction":"These words talk about AMOUNT and TIME. Understand the exact meaning of each.","definitions":{"enough":"you have what you need, not too much, not too little","several":"more than 2 but not a lot (about 3-7)","always":"every single time, no exceptions","better":"improved compared to before","against":"opposing, not on the same side"},"practiceActivity":"Sort these words by meaning: always (time), enough (amount), several (amount), against (position)","fillInBlank":["I ate ___ (enough/several) rice to feel full.","There are ___ (several/always) birds on the tree."]}',
 'phonics', 12, 30, 1, 1, GETDATE()),

-- Node 112: Directional Sight Words
('INTERVENTION', 112, 'score < 70',
 'Directional Words Remediation: Spatial Relationships',
 '{"instruction":"These words tell you WHERE things are or which DIRECTION to go. Draw pictures to help you remember!","words":{"different":"not the same as","toward":"moving in the direction of","across":"from one side to the other side","carry":"to hold and move something","between":"in the middle of two things"},"drawingActivity":"Draw a simple map: put a tree, a house, and a river. Now use the words to describe where things are: The house is between the tree and the river.","sentences":["Walk toward the gate.","The cat is across the street.","Put it between the two boxes."]}',
 'phonics', 10, 30, 1, 1, GETDATE());

END TRY
BEGIN CATCH
    PRINT 'Phonics supplemental nodes error: ' + ERROR_MESSAGE();
END CATCH
GO

-- =============================================================================
-- MODULE 2: VOCABULARY AND WORD KNOWLEDGE (AfterNodeID 201-212)
-- =============================================================================
BEGIN TRY
INSERT INTO SupplementalNodes
    (NodeType, AfterNodeID, TriggerLogic, Title, ContentJSON, SkillCategory, EstimatedDuration, XPReward, IsActive, IsVisible, CreatedDate)
VALUES
-- Node 201: High-Frequency Words
('INTERVENTION', 201, 'score < 70',
 'High-Frequency Words Remediation: Flash Practice',
 '{"instruction":"High-frequency words appear in almost every text. You need to READ them instantly without sounding out!","words":["the","is","are","we","she","he","they","like","go","see","because","their","have"],"practiceMethod":"Flash Card Drill - Look at the word for 1 second, say it, move on. Repeat daily!","sentenceFrames":["They _____ my friends.","We _____ to the park.","She _____ happy.","I _____ to read."],"goal":"Read all 13 words in under 15 seconds without mistakes."}',
 'vocabulary', 10, 30, 1, 1, GETDATE()),

-- Node 202: Regional Vocabulary
('INTERVENTION', 202, 'score < 70',
 'Regional Words Remediation: Filipino Community Vocabulary',
 '{"instruction":"These words come from Filipino community life. Connect them to things you SEE in your neighborhood!","words":{"barangay":"smallest government unit; your neighborhood","fiesta":"a big community celebration","palengke":"wet market where people buy food","jeepney":"colorful Filipino public vehicle","farmer":"person who grows food in the fields"},"matchingGame":"Match the word to its picture or description.","writeAboutYourBarangay":"Use 3 of these words to describe your neighborhood.","tip":"Have you been to a palengke or a fiesta? Use what you know from real life!"}',
 'vocabulary', 10, 30, 1, 1, GETDATE()),

-- Node 203: Content Words Math/Science
('INTERVENTION', 203, 'score < 70',
 'Math and Science Words Remediation',
 '{"instruction":"Content-specific words are used in other subjects. Learn them by connecting to what you study in Math and Science class!","mathWords":{"sum":"the answer when you ADD","difference":"the answer when you SUBTRACT","product":"the answer when you MULTIPLY","quotient":"the answer when you DIVIDE"},"scienceWords":{"soil":"dirt where plants grow","energy":"power that makes things work","experiment":"a test to find out if something is true"},"connectActivity":"Look at your Math and Science notebooks. Find these words and read the sentences around them."}',
 'vocabulary', 12, 30, 1, 1, GETDATE()),

-- Node 204: Nouns
('INTERVENTION', 204, 'score < 70',
 'Nouns Remediation: Common vs Proper, Gender',
 '{"instruction":"Nouns are NAMING words. Common nouns are general; proper nouns are specific names.","rules":{"common":"general names, no capital letter: city, month, dog","proper":"specific names, ALWAYS capital letter: Manila, December, Rex"},"genderRule":{"masculine":"male: father, uncle, king, boy","feminine":"female: mother, aunt, queen, girl","neutral":"either: student, child, teacher"},"sortingGame":"Sort these into common or proper: Juan, school, Manila, dog, Christmas, teacher","tip":"If you can put THE before it, it is probably a common noun: the school, the dog."}',
 'vocabulary', 12, 30, 1, 1, GETDATE()),

-- Node 205: Verbs
('INTERVENTION', 205, 'score < 70',
 'Verbs Remediation: Subject-Verb Agreement',
 '{"instruction":"Verbs tell what the subject DOES. For he/she/it (singular), add -s or -es to the verb!","rule":"Singular subject (he, she, it, one person) → add -s/es to verb. Plural (they, we) → no -s.","examples":[{"singular":"She reads every day.","plural":"They read every day."},{"singular":"The dog barks loudly.","plural":"The dogs bark loudly."}],"verbList":["run → runs","jump → jumps","eat → eats","play → plays","wash → washes"],"practiceFrames":["He _____ (run/runs) in the morning.","They _____ (eat/eats) lunch together."]}',
 'vocabulary', 12, 30, 1, 1, GETDATE()),

-- Node 206: Adjectives
('INTERVENTION', 206, 'score < 70',
 'Adjectives Remediation: Describing the 5 Senses',
 '{"instruction":"Adjectives DESCRIBE nouns. Use your 5 senses to think of good adjectives!","senses":{"sight":["red","shiny","tiny","tall","colorful"],"sound":["noisy","quiet","loud","soft"],"touch":["smooth","rough","cold","warm","hard"],"smell":["sweet","fresh","sour"],"taste":["spicy","bitter","salty","sweet"]},"practice":"Pick an object in your classroom. Write 3 adjectives for it using different senses.","sentenceBuilder":"The _____ (adjective) _____ (noun) _____ (verb).","tip":"Adjectives usually come BEFORE the noun they describe: the BIG tree, the COLD water."}',
 'vocabulary', 12, 30, 1, 1, GETDATE()),

-- Node 207: Demonstrative Pronouns + Synonyms/Antonyms
('INTERVENTION', 207, 'score < 70',
 'Demonstrative Pronouns Remediation: This, That, These, Those',
 '{"instruction":"Demonstrative pronouns tell you HOW FAR and HOW MANY!","table":{"this":"NEAR + ONE: This pencil is mine.","that":"FAR + ONE: That bird is singing.","these":"NEAR + MANY: These books are heavy.","those":"FAR + MANY: Those children are playing."},"distanceTrick":"NEAR = THIS/THESE. FAR = THAT/THOSE. SINGULAR = THIS/THAT. PLURAL = THESE/THOSE.","practice":[{"sentence":"_____ apple on my desk is red.","answer":"This"},{"sentence":"_____ mountains far away look beautiful.","answer":"Those"}],"synonymAntonymReview":{"synonymPairs":[["big","large"],["fast","quick"]],"antonymPairs":[["hot","cold"],["tall","short"]]}}',
 'vocabulary', 12, 30, 1, 1, GETDATE()),

-- Node 208: Vocabulary in Sentences
('INTERVENTION', 208, 'score < 70',
 'Vocabulary in Context Remediation: Applying Word Knowledge',
 '{"instruction":"The best way to remember vocabulary is to USE it in your own sentences!","reviewWords":{"verbs":["run","jump","eat","read","wash"],"adjectives":["happy","big","small","cold","bright"],"demonstrative":["this","that","these","those"]},"buildSentences":["Write a sentence using a verb + adjective + noun.","Use a demonstrative pronoun to point to something near you.","Write 2 sentences using different demonstrative pronouns."],"example":{"sentence":"This big book is very interesting to read.","wordsUsed":["This (demonstrative)","big (adjective)","read (verb)"]}}',
 'vocabulary', 10, 30, 1, 1, GETDATE()),

-- Node 209: Classify Words
('INTERVENTION', 209, 'score < 70',
 'Word Classification Remediation: Verbs, Adjectives, Demonstratives',
 '{"instruction":"To classify a word, ask: WHAT does it do in the sentence?","questions":{"verb":"Does it show an ACTION? (run, eat, jump)","adjective":"Does it DESCRIBE a noun? (happy, tall, cold)","demonstrative":"Does it POINT to something? (this, that, these, those)"},"sortingExercise":["beautiful (adjective)","These (demonstrative)","swim (verb)","that (demonstrative)","angry (adjective)","write (verb)","those (demonstrative)"],"selfTest":"Find 5 words in a book or story. Label each one: verb, adjective, or demonstrative pronoun."}',
 'vocabulary', 12, 30, 1, 1, GETDATE()),

-- Node 210: Word Families
('INTERVENTION', 210, 'score < 70',
 'Word Families Remediation: Rhyming Patterns',
 '{"instruction":"Words in a family share the same ENDING (rime). Change the beginning (onset) to make new words!","families":{"-at":"c-at, b-at, h-at, m-at, r-at, f-at","-ight":"l-ight, n-ight, r-ight, t-ight, s-ight","-ake":"c-ake, b-ake, l-ake, t-ake, m-ake","-an":"m-an, c-an, r-an, t-an, p-an"},"activity":"Start with CAT. Change the first letter to make new words in the -at family. How many can you find?","challenge":"Make your own word family: pick an ending sound (-ig, -op, -un) and find 5 words."}',
 'vocabulary', 10, 30, 1, 1, GETDATE()),

-- Node 211: Spelling Patterns
('INTERVENTION', 211, 'score < 70',
 'Spelling Remediation: Common Spelling Rules',
 '{"instruction":"Spelling rules help you write words correctly. Learn the pattern, not just the word!","rules":{"dropE":"Drop silent e before adding -ing or -ed: bake → baking, love → loving","doubleConsonant":"Short vowel + 1 consonant? Double it before -ing or -ed: run → running, hop → hopped","justAdd":"Most words: just add -ed: play → played, talk → talked"},"commonErrors":[["because","becos","becaus"],["happy","happi","hapy"],["running","runing"]],"spellCheck":"Write each correctly spelled word 3 times in a row.","tip":"When unsure, think of the rule first, then write the word."}',
 'vocabulary', 12, 30, 1, 1, GETDATE()),

-- Node 212: Root Words
('INTERVENTION', 212, 'score < 70',
 'Root Words Remediation: Finding the Base Word',
 '{"instruction":"The ROOT WORD (base word) is the main part of a word without any prefixes or suffixes.","suffixMeanings":{"-ing":"happening now: playing",""-ed":"happened in the past: played",""-er":"person who does it: teacher",""-ful":"full of: helpful",""-ly":"in that way: quickly",""-ness":"the state of: kindness",""-est":"most: fastest"},"practice":["playing → play + -ing","helpful → help + -ful","kindness → kind + -ness","fastest → fast + -est","teacher → teach + -er"],"challenge":"Find the root word: singing, beautiful, careless, slowly, happiest"}',
 'vocabulary', 12, 30, 1, 1, GETDATE());

END TRY
BEGIN CATCH
    PRINT 'Vocabulary supplemental nodes error: ' + ERROR_MESSAGE();
END CATCH
GO

-- =============================================================================
-- MODULE 3: GRAMMAR AWARENESS (AfterNodeID 301-312)
-- =============================================================================
BEGIN TRY
INSERT INTO SupplementalNodes
    (NodeType, AfterNodeID, TriggerLogic, Title, ContentJSON, SkillCategory, EstimatedDuration, XPReward, IsActive, IsVisible, CreatedDate)
VALUES
-- Node 301: Sentences vs Non-Sentences
('INTERVENTION', 301, 'score < 70',
 'Sentences Remediation: Complete Thought Check',
 '{"instruction":"A complete sentence has THREE things: (1) capital letter, (2) complete thought, (3) end mark.","test":"Ask yourself: Does this tell me SOMETHING COMPLETE? Or is it just a word or phrase?","examples":{"sentences":["The dog ran.","Maria is happy.","We eat lunch."],"nonSentences":["The dog.","Running fast.","Very happy."]},"fix":"Add a subject or verb to fix these: Running fast. → The boy is running fast.","selfCheck":"Read the sentence aloud. Does it sound finished? If yes, it is a complete sentence!"}',
 'grammar', 10, 30, 1, 1, GETDATE()),

-- Node 302: Sentence Word Order
('INTERVENTION', 302, 'score < 70',
 'Word Order Remediation: Subject + Verb + Object',
 '{"instruction":"English sentences usually follow this order: WHO does it → WHAT they do → to WHAT or WHERE","pattern":"Subject + Verb + Object/Complement","examples":[{"scrambled":"eats / Maria / apples","correct":"Maria eats apples."},{"scrambled":"is / sky / blue / The","correct":"The sky is blue."},{"scrambled":"in / fish / swim / water","correct":"Fish swim in water."}],"practice":"Unscramble: books / reads / The / boy","tip":"Find the WHO first (subject), then the ACTION (verb), then the rest."}',
 'grammar', 10, 30, 1, 1, GETDATE()),

-- Node 303: Subject and Predicate
('INTERVENTION', 303, 'score < 70',
 'Subject and Predicate Remediation',
 '{"instruction":"Every sentence has TWO parts: SUBJECT (who/what) and PREDICATE (what they do).","trick":"Ask WHO or WHAT is doing the action → that is the SUBJECT. Everything else is the PREDICATE.","examples":[{"sentence":"The bird flies.","subject":"The bird","predicate":"flies"},{"sentence":"Maria and Ben play outside.","subject":"Maria and Ben","predicate":"play outside"},{"sentence":"My mother cooks dinner.","subject":"My mother","predicate":"cooks dinner"}],"splitPractice":"Draw a line between the subject and predicate: The cat sleeps on the mat.","tip":"The subject is usually at the START of the sentence."}',
 'grammar', 12, 30, 1, 1, GETDATE()),

-- Node 304: Declarative and Interrogative
('INTERVENTION', 304, 'score < 70',
 'Sentence Types Remediation: Telling vs Asking',
 '{"instruction":"TELLING sentences (declarative) end with a PERIOD. ASKING sentences (interrogative) end with a QUESTION MARK.","trick":"If you could answer it with yes or no, or with information, it is an asking sentence!","examples":{"telling":["The sky is blue.","I am 8 years old.","Birds fly."],"asking":["Where is your bag?","Are you hungry?","What is your name?"]},"punctuationPractice":"Add . or ? to these sentences: Is the cat sleeping ___ I love mangoes ___ Where do you live ___","selfCheck":"Can you answer it? → Question mark. Does it just give information? → Period."}',
 'grammar', 10, 30, 1, 1, GETDATE()),

-- Node 305: Commands and Exclamations
('INTERVENTION', 305, 'score < 70',
 'Imperative and Exclamatory Sentences Remediation',
 '{"instruction":"COMMANDS tell someone to do something. They often start with a VERB. EXCLAMATIONS show strong FEELINGS.","examples":{"imperative":["Sit down.","Please help me.","Close the door.","Do your homework."],"exclamatory":["What a beautiful day!","I won!","Help!","Wow, that is amazing!"]},"hint":"Commands often do not have a visible subject because the subject is YOU (the listener).","practiceWrite":["Write a command for a younger student.","Write an exclamation you might say when you are surprised."],"endMarkRule":"Command = period (or !) | Exclamation = always !"}',
 'grammar', 10, 30, 1, 1, GETDATE()),

-- Node 306: Compound Sentences
('INTERVENTION', 306, 'score < 70',
 'Compound Sentences Remediation: And, But, Or',
 '{"instruction":"A compound sentence joins TWO simple sentences with a conjunction (and, but, or).","conjunctions":{"and":"use when ideas are SIMILAR or ADDED together","but":"use when there is a CONTRAST or difference","or":"use when there is a CHOICE"},"formulaWithComma":"Simple sentence , conjunction simple sentence.","examples":[{"wrong":"I like milk and cake.","right":"I like milk, and I like cake."},{"sentences":["I am tired.","I will study."],"compound":"I am tired, but I will study."}],"practiceJoin":"Combine using AND: I have a dog. I have a cat.","tip":"Put a comma BEFORE the conjunction when joining two complete sentences."}',
 'grammar', 12, 30, 1, 1, GETDATE()),

-- Node 307: Capitalization and Punctuation
('INTERVENTION', 307, 'score < 70',
 'Capitalization and Punctuation Remediation',
 '{"instruction":"ALWAYS capitalize: (1) first word of a sentence, (2) names of people and places, (3) the word I.","rules":["Sentences start with a CAPITAL letter","Proper names are capitalized: Maria, Cebu, Philippines","The word I is ALWAYS capital","End with . ? or !"],"errorFind":["my name is ana. → My name is Ana.","i live in manila. → I live in Manila.","where is the dog → Where is the dog?"],"practiceRewrite":"Rewrite correctly: she is from cebu city. her name is rose. can she sing"}',
 'grammar', 10, 30, 1, 1, GETDATE()),

-- Node 308: Discourse Markers
('INTERVENTION', 308, 'score < 70',
 'Time-Order Discourse Markers Remediation',
 '{"instruction":"Time-order words (First, Next, Then, Finally) help readers follow the ORDER of events or steps.","markers":{"First":"the starting step or event","Next":"what happens after First","Then":"what happens after Next","Finally":"the last step or event"},"orderGame":"Put these steps in order using First/Next/Then/Finally: eat breakfast, brush teeth, wake up, go to school.","writeAProcedure":"Write 4 steps for washing your hands using First, Next, Then, Finally.","checkList":"Did you use all 4 markers? Is each step in the right order?"}',
 'grammar', 10, 30, 1, 1, GETDATE()),

-- Node 309: Subjects and Verbs in Compound Sentences
('INTERVENTION', 309, 'score < 70',
 'Compound Sentence Analysis Remediation: Finding Both Subjects and Verbs',
 '{"instruction":"A compound sentence has TWO subjects and TWO verbs (one in each simple sentence part).","steps":["1. Find the conjunction (and/but/or)","2. Look at the part BEFORE the conjunction → find its subject and verb","3. Look at the part AFTER the conjunction → find its subject and verb"],"examples":[{"compound":"Ben eats and Lea drinks.","analysis":{"part1":{"subject":"Ben","verb":"eats"},"part2":{"subject":"Lea","verb":"drinks"}}},{"compound":"The dog barked and the cat ran.","analysis":{"part1":{"subject":"dog","verb":"barked"},"part2":{"subject":"cat","verb":"ran"}}}],"practice":"Find both subjects and verbs: The teacher smiled and the students cheered."}',
 'grammar', 12, 30, 1, 1, GETDATE()),

-- Node 310: National Themes in Sentences
('INTERVENTION', 310, 'score < 70',
 'National Theme Sentences Remediation: All Sentence Types',
 '{"instruction":"Review all 4 sentence types using words about the Philippines and Filipino culture.","review":{"declarative":"The Philippines is a beautiful country.","interrogative":"Is the Philippine flag red, blue, and white?","imperative":"Respect your flag always.","exclamatory":"Mabuhay ang Pilipinas!"},"writingChallenge":"Write one of each type of sentence about something Filipino (flag, hero, food, festival).","punctuationChart":{"telling":"period .","asking":"question mark ?","command":"period . or exclamation !","exclamation":"exclamation mark !"}}',
 'grammar', 10, 30, 1, 1, GETDATE()),

-- Node 311: Because (Explanatory)
('INTERVENTION', 311, 'score < 70',
 'Because Remediation: Cause and Reason Sentences',
 '{"instruction":"BECAUSE connects a RESULT with its REASON. It answers the question WHY?","formula":"[What happened] + because + [Why it happened]","examples":["I am happy because today is my birthday.","It is cold because it is raining.","She studied hard because she wanted to pass."],"avoidThis":"Do not start a sentence with BECAUSE alone: Because I am tired. (incomplete!) → I went to bed early because I was tired. (complete!)","practiceComplete":["I ate a lot _____ I was very hungry.","She wore a coat _____ the weather was cold.","They played inside _____ it was raining."]}',
 'grammar', 10, 30, 1, 1, GETDATE()),

-- Node 312: Sentence Intonation
('INTERVENTION', 312, 'score < 70',
 'Sentence Intonation Remediation: Rising and Falling Pitch',
 '{"instruction":"Your voice goes UP or DOWN at the end of a sentence to signal meaning.","rules":{"RISING pitch":"Questions → your voice goes UP at the end: Are you okay↗?","FALLING pitch":"Statements and commands → your voice goes DOWN at the end: I am okay↘.","HIGH then FALL":"Exclamations → start HIGH and fall: What a great day↗↘!"},"practiceAloud":["Read this as a question (voice up): You are coming to the party?","Read this as a statement (voice down): I am coming to the party."],"tip":"Practice in front of a mirror! Watch how your face changes with each sentence type."}',
 'grammar', 10, 30, 1, 1, GETDATE());

END TRY
BEGIN CATCH
    PRINT 'Grammar supplemental nodes error: ' + ERROR_MESSAGE();
END CATCH
GO

-- =============================================================================
-- MODULE 4: COMPREHENSION (AfterNodeID 401-412)
-- =============================================================================
BEGIN TRY
INSERT INTO SupplementalNodes
    (NodeType, AfterNodeID, TriggerLogic, Title, ContentJSON, SkillCategory, EstimatedDuration, XPReward, IsActive, IsVisible, CreatedDate)
VALUES
-- Node 401: Key Details
('INTERVENTION', 401, 'score < 70',
 'Key Details Remediation: Who, What, Where, When',
 '{"instruction":"When you read a story, hunt for these 4 key details like a detective!","keyQuestions":{"WHO":"Who is the story about? Look for character names.","WHAT":"What is happening? Look for actions and events.","WHERE":"Where does it happen? Look for place names or descriptions.","WHEN":"When does it happen? Look for time words (morning, after school, one day)."},"practiceText":"Lina walked to school on a rainy Monday morning. She forgot her umbrella. Her teacher helped her dry off.","findDetails":{"who":"Lina and her teacher","what":"Lina forgot her umbrella and got wet","where":"school","when":"Monday morning"},"tip":"Read the first and last sentence carefully - key details are often there!"}',
 'comprehension', 12, 30, 1, 1, GETDATE()),

-- Node 402: Sequencing Events
('INTERVENTION', 402, 'score < 70',
 'Sequencing Remediation: Story Order with Signal Words',
 '{"instruction":"Events in a story happen in ORDER. Signal words are CLUES for the order!","signalWords":{"first/to begin with":"the very first event","next/then/after that":"events in the middle","last/finally/in the end":"the very last event"},"practiceText":"First, Remy watered the plant. Next, she added soil. Then, she put it near the window. Finally, the plant grew flowers.","orderActivity":"Cut up event sentences and put them in the right order.","tip":"If you see THEN, something happened BEFORE it. If you see FINALLY, it is the END!"}',
 'comprehension', 10, 30, 1, 1, GETDATE()),

-- Node 403: Characters and Setting
('INTERVENTION', 403, 'score < 70',
 'Characters and Setting Remediation',
 '{"instruction":"Every story has CHARACTERS (people or animals) and a SETTING (where + when).","findingCharacters":"Look for names, pronouns (he, she, they), and words like boy, girl, cat, teacher.","findingSetting":{"where":"Look for place words: park, school, beach, kitchen, forest","when":"Look for time words: morning, yesterday, long ago, one rainy day"},"practiceText":"One sunny afternoon, two friends, Maya and Jun, played by the river near their village.","find":{"characters":"Maya and Jun","setting":{"where":"river near their village","when":"sunny afternoon"}},"tip":"Draw a quick picture of the setting to help you remember it!"}',
 'comprehension', 10, 30, 1, 1, GETDATE()),

-- Node 404: Main Idea
('INTERVENTION', 404, 'score < 70',
 'Main Idea Remediation: Big Idea vs. Details',
 '{"instruction":"The MAIN IDEA is what the whole passage is MOSTLY about. Details SUPPORT the main idea but are not the big picture.","test":"Does this idea cover EVERYTHING in the paragraph? → Main Idea. Does it only talk about ONE part? → Detail.","practiceText":"Dogs are wonderful pets. They are loyal and protect their owners. They also help blind people find their way. Dogs can be trained to do many helpful things.","identify":{"mainIdea":"Dogs are wonderful and helpful animals.","details":["They protect owners","They help blind people","They can be trained"]},"tip":"The main idea is often in the FIRST or LAST sentence of the paragraph!"}',
 'comprehension', 12, 30, 1, 1, GETDATE()),

-- Node 405: Cause and Effect
('INTERVENTION', 405, 'score < 70',
 'Cause and Effect Remediation: Why and What Happened',
 '{"instruction":"CAUSE = WHY it happened. EFFECT = WHAT happened. They always go together!","clueWords":{"cause clues":["because","since","due to","the reason is"],"effect clues":["so","therefore","as a result","that is why"]},"practiceTexts":[{"text":"It rained all day, so the playground was muddy.","cause":"It rained all day","effect":"the playground was muddy"},{"text":"Ben ate too much, so he felt sick.","cause":"Ben ate too much","effect":"he felt sick"}],"arrowTrick":"Draw an arrow: CAUSE → EFFECT","tip":"Ask WHY first. The answer is the CAUSE. Then ask WHAT HAPPENED. That is the EFFECT."}',
 'comprehension', 12, 30, 1, 1, GETDATE()),

-- Node 406: Predictions
('INTERVENTION', 406, 'score < 70',
 'Making Predictions Remediation: Using Clues',
 '{"instruction":"A PREDICTION is a smart guess about what will happen NEXT. Use clues from the text!","formula":"Text clue + What I already know = Prediction","steps":["1. Read carefully","2. Find clues in the text","3. Think about what usually happens in this situation","4. Make your prediction using: I predict that... / I think... because..."],"practiceText":"Dark clouds gathered. The wind blew hard. Children started running home.","prediction":"I predict that it will rain soon because dark clouds and strong winds usually mean rain is coming.","tip":"A good prediction is based on EVIDENCE, not just guessing!"}',
 'comprehension', 10, 30, 1, 1, GETDATE()),

-- Node 407: Problem and Solution
('INTERVENTION', 407, 'score < 70',
 'Problem and Solution Remediation: Story Conflict',
 '{"instruction":"In most stories, something goes WRONG (problem) and then someone fixes it (solution).","findProblem":"Ask: What went wrong? What was the character worried about? What caused trouble?","findSolution":"Ask: How was it fixed? What did the character do to make it better?","practiceText":"The kite was stuck in the tree. Ben tried to shake it loose, but it would not come down. His father used a long stick. Finally, the kite fell free.","analysis":{"problem":"The kite was stuck in the tree","solution":"The father used a long stick to free the kite"},"tip":"The PROBLEM is usually in the MIDDLE of the story. The SOLUTION comes near the END."}',
 'comprehension', 12, 30, 1, 1, GETDATE()),

-- Node 408: Compare and Contrast
('INTERVENTION', 408, 'score < 70',
 'Compare and Contrast Remediation: Similarities and Differences',
 '{"instruction":"COMPARE = how things are ALIKE. CONTRAST = how things are DIFFERENT.","signalWords":{"compare (alike)":["both","similarly","also","in the same way"],"contrast (different)":["but","however","on the other hand","while","unlike"]},"vennDiagram":"Draw two overlapping circles. Write similarities in the middle, differences on the sides.","practiceText":"Ants and bees are both insects. Ants live on the ground, but bees live in hives. Both work hard for their colony.","analysis":{"alike":["both are insects","both work hard"],"different":["ants live on ground","bees live in hives"]},"tip":"Look for BOTH to find similarities. Look for BUT or HOWEVER to find differences."}',
 'comprehension', 12, 30, 1, 1, GETDATE()),

-- Node 409: Context Clues
('INTERVENTION', 409, 'score < 70',
 'Context Clues Remediation: Using Surrounding Words',
 '{"instruction":"Context clues are HINTS in the text that help you figure out what an unknown word means. You do not always need a dictionary!","types":{"synonym clue":"A nearby word with the same meaning is given. Example: The rabbit is quick, or fast.","antonym clue":"An opposite meaning is given. Example: He was joyful, not sad at all.","definition clue":"The text defines the word. Example: Nocturnal, which means active at night, animals sleep during the day.","example clue":"Examples are given. Example: Reptiles, such as snakes and lizards, are cold-blooded."},"practice":"The enormous elephant was so large it could not fit through the gate.","clue":"enormous must mean very large","tip":"Read the WHOLE sentence (and the one before/after) before guessing the meaning!"}',
 'comprehension', 12, 30, 1, 1, GETDATE()),

-- Node 410: Drawing Conclusions / Inference
('INTERVENTION', 410, 'score < 70',
 'Inference Remediation: Reading Between the Lines',
 '{"instruction":"An INFERENCE is a conclusion you reach from CLUES + what you already know. The author does not always say everything directly!","formula":"Text Clue + Background Knowledge = Inference","steps":["1. Find a clue in the text","2. Think: what do I KNOW about this situation?","3. Put them together to make a logical conclusion"],"practiceText":"Marco came home with muddy shoes and wet hair. His mother handed him a towel.","textClue":"muddy shoes, wet hair","backgroundKnowledge":"This happens when you play outside in the rain","inference":"Marco was probably playing outside in the rain","tip":"Your inference should be LOGICAL - could you prove it with clues from the text?"}',
 'comprehension', 12, 30, 1, 1, GETDATE()),

-- Node 411: Text-to-Self Connections
('INTERVENTION', 411, 'score < 70',
 'Text-to-Self Connection Remediation: Relating Reading to Life',
 '{"instruction":"Text-to-self connections make reading MORE MEANINGFUL. Connect what you read to YOUR own experiences!","connectionStarters":["This reminds me of when I...","I felt like the character when...","I know how they feel because I once...","I have been to a place like this..."],"practiceText":"Sofia was nervous about her first day at a new school. She did not know anyone and did not know where to go.","makeConnection":"Have you ever felt nervous about something new? Write: This reminds me of when I...","types":["Text-to-Self: connects to your own life","Text-to-World: connects to world events","Text-to-Text: connects to another book"],"tip":"Good readers THINK while they read. Ask yourself: Does this remind me of something?"}',
 'comprehension', 10, 30, 1, 1, GETDATE()),

-- Node 412: Summarizing
('INTERVENTION', 412, 'score < 70',
 'Summarizing Remediation: Short and Accurate',
 '{"instruction":"A SUMMARY is a SHORT retelling of the MOST IMPORTANT ideas. It is NOT a copy!","rules":["Include the MAIN IDEA","Include only 2-3 KEY DETAILS","Leave out small or unimportant details","Use YOUR OWN WORDS","Keep it short (2-3 sentences)"],"practiceText":"Water is important for all living things. Humans need water to stay hydrated and healthy. Plants need water to grow and make food. Animals need water too, or they will die.","goodSummary":"Water is essential for humans, plants, and animals to survive.","badSummary":"Humans are hydrated. Plants grow. Animals die without water. Water is important.","tip":"Ask: What is the BIG IDEA? What are the 2 most important supporting facts? Write only those!"}',
 'comprehension', 12, 30, 1, 1, GETDATE());

END TRY
BEGIN CATCH
    PRINT 'Comprehension supplemental nodes error: ' + ERROR_MESSAGE();
END CATCH
GO

-- =============================================================================
-- MODULE 5: CREATING AND COMPOSING TEXT (AfterNodeID 501-512)
-- =============================================================================
BEGIN TRY
INSERT INTO SupplementalNodes
    (NodeType, AfterNodeID, TriggerLogic, Title, ContentJSON, SkillCategory, EstimatedDuration, XPReward, IsActive, IsVisible, CreatedDate)
VALUES
-- Node 501: Writing Simple Sentences
('INTERVENTION', 501, 'score < 70',
 'Simple Sentence Writing Remediation: Three-Part Check',
 '{"instruction":"Every sentence must pass the 3-PART CHECK before you write it!","check":{"1":"Does it start with a CAPITAL LETTER?","2":"Does it express a COMPLETE THOUGHT (subject + verb)?","3":"Does it end with the correct PUNCTUATION (. ? !)?"},"commonErrors":[{"wrong":"the dog runs fast","fix":"The dog runs fast."},{"wrong":"Running very fast.","fix":"The dog runs very fast. (Add a subject!)"},{"wrong":"I love school","fix":"I love school. (Add end punctuation!)"}],"writingPractice":"Write 5 simple sentences about your school. Check all 3 parts for each one.","tip":"Read your sentence aloud. Does it sound like a complete, finished thought?"}',
 'creating', 12, 30, 1, 1, GETDATE()),

-- Node 502: Describing with Adjectives
('INTERVENTION', 502, 'score < 70',
 'Adjective Writing Remediation: Adding Describing Words',
 '{"instruction":"Adjectives make your writing MORE INTERESTING and VIVID. Use your 5 senses!","beforeAndAfter":[{"before":"The cat sat.","after":"The small, fluffy orange cat sat quietly."},{"before":"She has a dress.","after":"She has a bright, colorful dress with tiny flowers."}],"senseWords":{"see":["shiny","tiny","tall","dark","colorful"],"hear":["noisy","quiet","buzzing"],"feel":["soft","rough","cold"],"smell":["sweet","fresh"],"taste":["sour","spicy","sweet"]},"improveThese":["The flower grows.","I ate food.","A bird sang."],"tip":"Add at LEAST 2 adjectives to each noun in your sentences for richer writing."}',
 'creating', 10, 30, 1, 1, GETDATE()),

-- Node 503: Writing a Paragraph
('INTERVENTION', 503, 'score < 70',
 'Paragraph Writing Remediation: Topic Sentence Focus',
 '{"instruction":"A paragraph is a group of sentences about ONE MAIN IDEA. The topic sentence tells what the paragraph is about.","structure":{"1_topicSentence":"States the main idea - usually the FIRST sentence","2_supportingSentences":"Give 2-3 details or examples about the main idea","3_conclusion":"Optional: wraps up with a final thought"},"example":{"topic":"I love my school.","support1":"The classrooms are bright and colorful.","support2":"My teachers are kind and helpful.","conclusion":"School is a great place to learn and make friends."},"errorToAvoid":"Do NOT jump to a new topic inside one paragraph!","practice":"Write a paragraph about your favorite food. Start with: My favorite food is ___."}',
 'creating', 15, 30, 1, 1, GETDATE()),

-- Node 504: Questions and Commands
('INTERVENTION', 504, 'score < 70',
 'Question and Command Writing Remediation',
 '{"instruction":"Practice writing QUESTIONS (? mark) and COMMANDS (starts with verb, period or !) correctly.","questionStarters":["Who...?","What...?","Where...?","When...?","Why...?","How...?","Is...?","Can...?","Do...?"],"commandVerbs":["Open","Close","Read","Write","Listen","Please bring","Help","Stand","Sit"],"practice":{"questions":["Write 3 questions you would ask a new friend.","Write 2 questions about a book you read."],"commands":["Write 3 classroom rules as command sentences.","Write 4 steps for a morning routine as commands."]},"tip":"Questions end with ?. Commands start with a VERB (action word)."}',
 'creating', 10, 30, 1, 1, GETDATE()),

-- Node 505: Compound Sentences Writing
('INTERVENTION', 505, 'score < 70',
 'Compound Sentence Writing Remediation: And, But, Or',
 '{"instruction":"Combine TWO simple sentences using AND, BUT, or OR to write compound sentences.","choiceGuide":{"AND":"similar or connected ideas → I read a book, and I enjoyed it.","BUT":"opposite or contrasting ideas → I am sleepy, but I will study.","OR":"a choice between ideas → You can play outside, or you can read inside."},"practiceJoin":[{"s1":"I like cats.","s2":"I like dogs.","hint":"Use AND"},{"s1":"It is raining.","s2":"We played inside.","hint":"Use SO or BUT"},{"s1":"We can eat rice.","s2":"We can eat bread.","hint":"Use OR"}],"commaRule":"Put a comma BEFORE and/but/or when joining two complete sentences.","tip":"Each part of the compound sentence must be a complete sentence on its own."}',
 'creating', 12, 30, 1, 1, GETDATE()),

-- Node 506: Writing a Short Story
('INTERVENTION', 506, 'score < 70',
 'Short Story Writing Remediation: Beginning, Middle, End',
 '{"instruction":"Every good story has THREE PARTS: Beginning (setup), Middle (problem), End (solution/conclusion).","storyMap":{"beginning":"Who is in the story? Where and when does it take place? What is the normal situation?","middle":"What PROBLEM or EXCITING EVENT happens? How does the character FEEL?","end":"How is the problem SOLVED? How does the story FINISH? How does the character FEEL now?"},"miniStory":"Write a 3-sentence story: one sentence for beginning, one for middle, one for end.","timeWords":"Use First, Then, Finally to connect your story parts.","tip":"Give your character a NAME and a FEELING to make the story interesting!"}',
 'creating', 15, 30, 1, 1, GETDATE()),

-- Node 507: Time-Order Words in Writing
('INTERVENTION', 507, 'score < 70',
 'Time-Order Writing Remediation: Procedure and Sequence',
 '{"instruction":"Time-order words help readers follow your steps or story events IN ORDER.","markers":{"First":"the starting step","Next":"the step that comes after First","Then":"what follows Next","After that":"optional extra step","Finally":"the LAST step"},"procedureWriting":{"topic":"How to make a glass of juice","steps":["First, wash the fruit.","Next, cut it into pieces.","Then, put the pieces in a blender.","After that, add water and sugar.","Finally, pour the juice into a glass."]},"practice":"Write the steps for making your bed using all 4 time-order markers.","errorCheck":"Did you use the markers in the RIGHT ORDER? First before Next, Next before Then, etc."}',
 'creating', 12, 30, 1, 1, GETDATE()),

-- Node 508: Writing with Because
('INTERVENTION', 508, 'score < 70',
 'Explanatory Writing Remediation: Using Because',
 '{"instruction":"Use BECAUSE to explain the REASON behind something you write.","structure":"[What happened / your opinion] + because + [the reason/evidence]","examples":["I love reading because it takes me to new worlds.","Plants need water because water helps them make food.","I wear a uniform because it shows I belong to my school."],"avoidFragments":"Never write BECAUSE alone as a sentence: Because it is fun. (WRONG) → I study because it is fun. (CORRECT)","practiceFrames":["I like _____ because _____.","We should _____ because _____.","The teacher smiled because _____."],"tip":"The REASON after because should be specific, not just ''it is good'' or ''I like it.''."}',
 'creating', 10, 30, 1, 1, GETDATE()),

-- Node 509: Comparative Sentences Writing
('INTERVENTION', 509, 'score < 70',
 'Comparison Writing Remediation: -er and -est',
 '{"instruction":"When writing comparisons, choose the right form of the adjective!","rules":{"positive":"describing one thing: big, fast, tall","comparative":"comparing TWO things: add -er: bigger, faster, taller","superlative":"comparing THREE or MORE: add -est: biggest, fastest, tallest"},"longWordRule":"For long adjectives (3+ syllables): use MORE and MOST instead of -er/-est","examples":[{"wrong":"This is the bigger building in the city.","fix":"This is the biggest building in the city. (comparing many)"},{"wrong":"She is more tall than Ben.","fix":"She is taller than Ben. (short adjective = -er)"}],"practice":"Write 3 sentences comparing things in your classroom using -er and -est."}',
 'creating', 12, 30, 1, 1, GETDATE()),

-- Node 510: Writing About National Themes
('INTERVENTION', 510, 'score < 70',
 'National Theme Writing Remediation: Filipino Culture in Sentences',
 '{"instruction":"Write about the Philippines and Filipino culture using all sentence types and correct grammar.","topics":["The Philippine flag and its colors","A Filipino hero you admire","Bayanihan (helping each other)","A local festival you know about","Filipino food you enjoy"],"sentenceTypeReminder":{"telling":"The Philippine flag has three colors: red, white, and blue.","asking":"Why is the Philippine flag special to Filipinos?","command":"Respect the Philippine flag always.","exclamation":"Mabuhay ang Pilipinas!"},"writingTask":"Write one sentence of EACH type about your favorite Filipino tradition.","vocabulary":["Mabuhay","bayanihan","pagmamahal","kababayan","bayani"]}',
 'creating', 12, 30, 1, 1, GETDATE()),

-- Node 511: Descriptive Paragraph Writing
('INTERVENTION', 511, 'score < 70',
 'Descriptive Paragraph Remediation: Sensory Details',
 '{"instruction":"A descriptive paragraph uses SENSORY WORDS to help the reader SEE, HEAR, FEEL, SMELL, or TASTE what you are describing.","structure":{"topicSentence":"Name what you are describing: My favorite place is the beach.","details":"Describe using different senses: I see sparkling blue water. I hear the waves crashing.","conclusion":"Wrap up with a feeling: The beach always makes me feel peaceful and happy."},"sensorChecklist":["Did I include a sight detail?","Did I include a sound detail?","Did I include a touch/feel detail?","Did I use vivid adjectives?"],"practice":"Write a 4-sentence descriptive paragraph about your classroom using at least 2 senses.","tip":"Close your eyes and imagine the place or thing first. What do you notice with each sense?"}',
 'creating', 15, 30, 1, 1, GETDATE()),

-- Node 512: Writing a Book Response
('INTERVENTION', 512, 'score < 70',
 'Book Response Writing Remediation: Sharing Your Opinion',
 '{"instruction":"A book response shares what YOU think and feel about a text. It should include a title, brief summary, and your opinion WITH REASONS.","structure":{"opening":"Name the book and author (if known)","summary":"Briefly tell what the book is about (2-3 sentences, main idea only)","opinion":"Tell what you liked or did not like, and WHY","connection":"Connect it to your own life or another book"},"sentenceFrames":["The book is about...","I liked/did not like the book because...","My favorite part was...","This book reminds me of..."],"practice":"Choose any story you know. Write a 4-sentence book response using the sentence frames.","tip":"Your opinion must have a REASON. Not just: I liked it. But: I liked it because it made me feel brave."}',
 'creating', 12, 30, 1, 1, GETDATE());

END TRY
BEGIN CATCH
    PRINT 'Creating supplemental nodes error: ' + ERROR_MESSAGE();
END CATCH
GO

-- =============================================================================
-- SUPPLEMENTAL nodes for Beginner (70-79%) students (select nodes per module)
-- =============================================================================
BEGIN TRY
INSERT INTO SupplementalNodes
    (NodeType, AfterNodeID, TriggerLogic, Title, ContentJSON, SkillCategory, EstimatedDuration, XPReward, IsActive, IsVisible, CreatedDate)
VALUES
('SUPPLEMENTAL', 103, 'score 70-79 AND level=1',
 'Phonics Extension: Mixed Syllable Practice',
 '{"instruction":"You are doing well! Let us practice more VCV and VCCV words to build speed.","mixedPractice":["Tiger (VCV)","Rabbit (VCCV)","Music (VCV)","Napkin (VCCV)","Paper (VCV)","Kitten (VCCV)","Open (VCV)","Button (VCCV)"],"goal":"Read all 8 words correctly within 30 seconds.","challenge":"Sort the words into VCV and VCCV columns. Check your work."}',
 'phonics', 10, 30, 1, 1, GETDATE()),

('SUPPLEMENTAL', 205, 'score 70-79 AND level=1',
 'Vocabulary Extension: Writing Verbs in Context',
 '{"instruction":"Good work on verbs! Now practice using them in different tenses.","tensePractice":{"present":"She runs every day.","past":"She ran yesterday.","future":"She will run tomorrow."},"verbsToConjugate":["play","eat","read","jump","write"],"challenge":"Write a paragraph using 5 different verbs in both present and past tense."}',
 'vocabulary', 10, 30, 1, 1, GETDATE()),

('SUPPLEMENTAL', 306, 'score 70-79 AND level=1',
 'Grammar Extension: Writing More Compound Sentences',
 '{"instruction":"Nice job on compound sentences! Practice writing more with all three conjunctions.","challenge":"Write 6 compound sentences: 2 using AND, 2 using BUT, 2 using OR.","topics":["Write about your family","Write about school","Write about your hobbies"],"checkList":["Comma before the conjunction?","Both parts are complete sentences?","Correct conjunction chosen for the relationship?"]}',
 'grammar', 10, 30, 1, 1, GETDATE()),

('SUPPLEMENTAL', 405, 'score 70-79 AND level=1',
 'Comprehension Extension: Cause and Effect Chains',
 '{"instruction":"You understand cause and effect! Now practice CHAINS - one cause leading to multiple effects.","example":{"cause":"It rained heavily","effect1":"The river flooded","effect2":"The road was blocked","effect3":"People could not go to school"},"practiceText":"A strong typhoon hit the province. The power went out. Trees fell on the roads. Families had to stay inside their homes.","task":"List the CAUSE and all the EFFECTS you can find.","challenge":"Write your own cause with 3 different effects."}',
 'comprehension', 10, 30, 1, 1, GETDATE()),

('SUPPLEMENTAL', 506, 'score 70-79 AND level=1',
 'Creating Extension: Adding Dialogue to Stories',
 '{"instruction":"Great short story writing! Make your stories even better by adding DIALOGUE (what characters say).","dialogueRules":["Put quotation marks around what someone says","Start a new line when a new person speaks","Add a dialogue tag: she said, he asked, they shouted"],"example":"Mia found a puppy. \"Are you lost?\" she asked softly. The puppy wagged its tail. \"I will take you home,\" Mia promised.","practice":"Rewrite your short story from node 506 and add at least 2 lines of dialogue."}',
 'creating', 12, 30, 1, 1, GETDATE());

END TRY
BEGIN CATCH
    PRINT 'Supplemental nodes error: ' + ERROR_MESSAGE();
END CATCH
GO

-- =============================================================================
-- ENRICHMENT nodes for Advanced (>=90%) students (Q4 nodes)
-- =============================================================================
BEGIN TRY
INSERT INTO SupplementalNodes
    (NodeType, AfterNodeID, TriggerLogic, Title, ContentJSON, SkillCategory, EstimatedDuration, XPReward, IsActive, IsVisible, CreatedDate)
VALUES
('ENRICHMENT', 112, 'score >= 90 AND level=3',
 'Phonics Enrichment: Advanced Word Patterns Challenge',
 '{"instruction":"Excellent phonics skills! Challenge yourself with more complex patterns.","advancedPatterns":{"CVVC":["rain","boat","read","coat","team"],"CVCe":["bake","home","tune","bike","hope"],"silent letters":["knight","write","knock","gnat","lamb"]},"challenge":"Create a word sort with 20 words using 4 different patterns. Label each column.","extension":"Write a story using at least 3 words from each pattern group."}',
 'phonics', 15, 50, 1, 1, GETDATE()),

('ENRICHMENT', 212, 'score >= 90 AND level=3',
 'Vocabulary Enrichment: Word Building with Prefixes and Suffixes',
 '{"instruction":"Amazing vocabulary! Now explore how words GROW using prefixes and suffixes.","prefixes":{"un-":"not: unhappy, unkind, unsafe","re-":"again: rewrite, reread, replay","pre-":"before: preview, prepay, preheat","mis-":"wrongly: mistake, misuse, misread"},"combined":["un+happy = unhappy","re+write = rewrite","mis+read = misread"],"challenge":"Create 10 new words by combining the prefixes with root words you know.","extension":"Write sentences using your 10 new words."}',
 'vocabulary', 15, 50, 1, 1, GETDATE()),

('ENRICHMENT', 312, 'score >= 90 AND level=3',
 'Grammar Enrichment: Complex Sentences with If, When, Although',
 '{"instruction":"Superb grammar! Now explore complex sentences with subordinating conjunctions.","conjunctions":{"if":"shows condition: If it rains, we will stay inside.","when":"shows time: When I grow up, I want to be a teacher.","although":"shows contrast: Although she was tired, she finished her homework.","because":"shows reason: I study hard because I want to learn."},"practice":"Transform these simple sentences into complex sentences using the conjunctions above.","challenge":"Write a paragraph using at least 3 different subordinating conjunctions."}',
 'grammar', 15, 50, 1, 1, GETDATE()),

('ENRICHMENT', 412, 'score >= 90 AND level=3',
 'Comprehension Enrichment: Critical Thinking and Author Purpose',
 '{"instruction":"Outstanding comprehension! Go deeper by thinking about WHY the author wrote the text.","authorPurpose":{"PIE":{"P":"Persuade: convince the reader of something","I":"Inform: teach facts and information","E":"Entertain: tell a story or make the reader enjoy"}},"criticalQuestions":["Why did the author write this?","What is the author trying to make me think or feel?","Do I agree with the author? Why or why not?","What evidence supports the main idea?"],"challenge":"Find a short article or story and write: (1) the purpose, (2) your opinion of the text, (3) one question you still have after reading."}',
 'comprehension', 15, 50, 1, 1, GETDATE()),

('ENRICHMENT', 512, 'score >= 90 AND level=3',
 'Creating Enrichment: Writing a Multi-Paragraph Essay',
 '{"instruction":"Fantastic writing! Challenge yourself to write a 3-paragraph essay.","essayStructure":{"paragraph1":"Introduction: Hook + topic + thesis (main point)","paragraph2":"Body: 3 supporting details with examples","paragraph3":"Conclusion: Restate thesis + wrap up with a final thought"},"hooks":["Ask a question: Have you ever wondered...?","Share a fact: Did you know that...?","Start with a feeling: Every time I see..., I feel..."],"topic":"Write about why reading is important. Use 3 paragraphs, 3-4 sentences each.","checklist":["Introduction has a hook?","Body has 3 details?","Conclusion restates the main idea?","All sentences are complete and correctly punctuated?"]}',
 'creating', 20, 50, 1, 1, GETDATE());

END TRY
BEGIN CATCH
    PRINT 'Enrichment nodes error: ' + ERROR_MESSAGE();
END CATCH
GO

PRINT 'Supplemental nodes seed complete.';
PRINT 'Total: 60 INTERVENTION + 5 SUPPLEMENTAL + 5 ENRICHMENT = 70 supplemental nodes.';
GO
