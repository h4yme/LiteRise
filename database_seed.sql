-- =============================================================
-- LiteRise Database Seed Script
-- MATATAG Grade 3 English Curriculum
-- Modules: Phonics, Vocabulary, Grammar, Comprehension, Creating
-- Generated from 4 lesson PDFs from h4yme/APIFINAL repo
-- Run this on SQL Server (LiteRiseDB)
-- =============================================================

USE LiteRiseDB;
GO

-- ── CLEANUP: delete in FK-safe order ─────────────────────────
IF OBJECT_ID('QuizQuestions','U') IS NOT NULL
    DELETE FROM QuizQuestions WHERE NodeID BETWEEN 101 AND 513;
IF OBJECT_ID('LessonGameContent','U') IS NOT NULL
    DELETE FROM LessonGameContent WHERE LessonID BETWEEN 101 AND 513;
IF OBJECT_ID('Nodes','U') IS NOT NULL
    DELETE FROM Nodes WHERE ModuleID IN (1,2,3,4,5);
IF OBJECT_ID('Modules','U') IS NOT NULL
    DELETE FROM Modules WHERE ModuleID IN (1,2,3,4,5);
GO

-- ── FIX COLUMN TYPES ──────────────────────────────────────────
ALTER TABLE QuizQuestions ALTER COLUMN CorrectAnswer NVARCHAR(500);
GO

-- ── MODULES ──────────────────────────────────────────────────
SET IDENTITY_INSERT Modules ON;
INSERT INTO Modules (ModuleID, ModuleName, ModuleCode, CategoryMapping, OrderIndex, TotalNodes, Description) VALUES
(1, 'Phonics and Word Study', 'EN3PWS', 1, 1, 13,
 'Learn letter-sound relationships, word patterns (CVCC, CCVC, VCV, VCCV), sight words, and syllable division across all four quarters.'),
(2, 'Vocabulary and Word Knowledge', 'EN3VWK', 2, 2, 13,
 'Build word knowledge through high-frequency words, regional themes, nouns, verbs, adjectives, synonyms, antonyms, word families, spelling patterns, and root words.'),
(3, 'Grammar Awareness and Grammatical Structures', 'EN3GAGS', 3, 3, 13,
 'Master sentence construction, types of sentences (declarative, interrogative, imperative, exclamatory), compound sentences, capitalization, punctuation, discourse markers, and intonation.'),
(4, 'Comprehending and Analyzing Texts', 'EN3CAT', 4, 4, 13,
 'Develop comprehension skills: key details, sequencing, characters and setting, main idea, cause and effect, problem and solution, compare and contrast, context clues, inference, and summarizing.'),
(5, 'Creating and Composing Text', 'EN3CCT', 5, 5, 13,
 'Express ideas through writing: simple sentences, descriptive paragraphs, narrative texts, informational writing, and creative compositions using correct grammar and structure.');
SET IDENTITY_INSERT Modules OFF;
GO

-- ── NODES: PHONICS (ModuleID=1, NodeIDs 101-113) ─────────────
SET IDENTITY_INSERT Nodes ON;
INSERT INTO Nodes (NodeID, ModuleID, NodeType, NodeNumber, Quarter, LessonTitle, LearningObjectives, ContentJSON, SkillCategory, EstimatedDuration, XPReward) VALUES
(101, 1, 'CORE_LESSON', 1, 1,
 'Sight Words and CVCC Patterns',
 'Recognize sight words (could, light, always, around, bright) by heart; identify and read CVCC pattern words (Hand, Belt, Milk, Fast, Lamp, Best, Mask, Pink, sand).',
 '{"keyWords":["could","light","always","around","bright","Hand","Belt","Milk","Fast","Lamp"],"patterns":["CVCC: Consonant-Vowel-Consonant-Consonant"],"examples":["Hand - H+a+n+d","Belt - B+e+l+t","Milk - M+i+l+k","Fast - F+a+s+t"],"rule":"CVCC words have two consonants at the end that make a smooth blend.","curriculumCode":"EN3PWS-I-1, EN3PWS-I-2.1"}',
 'phonics', 30, 50),
(102, 1, 'CORE_LESSON', 2, 1,
 'CCVC Patterns: Clusters and Diphthongs',
 'Read and identify CCVC words starting with consonant clusters (Frog, Stop, Plan, Drum, Clip); recognize diphthong sounds (oi in coin).',
 '{"keyWords":["Frog","Stop","Plan","Drum","Clip","Star","Coin","Spot","Grab","Brick","Skip"],"patterns":["CCVC: Consonant-Cluster + Vowel + Consonant"],"rule":"CCVC words start with two consonant sounds blended together.","diphthongs":["oi (coin)","ou (loud)"],"examples":["Fr-og","St-op","Pl-an","Dr-um"],"curriculumCode":"EN3PWS-I-2.2"}',
 'phonics', 30, 50),
(103, 1, 'CORE_LESSON', 3, 1,
 'VCV and VCCV Syllable Patterns',
 'Divide multi-syllable words using VCV (Tiger=Ti-ger, Robot=Ro-bot) and VCCV (Rabbit=Rab-bit, Puppy=Pup-py, Muffin=Muf-fin) patterns; identify long vs short vowel sounds.',
 '{"patterns":["VCV: Vowel-Consonant-Vowel (long vowel)","VCCV: Vowel-Consonant-Consonant-Vowel (short vowel)"],"vcvExamples":["Ti-ger","Ro-bot","Pa-per","Mu-sic","O-pen"],"vccvExamples":["Rab-bit","Pup-py","Muf-fin","Nap-kin","Kit-ten"],"rule":"In VCV, divide after first vowel (long sound). In VCCV, divide between the two consonants (short sound).","curriculumCode":"EN3PWS-I-2.3, EN3PWS-I-2.4"}',
 'phonics', 35, 60),
(104, 1, 'CORE_LESSON', 4, 2,
 'Q2 Sight Words in Community Sentences',
 'Automatically recognize and use sight words (carry, clean, drink, full, light, small, bright, wash, those, grow) in sentences about helping in the community.',
 '{"keyWords":["carry","clean","drink","full","light","small","bright","wash","those","grow"],"theme":"Helping in the Community","contextSentences":["We clean our yard.","The basket is full of mangoes.","I can carry my own school bag.","Please wash your hands before eating."],"curriculumCode":"EN3PWS-II-1"}',
 'phonics', 25, 50),
(105, 1, 'CORE_LESSON', 5, 2,
 'VCV Pattern: The Open Syllable (Long Vowels)',
 'Apply the VCV rule to divide words correctly: when one consonant is between two vowels, divide after the first vowel making it long (Pi-lot, Ba-sin, Si-lent, Tu-lip, Me-lon).',
 '{"rule":"V-C-V: Divide after the first vowel. The first vowel says its name (long sound).","examples":["Ti-ger","Pa-per","Ba-sin","Pi-lot","Si-lent","Tu-lip","Me-lon","Ho-tel","Ba-by","E-vil"],"theme":"My Local Environment","curriculumCode":"EN3PWS-II-2.1"}',
 'phonics', 30, 55),
(106, 1, 'CORE_LESSON', 6, 2,
 'VCCV Pattern: The Closed Syllable (Short Vowels)',
 'Apply the VCCV rule: when two consonants are between two vowels, divide between them making the first vowel short (Bas-ket, Din-ner, Gar-den, Hap-pen, Mar-ket, Can-teen, Jack-et).',
 '{"rule":"V-CC-V: Divide between the two consonants. The first vowel is short.","examples":["Bas-ket","Din-ner","Gar-den","Hap-pen","Mar-ket","Can-teen","Jack-et","Pup-py"],"theme":"Regional Foods and Traditions","curriculumCode":"EN3PWS-II-2.2"}',
 'phonics', 30, 55),
(107, 1, 'CORE_LESSON', 7, 3,
 'Narrative Sight Words: Action and Sequence',
 'Use action sight words (bring, carry, draw, drink, fall) correctly in narrative and sequencing contexts; identify correct spelling.',
 '{"keyWords":["bring","carry","draw","drink","fall"],"theme":"Storytelling and Sequencing","contextSentences":["Please bring your umbrella because it might rain.","The little boy can carry a heavy box.","I love to draw pictures of my family.","Be careful or you might fall on the wet floor.","Did you drink enough water after the race?"],"curriculumCode":"EN3PWS-III-1"}',
 'phonics', 25, 50),
(108, 1, 'CORE_LESSON', 8, 3,
 'Descriptive and Quantity Sight Words',
 'Use descriptive and quantity sight words (eight, far, full, hot, light) correctly; identify antonyms (near/far, cold/hot).',
 '{"keyWords":["eight","far","full","hot","light"],"theme":"Describing the Environment","antonyms":["near-far","cold-hot","empty-full","dark-light"],"contextSentences":["There are eight pupils in the front row.","The sun is too hot to play outside.","The stars are very far away in the sky.","Turn on the light so we can read."],"curriculumCode":"EN3PWS-III-1"}',
 'phonics', 25, 50),
(109, 1, 'CORE_LESSON', 9, 3,
 'Comparison and Observation Sight Words',
 'Use comparison sight words (better, best, both, clean, small) correctly in sentences; understand comparative/superlative forms.',
 '{"keyWords":["better","best","both","clean","small"],"theme":"Analyzing and Comparing","rule":"better = comparative (comparing two); best = superlative (comparing more than two)","contextSentences":["I feel better today than yesterday.","This is the best drawing I have made!","Both of my hands are clean.","The ant is very small compared to the dog."],"curriculumCode":"EN3PWS-III-1"}',
 'phonics', 25, 50),
(110, 1, 'CORE_LESSON', 10, 4,
 'Complex Sight Words: Reasoning and Abstract',
 'Differentiate and use abstract sight words (together, because, should, through, thought) in complex sentence contexts.',
 '{"keyWords":["together","because","should","through","thought"],"challenge":"Differentiating between similar sounds or abstract meanings","contextSentences":["We walked through the dark tunnel.","I thought it would rain, so I brought my umbrella.","You should always tell the truth.","The team won because they practiced every day.","When we work together, the task becomes easier."],"curriculumCode":"EN3PWS-IV-1"}',
 'phonics', 30, 60),
(111, 1, 'CORE_LESSON', 11, 4,
 'Words of Quantity, Time, and Comparison',
 'Use quantity and comparison words (enough, several, always, better, against) logically in sentences.',
 '{"keyWords":["enough","several","always","better","against"],"definitions":{"enough":"sufficient amount","several":"more than two but not many","always":"at all times","better":"improved or superior","against":"in opposition to"},"contextSentences":["Do we have enough rice for all the guests?","There are several ways to solve this problem.","It is always better to be early than late."],"curriculumCode":"EN3PWS-IV-1"}',
 'phonics', 30, 60),
(112, 1, 'CORE_LESSON', 12, 4,
 'Abstract Sight Words and Spatial Directions',
 'Use directional and abstract sight words (different, toward, across, carry, between) to describe spatial relationships.',
 '{"keyWords":["different","toward","across","carry","between"],"definitions":{"different":"not the same","toward":"in the direction of","across":"from one side to the other","between":"in the middle of two things"},"contextSentences":["The two brothers are very different in hobbies.","The cat ran across the street.","Walk toward the big tree, then turn left.","The secret is hidden between the two old boxes."],"curriculumCode":"EN3PWS-IV-1"}',
 'phonics', 30, 60),
(113, 1, 'FINAL_ASSESSMENT', 13, 4,
 'Phonics and Word Study: Final Assessment',
 'Demonstrate mastery of all phonics concepts: CVCC/CCVC patterns, syllable division (VCV/VCCV), and sight words across all four quarters.',
 '{"type":"cumulative","quarters":["Q1: CVCC/CCVC word patterns and diphthongs","Q2: VCV/VCCV syllable division rules","Q3: Narrative and descriptive sight words","Q4: Abstract and directional sight words"],"totalItems":20}',
 'phonics', 45, 100);
GO

-- ── NODES: VOCABULARY (ModuleID=2, NodeIDs 201-213) ──────────
INSERT INTO Nodes (NodeID, ModuleID, NodeType, NodeNumber, Quarter, LessonTitle, LearningObjectives, ContentJSON, SkillCategory, EstimatedDuration, XPReward) VALUES
(201, 2, 'CORE_LESSON', 1, 1,
 'High-Frequency Words',
 'Identify and read high-frequency words (the, is, are, we, she, he, they, like, go, see, because, their, have) accurately; use them in sentences.',
 '{"keyWords":["the","is","are","we","she","he","they","like","go","see","because","their","have"],"rule":"High-frequency words appear often in texts and cannot always be sounded out phonetically. We must remember them.","examples":["The cat is sleeping.","I like to play.","We went to the park.","She is happy.","They are my friends."],"curriculumCode":"EN3VWK-I-1"}',
 'vocabulary', 25, 50),
(202, 2, 'CORE_LESSON', 2, 1,
 'Regional and National Theme Vocabulary',
 'Use vocabulary words related to regional themes (barangay, fiesta, palengke, tricycle, jeepney, farmer) and national themes (flag, hero, festival, province).',
 '{"themeWords":["barangay","fiesta","palengke","jeepney","farmer","rice field","island","mountain","flag","hero","festival","province"],"contextSentences":["We celebrate a fiesta in our barangay.","My mother buys fish in the palengke.","The farmer works in the rice field."],"curriculumCode":"EN3VWK-I-2","theme":"Regional and National Themes"}',
 'vocabulary', 25, 50),
(203, 2, 'CORE_LESSON', 3, 1,
 'Content-Specific Words: Math and Science',
 'Use content-specific vocabulary for Mathematics (sum, difference, product, quotient, addition, subtraction) and Science (plant, animal, water, air, sun, soil).',
 '{"mathWords":["sum","difference","product","quotient","addition","subtraction"],"scienceWords":["plant","animal","water","air","sun","soil","energy","experiment"],"examples":{"math":["The sum of 7 and 5 is 12.","The difference of 10 and 6 is 4."],"science":["A plant grows in soil.","Animals need water to survive."]}}',
 'vocabulary', 30, 55),
(204, 2, 'CORE_LESSON', 4, 2,
 'Naming Words: Common and Proper Nouns',
 'Identify and classify common nouns (dog, school, city) and proper nouns (Juan, Manila, Christmas); recognize masculine, feminine, and neutral nouns.',
 '{"nounTypes":{"common":["boy","school","cat","park","teacher","dog","city","month"],"proper":["Juan","Manila","Christmas","Rizal Park","Cebu City","Maria","Monday","Ateneo"]},"gender":{"masculine":["father","uncle","king","hero","boy"],"feminine":["mother","aunt","queen","grandmother","princess"],"neutral":["student","child","teacher","person"]},"curriculumCode":"EN2VWK-IV-4"}',
 'vocabulary', 30, 55),
(205, 2, 'CORE_LESSON', 5, 2,
 'Action Words: Verbs',
 'Identify and use verbs (run, jump, eat, play, read, explain, jump, do, drink, cry) correctly in sentences; understand subject-verb agreement.',
 '{"definition":"Verbs tell what a person, animal, or thing is doing.","examples":["The boy runs fast.","The cat jumps on the chair.","Maria reads a book.","She drinks water."],"rule":"For singular subjects (he, she, it), add -s/-es to the verb.","verbList":["run","jump","eat","play","read","wash","explain","drink","cry","study","write","sing"],"curriculumCode":"EN2VWK-IV-4.2"}',
 'vocabulary', 30, 55),
(206, 2, 'CORE_LESSON', 6, 2,
 'Describing Words: Adjectives',
 'Identify and use adjectives (happy, tall, small, beautiful, noisy, exciting, sad, colorful, square, many) to describe nouns.',
 '{"definition":"An adjective describes a person, place, thing, animal, event, idea, or emotion.","examples":["The happy boy plays outside.","The tall building touches the sky.","The noisy dog barks loudly.","The red apple is sweet."],"adjectives":["happy","tall","small","beautiful","noisy","exciting","sad","colorful","big","red","blue","square","many","soft"],"curriculumCode":"EN2VWK-IV-4.3"}',
 'vocabulary', 30, 55),
(207, 2, 'CORE_LESSON', 7, 3,
 'Word Detectives: Verbs, Adjectives, Demonstrative Pronouns',
 'Identify verbs, adjectives, and demonstrative pronouns (this, that, these, those); recognize basic synonyms (big/large, fast/quick) and antonyms (hot/cold, tall/short).',
 '{"topics":["Verbs (action words)","Adjectives (describing words)","Demonstrative Pronouns: this (near, singular), that (far, singular), these (near, plural), those (far, plural)"],"synonymPairs":[["big","large"],["fast","quick"],["happy","glad"],["small","tiny"]],"antonymPairs":[["hot","cold"],["tall","short"],["happy","sad"],["early","late"]],"curriculumCode":"EN3VWK-IV-4, EN3VWK-IV-5"}',
 'vocabulary', 30, 55),
(208, 2, 'CORE_LESSON', 8, 3,
 'Word Builders: Using Vocabulary in Sentences',
 'Use verbs, adjectives, and demonstrative pronouns correctly in sentences; identify synonyms and antonyms in context.',
 '{"focus":"Applying vocabulary knowledge in sentence contexts","demonstrativeRules":{"this":"near + singular","that":"far + singular","these":"near + plural","those":"far + plural"},"sentenceExamples":["The cat jumps on the roof.","She has a blue dress.","This is my pencil.","Those are my crayons."],"synonymsInContext":["glad → happy","dirty → clean (antonym)"],"curriculumCode":"EN3VWK-IV-4, EN3VWK-IV-5"}',
 'vocabulary', 30, 55),
(209, 2, 'CORE_LESSON', 9, 3,
 'Word Masters: Classify and Analyze',
 'Independently classify word functions (verb, adjective, demonstrative pronoun) and distinguish synonyms from antonyms in complex sentences.',
 '{"focus":"Analysis and mastery level application","skills":["Identify verbs in sentences","Identify adjectives in sentences","Use correct demonstrative pronouns","Distinguish synonym vs antonym pairs","Analyze sentences for word functions"],"curriculumCode":"EN3VWK-IV-4, EN3VWK-IV-5"}',
 'vocabulary', 35, 60),
(210, 2, 'CORE_LESSON', 10, 4,
 'Word Pattern Challenge: Reading Word Families',
 'Read words correctly based on common patterns; recognize rhyming words and word families (-at: cat/bat/hat, -ight: light/night/sight, -ake: cake/bake/lake, -an, -ig, -ap).',
 '{"wordFamilies":{"-at":["cat","bat","hat","mat","rat"],"-ight":["light","night","sight","right","tight"],"-ake":["cake","bake","lake","take","make"],"-an":["man","ran","tan","pan","can"],"-ig":["pig","big","dig","fig","wig"],"-ap":["map","cap","tap","nap","lap"]},"rule":"Words in the same family share the same ending sound and spelling.","curriculumCode":"EN3VWK-IV-6"}',
 'vocabulary', 30, 55),
(211, 2, 'CORE_LESSON', 11, 4,
 'Spell It Right: Writing Words Correctly',
 'Identify and write correctly spelled words based on patterns; choose correctly spelled forms (because, night, lights, played, helpful, happy, jumped, little, running, study).',
 '{"focus":"Correct spelling patterns","commonErrors":[["because","becaus","becos"],["night","nite","nyght"],["played","playd","plaed"],["helpful","helpfull","helpfol"],["happy","happi","hapy"],["running","runing","runnung"]],"rule":"Adding -ing: drop silent e or double final consonant. Adding -ed: most words add -ed directly.","curriculumCode":"EN3VWK-IV-7"}',
 'vocabulary', 30, 55),
(212, 2, 'CORE_LESSON', 12, 4,
 'Root Words: Base Words in Nouns, Verbs, Adjectives',
 'Identify root (base) words in longer words; understand how suffixes (-ing, -ed, -er, -ful, -ly, -ness, -est) change meaning while keeping the root.',
 '{"rootWordExamples":[["playing","play"],["teacher","teach"],["helpful","help"],["jumped","jump"],["reader","read"],["happily","happy"],["fastest","fast"],["painted","paint"],["singer","sing"],["kindness","kind"]],"suffixes":{"-ing":"ongoing action","-ed":"past action","-er":"person who does something","-ful":"full of","-ly":"in a way","-ness":"state of being","-est":"most"},"curriculumCode":"EN3VWK-IV-8"}',
 'vocabulary', 30, 60),
(213, 2, 'FINAL_ASSESSMENT', 13, 4,
 'Vocabulary and Word Knowledge: Final Assessment',
 'Demonstrate mastery of all vocabulary concepts: high-frequency words, nouns, verbs, adjectives, synonyms, antonyms, word families, spelling patterns, and root words.',
 '{"type":"cumulative","quarters":["Q1: High-frequency words, regional/national vocabulary, content-specific words","Q2: Common/proper nouns, gender of nouns, verbs, adjectives","Q3: Verbs, adjectives, demonstrative pronouns, synonyms, antonyms","Q4: Word families, spelling patterns, root words"],"totalItems":20}',
 'vocabulary', 45, 100);
GO

-- ── NODES: GRAMMAR (ModuleID=3, NodeIDs 301-313) ─────────────
INSERT INTO Nodes (NodeID, ModuleID, NodeType, NodeNumber, Quarter, LessonTitle, LearningObjectives, ContentJSON, SkillCategory, EstimatedDuration, XPReward) VALUES
(301, 3, 'CORE_LESSON', 1, 1,
 'Sentences and Non-Sentences',
 'Distinguish complete sentences from non-sentences; understand that a sentence expresses a complete thought, starts with a capital letter, and ends with a punctuation mark.',
 '{"rule":"A sentence: (1) starts with a capital letter, (2) expresses a complete thought, (3) ends with a punctuation mark.","examples":{"sentences":["The cat is sleeping.","I love apples.","Birds fly.","She is happy."],"nonSentences":["The cat.","Running fast.","Blue sky.","Under the table."]},"curriculumCode":"EN3GAGS-I-1"}',
 'grammar', 25, 50),
(302, 3, 'CORE_LESSON', 2, 1,
 'Sequencing Words in Sentences',
 'Arrange words in correct order to form meaningful sentences; understand subject-verb-object structure.',
 '{"rule":"Words in a sentence must follow the correct order to make meaning. Usually: Subject + Verb + Object/Complement","examples":[["is / Manila / big","Manila is big."],["mango / The / sweet / is","The mango is sweet."],["plants / The / rice / farmer","The farmer plants rice."],["swim / Fish / water / in","Fish swim in water."]],"curriculumCode":"EN3GAGS-I-2, EN3GAGS-I-4"}',
 'grammar', 25, 50),
(303, 3, 'CORE_LESSON', 3, 1,
 'Parts of a Simple Sentence: Subject and Predicate',
 'Identify the subject (who/what) and predicate (action/what they do) in simple sentences.',
 '{"definitions":{"subject":"Who or what the sentence is about (the doer)","predicate":"What the subject does or is"},"examples":[{"sentence":"The bird flies.","subject":"bird","predicate":"flies"},{"sentence":"Maria dances.","subject":"Maria","predicate":"dances"},{"sentence":"The sun shines.","subject":"sun","predicate":"shines"},{"sentence":"My mother cooks.","subject":"mother","predicate":"cooks"}],"curriculumCode":"EN3GAGS-I-5"}',
 'grammar', 30, 55),
(304, 3, 'CORE_LESSON', 4, 2,
 'Telling and Asking Sentences',
 'Distinguish declarative sentences (telling, ends with .) from interrogative sentences (asking, ends with ?); use correct end punctuation.',
 '{"types":{"declarative":{"definition":"Tells a fact or information","endMark":".","examples":["I am 8 years old.","The sun is hot.","It is raining."]},"interrogative":{"definition":"Asks a question","endMark":"?","examples":["Where is Ben?","What is it?","Can you run?","Who are you?","How are you?"]}},"curriculumCode":"EN3GAGS-II-4.1, EN3GAGS-II-4.2"}',
 'grammar', 25, 50),
(305, 3, 'CORE_LESSON', 5, 2,
 'Commands and Exclamations',
 'Distinguish imperative sentences (commands/requests, ends with .) from exclamatory sentences (strong feelings, ends with !); use correct punctuation.',
 '{"types":{"imperative":{"definition":"Gives a command or request","endMark":". or !","examples":["Close the door.","Please sit.","Clean your desk.","Kindly help me."]},"exclamatory":{"definition":"Shows strong feelings","endMark":"!","examples":["Wow!","Ouch!","I am so happy!","We won!","Fire!"]}},"curriculumCode":"EN3GAGS-II-4.3, EN3GAGS-II-4.4"}',
 'grammar', 25, 50),
(306, 3, 'CORE_LESSON', 6, 2,
 'Compound Sentences with Conjunctions',
 'Combine two simple sentences into a compound sentence using conjunctions (and, but, or); identify the joining word and understand its function.',
 '{"conjunctions":{"and":"adds or combines similar ideas","but":"shows contrast or opposite ideas","or":"shows choice between two options"},"examples":[{"simple":["I like milk.","I like cake."],"compound":"I like milk and I like cake."},{"simple":["I ran.","I fell."],"compound":"I ran, but I fell."},{"simple":["Eat now.","Eat later."],"compound":"Eat now or later."}],"curriculumCode":"EN3GAGS-II-7"}',
 'grammar', 30, 55),
(307, 3, 'CORE_LESSON', 7, 3,
 'Capitalization and Punctuation Rules',
 'Apply capitalization rules (start of sentence, proper names, I) and use correct end punctuation (. ? !); fix incorrectly written sentences.',
 '{"rules":["Sentences start with a capital letter","Proper names are capitalized (Lito, Philippines, Cebu)","The word I is always capitalized","End with . (telling), ? (asking), or ! (exclamation/command)"],"examples":{"correct":["My name is Anna.","I live in Cebu.","Help! I am lost!","Do you like milk?"],"incorrect":["my name is anna.","i live in cebu.","help i am lost!","do you like milk."]},"curriculumCode":"EN3GAGS-III-6"}',
 'grammar', 30, 55),
(308, 3, 'CORE_LESSON', 8, 3,
 'Discourse Markers: Time Order Words',
 'Use time order words (First, Next, Then, Finally) to sequence events and write/read procedures in the correct order.',
 '{"markers":{"First":"begins a sequence; the starting step","Next":"the following step","Then":"what comes after next","Finally":"the last step"},"examples":["First, wash your hands.","Next, use soap.","Then, rinse well.","Finally, dry your hands."],"usage":"These words help readers follow steps in a process or events in a story.","curriculumCode":"EN3GAGS-III-9.1"}',
 'grammar', 25, 50),
(309, 3, 'CORE_LESSON', 9, 3,
 'Doers and Actions in Compound Sentences',
 'Identify the subject (doer) and verb (action) in each part of a compound sentence; understand that compound sentences have two doers and two actions.',
 '{"rule":"A compound sentence joins two simple sentences. Each part has its own subject and verb.","examples":[{"compound":"Ben eats and Lea drinks.","part1":{"subject":"Ben","verb":"eats"},"part2":{"subject":"Lea","verb":"drinks"}},{"compound":"The dog barked and the cat ran.","part1":{"subject":"dog","verb":"barked"},"part2":{"subject":"cat","verb":"ran"}}],"curriculumCode":"EN3GAGS-III-7.3"}',
 'grammar', 30, 55),
(310, 3, 'CORE_LESSON', 10, 4,
 'National Themes in Sentences',
 'Write and identify sentences about national pride and Filipino culture using correct sentence structure, types, and punctuation.',
 '{"nationalThemeWords":["Philippines","flag","Filipino","Mabuhay","hero","nation","people","respect","elders"],"sentenceTypes":{"telling":"The Philippines is my country.","asking":"Is the sun yellow?","command":"Respect the elders.","exclamation":"Mabuhay!"},"curriculumCode":"EN3GAGS-IV-2"}',
 'grammar', 25, 50),
(311, 3, 'CORE_LESSON', 11, 4,
 'Explanatory Discourse: Using Because',
 'Use the connector "because" to explain reasons and causes; distinguish explanatory sentences from descriptive or command sentences.',
 '{"rule":"Because connects a cause (reason) to an effect (result). It answers the question WHY.","examples":["I am happy because it is my birthday.","It is raining because there are clouds.","It stopped because it had no gas.","The dog barked because it saw a cat.","Wash your hands because they are dirty."],"curriculumCode":"EN3GAGS-IV-9.3"}',
 'grammar', 25, 50),
(312, 3, 'CORE_LESSON', 12, 4,
 'Sentence Intonation and Pitch',
 'Understand that intonation (rising/falling pitch) signals the type of sentence; use rising pitch for questions and falling pitch for statements.',
 '{"pitchTypes":{"rising":"Used for questions. Voice goes up at the end. Example: Can I eat?","falling":"Used for telling sentences. Voice goes down at the end. Example: I am here.","high":"Often shows excitement or surprise. Example: Wow!"},"examples":[{"sentence":"Who are you?","pitch":"Rising"},{"sentence":"I am here.","pitch":"Falling"},{"sentence":"We won!","pitch":"High/Falling"}],"curriculumCode":"EN3GAGS-IV-3"}',
 'grammar', 25, 50),
(313, 3, 'FINAL_ASSESSMENT', 13, 4,
 'Grammar Awareness: Final Assessment',
 'Demonstrate mastery of all grammar concepts: sentences, word order, subjects and predicates, sentence types, compound sentences, punctuation, discourse markers, and intonation.',
 '{"type":"cumulative","quarters":["Q1: Sentences vs non-sentences, word sequencing, subject and predicate","Q2: Types of sentences (declarative, interrogative, imperative, exclamatory), compound sentences","Q3: Capitalization, punctuation, time-order discourse markers, compound sentence analysis","Q4: National theme sentences, because (explanation), sentence intonation and pitch"],"totalItems":20}',
 'grammar', 45, 100);
GO

-- ── NODES: COMPREHENSION (ModuleID=4, NodeIDs 401-413) ───────
INSERT INTO Nodes (NodeID, ModuleID, NodeType, NodeNumber, Quarter, LessonTitle, LearningObjectives, ContentJSON, SkillCategory, EstimatedDuration, XPReward) VALUES
(401, 4, 'CORE_LESSON', 1, 1,
 'Identifying Key Details: Who, What, Where, When',
 'Extract important details (who, what, where, when) from a short literary or informational text.',
 '{"questionWords":{"who":"the character(s)","what":"the main action or object","where":"the setting/location","when":"the time of events"},"strategy":"After reading, ask yourself: Who is in the story? What happened? Where did it happen? When did it happen?","practice":"Ana has a small brown dog. They play in the park every afternoon."}',
 'comprehension', 30, 55),
(402, 4, 'CORE_LESSON', 2, 1,
 'Sequencing Events with Signal Words',
 'Arrange story events in correct chronological order using sequence signal words (first, next, then, last).',
 '{"signalWords":{"first":"the beginning event","next":"the second event","then":"what follows","last":"the final event"},"strategy":"Look for signal words to find the order of events. Make sure events are in the right time sequence.","practice":"First, Tom woke up. Next, he brushed his teeth. Last, he ate breakfast."}',
 'comprehension', 30, 55),
(403, 4, 'CORE_LESSON', 3, 1,
 'Characters and Setting',
 'Identify and describe the characters (people/animals in the story) and setting (where and when the story takes place).',
 '{"definitions":{"character":"The person, animal, or being in the story","setting":"Where and when the story happens"},"strategy":"Characters are the people or animals. Setting is the place and time.","examples":{"character":"Liza and her brother","setting":"the beach"},"practice":"Liza and her brother went to the beach. They built a sandcastle."}',
 'comprehension', 25, 50),
(404, 4, 'CORE_LESSON', 4, 2,
 'Main Idea and Supporting Details',
 'Identify the main idea (the big idea) of a short text and distinguish it from supporting details.',
 '{"definitions":{"mainIdea":"The most important idea the text is about","supportingDetails":"Facts or examples that tell more about the main idea"},"strategy":"Ask: What is the whole passage mostly about? That is the main idea.","practice":"Dogs are helpful animals. They guard houses and help people.","hint":"The title often hints at the main idea."}',
 'comprehension', 30, 55),
(405, 4, 'CORE_LESSON', 5, 2,
 'Cause and Effect Relationships',
 'Identify cause-and-effect relationships in simple texts; understand that a cause is why something happened and the effect is what happened.',
 '{"definitions":{"cause":"The reason WHY something happened","effect":"WHAT happened as a result"},"clueWords":["because","so","therefore","as a result","since"],"strategy":"Ask: Why did this happen? (cause) What happened because of it? (effect)","practice":"It rained hard. The streets became wet.","examples":[{"cause":"It rained hard","effect":"The streets became wet"}]}',
 'comprehension', 30, 55),
(406, 4, 'CORE_LESSON', 6, 2,
 'Making Predictions and Asking Questions',
 'Use text clues and prior knowledge to make predictions about what might happen next; generate questions while reading.',
 '{"skills":["Making predictions based on text clues","Asking who/what/where/when/why/how questions","Using prior knowledge to predict outcomes"],"strategy":"Look for clues in the text + think about what you already know = make a smart prediction.","questionStarters":["I predict that...","I think... because...","What might happen if...?"],"curriculumApprox":"Q2L3 Comprehension"}',
 'comprehension', 30, 55),
(407, 4, 'CORE_LESSON', 7, 3,
 'Problem and Solution in Narrative Texts',
 'Identify the problem (what went wrong or the conflict) and the solution (how the problem was resolved) in a narrative text.',
 '{"definitions":{"problem":"What goes wrong or the difficulty the character faces","solution":"How the problem is fixed or resolved"},"strategy":"Ask: What went wrong? How was it fixed?","practice":"Ben brought his kite to the park. The wind was very strong. The kite got stuck in a tree. Ben felt sad. His father used a long stick to get the kite down. Ben smiled again.","analysis":{"problem":"The kite got stuck in a tree","solution":"Father used a long stick to get the kite down"}}',
 'comprehension', 30, 55),
(408, 4, 'CORE_LESSON', 8, 3,
 'Comparing and Contrasting Characters',
 'Compare and contrast characters, events, or ideas by identifying similarities (how they are alike) and differences (how they are different).',
 '{"definitions":{"compare":"Find how two things are ALIKE (use: both, similarly, also)","contrast":"Find how two things are DIFFERENT (use: but, however, on the other hand, while)"},"practice":"Ana likes to read books quietly in her room. Mia likes to play outside with her friends. Both girls enjoy learning new things.","analysis":{"similarities":["Both enjoy learning new things","Both are girls"],"differences":["Ana reads quietly indoors","Mia plays outside with friends"]}}',
 'comprehension', 30, 55),
(409, 4, 'CORE_LESSON', 9, 3,
 'Context Clues: Synonyms and Antonyms',
 'Use context clues from surrounding words and sentences to determine the meaning of unfamiliar words; identify synonyms and antonyms in context.',
 '{"definitions":{"contextClues":"Hints in the text that help you figure out what an unfamiliar word means","synonyms":"Words that have the SAME or similar meaning (quick/fast)","antonyms":"Words with OPPOSITE meaning (quick/slow)"},"practice":"The rabbit is quick. It runs very fast. The turtle is slow. It moves very slowly.","analysis":{"synonymPair":["quick","fast"],"antonymPair":["quick","slow"]},"strategy":"Look at nearby words and sentences to figure out what an unknown word means."}',
 'comprehension', 30, 55),
(410, 4, 'CORE_LESSON', 10, 4,
 'Drawing Conclusions and Making Inferences',
 'Draw conclusions and make inferences using text evidence combined with prior knowledge; go beyond what is directly stated.',
 '{"definitions":{"inference":"A smart guess based on clues from the text + what you already know","drawingConclusions":"Using evidence to come to a logical decision"},"formula":"Text Clue + Prior Knowledge = Inference","practice":"Dark clouds filled the sky. The wind blew hard. Carlo grabbed his umbrella before leaving the house.","inference":"It is probably going to rain.","strategy":"Look for clues in the text. Think: what does this suggest? What do I already know about this?"}',
 'comprehension', 35, 60),
(411, 4, 'CORE_LESSON', 11, 4,
 'Text-to-Self Connections',
 'Relate ideas from a text to personal experiences; express simple reflections by connecting story events to own life.',
 '{"definition":"Text-to-self connection: Relating what you read to your own experiences, feelings, or memories.","strategy":"While reading, think: Does this remind me of something in my life? Have I felt this way before?","connectionTypes":["Text-to-Self: The story reminds me of...","Text-to-World: This connects to what I know about the world...","Text-to-Text: This reminds me of another book I read..."],"reflectionStarters":["This reminds me of when I...","I felt like the character when...","I know how the character feels because..."]}',
 'comprehension', 25, 50),
(412, 4, 'CORE_LESSON', 12, 4,
 'Summarizing: Main Idea and Key Details',
 'Summarize a text by identifying the main idea and key details; retell information in a clear, concise way.',
 '{"definition":"A summary tells the most important ideas in a short, clear way. It is NOT a copy of the original text.","rules":["Include the main idea","Include 2-3 key details","Use your own words","Keep it short"],"practice":"Plants need sunlight, water, and soil to grow and stay healthy.","goodSummary":"Plants need sunlight, water, and soil to grow and stay healthy.","badSummary":"Plants are green and small. Water is wet.","strategy":"Ask: What is this MOSTLY about? What are the 2-3 most important facts?"}',
 'comprehension', 30, 55),
(413, 4, 'FINAL_ASSESSMENT', 13, 4,
 'Comprehension: Final Assessment',
 'Demonstrate mastery of all comprehension strategies: key details, sequencing, characters and setting, main idea, cause and effect, problem and solution, compare and contrast, context clues, inference, and summarizing.',
 '{"type":"cumulative","quarters":["Q1: Key details (who, what, where, when), sequencing, characters and setting","Q2: Main idea, cause and effect, predictions","Q3: Problem and solution, compare and contrast, context clues","Q4: Drawing conclusions, text-to-self connections, summarizing"],"totalItems":20}',
 'comprehension', 45, 100);
GO

-- ── NODES: CREATING (ModuleID=5, NodeIDs 501-513) ────────────
INSERT INTO Nodes (NodeID, ModuleID, NodeType, NodeNumber, Quarter, LessonTitle, LearningObjectives, ContentJSON, SkillCategory, EstimatedDuration, XPReward) VALUES
(501, 5, 'CORE_LESSON', 1, 1,
 'Writing Simple Sentences',
 'Write complete, correctly punctuated simple sentences; start with a capital letter, express a complete thought, and end with the correct mark.',
 '{"skills":["Writing complete sentences","Using capital letters at the start","Using correct end punctuation (. ? !)","Expressing a complete thought"],"examples":["I have a dog.","The sky is blue.","Can you help me?","Wow, it is beautiful!"],"checklist":["Capital letter at start?","Complete thought?","Correct end mark?"]}',
 'creating', 30, 55),
(502, 5, 'CORE_LESSON', 2, 1,
 'Describing with Adjectives',
 'Write descriptive sentences using adjectives; paint a vivid picture by adding describing words to nouns.',
 '{"skill":"Using adjectives to make writing more vivid and interesting","strategy":"Think of the 5 senses: what does it look, sound, feel, smell, or taste like?","examples":["The big, brown dog slept peacefully.","She wore a bright, colorful dress.","The sweet, juicy mango tasted delicious."],"practice":"Add adjectives to improve: The cat sat. → The small, fluffy cat sat quietly."}',
 'creating', 30, 55),
(503, 5, 'CORE_LESSON', 3, 1,
 'Writing a Simple Paragraph',
 'Write a simple paragraph with a topic sentence and 2-3 supporting sentences; understand paragraph structure.',
 '{"structure":{"topicSentence":"States the main idea of the paragraph","supportingSentences":"Give details or examples about the main idea","conclusion":"Optional: wraps up the paragraph"},"example":"I love my dog. He is small and brown. He plays with me every day. My dog is my best friend.","tips":["All sentences in a paragraph are about ONE main idea","Indent the first line","Stay on topic"]}',
 'creating', 35, 60),
(504, 5, 'CORE_LESSON', 4, 2,
 'Writing Questions and Commands',
 'Write interrogative sentences (questions) and imperative sentences (commands/requests) with correct punctuation.',
 '{"questionWords":["Who?","What?","Where?","When?","Why?","How?"],"examples":{"questions":["Where is my bag?","Who is your teacher?","When do we eat?"],"commands":["Please sit down.","Clean your desk.","Help your classmates."]},"punctuation":{"question":"ends with ?","command":"ends with . or !"}}',
 'creating', 25, 50),
(505, 5, 'CORE_LESSON', 5, 2,
 'Writing with Joining Words',
 'Write compound sentences using conjunctions (and, but, or); combine two ideas into one flowing sentence.',
 '{"conjunctions":{"and":"adds ideas: I like rice and I like bread.","but":"shows contrast: I am tired but I will study.","or":"shows choice: We can play inside or outside."},"practice":{"combine":[["I like cats.","I like dogs.","I like cats and I like dogs."],["She is smart.","She is kind.","She is smart and kind."],["It is hot.","I feel cold.","It is hot, but I feel cold."]]},"tip":"Use a comma before but or or when joining two complete sentences."}',
 'creating', 30, 55),
(506, 5, 'CORE_LESSON', 6, 2,
 'Writing a Short Story',
 'Write a short narrative with a clear beginning, middle, and end; include characters, setting, and a simple problem-solution structure.',
 '{"storyParts":{"beginning":"Introduce characters and setting","middle":"Tell the problem or main event","end":"Tell how the problem was solved or how the story ended"},"example":{"beginning":"Mia found a tiny lost puppy near the school gate.","middle":"She did not know who owned it. She felt sad for the puppy.","end":"She brought it home and took care of it. It became her pet!"},"tips":["Use time words: First, Then, Finally","Give characters names","Show feelings"]}',
 'creating', 35, 60),
(507, 5, 'CORE_LESSON', 7, 3,
 'Using Time-Order Words in Writing',
 'Write procedural and narrative texts using discourse markers (First, Next, Then, Finally) to sequence events or steps clearly.',
 '{"markers":["First","Next","Then","After that","Finally","Last"],"writingTypes":{"procedure":"Steps to do something (how to make a sandwich, how to plant seeds)","narrative":"Events in a story in time order"},"example":"First, I wake up early. Next, I brush my teeth. Then, I eat breakfast. Finally, I go to school.","practice":"Write the steps for making a fruit salad using First, Next, Then, Finally."}',
 'creating', 30, 55),
(508, 5, 'CORE_LESSON', 8, 3,
 'Writing Explanations with Because',
 'Write explanatory sentences using "because" to give reasons; explain why things happen.',
 '{"skill":"Using because to explain reasons","structure":"Result/Effect + because + Reason/Cause","examples":["I wear a jacket because it is cold.","The plants grow tall because they get plenty of sunlight.","I study hard because I want to pass my exams.","She smiled because she received a gift."],"practice":"Complete these sentences: I drink water because ___. I love reading because ___."}',
 'creating', 25, 50),
(509, 5, 'CORE_LESSON', 9, 3,
 'Writing Comparative Sentences',
 'Write sentences comparing two things using comparison words (bigger than, smaller than, better, best, both); use adjectives to compare.',
 '{"comparisonForms":{"positive":"big, fast, small","comparative":"bigger, faster, smaller (comparing 2 things)","superlative":"biggest, fastest, smallest (comparing 3 or more)"},"examples":["The elephant is bigger than the cat.","Maria runs faster than Ben.","This is the best mango I have ever tasted.","Both dogs are friendly."],"tip":"Add -er for comparing 2; add -est for comparing 3 or more. For long words, use more/most."}',
 'creating', 30, 55),
(510, 5, 'CORE_LESSON', 10, 4,
 'Writing About National Themes',
 'Write sentences and short paragraphs about Filipino culture, national pride, and community values using correct grammar.',
 '{"themes":["Philippine flag and symbols","Filipino heroes","Bayanihan (community spirit)","Local festivals and traditions","Respect for elders"],"example":"The Philippines is a beautiful country. Our flag has three colors: blue, red, and white. We are proud to be Filipinos.","vocabulary":["Mabuhay","bayanihan","kababayan","pamana","pagmamahal sa bayan"]}',
 'creating', 30, 55),
(511, 5, 'CORE_LESSON', 11, 4,
 'Writing a Descriptive Paragraph',
 'Write a descriptive paragraph about a person, place, or thing; use vivid adjectives, sensory details, and varied sentence structures.',
 '{"structure":"Topic sentence (what is being described) + 3-4 descriptive details + concluding sentence","sensoryWords":{"sight":["colorful","bright","dark","tiny","enormous"],"sound":["noisy","quiet","loud","soft","humming"],"touch":["rough","smooth","soft","cold","warm"],"smell":["sweet","fresh","sour","fragrant"]},"example":"My classroom is a wonderful place to learn. It has colorful charts on the white walls. The room smells like fresh paper and crayons. My classmates are always friendly and helpful. I love being in my classroom every day."}',
 'creating', 35, 60),
(512, 5, 'CORE_LESSON', 12, 4,
 'Writing a Book Response',
 'Write a short response to a text by summarizing the main idea, sharing a personal connection, and giving an opinion.',
 '{"structure":{"summary":"What was the text about? (main idea + key details)","connection":"How does it connect to your life? (text-to-self)","opinion":"Did you like it? Why or why not?"},"sentenceStarters":["This story is about...","The main idea is...","This reminds me of...","I think this story is... because...","My favorite part is... because..."],"example":"This story is about a lost puppy that found a new home. It reminds me of when I found a stray cat near our house. I think this story is heartwarming because the puppy was saved."}',
 'creating', 30, 55),
(513, 5, 'FINAL_ASSESSMENT', 13, 4,
 'Creating and Composing: Final Assessment',
 'Demonstrate mastery of all composition skills: simple sentences, descriptive writing, narrative texts, explanatory writing, comparative sentences, and complete paragraphs.',
 '{"type":"cumulative","quarters":["Q1: Simple sentences, descriptive sentences, paragraph structure","Q2: Questions and commands, compound sentences, short stories","Q3: Time-order words, explanatory writing with because, comparative sentences","Q4: National theme writing, descriptive paragraphs, book responses"],"totalItems":20}',
 'creating', 45, 100);
SET IDENTITY_INSERT Nodes OFF;
GO

-- ── QUIZ QUESTIONS: PHONICS MODULE (NodeIDs 101-113) ─────────

-- Node 101: Sight Words & CVCC Patterns
INSERT INTO QuizQuestions (NodeID,QuestionText,QuestionType,OptionsJSON,CorrectAnswer,EstimatedDifficulty,SkillCategory,ReadingPassage) VALUES
(101,'I ____ see the moon from my window.','multiple_choice','["could","cold"]','could',0.2,'phonics',NULL),
(101,'Please turn on the ____ so we can see.','multiple_choice','["right","light"]','light',0.2,'phonics',NULL),
(101,'You should ____ wash your hands.','multiple_choice','["always","away"]','always',0.2,'phonics',NULL),
(101,'We ran ____ the playground.','multiple_choice','["around","about"]','around',0.2,'phonics',NULL),
(101,'The sun is ____ today.','multiple_choice','["bright","bring"]','bright',0.3,'phonics',NULL),
(101,'Which word ends with two consonants? (CVCC pattern)','multiple_choice','["Cat","Lamp"]','Lamp',0.3,'phonics',NULL),
(101,'Which word is a CVCC word?','multiple_choice','["Best","Blue"]','Best',0.3,'phonics',NULL),
(101,'Choose the correct ending for "Ma__"','multiple_choice','["sk","oo"]','sk',0.4,'phonics',NULL),
(101,'Which word fits the CVCC pattern?','multiple_choice','["See","Pink"]','Pink',0.4,'phonics',NULL),
(101,'Identify the CVCC word in this sentence: "The sand is warm."','multiple_choice','["sand","warm"]','sand',0.4,'phonics',NULL);

-- Node 102: CCVC Patterns
INSERT INTO QuizQuestions (NodeID,QuestionText,QuestionType,OptionsJSON,CorrectAnswer,EstimatedDifficulty,SkillCategory,ReadingPassage) VALUES
(102,'Which word starts with a consonant blend?','multiple_choice','["Apple","Frog"]','Frog',0.3,'phonics',NULL),
(102,'Fill in the blank: "The ___ar is in the sky."','multiple_choice','["St","Pl"]','St',0.3,'phonics',NULL),
(102,'Which word is CCVC?','multiple_choice','["Drum","Red"]','Drum',0.3,'phonics',NULL),
(102,'Choose the word that starts like Clap:','multiple_choice','["Clip","Cap"]','Clip',0.3,'phonics',NULL),
(102,'What is the cluster in the word ST-O-P?','multiple_choice','["ST","OP"]','ST',0.3,'phonics',NULL),
(102,'Which word has a "boiling" vowel sound (diphthong)?','multiple_choice','["Coin","Cone"]','Coin',0.4,'phonics',NULL),
(102,'Pick the CCVC word:','multiple_choice','["Spot","Pots"]','Spot',0.4,'phonics',NULL),
(102,'Which word starts with "Gr"?','multiple_choice','["Grab","Bag"]','Grab',0.3,'phonics',NULL),
(102,'Choose the correct CCVC word for a heavy object:','multiple_choice','["Rock","Brick"]','Brick',0.5,'phonics',NULL),
(102,'Which word rhymes with Trip?','multiple_choice','["Skip","Tip"]','Skip',0.4,'phonics',NULL);

-- Node 103: VCV and VCCV Patterns
INSERT INTO QuizQuestions (NodeID,QuestionText,QuestionType,OptionsJSON,CorrectAnswer,EstimatedDifficulty,SkillCategory,ReadingPassage) VALUES
(103,'In the word Rabbit, where do we split the syllables?','multiple_choice','["Ra-bbit","Rab-bit"]','Rab-bit',0.4,'phonics',NULL),
(103,'Is Robot a VCV or VCCV word?','multiple_choice','["VCV","VCCV"]','VCV',0.4,'phonics',NULL),
(103,'Which word follows the VCCV pattern?','multiple_choice','["Kitten","Tiger"]','Kitten',0.4,'phonics',NULL),
(103,'Choose the VCV word:','multiple_choice','["Music","Basket"]','Music',0.4,'phonics',NULL),
(103,'The word Muffin is:','multiple_choice','["VCV","VCCV"]','VCCV',0.4,'phonics',NULL),
(103,'Which word has a long "o" sound in a VCV pattern?','multiple_choice','["Open","Often"]','Open',0.5,'phonics',NULL),
(103,'Divide the word Napkin:','multiple_choice','["Nap-kin","Na-pkin"]','Nap-kin',0.5,'phonics',NULL),
(103,'Which word is a VCV pattern?','multiple_choice','["Paper","Pepper"]','Paper',0.4,'phonics',NULL),
(103,'Fill in the missing VCCV letters for a small dog: Pu__y','multiple_choice','["pp","b"]','pp',0.5,'phonics',NULL),
(103,'Which word fits the VCV pattern?','multiple_choice','["Seven","Summer"]','Seven',0.5,'phonics',NULL);

-- Node 104: Q2 Sight Words
INSERT INTO QuizQuestions (NodeID,QuestionText,QuestionType,OptionsJSON,CorrectAnswer,EstimatedDifficulty,SkillCategory,ReadingPassage) VALUES
(104,'Please ________ your hands before eating.','fill_blank','["clean","carry","drink","full","small"]','clean',0.2,'phonics',NULL),
(104,'I can ________ my own school bag.','fill_blank','["clean","carry","drink","full","small"]','carry',0.2,'phonics',NULL),
(104,'The glass is ________ of fresh milk.','fill_blank','["clean","carry","drink","full","small"]','full',0.2,'phonics',NULL),
(104,'The ________ kitten is sleeping under the tree.','fill_blank','["clean","carry","drink","full","small"]','small',0.2,'phonics',NULL),
(104,'We should ________ plenty of water every day.','fill_blank','["clean","carry","drink","full","small"]','drink',0.2,'phonics',NULL),
(104,'Choose the correctly spelled word for the number after seven:','multiple_choice','["eight","eyght"]','eight',0.3,'phonics',NULL),
(104,'Choose the correctly spelled word: The sun is very _____.','multiple_choice','["bright","brite"]','bright',0.3,'phonics',NULL),
(104,'Choose the correctly spelled word: Please _____ the dishes.','multiple_choice','["wash","wush"]','wash',0.3,'phonics',NULL),
(104,'Choose the correctly spelled word: _____ are my favorite shoes.','multiple_choice','["those","thos"]','those',0.3,'phonics',NULL),
(104,'Choose the correctly spelled word: Plants _____ in the garden.','multiple_choice','["grow","gro"]','grow',0.3,'phonics',NULL);

-- Node 105: VCV Open Syllable
INSERT INTO QuizQuestions (NodeID,QuestionText,QuestionType,OptionsJSON,CorrectAnswer,EstimatedDifficulty,SkillCategory,ReadingPassage) VALUES
(105,'Which word follows the VCV pattern?','multiple_choice','["Apple","Music"]','Music',0.4,'phonics',NULL),
(105,'How do you divide the word PILOT?','multiple_choice','["Pi-lot","Pil-ot"]','Pi-lot',0.4,'phonics',NULL),
(105,'In the word ROBOT, the first vowel ''O'' sounds:','multiple_choice','["Short","Long"]','Long',0.4,'phonics',NULL),
(105,'Choose the VCV word that is a fruit:','multiple_choice','["Melon","Cherry"]','Melon',0.4,'phonics',NULL),
(105,'Divide the word SILENT:','multiple_choice','["Si-lent","Sil-ent"]','Si-lent',0.5,'phonics',NULL),
(105,'Which word is a VCV word?','multiple_choice','["Tulip","Button"]','Tulip',0.5,'phonics',NULL),
(105,'What is the first syllable of BABY?','multiple_choice','["Ba","Bab"]','Ba',0.4,'phonics',NULL),
(105,'In EVIL, the division is:','multiple_choice','["E-vil","Ev-il"]','E-vil',0.5,'phonics',NULL),
(105,'Which word fits the pattern V-C-V?','multiple_choice','["Hotel","Hammer"]','Hotel',0.5,'phonics',NULL),
(105,'In a VCV word, the first vowel usually sounds:','multiple_choice','["Long (says its name)","Short"]','Long (says its name)',0.3,'phonics',NULL);

-- Node 106: VCCV Closed Syllable
INSERT INTO QuizQuestions (NodeID,QuestionText,QuestionType,OptionsJSON,CorrectAnswer,EstimatedDifficulty,SkillCategory,ReadingPassage) VALUES
(106,'How many syllables are in the word HAPPEN?','multiple_choice','["1","2","3"]','2',0.3,'phonics',NULL),
(106,'Divide the word BASKET:','multiple_choice','["Bas-ket","Ba-sket"]','Bas-ket',0.4,'phonics',NULL),
(106,'Which word follows the VCCV pattern?','multiple_choice','["Puppy","Tiger"]','Puppy',0.4,'phonics',NULL),
(106,'Divide the word MARKET:','multiple_choice','["Mar-ket","Mark-et"]','Mar-ket',0.4,'phonics',NULL),
(106,'In a VCCV word, the first vowel is usually:','multiple_choice','["Short","Long"]','Short',0.4,'phonics',NULL),
(106,'Which word follows VCCV? (Vowel-Consonant-Consonant-Vowel)','multiple_choice','["Basket","Bacon"]','Basket',0.5,'phonics',NULL),
(106,'Divide the word CANTEEN:','multiple_choice','["Can-teen","Ca-nteen"]','Can-teen',0.5,'phonics',NULL),
(106,'Which VCV word has a long vowel?','multiple_choice','["Open","Often"]','Open',0.5,'phonics',NULL),
(106,'The word DINNER divides as:','multiple_choice','["Din-ner","Di-nner"]','Din-ner',0.4,'phonics',NULL),
(106,'The word JACKET divides as:','multiple_choice','["Jack-et","Ja-cket"]','Jack-et',0.4,'phonics',NULL);

-- Node 107: Narrative Sight Words
INSERT INTO QuizQuestions (NodeID,QuestionText,QuestionType,OptionsJSON,CorrectAnswer,EstimatedDifficulty,SkillCategory,ReadingPassage) VALUES
(107,'Please _______ your umbrella because it might rain.','multiple_choice','["bring","drink"]','bring',0.2,'phonics',NULL),
(107,'The little boy can _______ a heavy box.','multiple_choice','["carry","clean"]','carry',0.2,'phonics',NULL),
(107,'I love to _______ pictures of my family.','multiple_choice','["drink","draw"]','draw',0.2,'phonics',NULL),
(107,'Be careful, or you might _______ on the wet floor.','multiple_choice','["fall","full"]','fall',0.2,'phonics',NULL),
(107,'Did you _______ enough water after the race?','multiple_choice','["draw","drink"]','drink',0.2,'phonics',NULL),
(107,'Which word is spelled correctly?','multiple_choice','["carry","karey"]','carry',0.3,'phonics',NULL),
(107,'"I will _______ my lunch to school."','multiple_choice','["bring","bring"]','bring',0.2,'phonics',NULL),
(107,'Choose the correct word: "The leaves _______ in Autumn."','multiple_choice','["fall","feel"]','fall',0.3,'phonics',NULL),
(107,'"Can you _______ a straight line?"','multiple_choice','["draw","drow"]','draw',0.3,'phonics',NULL),
(107,'"Help me _______ these groceries."','multiple_choice','["carry","curry"]','carry',0.3,'phonics',NULL);

-- Node 108: Descriptive Sight Words
INSERT INTO QuizQuestions (NodeID,QuestionText,QuestionType,OptionsJSON,CorrectAnswer,EstimatedDifficulty,SkillCategory,ReadingPassage) VALUES
(108,'There are _______ pupils sitting in the front row.','multiple_choice','["eight","even"]','eight',0.2,'phonics',NULL),
(108,'The sun is too _______ to play outside today.','multiple_choice','["hot","hat"]','hot',0.2,'phonics',NULL),
(108,'The stars are very _______ away in the sky.','multiple_choice','["for","far"]','far',0.2,'phonics',NULL),
(108,'My stomach is _______ after eating dinner.','multiple_choice','["fall","full"]','full',0.2,'phonics',NULL),
(108,'Turn on the _______ so we can read the book.','multiple_choice','["light","night"]','light',0.2,'phonics',NULL),
(108,'Choose the number 8 in words:','multiple_choice','["eight","height"]','eight',0.3,'phonics',NULL),
(108,'The opposite of "near" is _______.','multiple_choice','["far","for"]','far',0.3,'phonics',NULL),
(108,'The opposite of "cold" is _______.','multiple_choice','["hat","hot"]','hot',0.3,'phonics',NULL),
(108,'"The bag is _______ of toys."','multiple_choice','["full","fool"]','full',0.3,'phonics',NULL),
(108,'"The sun gives us _______."','multiple_choice','["light","lite"]','light',0.3,'phonics',NULL);

-- Node 109: Comparison Sight Words
INSERT INTO QuizQuestions (NodeID,QuestionText,QuestionType,OptionsJSON,CorrectAnswer,EstimatedDifficulty,SkillCategory,ReadingPassage) VALUES
(109,'I feel _______ today than I did yesterday.','multiple_choice','["better","best"]','better',0.3,'phonics',NULL),
(109,'This is the _______ drawing I have ever made!','multiple_choice','["better","best"]','best',0.3,'phonics',NULL),
(109,'_______ of my hands are clean.','multiple_choice','["Both","But"]','Both',0.3,'phonics',NULL),
(109,'Please keep your desk _______ and tidy.','multiple_choice','["clean","carry"]','clean',0.2,'phonics',NULL),
(109,'The ant is very _______ compared to the dog.','multiple_choice','["small","shall"]','small',0.2,'phonics',NULL),
(109,'Which word is the "top" or highest level?','multiple_choice','["better","best"]','best',0.4,'phonics',NULL),
(109,'"I like _______ chocolate and vanilla."','multiple_choice','["Both","Birth"]','Both',0.3,'phonics',NULL),
(109,'"My shirt is _______."','multiple_choice','["clean","clen"]','clean',0.2,'phonics',NULL),
(109,'The opposite of "big" is _______.','multiple_choice','["small","smell"]','small',0.3,'phonics',NULL),
(109,'"This apple tastes _______ than that one."','multiple_choice','["better","bitter"]','better',0.4,'phonics',NULL);

-- Node 110: Complex Sight Words Q4
INSERT INTO QuizQuestions (NodeID,QuestionText,QuestionType,OptionsJSON,CorrectAnswer,EstimatedDifficulty,SkillCategory,ReadingPassage) VALUES
(110,'We walked _______ the dark tunnel to get to the other side.','multiple_choice','["though","through"]','through',0.5,'phonics',NULL),
(110,'I _______ it would rain, so I brought my umbrella.','multiple_choice','["thought","through"]','thought',0.5,'phonics',NULL),
(110,'You _______ always tell the truth to your parents.','multiple_choice','["should","shall"]','should',0.4,'phonics',NULL),
(110,'The team won the game _______ they practiced every day.','multiple_choice','["because","before"]','because',0.3,'phonics',NULL),
(110,'When we work _______, the task becomes much easier.','multiple_choice','["together","today"]','together',0.3,'phonics',NULL),
(110,'Which word best completes: "I am tired _______ I slept late."','multiple_choice','["because","should"]','because',0.4,'phonics',NULL),
(110,'Identify the correctly spelled word for "past thinking":','multiple_choice','["thought","thout"]','thought',0.5,'phonics',NULL),
(110,'"The river flows _______ the valley."','multiple_choice','["through","thought"]','through',0.5,'phonics',NULL),
(110,'"We _______ be kind to animals."','multiple_choice','["should","sound"]','should',0.4,'phonics',NULL),
(110,'"They went to the market _______."','multiple_choice','["together","gather"]','together',0.3,'phonics',NULL);

-- Node 111: Quantity, Time, Comparison Words
INSERT INTO QuizQuestions (NodeID,QuestionText,QuestionType,OptionsJSON,CorrectAnswer,EstimatedDifficulty,SkillCategory,ReadingPassage) VALUES
(111,'Do we have _______ rice to feed all the guests?','multiple_choice','["enough","several"]','enough',0.4,'phonics',NULL),
(111,'There are _______ ways to solve this math problem.','multiple_choice','["several","seven"]','several',0.4,'phonics',NULL),
(111,'You must lean the ladder _______ the wall.','multiple_choice','["around","against"]','against',0.5,'phonics',NULL),
(111,'This new medicine made me feel much _______.','multiple_choice','["better","best"]','better',0.4,'phonics',NULL),
(111,'It is _______ better to be early than to be late.','multiple_choice','["always","only"]','always',0.3,'phonics',NULL),
(111,'Which word means "sufficient" or "just the right amount"?','multiple_choice','["enough","much"]','enough',0.4,'phonics',NULL),
(111,'"The basketball players played _______ each other."','multiple_choice','["around","against"]','against',0.5,'phonics',NULL),
(111,'"He read _______ books during the summer break."','multiple_choice','["several","small"]','several',0.4,'phonics',NULL),
(111,'"I hope the weather gets _______ tomorrow."','multiple_choice','["better","best"]','better',0.4,'phonics',NULL),
(111,'"I _______ do my best in every school activity."','multiple_choice','["always","almost"]','always',0.3,'phonics',NULL);

-- Node 112: Abstract Sight Words and Directions
INSERT INTO QuizQuestions (NodeID,QuestionText,QuestionType,OptionsJSON,CorrectAnswer,EstimatedDifficulty,SkillCategory,ReadingPassage) VALUES
(112,'The two brothers are very _______ in their hobbies.','multiple_choice','["different","difficult"]','different',0.4,'phonics',NULL),
(112,'The cat ran _______ the street to catch the mouse.','multiple_choice','["across","around"]','across',0.4,'phonics',NULL),
(112,'Walk _______ the big tree, and then turn left.','multiple_choice','["toward","today"]','toward',0.5,'phonics',NULL),
(112,'The secret is hidden _______ the two old boxes.','multiple_choice','["between","before"]','between',0.4,'phonics',NULL),
(112,'Strong winds can _______ the seeds to far places.','multiple_choice','["carry","hurry"]','carry',0.3,'phonics',NULL),
(112,'Which word describes things that are not the same?','multiple_choice','["different","during"]','different',0.4,'phonics',NULL),
(112,'"The bridge goes _______ the wide river."','multiple_choice','["across","against"]','across',0.4,'phonics',NULL),
(112,'"The puppy is walking _______ its mother."','multiple_choice','["toward","through"]','toward',0.5,'phonics',NULL),
(112,'"There is a small space _______ the desk and the wall."','multiple_choice','["between","because"]','between',0.4,'phonics',NULL),
(112,'"Can you _______ this message to the principal?"','multiple_choice','["carry","show"]','carry',0.3,'phonics',NULL);

-- Node 113: Phonics Final Assessment (20 questions)
INSERT INTO QuizQuestions (NodeID,QuestionText,QuestionType,OptionsJSON,CorrectAnswer,EstimatedDifficulty,SkillCategory,ReadingPassage) VALUES
(113,'Which word follows the CVCC pattern?','multiple_choice','["Frog","Milk"]','Milk',0.4,'phonics',NULL),
(113,'Choose the CCVC word that starts with a consonant blend:','multiple_choice','["Stop","Step"]','Stop',0.4,'phonics',NULL),
(113,'In the word "Plant", what is the final consonant blend?','multiple_choice','["Pl","nt"]','nt',0.4,'phonics',NULL),
(113,'Which word contains a diphthong (vowel team sound)?','multiple_choice','["Coin","Cone"]','Coin',0.4,'phonics',NULL),
(113,'Identify the CCVC word in this sentence: "The drum is loud."','multiple_choice','["drum","loud"]','drum',0.4,'phonics',NULL),
(113,'Where is the correct syllable division for the VCV word TIGER?','multiple_choice','["Tig-er","Ti-ger"]','Ti-ger',0.5,'phonics',NULL),
(113,'Which word follows the VCCV pattern?','multiple_choice','["Button","Bacon"]','Button',0.5,'phonics',NULL),
(113,'In a VCV pattern like "Paper", the first vowel usually sounds:','multiple_choice','["Long (says its name)","Short"]','Long (says its name)',0.4,'phonics',NULL),
(113,'Divide the VCCV word WINDOW:','multiple_choice','["Win-dow","Wind-ow"]','Win-dow',0.5,'phonics',NULL),
(113,'Which word is a VCV word used to describe music?','multiple_choice','["Solo","Supper"]','Solo',0.5,'phonics',NULL),
(113,'Which sight word tells us the total amount of something?','multiple_choice','["Full","Fall"]','Full',0.3,'phonics',NULL),
(113,'Complete the sentence: "The mountain is too ______ to climb today."','multiple_choice','["far","for"]','far',0.3,'phonics',NULL),
(113,'Complete the sentence: "I will ______ my bag to the car."','multiple_choice','["bring","drink"]','bring',0.3,'phonics',NULL),
(113,'Which word is the opposite of "worst"?','multiple_choice','["Better","Best"]','Best',0.4,'phonics',NULL),
(113,'Use the correct word: "I saw ______ ducks in the pond."','multiple_choice','["eight","height"]','eight',0.3,'phonics',NULL),
(113,'"We walked ______ the forest to find the hidden lake."','multiple_choice','["thought","through"]','through',0.5,'phonics',NULL),
(113,'Choose the word that means "all the time":','multiple_choice','["always","never"]','always',0.3,'phonics',NULL),
(113,'"The two shirts have ______ colors; one is red and one is blue."','multiple_choice','["different","between"]','different',0.4,'phonics',NULL),
(113,'Which word means "sufficient"? "We have ______ food for everyone."','multiple_choice','["several","enough"]','enough',0.4,'phonics',NULL),
(113,'Complete the direction: "The bridge goes ______ the river."','multiple_choice','["across","against"]','across',0.5,'phonics',NULL);
GO

-- ── QUIZ QUESTIONS: GRAMMAR MODULE (NodeIDs 301-313) ─────────

-- Node 301: Sentences and Non-Sentences
INSERT INTO QuizQuestions (NodeID,QuestionText,QuestionType,OptionsJSON,CorrectAnswer,EstimatedDifficulty,SkillCategory,ReadingPassage) VALUES
(301,'Which is a complete sentence?','multiple_choice','["The cat.","The cat is sleeping."]','The cat is sleeping.',0.2,'grammar',NULL),
(301,'Is "in the garden" a sentence?','multiple_choice','["Yes","No"]','No',0.2,'grammar',NULL),
(301,'Choose the sentence:','multiple_choice','["I love apples.","Running fast."]','I love apples.',0.2,'grammar',NULL),
(301,'"the sun is hot" needs a:','multiple_choice','["Capital letter","Small letter"]','Capital letter',0.2,'grammar',NULL),
(301,'A sentence ends with a:','multiple_choice','["Period","Comma"]','Period',0.2,'grammar',NULL),
(301,'"Red ball" is a:','multiple_choice','["Sentence","Non-sentence"]','Non-sentence',0.3,'grammar',NULL),
(301,'Which is a sentence?','multiple_choice','["Birds fly.","Blue sky."]','Birds fly.',0.2,'grammar',NULL),
(301,'Does a sentence make sense?','multiple_choice','["Yes","No"]','Yes',0.2,'grammar',NULL),
(301,'"Under the table" is a:','multiple_choice','["Sentence","Non-sentence"]','Non-sentence',0.3,'grammar',NULL),
(301,'"She is happy." is a:','multiple_choice','["Sentence","Non-sentence"]','Sentence',0.2,'grammar',NULL);

-- Node 302: Sequencing Words
INSERT INTO QuizQuestions (NodeID,QuestionText,QuestionType,OptionsJSON,CorrectAnswer,EstimatedDifficulty,SkillCategory,ReadingPassage) VALUES
(302,'Arrange these words into a sentence: is / Manila / big','sentence_arrange','["is","Manila","big"]','Manila is big.',0.3,'grammar',NULL),
(302,'Arrange these words: mango / The / sweet / is','sentence_arrange','["mango","The","sweet","is"]','The mango is sweet.',0.3,'grammar',NULL),
(302,'Choose the correct order:','multiple_choice','["I my town love.","I love my town."]','I love my town.',0.2,'grammar',NULL),
(302,'Fix this sentence: "plants / The / rice / farmer"','sentence_arrange','["plants","The","rice","farmer"]','The farmer plants rice.',0.4,'grammar',NULL),
(302,'Which makes sense?','multiple_choice','["We see Mayon.","Mayon we see."]','We see Mayon.',0.2,'grammar',NULL),
(302,'Arrange: colorful / festival / The / is','sentence_arrange','["colorful","festival","The","is"]','The festival is colorful.',0.3,'grammar',NULL),
(302,'Is "Red the apple is" correct word order?','multiple_choice','["Yes","No"]','No',0.2,'grammar',NULL),
(302,'Arrange: teacher / My / kind / is','sentence_arrange','["teacher","My","kind","is"]','My teacher is kind.',0.3,'grammar',NULL),
(302,'Arrange: swim / Fish / water / in','sentence_arrange','["swim","Fish","water","in"]','Fish swim in water.',0.3,'grammar',NULL),
(302,'Fix: play / We / the / park / in','sentence_arrange','["play","We","the","park","in"]','We play in the park.',0.3,'grammar',NULL);

-- Node 303: Subject and Predicate
INSERT INTO QuizQuestions (NodeID,QuestionText,QuestionType,OptionsJSON,CorrectAnswer,EstimatedDifficulty,SkillCategory,ReadingPassage) VALUES
(303,'In "The bird flies," who is the doer?','multiple_choice','["bird","flies"]','bird',0.3,'grammar',NULL),
(303,'What is the action in "Maria dances"?','multiple_choice','["Maria","dances"]','dances',0.3,'grammar',NULL),
(303,'In "The car stopped," what is the subject?','multiple_choice','["car","stopped"]','car',0.3,'grammar',NULL),
(303,'Identify the action part: "The boy eats."','multiple_choice','["boy","eats"]','eats',0.3,'grammar',NULL),
(303,'"Lito runs." Who is running?','multiple_choice','["Lito","runs"]','Lito',0.2,'grammar',NULL),
(303,'Subject of "The water is cold":','multiple_choice','["water","cold"]','water',0.3,'grammar',NULL),
(303,'Action in "Students study":','multiple_choice','["students","study"]','study',0.3,'grammar',NULL),
(303,'"My mother cooks." Who is the subject?','multiple_choice','["mother","cooks"]','mother',0.3,'grammar',NULL),
(303,'Action in "The dog barked":','multiple_choice','["dog","barked"]','barked',0.3,'grammar',NULL),
(303,'Subject of "The sun shines":','multiple_choice','["sun","shines"]','sun',0.2,'grammar',NULL);

-- Node 304: Telling and Asking Sentences
INSERT INTO QuizQuestions (NodeID,QuestionText,QuestionType,OptionsJSON,CorrectAnswer,EstimatedDifficulty,SkillCategory,ReadingPassage) VALUES
(304,'"Where is Ben" needs which end mark?','multiple_choice','[".","?"]','?',0.2,'grammar',NULL),
(304,'"I am 8 years old" is:','multiple_choice','["Telling","Asking"]','Telling',0.2,'grammar',NULL),
(304,'Which asks a question?','multiple_choice','["What is it?","It is a cat."]','What is it?',0.2,'grammar',NULL),
(304,'A telling sentence ends with:','multiple_choice','[".","/"]','.', 0.2,'grammar',NULL),
(304,'"Can you run" is:','multiple_choice','["Declarative","Interrogative"]','Interrogative',0.3,'grammar',NULL),
(304,'"The sun is hot" is:','multiple_choice','["Declarative","Interrogative"]','Declarative',0.3,'grammar',NULL),
(304,'Which needs a question mark?','multiple_choice','["Who are you","I am Sam"]','Who are you',0.3,'grammar',NULL),
(304,'Is "How are you" a telling sentence?','multiple_choice','["Yes","No"]','No',0.2,'grammar',NULL),
(304,'"It is raining" is:','multiple_choice','["Telling","Asking"]','Telling',0.2,'grammar',NULL),
(304,'Use "?" for:','multiple_choice','["Asking","Telling"]','Asking',0.2,'grammar',NULL);

-- Node 305: Commands and Exclamations
INSERT INTO QuizQuestions (NodeID,QuestionText,QuestionType,OptionsJSON,CorrectAnswer,EstimatedDifficulty,SkillCategory,ReadingPassage) VALUES
(305,'"Please sit" is a:','multiple_choice','["Request","Feeling"]','Request',0.3,'grammar',NULL),
(305,'"Wow!" ends with:','multiple_choice','["!","?"]','!',0.2,'grammar',NULL),
(305,'Which is a command?','multiple_choice','["Close the door.","The door is blue."]','Close the door.',0.3,'grammar',NULL),
(305,'"I am so happy!" is:','multiple_choice','["Exclamatory","Imperative"]','Exclamatory',0.3,'grammar',NULL),
(305,'"Ouch!" shows:','multiple_choice','["Command","Feeling"]','Feeling',0.3,'grammar',NULL),
(305,'"Clean your desk" is:','multiple_choice','["Imperative","Declarative"]','Imperative',0.3,'grammar',NULL),
(305,'Which needs "!"?','multiple_choice','["Fire","I like bread"]','Fire',0.4,'grammar',NULL),
(305,'"Kindly help me" is:','multiple_choice','["Request","Question"]','Request',0.3,'grammar',NULL),
(305,'"We won!" is:','multiple_choice','["Exclamatory","Asking"]','Exclamatory',0.3,'grammar',NULL),
(305,'Imperative sentences give:','multiple_choice','["Orders","Questions"]','Orders',0.2,'grammar',NULL);

-- Node 306: Compound Sentences
INSERT INTO QuizQuestions (NodeID,QuestionText,QuestionType,OptionsJSON,CorrectAnswer,EstimatedDifficulty,SkillCategory,ReadingPassage) VALUES
(306,'Join with: "I like milk ___ I like cake."','multiple_choice','["and","but"]','and',0.3,'grammar',NULL),
(306,'A compound sentence has ___ ideas.','multiple_choice','["one","two"]','two',0.3,'grammar',NULL),
(306,'Joining word in "The sun rose and birds sang":','multiple_choice','["sun","and"]','and',0.3,'grammar',NULL),
(306,'"I ran, but I fell." What is the joining word?','multiple_choice','["but","fell"]','but',0.3,'grammar',NULL),
(306,'Which is compound?','multiple_choice','["I like red and blue.","I like red."]','I like red and blue.',0.4,'grammar',NULL),
(306,'Use "or" for:','multiple_choice','["Choice","Adding"]','Choice',0.3,'grammar',NULL),
(306,'"Cats purr and dogs bark." Who barks?','multiple_choice','["cats","dogs"]','dogs',0.3,'grammar',NULL),
(306,'Joining word for opposite ideas:','multiple_choice','["and","but"]','but',0.3,'grammar',NULL),
(306,'"Eat now or later." What is the joining word?','multiple_choice','["or","now"]','or',0.3,'grammar',NULL),
(306,'Does a compound sentence use a joining word?','multiple_choice','["Yes","No"]','Yes',0.2,'grammar',NULL);

-- Node 307: Capitalization and Punctuation
INSERT INTO QuizQuestions (NodeID,QuestionText,QuestionType,OptionsJSON,CorrectAnswer,EstimatedDifficulty,SkillCategory,ReadingPassage) VALUES
(307,'Which is correct?','multiple_choice','["my name is anna.","My name is Anna."]','My name is Anna.',0.3,'grammar',NULL),
(307,'Which is right?','multiple_choice','["I live in Cebu.","i live in cebu."]','I live in Cebu.',0.3,'grammar',NULL),
(307,'The name "luis" in a sentence should be:','multiple_choice','["luis","Luis"]','Luis',0.3,'grammar',NULL),
(307,'Fix: "do you like milk."','multiple_choice','["do you like milk.","Do you like milk?"]','Do you like milk?',0.4,'grammar',NULL),
(307,'Which word needs a capital letter?','multiple_choice','["friday","book"]','friday',0.3,'grammar',NULL),
(307,'Start of sentence: "___ is big."','multiple_choice','["it","It"]','It',0.2,'grammar',NULL),
(307,'Is "happy birthday" correct capitalization?','multiple_choice','["Yes","No"]','No',0.4,'grammar',NULL),
(307,'"we love the philippines." - what needs a capital?','multiple_choice','["we + philippines","nothing"]','we + philippines',0.3,'grammar',NULL),
(307,'Which ends with a period?','multiple_choice','["I am eating","Who is he"]','I am eating',0.2,'grammar',NULL),
(307,'Fix: "help i am lost!"','multiple_choice','["Help i am lost!","Help! I am lost!"]','Help! I am lost!',0.4,'grammar',NULL);

-- Node 308: Time Order Discourse Markers
INSERT INTO QuizQuestions (NodeID,QuestionText,QuestionType,OptionsJSON,CorrectAnswer,EstimatedDifficulty,SkillCategory,ReadingPassage) VALUES
(308,'To start a story or process, use:','multiple_choice','["Finally","First"]','First',0.2,'grammar',NULL),
(308,'The last step word is:','multiple_choice','["Next","Finally"]','Finally',0.2,'grammar',NULL),
(308,'"___, wash your hands. Next, use soap."','multiple_choice','["First","Then"]','First',0.3,'grammar',NULL),
(308,'Which is a time order word?','multiple_choice','["Small","Then"]','Then',0.2,'grammar',NULL),
(308,'Put these in order: Finally, First, Next','sequence','["First","Next","Finally"]','First, Next, Finally',0.4,'grammar',NULL),
(308,'"Next" shows:','multiple_choice','["First step","Following step"]','Following step',0.3,'grammar',NULL),
(308,'Which shows sequence?','multiple_choice','["And","Second"]','Second',0.4,'grammar',NULL),
(308,'"I ate, ___ I slept."','multiple_choice','["Then","Before"]','Then',0.3,'grammar',NULL),
(308,'Procedure words help us follow:','multiple_choice','["Steps","Colors"]','Steps',0.2,'grammar',NULL),
(308,'After "First" comes:','multiple_choice','["Next","Last"]','Next',0.2,'grammar',NULL);

-- Node 309: Doers and Actions in Compound
INSERT INTO QuizQuestions (NodeID,QuestionText,QuestionType,OptionsJSON,CorrectAnswer,EstimatedDifficulty,SkillCategory,ReadingPassage) VALUES
(309,'"Ben eats and Lea drinks." Who are the doers?','multiple_choice','["Ben/Lea","eats/drinks"]','Ben/Lea',0.3,'grammar',NULL),
(309,'Action in 2nd part: "Birds fly but fish swim."','multiple_choice','["fly","swim"]','swim',0.3,'grammar',NULL),
(309,'Doer in 2nd part: "I ran, but he walked."','multiple_choice','["I","he"]','he',0.3,'grammar',NULL),
(309,'First action: "The sun shines and wind blows."','multiple_choice','["shines","blows"]','shines',0.3,'grammar',NULL),
(309,'"Mom cooks and Dad cleans." How many doers?','multiple_choice','["1","2"]','2',0.3,'grammar',NULL),
(309,'Who barked: "The dog barked and cat ran"?','multiple_choice','["dog","cat"]','dog',0.3,'grammar',NULL),
(309,'Action of the cat in "The dog barked and cat ran":','multiple_choice','["barked","ran"]','ran',0.3,'grammar',NULL),
(309,'Joining word: "She sang, but I danced."','multiple_choice','["but","danced"]','but',0.2,'grammar',NULL),
(309,'Are there 2 actions in a compound sentence?','multiple_choice','["Yes","No"]','Yes',0.2,'grammar',NULL),
(309,'2nd doer: "Liza wrote and Mark read."','multiple_choice','["Liza","Mark"]','Mark',0.3,'grammar',NULL);

-- Node 310-312: Grammar Q4 (national themes, because, intonation)
INSERT INTO QuizQuestions (NodeID,QuestionText,QuestionType,OptionsJSON,CorrectAnswer,EstimatedDifficulty,SkillCategory,ReadingPassage) VALUES
(310,'"The Philippines is my ____."','multiple_choice','["Country","Room","Bag"]','Country',0.2,'grammar',NULL),
(310,'"I love my flag" is what type of sentence?','multiple_choice','["Asking","Telling","Command"]','Telling',0.2,'grammar',NULL),
(310,'Which is a national theme sentence?','multiple_choice','["I have a pen.","The people are kind.","The cat is fat."]','The people are kind.',0.3,'grammar',NULL),
(310,'Subject in: "The flag has three colors."','multiple_choice','["flag","colors","three"]','flag',0.3,'grammar',NULL),
(310,'Which is in correct word order?','multiple_choice','["Flag our is beautiful.","Our flag is beautiful.","Beautiful our flag is."]','Our flag is beautiful.',0.3,'grammar',NULL),
(310,'"Respect the elders" is a:','multiple_choice','["Question","Command","Exclamation"]','Command',0.3,'grammar',NULL),
(310,'Is "Mabuhay!" an exclamatory sentence?','multiple_choice','["Yes","No"]','Yes',0.3,'grammar',NULL),
(310,'"Is the sun yellow" needs which mark?','multiple_choice','[".","!","?"]','?',0.2,'grammar',NULL),
(310,'Which is a national theme word?','multiple_choice','["Small","National","Fast"]','National',0.3,'grammar',NULL),
(310,'Subject in: "We are Filipinos."','multiple_choice','["We","Filipinos","are"]','We',0.2,'grammar',NULL);

INSERT INTO QuizQuestions (NodeID,QuestionText,QuestionType,OptionsJSON,CorrectAnswer,EstimatedDifficulty,SkillCategory,ReadingPassage) VALUES
(311,'Which word is used to show "why"?','multiple_choice','["because","first","next"]','because',0.2,'grammar',NULL),
(311,'"The grass is green" is a:','multiple_choice','["Explanation","Description","Command"]','Description',0.3,'grammar',NULL),
(311,'"I am happy ___ it is my birthday."','multiple_choice','["but","or","because"]','because',0.3,'grammar',NULL),
(311,'Which sentence explains a result?','multiple_choice','["I fell because it was wet.","I fell on the floor.","The floor is wet."]','I fell because it was wet.',0.4,'grammar',NULL),
(311,'"The sun is hot." This is a:','multiple_choice','["Description","Action","Reason"]','Description',0.3,'grammar',NULL),
(311,'"It is raining ___ there are clouds."','multiple_choice','["so","because","but"]','because',0.3,'grammar',NULL),
(311,'"It stopped ___ it had no gas."','multiple_choice','["or","and","because"]','because',0.3,'grammar',NULL),
(311,'The word "Because" shows a:','multiple_choice','["Time","Reason","Place"]','Reason',0.2,'grammar',NULL),
(311,'"The dog barked because it saw a cat." This shows:','multiple_choice','["Choice","Reason","Order"]','Reason',0.3,'grammar',NULL),
(311,'"Wash hands ___ they are dirty."','multiple_choice','["because","and","but"]','because',0.3,'grammar',NULL);

INSERT INTO QuizQuestions (NodeID,QuestionText,QuestionType,OptionsJSON,CorrectAnswer,EstimatedDifficulty,SkillCategory,ReadingPassage) VALUES
(312,'A question usually has a ____ pitch at the end.','multiple_choice','["Rising","Falling","Flat"]','Rising',0.3,'grammar',NULL),
(312,'A telling sentence has a ____ pitch at the end.','multiple_choice','["Rising","Falling","High"]','Falling',0.3,'grammar',NULL),
(312,'Does "Go!" sound the same as "Go?"','multiple_choice','["Yes","No"]','No',0.3,'grammar',NULL),
(312,'A high pitch often shows:','multiple_choice','["Sleepiness","Boredom","Excitement"]','Excitement',0.4,'grammar',NULL),
(312,'In "Who are you?", your voice goes:','multiple_choice','["Up","Down","Nowhere"]','Up',0.3,'grammar',NULL),
(312,'"I am here." (Falling pitch) is a:','multiple_choice','["Question","Statement","Command"]','Statement',0.3,'grammar',NULL),
(312,'Which word shows surprise?','multiple_choice','["The","Oh!","And"]','Oh!',0.3,'grammar',NULL),
(312,'Sentence rhythm is most like:','multiple_choice','["Music","A stone","A wall"]','Music',0.4,'grammar',NULL),
(312,'"Can I eat?" (Rising pitch) is a:','multiple_choice','["Command","Question","Statement"]','Question',0.3,'grammar',NULL),
(312,'Intonation helps us ____ each other better.','multiple_choice','["See","Hear","Understand"]','Understand',0.3,'grammar',NULL);

-- Node 313: Grammar Final Assessment
INSERT INTO QuizQuestions (NodeID,QuestionText,QuestionType,OptionsJSON,CorrectAnswer,EstimatedDifficulty,SkillCategory,ReadingPassage) VALUES
(313,'Which of these is a non-sentence?','multiple_choice','["Running fast","The boy is running.","She sits down."]','Running fast',0.3,'grammar',NULL),
(313,'Choose the correct sentence sequence:','multiple_choice','["Philippines the I love.","I love the Philippines.","Love Philippines the I."]','I love the Philippines.',0.3,'grammar',NULL),
(313,'Identify the action (predicate) in: "The cat jumps."','multiple_choice','["cat","the","jumps"]','jumps',0.3,'grammar',NULL),
(313,'What is the subject (doer) in: "The teacher is kind."','multiple_choice','["teacher","kind","is"]','teacher',0.3,'grammar',NULL),
(313,'Is "Blue the sky is" a correctly sequenced sentence?','multiple_choice','["Yes","No","Maybe"]','No',0.2,'grammar',NULL),
(313,'What type of sentence is: "Where is my bag?"','multiple_choice','["Telling (Declarative)","Asking (Interrogative)","Command (Imperative)"]','Asking (Interrogative)',0.3,'grammar',NULL),
(313,'Correct end mark for a telling sentence:','multiple_choice','[".","?",","]','.',0.2,'grammar',NULL),
(313,'Correct joining word: "I like red ___ I like blue."','multiple_choice','["and","or","but"]','and',0.3,'grammar',NULL),
(313,'What type of sentence is: "Wow, it''s huge!"','multiple_choice','["Imperative","Exclamatory","Interrogative"]','Exclamatory',0.3,'grammar',NULL),
(313,'Join: "I am hungry" and "I ate."','multiple_choice','["I am hungry and I ate.","I am hungry or I ate.","I am hungry but I ate."]','I am hungry and I ate.',0.4,'grammar',NULL),
(313,'Which shows correct capitalization?','multiple_choice','["my name is lito","My name is lito.","My name is Lito."]','My name is Lito.',0.3,'grammar',NULL),
(313,'Procedure word used for the very START of a task:','multiple_choice','["Finally","Next","First"]','First',0.2,'grammar',NULL),
(313,'Doer in 2nd part: "I cook and she cleans."','multiple_choice','["I","she","cleans"]','she',0.3,'grammar',NULL),
(313,'A declarative sentence usually ends with:','multiple_choice','"[\".\",\"?\",\"!\"]"','.',0.2,'grammar',NULL),
(313,'Sentences always start with:','multiple_choice','["Capital letter","Small letter","Number"]','Capital letter',0.2,'grammar',NULL),
(313,'Word used for explanation or giving reason:','multiple_choice','["then","because","first"]','because',0.3,'grammar',NULL),
(313,'"The sun is yellow." This is a:','multiple_choice','["Description","Explanation","Command"]','Description',0.3,'grammar',NULL),
(313,'Voice pitch for asking "Are you okay?":','multiple_choice','["Falling","Rising","Flat"]','Rising',0.3,'grammar',NULL),
(313,'Best punctuation for strong feeling "Help!":','multiple_choice','["!","?","."]','!',0.2,'grammar',NULL),
(313,'Word used for the LAST step of a process:','multiple_choice','["Next","First","Finally"]','Finally',0.2,'grammar',NULL);
GO

-- ── QUIZ QUESTIONS: COMPREHENSION MODULE (NodeIDs 401-413) ───

-- Node 401: Key Details
INSERT INTO QuizQuestions (NodeID,QuestionText,QuestionType,OptionsJSON,CorrectAnswer,EstimatedDifficulty,SkillCategory,ReadingPassage) VALUES
(401,'Who has a dog?','multiple_choice','["Ben","Ana","Mia"]','Ana',0.2,'comprehension','Ana has a small brown dog. They play in the park every afternoon.'),
(401,'What animal does Ana have?','multiple_choice','["Cat","Dog","Bird"]','Dog',0.2,'comprehension','Ana has a small brown dog. They play in the park every afternoon.'),
(401,'What color is the dog?','multiple_choice','["Brown","Black","White"]','Brown',0.2,'comprehension','Ana has a small brown dog. They play in the park every afternoon.'),
(401,'Where do they play?','multiple_choice','["School","Park","Mall"]','Park',0.2,'comprehension','Ana has a small brown dog. They play in the park every afternoon.'),
(401,'When do they play?','multiple_choice','["Morning","Afternoon","Night"]','Afternoon',0.2,'comprehension','Ana has a small brown dog. They play in the park every afternoon.'),
(401,'What size is the dog?','multiple_choice','["Big","Small","Tall"]','Small',0.2,'comprehension','Ana has a small brown dog. They play in the park every afternoon.'),
(401,'Who plays with the dog?','multiple_choice','["Teacher","Ana","Guard"]','Ana',0.2,'comprehension','Ana has a small brown dog. They play in the park every afternoon.'),
(401,'The story is mostly about:','multiple_choice','["A cat","A dog","A park"]','A dog',0.3,'comprehension','Ana has a small brown dog. They play in the park every afternoon.'),
(401,'What is Ana''s pet?','multiple_choice','["Fish","Dog","Bird"]','Dog',0.2,'comprehension','Ana has a small brown dog. They play in the park every afternoon.'),
(401,'The dog is:','multiple_choice','["Brown","Blue","Green"]','Brown',0.2,'comprehension','Ana has a small brown dog. They play in the park every afternoon.');

-- Node 402: Sequencing Events
INSERT INTO QuizQuestions (NodeID,QuestionText,QuestionType,OptionsJSON,CorrectAnswer,EstimatedDifficulty,SkillCategory,ReadingPassage) VALUES
(402,'What did Tom do first?','multiple_choice','["Ate breakfast","Woke up","Played"]','Woke up',0.2,'comprehension','First, Tom woke up. Next, he brushed his teeth. Last, he ate breakfast.'),
(402,'What did Tom do next?','multiple_choice','["Brushed teeth","Slept","Ran"]','Brushed teeth',0.2,'comprehension','First, Tom woke up. Next, he brushed his teeth. Last, he ate breakfast.'),
(402,'What did Tom do last?','multiple_choice','["Ate breakfast","Brushed teeth","Woke up"]','Ate breakfast',0.2,'comprehension','First, Tom woke up. Next, he brushed his teeth. Last, he ate breakfast.'),
(402,'Which word shows the first action?','multiple_choice','["Next","Last","First"]','First',0.2,'comprehension','First, Tom woke up. Next, he brushed his teeth. Last, he ate breakfast.'),
(402,'Which word shows the final action?','multiple_choice','["Next","Last","First"]','Last',0.2,'comprehension','First, Tom woke up. Next, he brushed his teeth. Last, he ate breakfast.'),
(402,'Tom brushed his teeth before:','multiple_choice','["Sleeping","Eating","Running"]','Eating',0.3,'comprehension','First, Tom woke up. Next, he brushed his teeth. Last, he ate breakfast.'),
(402,'Tom ate breakfast after:','multiple_choice','["Waking up","Brushing teeth","Sleeping"]','Brushing teeth',0.3,'comprehension','First, Tom woke up. Next, he brushed his teeth. Last, he ate breakfast.'),
(402,'The events are arranged in:','multiple_choice','["Order","Random","Reverse"]','Order',0.3,'comprehension','First, Tom woke up. Next, he brushed his teeth. Last, he ate breakfast.'),
(402,'What happened second?','multiple_choice','["Ate breakfast","Brushed teeth","Woke up"]','Brushed teeth',0.3,'comprehension','First, Tom woke up. Next, he brushed his teeth. Last, he ate breakfast.'),
(402,'Sequencing means putting events in:','multiple_choice','["Size","Color","Order"]','Order',0.2,'comprehension','First, Tom woke up. Next, he brushed his teeth. Last, he ate breakfast.');

-- Node 403: Characters and Setting
INSERT INTO QuizQuestions (NodeID,QuestionText,QuestionType,OptionsJSON,CorrectAnswer,EstimatedDifficulty,SkillCategory,ReadingPassage) VALUES
(403,'Who are the characters?','multiple_choice','["Liza and her brother","Beach","Sandcastle"]','Liza and her brother',0.2,'comprehension','Liza and her brother went to the beach. They built a sandcastle.'),
(403,'Where did the story happen?','multiple_choice','["Park","Beach","School"]','Beach',0.2,'comprehension','Liza and her brother went to the beach. They built a sandcastle.'),
(403,'What did they build?','multiple_choice','["House","Sandcastle","Boat"]','Sandcastle',0.2,'comprehension','Liza and her brother went to the beach. They built a sandcastle.'),
(403,'The beach is the:','multiple_choice','["Character","Setting","Problem"]','Setting',0.3,'comprehension','Liza and her brother went to the beach. They built a sandcastle.'),
(403,'Liza is a:','multiple_choice','["Place","Person","Animal"]','Person',0.2,'comprehension','Liza and her brother went to the beach. They built a sandcastle.'),
(403,'The story happens at the:','multiple_choice','["Beach","Mall","Church"]','Beach',0.2,'comprehension','Liza and her brother went to the beach. They built a sandcastle.'),
(403,'Who went with Liza?','multiple_choice','["Friend","Brother","Teacher"]','Brother',0.2,'comprehension','Liza and her brother went to the beach. They built a sandcastle.'),
(403,'The sandcastle is a:','multiple_choice','["Thing","Place","Person"]','Thing',0.3,'comprehension','Liza and her brother went to the beach. They built a sandcastle.'),
(403,'Characters are:','multiple_choice','["People in the story","Places","Problems"]','People in the story',0.2,'comprehension','Liza and her brother went to the beach. They built a sandcastle.'),
(403,'Setting tells us:','multiple_choice','["Who","Where","Why"]','Where',0.2,'comprehension','Liza and her brother went to the beach. They built a sandcastle.');

-- Node 404: Main Idea
INSERT INTO QuizQuestions (NodeID,QuestionText,QuestionType,OptionsJSON,CorrectAnswer,EstimatedDifficulty,SkillCategory,ReadingPassage) VALUES
(404,'What is the text mostly about?','multiple_choice','["Cats","Dogs","Birds"]','Dogs',0.2,'comprehension','Dogs are helpful animals. They guard houses and help people.'),
(404,'Dogs can:','multiple_choice','["Guard houses","Fly","Swim only"]','Guard houses',0.2,'comprehension','Dogs are helpful animals. They guard houses and help people.'),
(404,'The main idea is:','multiple_choice','["Dogs are helpful","Cats are cute","Birds sing"]','Dogs are helpful',0.3,'comprehension','Dogs are helpful animals. They guard houses and help people.'),
(404,'"Helpful" means:','multiple_choice','["Kind","Angry","Loud"]','Kind',0.3,'comprehension','Dogs are helpful animals. They guard houses and help people.'),
(404,'The best title for this passage is:','multiple_choice','["Helpful Dogs","Blue Sky","Fast Cars"]','Helpful Dogs',0.3,'comprehension','Dogs are helpful animals. They guard houses and help people.'),
(404,'Dogs help:','multiple_choice','["People","Cars","Chairs"]','People',0.2,'comprehension','Dogs are helpful animals. They guard houses and help people.'),
(404,'The passage is about one main:','multiple_choice','["Idea","Color","Number"]','Idea',0.2,'comprehension','Dogs are helpful animals. They guard houses and help people.'),
(404,'"Guard" means:','multiple_choice','["Protect","Run","Sleep"]','Protect',0.3,'comprehension','Dogs are helpful animals. They guard houses and help people.'),
(404,'Supporting details give more information about the:','multiple_choice','["Title","Main idea","Pictures"]','Main idea',0.3,'comprehension','Dogs are helpful animals. They guard houses and help people.'),
(404,'Main idea means:','multiple_choice','["Small detail","Big idea","Question"]','Big idea',0.2,'comprehension','Dogs are helpful animals. They guard houses and help people.');

-- Node 405: Cause and Effect
INSERT INTO QuizQuestions (NodeID,QuestionText,QuestionType,OptionsJSON,CorrectAnswer,EstimatedDifficulty,SkillCategory,ReadingPassage) VALUES
(405,'Why did the streets get wet?','multiple_choice','["It rained","It was sunny","It snowed"]','It rained',0.2,'comprehension','It rained hard. The streets became wet.'),
(405,'What happened because it rained?','multiple_choice','["Wet streets","Dry ground","Hot weather"]','Wet streets',0.2,'comprehension','It rained hard. The streets became wet.'),
(405,'Rain is the:','multiple_choice','["Effect","Cause","Detail"]','Cause',0.3,'comprehension','It rained hard. The streets became wet.'),
(405,'Wet streets are the:','multiple_choice','["Cause","Effect","Title"]','Effect',0.3,'comprehension','It rained hard. The streets became wet.'),
(405,'Cause means:','multiple_choice','["Why","Where","Who"]','Why',0.2,'comprehension','It rained hard. The streets became wet.'),
(405,'Effect means:','multiple_choice','["Result","Question","Person"]','Result',0.2,'comprehension','It rained hard. The streets became wet.'),
(405,'If it rains, we use:','multiple_choice','["Umbrella","Fan","Heater"]','Umbrella',0.3,'comprehension','It rained hard. The streets became wet.'),
(405,'Wet streets happen after:','multiple_choice','["Rain","Sun","Wind"]','Rain',0.2,'comprehension','It rained hard. The streets became wet.'),
(405,'Hard rain causes:','multiple_choice','["Wet streets","Fire","Snow"]','Wet streets',0.2,'comprehension','It rained hard. The streets became wet.'),
(405,'The passage shows:','multiple_choice','["Problem","Cause and effect","Setting"]','Cause and effect',0.3,'comprehension','It rained hard. The streets became wet.');

-- Node 407: Problem and Solution
INSERT INTO QuizQuestions (NodeID,QuestionText,QuestionType,OptionsJSON,CorrectAnswer,EstimatedDifficulty,SkillCategory,ReadingPassage) VALUES
(407,'Where did Ben bring his kite?','multiple_choice','["School","Park","Beach"]','Park',0.2,'comprehension','Ben brought his kite to the park. The wind was very strong. The kite got stuck in a tree. Ben felt sad. His father used a long stick to get the kite down. Ben smiled again.'),
(407,'The wind was:','multiple_choice','["Calm","Strong","Warm"]','Strong',0.2,'comprehension','Ben brought his kite to the park. The wind was very strong. The kite got stuck in a tree. Ben felt sad. His father used a long stick to get the kite down. Ben smiled again.'),
(407,'Why did Ben feel sad?','multiple_choice','["He lost his toy","The kite got stuck","He went home"]','The kite got stuck',0.3,'comprehension','Ben brought his kite to the park. The wind was very strong. The kite got stuck in a tree. Ben felt sad. His father used a long stick to get the kite down. Ben smiled again.'),
(407,'Who helped Ben?','multiple_choice','["His friend","His father","His teacher"]','His father',0.2,'comprehension','Ben brought his kite to the park. The wind was very strong. The kite got stuck in a tree. Ben felt sad. His father used a long stick to get the kite down. Ben smiled again.'),
(407,'How did they solve the problem?','multiple_choice','["Climbed the tree","Used a long stick","Bought a new kite"]','Used a long stick',0.3,'comprehension','Ben brought his kite to the park. The wind was very strong. The kite got stuck in a tree. Ben felt sad. His father used a long stick to get the kite down. Ben smiled again.'),
(407,'What happened after the kite was removed?','multiple_choice','["Ben cried","Ben smiled","Ben ran away"]','Ben smiled',0.2,'comprehension','Ben brought his kite to the park. The wind was very strong. The kite got stuck in a tree. Ben felt sad. His father used a long stick to get the kite down. Ben smiled again.'),
(407,'The strong wind caused the kite to:','multiple_choice','["Fly low","Tear","Get stuck"]','Get stuck',0.3,'comprehension','Ben brought his kite to the park. The wind was very strong. The kite got stuck in a tree. Ben felt sad. His father used a long stick to get the kite down. Ben smiled again.'),
(407,'The solution in the story is:','multiple_choice','["Going home","Using a stick to get the kite","Buying food"]','Using a stick to get the kite',0.3,'comprehension','Ben brought his kite to the park. The wind was very strong. The kite got stuck in a tree. Ben felt sad. His father used a long stick to get the kite down. Ben smiled again.'),
(407,'What is the main problem?','multiple_choice','["Ben forgot the kite","The kite got stuck","The park was closed"]','The kite got stuck',0.3,'comprehension','Ben brought his kite to the park. The wind was very strong. The kite got stuck in a tree. Ben felt sad. His father used a long stick to get the kite down. Ben smiled again.'),
(407,'The story teaches that problems can be:','multiple_choice','["Ignored","Solved with help","Forgotten"]','Solved with help',0.3,'comprehension','Ben brought his kite to the park. The wind was very strong. The kite got stuck in a tree. Ben felt sad. His father used a long stick to get the kite down. Ben smiled again.');

-- Node 408: Comparing Characters
INSERT INTO QuizQuestions (NodeID,QuestionText,QuestionType,OptionsJSON,CorrectAnswer,EstimatedDifficulty,SkillCategory,ReadingPassage) VALUES
(408,'Who likes reading books?','multiple_choice','["Ana","Mia","Both"]','Ana',0.2,'comprehension','Ana likes to read books quietly in her room. Mia likes to play outside with her friends. Both girls enjoy learning new things.'),
(408,'Who likes playing outside?','multiple_choice','["Ana","Mia","Both"]','Mia',0.2,'comprehension','Ana likes to read books quietly in her room. Mia likes to play outside with her friends. Both girls enjoy learning new things.'),
(408,'Where does Ana read?','multiple_choice','["Park","Room","Library"]','Room',0.2,'comprehension','Ana likes to read books quietly in her room. Mia likes to play outside with her friends. Both girls enjoy learning new things.'),
(408,'What do both girls enjoy?','multiple_choice','["Sleeping","Learning new things","Cooking"]','Learning new things',0.3,'comprehension','Ana likes to read books quietly in her room. Mia likes to play outside with her friends. Both girls enjoy learning new things.'),
(408,'Ana is quiet while:','multiple_choice','["Playing","Reading","Running"]','Reading',0.2,'comprehension','Ana likes to read books quietly in her room. Mia likes to play outside with her friends. Both girls enjoy learning new things.'),
(408,'Mia prefers to:','multiple_choice','["Stay inside","Read quietly","Play outside"]','Play outside',0.2,'comprehension','Ana likes to read books quietly in her room. Mia likes to play outside with her friends. Both girls enjoy learning new things.'),
(408,'One similarity between Ana and Mia:','multiple_choice','["Both read books","Both enjoy learning","Both play outside"]','Both enjoy learning',0.3,'comprehension','Ana likes to read books quietly in her room. Mia likes to play outside with her friends. Both girls enjoy learning new things.'),
(408,'One difference between Ana and Mia:','multiple_choice','["Both are girls","One reads, one plays","Both are friends"]','One reads, one plays',0.3,'comprehension','Ana likes to read books quietly in her room. Mia likes to play outside with her friends. Both girls enjoy learning new things.'),
(408,'The word "both" means:','multiple_choice','["Only one","The two","None"]','The two',0.2,'comprehension','Ana likes to read books quietly in her room. Mia likes to play outside with her friends. Both girls enjoy learning new things.'),
(408,'Ana and Mia are alike because they:','multiple_choice','["Like noise","Enjoy learning","Stay home"]','Enjoy learning',0.3,'comprehension','Ana likes to read books quietly in her room. Mia likes to play outside with her friends. Both girls enjoy learning new things.');

-- Node 409: Context Clues
INSERT INTO QuizQuestions (NodeID,QuestionText,QuestionType,OptionsJSON,CorrectAnswer,EstimatedDifficulty,SkillCategory,ReadingPassage) VALUES
(409,'The rabbit is:','multiple_choice','["Slow","Quick","Heavy"]','Quick',0.2,'comprehension','The rabbit is quick. It runs very fast. The turtle is slow. It moves very slowly.'),
(409,'"Quick" means:','multiple_choice','["Fast","Small","Loud"]','Fast',0.2,'comprehension','The rabbit is quick. It runs very fast. The turtle is slow. It moves very slowly.'),
(409,'The turtle is:','multiple_choice','["Fast","Slow","Tall"]','Slow',0.2,'comprehension','The rabbit is quick. It runs very fast. The turtle is slow. It moves very slowly.'),
(409,'"Slow" means:','multiple_choice','["Fast","Not fast","Big"]','Not fast',0.3,'comprehension','The rabbit is quick. It runs very fast. The turtle is slow. It moves very slowly.'),
(409,'Quick and fast are:','multiple_choice','["Antonyms","Synonyms","Opposites"]','Synonyms',0.3,'comprehension','The rabbit is quick. It runs very fast. The turtle is slow. It moves very slowly.'),
(409,'Slow and fast are:','multiple_choice','["Synonyms","Antonyms","Same"]','Antonyms',0.3,'comprehension','The rabbit is quick. It runs very fast. The turtle is slow. It moves very slowly.'),
(409,'The rabbit runs:','multiple_choice','["Slowly","Quickly","Sadly"]','Quickly',0.2,'comprehension','The rabbit is quick. It runs very fast. The turtle is slow. It moves very slowly.'),
(409,'The turtle moves:','multiple_choice','["Fast","Quickly","Slowly"]','Slowly',0.2,'comprehension','The rabbit is quick. It runs very fast. The turtle is slow. It moves very slowly.'),
(409,'The opposite of quick is:','multiple_choice','["Fast","Slow","Big"]','Slow',0.2,'comprehension','The rabbit is quick. It runs very fast. The turtle is slow. It moves very slowly.'),
(409,'Context clues help us:','multiple_choice','["Guess meaning","Skip reading","Draw pictures"]','Guess meaning',0.3,'comprehension','The rabbit is quick. It runs very fast. The turtle is slow. It moves very slowly.');

-- Node 410: Drawing Conclusions
INSERT INTO QuizQuestions (NodeID,QuestionText,QuestionType,OptionsJSON,CorrectAnswer,EstimatedDifficulty,SkillCategory,ReadingPassage) VALUES
(410,'What filled the sky?','multiple_choice','["Stars","Clouds","Birds"]','Clouds',0.2,'comprehension','Dark clouds filled the sky. The wind blew hard. Carlo grabbed his umbrella before leaving the house.'),
(410,'The wind was:','multiple_choice','["Calm","Hard","Warm"]','Hard',0.2,'comprehension','Dark clouds filled the sky. The wind blew hard. Carlo grabbed his umbrella before leaving the house.'),
(410,'Why did Carlo grab an umbrella?','multiple_choice','["It might rain","It is sunny","It is cold"]','It might rain',0.3,'comprehension','Dark clouds filled the sky. The wind blew hard. Carlo grabbed his umbrella before leaving the house.'),
(410,'Dark clouds tell us it may:','multiple_choice','["Snow","Rain","Shine"]','Rain',0.3,'comprehension','Dark clouds filled the sky. The wind blew hard. Carlo grabbed his umbrella before leaving the house.'),
(410,'The clue that shows rain is coming:','multiple_choice','["Stars","Strong wind","Dark clouds"]','Dark clouds',0.3,'comprehension','Dark clouds filled the sky. The wind blew hard. Carlo grabbed his umbrella before leaving the house.'),
(410,'Carlo felt:','multiple_choice','["Prepared","Sleepy","Angry"]','Prepared',0.3,'comprehension','Dark clouds filled the sky. The wind blew hard. Carlo grabbed his umbrella before leaving the house.'),
(410,'The weather is likely:','multiple_choice','["Rainy","Sunny","Dry"]','Rainy',0.3,'comprehension','Dark clouds filled the sky. The wind blew hard. Carlo grabbed his umbrella before leaving the house.'),
(410,'An inference is:','multiple_choice','["A guess using clues","A fact only","A drawing"]','A guess using clues',0.3,'comprehension','Dark clouds filled the sky. The wind blew hard. Carlo grabbed his umbrella before leaving the house.'),
(410,'The best conclusion is:','multiple_choice','["It will be hot","It may rain","It is night"]','It may rain',0.3,'comprehension','Dark clouds filled the sky. The wind blew hard. Carlo grabbed his umbrella before leaving the house.'),
(410,'What helped Carlo make a good decision?','multiple_choice','["The story","The clues in the sky","His teacher"]','The clues in the sky',0.4,'comprehension','Dark clouds filled the sky. The wind blew hard. Carlo grabbed his umbrella before leaving the house.');

-- Node 412: Summarizing
INSERT INTO QuizQuestions (NodeID,QuestionText,QuestionType,OptionsJSON,CorrectAnswer,EstimatedDifficulty,SkillCategory,ReadingPassage) VALUES
(412,'What is the text mostly about?','multiple_choice','["Animals","Plants","Cars"]','Plants',0.2,'comprehension','Plants need sunlight, water, and soil to grow and stay healthy.'),
(412,'Plants need sunlight to:','multiple_choice','["Sleep","Make food","Jump"]','Make food',0.3,'comprehension','Plants need sunlight, water, and soil to grow and stay healthy.'),
(412,'Without water, plants:','multiple_choice','["Grow taller","Cannot live","Become blue"]','Cannot live',0.3,'comprehension','Plants need sunlight, water, and soil to grow and stay healthy.'),
(412,'Healthy soil helps plants:','multiple_choice','["Grow strong","Fly","Swim"]','Grow strong',0.3,'comprehension','Plants need sunlight, water, and soil to grow and stay healthy.'),
(412,'Which is an important detail?','multiple_choice','["Plants are green","Plants need water","Plants are pretty"]','Plants need water',0.3,'comprehension','Plants need sunlight, water, and soil to grow and stay healthy.'),
(412,'A summary should include:','multiple_choice','["Every small detail","Main idea and key details","Only pictures"]','Main idea and key details',0.3,'comprehension','Plants need sunlight, water, and soil to grow and stay healthy.'),
(412,'Which is NOT needed by plants?','multiple_choice','["Soil","Water","Candy"]','Candy',0.2,'comprehension','Plants need sunlight, water, and soil to grow and stay healthy.'),
(412,'The main idea of the passage is:','multiple_choice','["Plants need sunlight, water, and soil to grow","Plants are colorful","Soil is brown"]','Plants need sunlight, water, and soil to grow',0.3,'comprehension','Plants need sunlight, water, and soil to grow and stay healthy.'),
(412,'Which is the best summary?','multiple_choice','["Plants need sunlight, water, and soil to grow and stay healthy.","Plants are green and small.","Water is wet."]','Plants need sunlight, water, and soil to grow and stay healthy.',0.3,'comprehension','Plants need sunlight, water, and soil to grow and stay healthy.'),
(412,'A good summary is:','multiple_choice','["Long and detailed","Short and clear","Confusing"]','Short and clear',0.3,'comprehension','Plants need sunlight, water, and soil to grow and stay healthy.');
GO

-- ── QUIZ QUESTIONS: VOCABULARY MODULE (NodeIDs 201-213) ──────

-- Node 201: High-Frequency Words
INSERT INTO QuizQuestions (NodeID,QuestionText,QuestionType,OptionsJSON,CorrectAnswer,EstimatedDifficulty,SkillCategory,ReadingPassage) VALUES
(201,'I ___ happy.','multiple_choice','["am","an","in"]','am',0.2,'vocabulary',NULL),
(201,'She ___ my friend.','multiple_choice','["is","are","am"]','is',0.2,'vocabulary',NULL),
(201,'We ___ going to school.','multiple_choice','["are","is","am"]','are',0.2,'vocabulary',NULL),
(201,'I have ___ apple.','multiple_choice','["a","an","the"]','an',0.3,'vocabulary',NULL),
(201,'___ is my bag.','multiple_choice','["This","These","Those"]','This',0.2,'vocabulary',NULL),
(201,'Choose the correct spelling:','multiple_choice','["because","becos","becuse"]','because',0.3,'vocabulary',NULL),
(201,'The cat is ___ the table.','multiple_choice','["on","in","at"]','on',0.2,'vocabulary',NULL),
(201,'Choose the correct word:','multiple_choice','["their","thier","ther"]','their',0.3,'vocabulary',NULL),
(201,'I ___ a pencil.','multiple_choice','["have","has","had"]','have',0.2,'vocabulary',NULL),
(201,'She ___ running fast.','multiple_choice','["is","are","am"]','is',0.2,'vocabulary',NULL);

-- Node 202: Regional and National Theme Vocabulary
INSERT INTO QuizQuestions (NodeID,QuestionText,QuestionType,OptionsJSON,CorrectAnswer,EstimatedDifficulty,SkillCategory,ReadingPassage) VALUES
(202,'A barangay is a:','multiple_choice','["small community","big country","animal"]','small community',0.2,'vocabulary',NULL),
(202,'A fiesta is a:','multiple_choice','["celebration","homework","storm"]','celebration',0.2,'vocabulary',NULL),
(202,'The palengke is a:','multiple_choice','["market","classroom","playground"]','market',0.2,'vocabulary',NULL),
(202,'A jeepney is used for:','multiple_choice','["transportation","cooking","planting"]','transportation',0.2,'vocabulary',NULL),
(202,'A farmer works in the:','multiple_choice','["rice field","hospital","mall"]','rice field',0.2,'vocabulary',NULL),
(202,'In Math, to add means:','multiple_choice','["put together","take away","cut"]','put together',0.2,'vocabulary',NULL),
(202,'In Math, the sum is the:','multiple_choice','["answer to addition","answer to subtraction","shape"]','answer to addition',0.3,'vocabulary',NULL),
(202,'Plants need water to:','multiple_choice','["grow","jump","sing"]','grow',0.2,'vocabulary',NULL),
(202,'The sun gives us:','multiple_choice','["light","shoes","books"]','light',0.2,'vocabulary',NULL),
(202,'An animal is a:','multiple_choice','["living thing","number","building"]','living thing',0.2,'vocabulary',NULL);

-- Node 204: Common and Proper Nouns
INSERT INTO QuizQuestions (NodeID,QuestionText,QuestionType,OptionsJSON,CorrectAnswer,EstimatedDifficulty,SkillCategory,ReadingPassage) VALUES
(204,'Which is a proper noun?','multiple_choice','["school","Manila","cat","boy"]','Manila',0.3,'vocabulary',NULL),
(204,'Which is a common noun?','multiple_choice','["Juan","Christmas","dog","May"]','dog',0.3,'vocabulary',NULL),
(204,'Choose the proper noun:','multiple_choice','["park","Rizal Park","tree","river"]','Rizal Park',0.3,'vocabulary',NULL),
(204,'Which is a common noun?','multiple_choice','["Maria","teacher","Monday","Ateneo"]','teacher',0.3,'vocabulary',NULL),
(204,'Choose the proper noun:','multiple_choice','["town","Cebu City","street","market"]','Cebu City',0.3,'vocabulary',NULL),
(204,'Which is a masculine noun?','multiple_choice','["mother","uncle","aunt","sister"]','uncle',0.3,'vocabulary',NULL),
(204,'Which is a feminine noun?','multiple_choice','["father","brother","grandmother","boy"]','grandmother',0.3,'vocabulary',NULL),
(204,'Choose the masculine noun:','multiple_choice','["king","queen","princess","mother"]','king',0.3,'vocabulary',NULL),
(204,'Which is a feminine noun?','multiple_choice','["hero","mother","man","boy"]','mother',0.3,'vocabulary',NULL),
(204,'Choose the neutral noun (used for any gender):','multiple_choice','["student","uncle","grandmother","father"]','student',0.4,'vocabulary',NULL);

-- Node 205: Verbs
INSERT INTO QuizQuestions (NodeID,QuestionText,QuestionType,OptionsJSON,CorrectAnswer,EstimatedDifficulty,SkillCategory,ReadingPassage) VALUES
(205,'The boy ___ fast.','multiple_choice','["run","runs","ran","running"]','runs',0.2,'vocabulary',NULL),
(205,'Maria ___ a book.','multiple_choice','["read","reads","reading","reader"]','reads',0.2,'vocabulary',NULL),
(205,'The dog ___ in the yard.','multiple_choice','["play","plays","played","playing"]','plays',0.2,'vocabulary',NULL),
(205,'We ___ our hands before eating.','multiple_choice','["wash","washes","washing","washed"]','wash',0.3,'vocabulary',NULL),
(205,'The teacher ___ the lesson clearly.','multiple_choice','["explain","explains","explained","explaining"]','explains',0.3,'vocabulary',NULL),
(205,'The cat ___ on the roof.','multiple_choice','["jump","jumps","jumped","jumping"]','jumps',0.2,'vocabulary',NULL),
(205,'I ___ my homework every day.','multiple_choice','["do","does","did","doing"]','do',0.3,'vocabulary',NULL),
(205,'They ___ football in the park.','multiple_choice','["play","plays","played","playing"]','play',0.2,'vocabulary',NULL),
(205,'She ___ water from the bottle.','multiple_choice','["drink","drinks","drank","drinking"]','drinks',0.2,'vocabulary',NULL),
(205,'The baby ___ loudly when hungry.','multiple_choice','["cry","cries","cried","crying"]','cries',0.3,'vocabulary',NULL);

-- Node 206: Adjectives
INSERT INTO QuizQuestions (NodeID,QuestionText,QuestionType,OptionsJSON,CorrectAnswer,EstimatedDifficulty,SkillCategory,ReadingPassage) VALUES
(206,'The kitten is very ___ and soft.','multiple_choice','["small","run","eat","jump"]','small',0.2,'vocabulary',NULL),
(206,'Maria wore a ___ dress to the party.','multiple_choice','["red","run","dog","play"]','red',0.2,'vocabulary',NULL),
(206,'The boy felt ___ after winning the game.','multiple_choice','["happy","cry","eat","tall"]','happy',0.2,'vocabulary',NULL),
(206,'This bag is ___ than mine.','multiple_choice','["big","dog","run","play"]','big',0.2,'vocabulary',NULL),
(206,'The sky is ___ today with no clouds.','multiple_choice','["blue","sing","tall","fast"]','blue',0.2,'vocabulary',NULL),
(206,'The storybook is very ___.','multiple_choice','["exciting","dog","eat","run"]','exciting',0.3,'vocabulary',NULL),
(206,'The flowers in the garden are ___ and colorful.','multiple_choice','["beautiful","cat","jump","small"]','beautiful',0.2,'vocabulary',NULL),
(206,'He is ___ because he lost his toy.','multiple_choice','["sad","happy","run","tall"]','sad',0.2,'vocabulary',NULL),
(206,'There are ___ apples on the table.','multiple_choice','["many","red","big","sad"]','many',0.3,'vocabulary',NULL),
(206,'The cube is ___ in shape.','multiple_choice','["square","tall","dog","jump"]','square',0.3,'vocabulary',NULL);

-- Node 207: Synonyms and Antonyms
INSERT INTO QuizQuestions (NodeID,QuestionText,QuestionType,OptionsJSON,CorrectAnswer,EstimatedDifficulty,SkillCategory,ReadingPassage) VALUES
(207,'Which word is a verb?','multiple_choice','["jump","blue","chair"]','jump',0.2,'vocabulary',NULL),
(207,'Which word is an adjective?','multiple_choice','["run","tall","swim"]','tall',0.2,'vocabulary',NULL),
(207,'Which is a demonstrative pronoun?','multiple_choice','["this","happy","walk"]','this',0.2,'vocabulary',NULL),
(207,'Which word is a synonym of big?','multiple_choice','["large","tiny","short"]','large',0.3,'vocabulary',NULL),
(207,'Which word is an antonym of hot?','multiple_choice','["warm","cold","sunny"]','cold',0.3,'vocabulary',NULL),
(207,'Choose the verb:','multiple_choice','["dance","pretty","long"]','dance',0.2,'vocabulary',NULL),
(207,'Choose the adjective:','multiple_choice','["jump","sing","beautiful"]','beautiful',0.2,'vocabulary',NULL),
(207,'"___ are my shoes." (far away, more than one)','multiple_choice','["Those","Jump","Happy"]','Those',0.2,'vocabulary',NULL),
(207,'Which word means the same as fast?','multiple_choice','["quick","slow","weak"]','quick',0.3,'vocabulary',NULL),
(207,'Which word means the opposite of tall?','multiple_choice','["big","high","short"]','short',0.3,'vocabulary',NULL);

-- Node 210: Word Families
INSERT INTO QuizQuestions (NodeID,QuestionText,QuestionType,OptionsJSON,CorrectAnswer,EstimatedDifficulty,SkillCategory,ReadingPassage) VALUES
(210,'Which word belongs to the -at family?','multiple_choice','["cat","cut","cot"]','cat',0.2,'vocabulary',NULL),
(210,'Which word rhymes with cake?','multiple_choice','["bike","bake","book"]','bake',0.2,'vocabulary',NULL),
(210,'Which word has the same pattern as light?','multiple_choice','["night","late","let"]','night',0.2,'vocabulary',NULL),
(210,'Which word belongs to the -an family?','multiple_choice','["man","men","moon"]','man',0.2,'vocabulary',NULL),
(210,'Which word rhymes with ball?','multiple_choice','["tall","tell","tool"]','tall',0.2,'vocabulary',NULL),
(210,'Which word has the -ake pattern?','multiple_choice','["lake","look","lock"]','lake',0.2,'vocabulary',NULL),
(210,'Which word rhymes with sun?','multiple_choice','["run","ran","sin"]','run',0.2,'vocabulary',NULL),
(210,'Which word belongs to the -ig family?','multiple_choice','["pig","peg","pug"]','pig',0.2,'vocabulary',NULL),
(210,'Which word has the same ending sound as book?','multiple_choice','["cook","cake","coat"]','cook',0.3,'vocabulary',NULL),
(210,'Which word belongs to the -ap family?','multiple_choice','["map","mop","mob"]','map',0.2,'vocabulary',NULL);

-- Node 211: Spelling
INSERT INTO QuizQuestions (NodeID,QuestionText,QuestionType,OptionsJSON,CorrectAnswer,EstimatedDifficulty,SkillCategory,ReadingPassage) VALUES
(211,'Choose the correctly spelled word:','multiple_choice','["becaus","because","becose"]','because',0.3,'vocabulary',NULL),
(211,'Choose the correctly spelled word:','multiple_choice','["nite","night","nyght"]','night',0.3,'vocabulary',NULL),
(211,'Choose the correctly spelled word:','multiple_choice','["lites","lights","lightes"]','lights',0.3,'vocabulary',NULL),
(211,'Choose the correctly spelled word:','multiple_choice','["playd","plaed","played"]','played',0.3,'vocabulary',NULL),
(211,'Choose the correctly spelled word:','multiple_choice','["helpfull","helpful","helpfol"]','helpful',0.3,'vocabulary',NULL),
(211,'Choose the correctly spelled word:','multiple_choice','["happi","happy","hapy"]','happy',0.2,'vocabulary',NULL),
(211,'Choose the correctly spelled word:','multiple_choice','["jumpd","jumped","jumpt"]','jumped',0.3,'vocabulary',NULL),
(211,'Choose the correctly spelled word:','multiple_choice','["littel","little","litle"]','little',0.2,'vocabulary',NULL),
(211,'Choose the correctly spelled word:','multiple_choice','["runing","running","runnung"]','running',0.3,'vocabulary',NULL),
(211,'Choose the correctly spelled word:','multiple_choice','["studdy","study","stody"]','study',0.2,'vocabulary',NULL);

-- Node 212: Root Words
INSERT INTO QuizQuestions (NodeID,QuestionText,QuestionType,OptionsJSON,CorrectAnswer,EstimatedDifficulty,SkillCategory,ReadingPassage) VALUES
(212,'What is the root word of "playing"?','multiple_choice','["play","playing","played"]','play',0.2,'vocabulary',NULL),
(212,'What is the root word of "teacher"?','multiple_choice','["teach","teacher","teaches"]','teach',0.3,'vocabulary',NULL),
(212,'What is the root word of "helpful"?','multiple_choice','["helping","help","helpful"]','help',0.3,'vocabulary',NULL),
(212,'What is the root word of "jumped"?','multiple_choice','["jumping","jump","jumps"]','jump',0.2,'vocabulary',NULL),
(212,'What is the root word of "reader"?','multiple_choice','["read","reader","reading"]','read',0.3,'vocabulary',NULL),
(212,'What is the root word of "happily"?','multiple_choice','["happy","happily","happiness"]','happy',0.3,'vocabulary',NULL),
(212,'What is the root word of "fastest"?','multiple_choice','["fast","faster","fastest"]','fast',0.3,'vocabulary',NULL),
(212,'What is the root word of "painted"?','multiple_choice','["painting","paint","painter"]','paint',0.2,'vocabulary',NULL),
(212,'What is the root word of "singer"?','multiple_choice','["sing","singing","singer"]','sing',0.3,'vocabulary',NULL),
(212,'What is the root word of "kindness"?','multiple_choice','["kind","kindly","kindness"]','kind',0.3,'vocabulary',NULL);
GO

-- ── LESSON GAME CONTENT (LessonGameContent table) ────────────
-- SKIPPED: LessonGameContent.LessonID is a FK to dbo.Lessons (not Nodes).
-- Insert valid Lessons first, then re-enable these inserts with correct LessonIDs.
/*
INSERT INTO LessonGameContent (LessonID, GameType, ContentText, ContentData, Difficulty, Category) VALUES
(101,'fill_in_blanks','The cat is _____ on the mat.',
 '{"beforeBlank":"The cat is","afterBlank":"on the mat.","correctAnswer":"sitting","options":["sitting","running","flying","sleeping"]}',0.3,'phonics'),
(101,'fill_in_blanks','The dog _____ in the park.',
 '{"beforeBlank":"The dog","afterBlank":"in the park.","correctAnswer":"plays","options":["plays","flies","swims","reads"]}',0.3,'phonics'),
(101,'fill_in_blanks','I _____ see the moon.',
 '{"beforeBlank":"I","afterBlank":"see the moon.","correctAnswer":"could","options":["could","cold","can''t","never"]}',0.3,'phonics'),
(101,'fill_in_blanks','Please turn on the _____.',
 '{"beforeBlank":"Please turn on the","afterBlank":".","correctAnswer":"light","options":["light","right","night","bright"]}',0.3,'phonics'),
(101,'fill_in_blanks','You should _____ wash your hands.',
 '{"beforeBlank":"You should","afterBlank":"wash your hands.","correctAnswer":"always","options":["always","away","also","after"]}',0.3,'phonics'),
-- Grammar: Fill-in-blanks content
(301,'fill_in_blanks','The _____ is sleeping.',
 '{"beforeBlank":"The","afterBlank":"is sleeping.","correctAnswer":"cat","options":["cat","run","blue","fast"]}',0.3,'grammar'),
(301,'fill_in_blanks','Birds _____ in the sky.',
 '{"beforeBlank":"Birds","afterBlank":"in the sky.","correctAnswer":"fly","options":["fly","big","happy","slowly"]}',0.3,'grammar'),
(304,'fill_in_blanks','I am 8 years _____.',
 '{"beforeBlank":"I am 8 years","afterBlank":".","correctAnswer":"old","options":["old","new","big","tall"]}',0.3,'grammar'),
(306,'fill_in_blanks','I like cats _____ I like dogs.',
 '{"beforeBlank":"I like cats","afterBlank":"I like dogs.","correctAnswer":"and","options":["and","but","or","so"]}',0.5,'grammar'),
(311,'fill_in_blanks','I wear a jacket _____ it is cold.',
 '{"beforeBlank":"I wear a jacket","afterBlank":"it is cold.","correctAnswer":"because","options":["because","but","and","when"]}',0.5,'grammar'),
-- Vocabulary: Fill-in-blanks content
(201,'fill_in_blanks','She _____ my friend.',
 '{"beforeBlank":"She","afterBlank":"my friend.","correctAnswer":"is","options":["is","are","am","be"]}',0.3,'vocabulary'),
(205,'fill_in_blanks','The boy _____ fast.',
 '{"beforeBlank":"The boy","afterBlank":"fast.","correctAnswer":"runs","options":["runs","run","ran","running"]}',0.3,'vocabulary'),
(206,'fill_in_blanks','The _____ cat slept on the mat.',
 '{"beforeBlank":"The","afterBlank":"cat slept on the mat.","correctAnswer":"small","options":["small","run","quickly","is"]}',0.3,'vocabulary'),
(207,'fill_in_blanks','The opposite of hot is _____.',
 '{"beforeBlank":"The opposite of hot is","afterBlank":".","correctAnswer":"cold","options":["cold","warm","cool","hot"]}',0.5,'vocabulary'),
(212,'fill_in_blanks','The root word of "playing" is _____.',
 '{"beforeBlank":"The root word of playing is","afterBlank":".","correctAnswer":"play","options":["play","playing","played","plays"]}',0.5,'vocabulary'),
-- Comprehension: Fill-in-blanks content
(401,'fill_in_blanks','The _____ tells us where the story happened.',
 '{"beforeBlank":"The","afterBlank":"tells us where the story happened.","correctAnswer":"setting","options":["setting","character","problem","solution"]}',0.5,'comprehension'),
(405,'fill_in_blanks','Rain is the _____, and wet streets are the _____.',
 '{"beforeBlank":"Rain is the","afterBlank":"(effect/cause).","correctAnswer":"cause","options":["cause","effect","result","ending"]}',0.5,'comprehension'),
(409,'fill_in_blanks','Quick and fast are _____ because they mean the same.',
 '{"beforeBlank":"Quick and fast are","afterBlank":"because they mean the same.","correctAnswer":"synonyms","options":["synonyms","antonyms","verbs","nouns"]}',0.5,'comprehension'),
(410,'fill_in_blanks','We use _____ from the text to make an inference.',
 '{"beforeBlank":"We use","afterBlank":"from the text to make an inference.","correctAnswer":"clues","options":["clues","colors","numbers","shapes"]}',0.5,'comprehension'),
(412,'fill_in_blanks','A good summary should include the _____ idea.',
 '{"beforeBlank":"A good summary should include the","afterBlank":"idea.","correctAnswer":"main","options":["main","small","funny","boring"]}',0.5,'comprehension');
*/

-- ── SUMMARY ──────────────────────────────────────────────────
-- This script inserts:
--   5 Modules (Phonics, Vocabulary, Grammar, Comprehension, Creating)
--  65 Nodes  (13 per module: 12 lesson nodes + 1 final assessment)
-- ~350 QuizQuestions from 4 DepEd MATATAG Grade 3 PDFs
--  20 LessonGameContent rows for fill-in-blanks game data
--
-- Curriculum Sources:
--   PDF 1: Phonics and Word Study (EN3PWS) — 12 lessons, 4 quarters
--   PDF 2: Vocabulary and Word Knowledge (EN3VWK) — 12 lessons, 4 quarters
--   PDF 3: Grammar Awareness (EN3GAGS) — 12 lessons, 4 quarters
--   PDF 4: Comprehending and Analyzing Texts — 12 lessons, 4 quarters
--   Module 5 (Creating/Composing) — constructed from MATATAG curriculum framework
--
-- Module Color Themes (for game UI):
--   Phonics      (#FF6B6B → #FF8E53) warm red-orange
--   Vocabulary   (#4ECDC4 → #44A08D) teal-green
--   Grammar      (#A770EF → #CF57A3) purple-pink
--   Comprehension(#FFD93D → #FFA93D) gold-amber
--   Creating     (#667EEA → #764BA2) blue-purple
--
-- Run on SQL Server: sqlcmd -S <server> -d LiteRiseDB -i database_seed.sql
-- =============================================================
