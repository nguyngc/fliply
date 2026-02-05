USE fliply;
SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE TABLE QUIZ_DETAILS;
TRUNCATE TABLE Flashcard;
TRUNCATE TABLE FlashcardSet;
TRUNCATE TABLE Study;
TRUNCATE TABLE CLASS_DETAILS;
TRUNCATE TABLE ClassModel;
TRUNCATE TABLE Quiz;
TRUNCATE TABLE User;

SET FOREIGN_KEY_CHECKS = 1;

-- ============================
-- 1. USER
-- ============================
INSERT INTO User (role, email, firstName, lastName, googleId)
VALUES
    (1, 'teacher1@example.com', 'Alice', 'Doe', 'g123'),
    (1, 'teacher2@example.com', 'Bob', 'Chan', 'g456'),
    (0, 'student1@example.com', 'Charlie', 'David', 'g789'),
    (0, 'student2@example.com', 'David', 'Helen', 'g101');

-- ============================
-- 2. CLASSMODEL
-- ============================
INSERT INTO ClassModel (teacherId, className)
VALUES
    (1, 'Java Programming'),
    (2, 'DevOps Fundamentals');

-- ============================
-- 3. CLASS_DETAILS (students in class)
-- ============================
INSERT INTO CLASS_DETAILS (classId, userId)
VALUES
    (1, 3),   -- Charlie in Java Programming
    (1, 4),   -- David in Java Programming
    (2, 3);   -- Charlie in DevOps Fundamentals

-- ============================
-- 4. FLASHCARDSET
-- ============================
INSERT INTO FlashcardSet (classId, subject)
VALUES
    (1, 'OOP Basics'),
    (1, 'Java Keywords'),
    (2, 'Docker Concepts');

-- ============================
-- 5. FLASHCARD
-- ============================
INSERT INTO Flashcard (flashcardSetId, userId, term, definition)
VALUES
    (1, 1, 'Encapsulation', 'Bundling data and methods together'),
    (1, 1, 'Inheritance', 'Mechanism to acquire properties of another class'),
    (2, 1, 'static', 'Keyword for class-level members'),
    (3, 2, 'Container', 'A lightweight isolated environment'),
    (3, 2, 'Image', 'A template used to create containers');

-- ============================
-- 6. QUIZ
-- ============================
INSERT INTO Quiz (noOfQuestions, userId)
VALUES
    (3, 1),
    (2, 2);


-- ============================
-- 7. QUIZ_DETAILS (flashcards inside quizzes)
-- ============================
INSERT INTO QUIZ_DETAILS (flashcardId, quizId)
VALUES
    (1, 1),
    (2, 1),
    (3, 1),
    (4, 2),
    (5, 2);

-- ============================
-- 8. STUDY (tracking study progress)
-- ============================
INSERT INTO Study (flashcardSetId, statistic, userId)
VALUES
    (1, 5, 3),   -- Charlie studied OOP Basics
    (2, 2, 3),   -- Charlie studied Java Keywords
    (3, 1, 4);   -- David studied Docker Concepts
