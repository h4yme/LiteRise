-- Seed quiz questions for 7 missing nodes (203, 208, 209, 213, 406, 411, 413)
-- 90 questions total | Run against LiteRiseDB

-- Node 203: Content-Specific Words: Math and Science
INSERT INTO QuizQuestions (NodeID, QuestionText, QuestionType, OptionsJSON, CorrectAnswer, EstimatedDifficulty, SkillCategory, ReadingPassage) VALUES
(203, N'The answer when you add two numbers is called the:', 'multiple_choice', N'["sum","difference","product"]', N'sum', 0.2, 'vocabulary', NULL),
(203, N'The answer when you subtract is called the:', 'multiple_choice', N'["product","difference","quotient"]', N'difference', 0.2, 'vocabulary', NULL),
(203, N'Multiplication gives us the:', 'multiple_choice', N'["sum","quotient","product"]', N'product', 0.3, 'vocabulary', NULL),
(203, N'Division gives us the:', 'multiple_choice', N'["quotient","sum","difference"]', N'quotient', 0.3, 'vocabulary', NULL),
(203, N'Adding numbers together is called:', 'multiple_choice', N'["subtraction","addition","division"]', N'addition', 0.2, 'vocabulary', NULL),
(203, N'Taking away a number from another is called:', 'multiple_choice', N'["addition","multiplication","subtraction"]', N'subtraction', 0.2, 'vocabulary', NULL),
(203, N'Plants grow in:', 'multiple_choice', N'["soil","air","water"]', N'soil', 0.2, 'vocabulary', NULL),
(203, N'Animals and plants are examples of:', 'multiple_choice', N'["machines","living things","numbers"]', N'living things', 0.2, 'vocabulary', NULL),
(203, N'The sun gives plants:', 'multiple_choice', N'["energy","soil","animals"]', N'energy', 0.3, 'vocabulary', NULL),
(203, N'A scientist tests ideas by doing an:', 'multiple_choice', N'["experiment","addition","noun"]', N'experiment', 0.3, 'vocabulary', NULL);

-- Node 208: Word Builders: Using Vocabulary in Sentences
INSERT INTO QuizQuestions (NodeID, QuestionText, QuestionType, OptionsJSON, CorrectAnswer, EstimatedDifficulty, SkillCategory, ReadingPassage) VALUES
(208, N'The cat ___ on the roof.', 'multiple_choice', N'["jumps","jump","jumping"]', N'jumps', 0.2, 'vocabulary', NULL),
(208, N'She has a ___ dress.', 'multiple_choice', N'["blue","jump","eat"]', N'blue', 0.2, 'vocabulary', NULL),
(208, N'___ is my pencil. (near, one)', 'multiple_choice', N'["This","Those","These"]', N'This', 0.2, 'vocabulary', NULL),
(208, N'___ are my crayons. (far, many)', 'multiple_choice', N'["Those","This","That"]', N'Those', 0.2, 'vocabulary', NULL),
(208, N'Which word is a verb?', 'multiple_choice', N'["dance","happy","chair"]', N'dance', 0.2, 'vocabulary', NULL),
(208, N'Which word is an adjective?', 'multiple_choice', N'["run","tall","book"]', N'tall', 0.2, 'vocabulary', NULL),
(208, N'Which is a synonym of glad?', 'multiple_choice', N'["happy","sad","angry"]', N'happy', 0.3, 'vocabulary', NULL),
(208, N'Which is an antonym of clean?', 'multiple_choice', N'["dirty","tidy","neat"]', N'dirty', 0.3, 'vocabulary', NULL),
(208, N'___ is my bag. (near, one)', 'multiple_choice', N'["This","These","Those"]', N'This', 0.2, 'vocabulary', NULL),
(208, N'The boy ___ very fast.', 'multiple_choice', N'["runs","running","run"]', N'runs', 0.3, 'vocabulary', NULL);

-- Node 209: Word Masters: Classify and Analyze
INSERT INTO QuizQuestions (NodeID, QuestionText, QuestionType, OptionsJSON, CorrectAnswer, EstimatedDifficulty, SkillCategory, ReadingPassage) VALUES
(209, N'Identify the verb: The bird sings loudly.', 'multiple_choice', N'["bird","sings","loudly"]', N'sings', 0.3, 'vocabulary', NULL),
(209, N'Identify the adjective: The tall tree fell.', 'multiple_choice', N'["tall","tree","fell"]', N'tall', 0.3, 'vocabulary', NULL),
(209, N'Which is the correct demonstrative pronoun? (___ are my shoes. Far away, many.)', 'multiple_choice', N'["Those","This","That"]', N'Those', 0.3, 'vocabulary', NULL),
(209, N'Happy and joyful are:', 'multiple_choice', N'["antonyms","synonyms","verbs"]', N'synonyms', 0.3, 'vocabulary', NULL),
(209, N'Hot and cold are:', 'multiple_choice', N'["synonyms","adjectives","antonyms"]', N'antonyms', 0.3, 'vocabulary', NULL),
(209, N'Identify the verb: She writes every day.', 'multiple_choice', N'["She","writes","every"]', N'writes', 0.3, 'vocabulary', NULL),
(209, N'Identify the adjective: The small kitten slept.', 'multiple_choice', N'["small","kitten","slept"]', N'small', 0.2, 'vocabulary', NULL),
(209, N'Which is a synonym of big?', 'multiple_choice', N'["tiny","large","short"]', N'large', 0.3, 'vocabulary', NULL),
(209, N'Which is an antonym of day?', 'multiple_choice', N'["light","bright","night"]', N'night', 0.3, 'vocabulary', NULL),
(209, N'Which sentence uses the correct demonstrative? (near, one thing)', 'multiple_choice', N'["This is my book.","These is my book.","Those is my book."]', N'This is my book.', 0.4, 'vocabulary', NULL);

-- Node 213: Vocabulary Final Assessment
INSERT INTO QuizQuestions (NodeID, QuestionText, QuestionType, OptionsJSON, CorrectAnswer, EstimatedDifficulty, SkillCategory, ReadingPassage) VALUES
(213, N'Choose the correct high-frequency word: I ___ go to school.', 'multiple_choice', N'["can","cat","car"]', N'can', 0.2, 'vocabulary', NULL),
(213, N'A palengke is a:', 'multiple_choice', N'["market","school","hospital"]', N'market', 0.2, 'vocabulary', NULL),
(213, N'The ___ of 8 and 2 is 10.', 'multiple_choice', N'["sum","difference","product"]', N'sum', 0.2, 'vocabulary', NULL),
(213, N'Plants need ___ to grow.', 'multiple_choice', N'["soil","books","plastic"]', N'soil', 0.2, 'vocabulary', NULL),
(213, N'Which is a proper noun?', 'multiple_choice', N'["dog","Manila","city"]', N'Manila', 0.3, 'vocabulary', NULL),
(213, N'Which is a feminine noun?', 'multiple_choice', N'["uncle","father","mother"]', N'mother', 0.3, 'vocabulary', NULL),
(213, N'The boy ___ to school.', 'multiple_choice', N'["walk","walks","walking"]', N'walks', 0.2, 'vocabulary', NULL),
(213, N'Choose the adjective: The ___ flower smells sweet.', 'multiple_choice', N'["runs","beautiful","quickly"]', N'beautiful', 0.2, 'vocabulary', NULL),
(213, N'___ are my pencils. (near, many)', 'multiple_choice', N'["These","This","Those"]', N'These', 0.3, 'vocabulary', NULL),
(213, N'Which is a synonym of fast?', 'multiple_choice', N'["slow","quick","late"]', N'quick', 0.3, 'vocabulary', NULL),
(213, N'Which is an antonym of big?', 'multiple_choice', N'["large","small","tall"]', N'small', 0.3, 'vocabulary', NULL),
(213, N'Identify the verb: She ___ every morning.', 'multiple_choice', N'["reads","happy","blue"]', N'reads', 0.3, 'vocabulary', NULL),
(213, N'Which word belongs to the -an family?', 'multiple_choice', N'["man","men","moon"]', N'man', 0.2, 'vocabulary', NULL),
(213, N'Choose the correctly spelled word:', 'multiple_choice', N'["becuse","because","becos"]', N'because', 0.3, 'vocabulary', NULL),
(213, N'Choose the correctly spelled word:', 'multiple_choice', N'["happy","happi","hapy"]', N'happy', 0.2, 'vocabulary', NULL),
(213, N'Which word has the same -ight pattern as night?', 'multiple_choice', N'["light","late","lot"]', N'light', 0.3, 'vocabulary', NULL),
(213, N'The root word of playing is:', 'multiple_choice', N'["plays","play","played"]', N'play', 0.3, 'vocabulary', NULL),
(213, N'Which word rhymes with cake?', 'multiple_choice', N'["book","bake","bike"]', N'bake', 0.2, 'vocabulary', NULL),
(213, N'The root word of kindness is:', 'multiple_choice', N'["kinds","kindly","kind"]', N'kind', 0.3, 'vocabulary', NULL),
(213, N'Which correctly completes the word family -at? c___', 'multiple_choice', N'["cot","cut","cat"]', N'cat', 0.2, 'vocabulary', NULL);

-- Node 406: Making Predictions and Asking Questions
INSERT INTO QuizQuestions (NodeID, QuestionText, QuestionType, OptionsJSON, CorrectAnswer, EstimatedDifficulty, SkillCategory, ReadingPassage) VALUES
(406, N'What did Mia see in the sky?', 'multiple_choice', N'["Dark clouds","Stars","Birds"]', N'Dark clouds', 0.2, 'comprehension', N'Mia saw dark clouds in the sky. The wind started to blow hard. She looked at her umbrella by the door.'),
(406, N'What happened to the wind?', 'multiple_choice', N'["It stopped","It blew hard","It became warm"]', N'It blew hard', 0.2, 'comprehension', N'Mia saw dark clouds in the sky. The wind started to blow hard. She looked at her umbrella by the door.'),
(406, N'Where was Mia''s umbrella?', 'multiple_choice', N'["In her bag","By the door","On the table"]', N'By the door', 0.2, 'comprehension', N'Mia saw dark clouds in the sky. The wind started to blow hard. She looked at her umbrella by the door.'),
(406, N'What do you predict will happen next?', 'multiple_choice', N'["It will rain","It will be sunny","It will snow"]', N'It will rain', 0.3, 'comprehension', N'Mia saw dark clouds in the sky. The wind started to blow hard. She looked at her umbrella by the door.'),
(406, N'What clue tells us it might rain?', 'multiple_choice', N'["Dark clouds and strong wind","Sunny sky","Birds singing"]', N'Dark clouds and strong wind', 0.3, 'comprehension', N'Mia saw dark clouds in the sky. The wind started to blow hard. She looked at her umbrella by the door.'),
(406, N'A good question to ask about this passage is:', 'multiple_choice', N'["What will Mia do with her umbrella?","What color is the table?","How many birds are there?"]', N'What will Mia do with her umbrella?', 0.3, 'comprehension', N'Mia saw dark clouds in the sky. The wind started to blow hard. She looked at her umbrella by the door.'),
(406, N'Making a prediction means:', 'multiple_choice', N'["Guessing what will happen next","Forgetting the story","Drawing pictures"]', N'Guessing what will happen next', 0.2, 'comprehension', N'Mia saw dark clouds in the sky. The wind started to blow hard. She looked at her umbrella by the door.'),
(406, N'Asking questions while reading helps you:', 'multiple_choice', N'["Understand the text better","Skip parts","Read faster"]', N'Understand the text better', 0.3, 'comprehension', N'Mia saw dark clouds in the sky. The wind started to blow hard. She looked at her umbrella by the door.'),
(406, N'The best prediction based on the clues is:', 'multiple_choice', N'["Mia will use the umbrella","Mia will go swimming","Mia will sleep"]', N'Mia will use the umbrella', 0.3, 'comprehension', N'Mia saw dark clouds in the sky. The wind started to blow hard. She looked at her umbrella by the door.'),
(406, N'We use ___ knowledge to make predictions.', 'multiple_choice', N'["prior","no","wrong"]', N'prior', 0.4, 'comprehension', N'Mia saw dark clouds in the sky. The wind started to blow hard. She looked at her umbrella by the door.');

-- Node 411: Text-to-Self Connections
INSERT INTO QuizQuestions (NodeID, QuestionText, QuestionType, OptionsJSON, CorrectAnswer, EstimatedDifficulty, SkillCategory, ReadingPassage) VALUES
(411, N'How did Lena feel on her first day?', 'multiple_choice', N'["Nervous","Happy","Angry"]', N'Nervous', 0.2, 'comprehension', N'Lena felt nervous on her first day of school. She did not know anyone. Her hands were shaking. Then a girl named Rosa smiled and said hello.'),
(411, N'Why was Lena nervous?', 'multiple_choice', N'["She did not know anyone","She lost her bag","She forgot her lunch"]', N'She did not know anyone', 0.2, 'comprehension', N'Lena felt nervous on her first day of school. She did not know anyone. Her hands were shaking. Then a girl named Rosa smiled and said hello.'),
(411, N'What did Rosa do?', 'multiple_choice', N'["Ran away","Smiled and said hello","Cried"]', N'Smiled and said hello', 0.2, 'comprehension', N'Lena felt nervous on her first day of school. She did not know anyone. Her hands were shaking. Then a girl named Rosa smiled and said hello.'),
(411, N'A text-to-self connection means:', 'multiple_choice', N'["Relating the story to your own life","Drawing a map","Counting words"]', N'Relating the story to your own life', 0.2, 'comprehension', N'Lena felt nervous on her first day of school. She did not know anyone. Her hands were shaking. Then a girl named Rosa smiled and said hello.'),
(411, N'Have you ever felt nervous like Lena? This is an example of:', 'multiple_choice', N'["Text-to-self connection","Text-to-world","Sequencing"]', N'Text-to-self connection', 0.3, 'comprehension', N'Lena felt nervous on her first day of school. She did not know anyone. Her hands were shaking. Then a girl named Rosa smiled and said hello.'),
(411, N'Which reflection starter fits best?', 'multiple_choice', N'["This reminds me of when I...","The dog ran fast...","Plants need water..."]', N'This reminds me of when I...', 0.3, 'comprehension', N'Lena felt nervous on her first day of school. She did not know anyone. Her hands were shaking. Then a girl named Rosa smiled and said hello.'),
(411, N'A text-to-world connection means:', 'multiple_choice', N'["Connecting to something in the world","Skipping pages","Drawing only"]', N'Connecting to something in the world', 0.3, 'comprehension', N'Lena felt nervous on her first day of school. She did not know anyone. Her hands were shaking. Then a girl named Rosa smiled and said hello.'),
(411, N'What helps us make a text-to-self connection?', 'multiple_choice', N'["Our own experiences","A dictionary","A map"]', N'Our own experiences', 0.3, 'comprehension', N'Lena felt nervous on her first day of school. She did not know anyone. Her hands were shaking. Then a girl named Rosa smiled and said hello.'),
(411, N'After Rosa smiled at Lena, Lena probably felt:', 'multiple_choice', N'["Better","Worse","Sleepy"]', N'Better', 0.3, 'comprehension', N'Lena felt nervous on her first day of school. She did not know anyone. Her hands were shaking. Then a girl named Rosa smiled and said hello.'),
(411, N'Reflecting on a text means:', 'multiple_choice', N'["Thinking about how it connects to you","Running fast","Counting sentences"]', N'Thinking about how it connects to you', 0.4, 'comprehension', N'Lena felt nervous on her first day of school. She did not know anyone. Her hands were shaking. Then a girl named Rosa smiled and said hello.');

-- Node 413: Comprehension Final Assessment
INSERT INTO QuizQuestions (NodeID, QuestionText, QuestionType, OptionsJSON, CorrectAnswer, EstimatedDifficulty, SkillCategory, ReadingPassage) VALUES
(413, N'Who went to the park?', 'multiple_choice', N'["Cora and her brother","Cora alone","Two friends"]', N'Cora and her brother', 0.2, 'comprehension', N'Cora and her little brother went to the park. They saw ducks near a pond. First, they fed the ducks. Then, Cora taught her brother how to skip stones. Finally, they went home happy.'),
(413, N'What did they see near the pond?', 'multiple_choice', N'["Ducks","Frogs","Fish"]', N'Ducks', 0.2, 'comprehension', N'Cora and her little brother went to the park. They saw ducks near a pond. First, they fed the ducks. Then, Cora taught her brother how to skip stones. Finally, they went home happy.'),
(413, N'What did they do first?', 'multiple_choice', N'["Skipped stones","Fed the ducks","Went home"]', N'Fed the ducks', 0.2, 'comprehension', N'Cora and her little brother went to the park. They saw ducks near a pond. First, they fed the ducks. Then, Cora taught her brother how to skip stones. Finally, they went home happy.'),
(413, N'What did they do last?', 'multiple_choice', N'["Fed ducks","Skipped stones","Went home happy"]', N'Went home happy', 0.2, 'comprehension', N'Cora and her little brother went to the park. They saw ducks near a pond. First, they fed the ducks. Then, Cora taught her brother how to skip stones. Finally, they went home happy.'),
(413, N'The setting is:', 'multiple_choice', N'["School","Park","Beach"]', N'Park', 0.2, 'comprehension', N'Cora and her little brother went to the park. They saw ducks near a pond. First, they fed the ducks. Then, Cora taught her brother how to skip stones. Finally, they went home happy.'),
(413, N'The main characters are:', 'multiple_choice', N'["Cora and her brother","Two boys","Ducks"]', N'Cora and her brother', 0.2, 'comprehension', N'Cora and her little brother went to the park. They saw ducks near a pond. First, they fed the ducks. Then, Cora taught her brother how to skip stones. Finally, they went home happy.'),
(413, N'The main idea of the story is:', 'multiple_choice', N'["Cora and her brother had a fun day at the park","Ducks swim in ponds","Stones are heavy"]', N'Cora and her brother had a fun day at the park', 0.3, 'comprehension', N'Cora and her little brother went to the park. They saw ducks near a pond. First, they fed the ducks. Then, Cora taught her brother how to skip stones. Finally, they went home happy.'),
(413, N'Cora''s brother learned to skip stones because:', 'multiple_choice', N'["Cora taught him","He read a book","A duck showed him"]', N'Cora taught him', 0.3, 'comprehension', N'Cora and her little brother went to the park. They saw ducks near a pond. First, they fed the ducks. Then, Cora taught her brother how to skip stones. Finally, they went home happy.'),
(413, N'What skill did Cora share with her brother?', 'multiple_choice', N'["Swimming","Skipping stones","Feeding ducks"]', N'Skipping stones', 0.3, 'comprehension', N'Cora and her little brother went to the park. They saw ducks near a pond. First, they fed the ducks. Then, Cora taught her brother how to skip stones. Finally, they went home happy.'),
(413, N'How were Cora and her brother alike?', 'multiple_choice', N'["Both fed ducks","Both stayed home","Both cried"]', N'Both fed ducks', 0.3, 'comprehension', N'Cora and her little brother went to the park. They saw ducks near a pond. First, they fed the ducks. Then, Cora taught her brother how to skip stones. Finally, they went home happy.'),
(413, N'The word ''pond'' most likely means:', 'multiple_choice', N'["A small lake","A tree","A road"]', N'A small lake', 0.3, 'comprehension', N'Cora and her little brother went to the park. They saw ducks near a pond. First, they fed the ducks. Then, Cora taught her brother how to skip stones. Finally, they went home happy.'),
(413, N'We can conclude that Cora and her brother:', 'multiple_choice', N'["Enjoyed their time together","Did not like the park","Were angry"]', N'Enjoyed their time together', 0.3, 'comprehension', N'Cora and her little brother went to the park. They saw ducks near a pond. First, they fed the ducks. Then, Cora taught her brother how to skip stones. Finally, they went home happy.'),
(413, N'From the passage, Cora seems to be:', 'multiple_choice', N'["A caring sister","A shy girl","A mean person"]', N'A caring sister', 0.3, 'comprehension', N'Cora and her little brother went to the park. They saw ducks near a pond. First, they fed the ducks. Then, Cora taught her brother how to skip stones. Finally, they went home happy.'),
(413, N'What will they likely do next time they visit the park?', 'multiple_choice', N'["Feed the ducks again","Stay home","Go to school"]', N'Feed the ducks again', 0.3, 'comprehension', N'Cora and her little brother went to the park. They saw ducks near a pond. First, they fed the ducks. Then, Cora taught her brother how to skip stones. Finally, they went home happy.'),
(413, N'Which reflection best shows text-to-self connection?', 'multiple_choice', N'["This reminds me of a trip I took with my sibling","Ducks are birds","Parks have trees"]', N'This reminds me of a trip I took with my sibling', 0.3, 'comprehension', N'Cora and her little brother went to the park. They saw ducks near a pond. First, they fed the ducks. Then, Cora taught her brother how to skip stones. Finally, they went home happy.'),
(413, N'The best summary of the story is:', 'multiple_choice', N'["Cora and her brother visited the park, fed ducks, and skipped stones.","Cora likes ducks.","The park is near a pond."]', N'Cora and her brother visited the park, fed ducks, and skipped stones.', 0.3, 'comprehension', N'Cora and her little brother went to the park. They saw ducks near a pond. First, they fed the ducks. Then, Cora taught her brother how to skip stones. Finally, they went home happy.'),
(413, N'A good prediction before reading is:', 'multiple_choice', N'["The story is about a park trip","Ducks are scary","Stones are colorful"]', N'The story is about a park trip', 0.3, 'comprehension', N'Cora and her little brother went to the park. They saw ducks near a pond. First, they fed the ducks. Then, Cora taught her brother how to skip stones. Finally, they went home happy.'),
(413, N'Sequencing means:', 'multiple_choice', N'["Putting events in order","Counting words","Drawing the story"]', N'Putting events in order', 0.2, 'comprehension', N'Cora and her little brother went to the park. They saw ducks near a pond. First, they fed the ducks. Then, Cora taught her brother how to skip stones. Finally, they went home happy.'),
(413, N'The main idea is the:', 'multiple_choice', N'["Most important point","A small detail","A question"]', N'Most important point', 0.2, 'comprehension', N'Cora and her little brother went to the park. They saw ducks near a pond. First, they fed the ducks. Then, Cora taught her brother how to skip stones. Finally, they went home happy.'),
(413, N'Context clues help readers:', 'multiple_choice', N'["Understand unknown words","Draw pictures","Write poems"]', N'Understand unknown words', 0.3, 'comprehension', N'Cora and her little brother went to the park. They saw ducks near a pond. First, they fed the ducks. Then, Cora taught her brother how to skip stones. Finally, they went home happy.');
