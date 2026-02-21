USE fliply;
SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE TABLE `QUIZ_DETAILS`;
TRUNCATE TABLE `CLASS_DETAILS`;
TRUNCATE TABLE `STUDY`;
TRUNCATE TABLE `FLASHCARD`;
TRUNCATE TABLE `QUIZ`;
TRUNCATE TABLE `FLASHCARDSET`;
TRUNCATE TABLE `CLASS`;
TRUNCATE TABLE `USER`;

SET FOREIGN_KEY_CHECKS = 1;

-- ============================
-- 1) USER  ( Password)
-- ============================
INSERT INTO `USER` (Role, Email, Password, FirstName, LastName)
VALUES
    (1, 'teacher1@example.com', '123', 'Alice',   'Doe'),
    (1, 'teacher2@example.com', '123', 'Bob',     'Chan'),
    (0, 'student1@example.com', '123', 'Charlie', 'David'),
    (0, 'student2@example.com', '123', 'David',   'Helen');

-- Fetch IDs safely (no hard-code 1,2,3...)
SET @t1 := (SELECT UserId FROM `USER` WHERE Email='teacher1@example.com');
SET @t2 := (SELECT UserId FROM `USER` WHERE Email='teacher2@example.com');
SET @s1 := (SELECT UserId FROM `USER` WHERE Email='student1@example.com');
SET @s2 := (SELECT UserId FROM `USER` WHERE Email='student2@example.com');

-- ============================
-- 2) CLASS
-- ============================
INSERT INTO `CLASS` (ClassName, TeacherId)
VALUES
    ('Java Programming',     @t1),
    ('DevOps Fundamentals',  @t2);

SET @c1 := (SELECT ClassId FROM `CLASS` WHERE ClassName='Java Programming');
SET @c2 := (SELECT ClassId FROM `CLASS` WHERE ClassName='DevOps Fundamentals');

-- ============================
-- 3) CLASS_DETAILS (students in class)
-- ============================
INSERT INTO `CLASS_DETAILS` (ClassId, StudentId)
VALUES
    (@c1, @s1),
    (@c1, @s2),
    (@c2, @s1);

-- ============================
-- 4) FLASHCARDSET
-- ============================
INSERT INTO `FLASHCARDSET` (Subject, ClassId)
VALUES
    ('OOP Basics',      @c1),
    ('Java Keywords',   @c1),
    ('Docker Concepts', @c2);

SET @fs1 := (SELECT FlashcardSetId FROM `FLASHCARDSET` WHERE Subject='OOP Basics' AND ClassId=@c1);
SET @fs2 := (SELECT FlashcardSetId FROM `FLASHCARDSET` WHERE Subject='Java Keywords' AND ClassId=@c1);
SET @fs3 := (SELECT FlashcardSetId FROM `FLASHCARDSET` WHERE Subject='Docker Concepts' AND ClassId=@c2);

-- ============================
-- 5) FLASHCARD
-- ============================
INSERT INTO `FLASHCARD` (Term, Definition, FlashcardSetId, UserId)
VALUES
    ('Encapsulation', 'Bundling data and methods together',               @fs1, @t1),
    ('Inheritance',   'Mechanism to acquire properties of another class', @fs1, @t1),
    ('static',        'Keyword for class-level members',                  @fs2, @t1),
    ('Container',     'A lightweight isolated environment',               @fs3, @t2),
    ('Image',         'A template used to create containers',             @fs3, @t2);

SET @f1 := (SELECT FlashcardId FROM `FLASHCARD` WHERE Term='Encapsulation' AND FlashcardSetId=@fs1);
SET @f2 := (SELECT FlashcardId FROM `FLASHCARD` WHERE Term='Inheritance'   AND FlashcardSetId=@fs1);
SET @f3 := (SELECT FlashcardId FROM `FLASHCARD` WHERE Term='static'        AND FlashcardSetId=@fs2);
SET @f4 := (SELECT FlashcardId FROM `FLASHCARD` WHERE Term='Container'     AND FlashcardSetId=@fs3);
SET @f5 := (SELECT FlashcardId FROM `FLASHCARD` WHERE Term='Image'         AND FlashcardSetId=@fs3);

-- ============================
-- 6) QUIZ
-- ============================
INSERT INTO `QUIZ` (NoOfQuestions, UserId)
VALUES
    (3, @t1),
    (2, @t2);

SET @q1 := (SELECT QuizId FROM `QUIZ` WHERE UserId=@t1 AND NoOfQuestions=3 ORDER BY QuizId DESC LIMIT 1);
SET @q2 := (SELECT QuizId FROM `QUIZ` WHERE UserId=@t2 AND NoOfQuestions=2 ORDER BY QuizId DESC LIMIT 1);

-- ============================
-- 7) QUIZ_DETAILS
-- ============================
INSERT INTO `QUIZ_DETAILS` (QuizId, FlashcardId)
VALUES
    (@q1, @f1),
    (@q1, @f2),
    (@q1, @f3),
    (@q2, @f4),
    (@q2, @f5);

-- ============================
-- 8) STUDY
-- ============================
INSERT INTO `STUDY` (Statistic, UserId, FlashcardSetId)
VALUES
    (5, @s1, @fs1),
    (2, @s1, @fs2),
    (1, @s2, @fs3);
