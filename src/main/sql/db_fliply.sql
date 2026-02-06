DROP DATABASE IF EXISTS fliply;
CREATE DATABASE fliply;
USE fliply;

DROP USER IF EXISTS 'appuser'@'%';
CREATE USER 'appuser'@'%' IDENTIFIED BY 'password';
GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, DROP, ALTER ON fliply.* TO 'appuser'@'%';
FLUSH PRIVILEGES;

CREATE TABLE `USER`
(
    UserId    INT NOT NULL AUTO_INCREMENT,
    FirstName VARCHAR(50)  NOT NULL,
    LastName  VARCHAR(50)  NOT NULL,
    Email     VARCHAR(255) NOT NULL,
    GoogleId  VARCHAR(128) NOT NULL,
    Role      INT NOT NULL,
    PRIMARY KEY (UserId)
);

CREATE TABLE `CLASS`
(
    ClassId   INT NOT NULL AUTO_INCREMENT,
    ClassName VARCHAR(100) NOT NULL,
    TeacherId INT NOT NULL,
    PRIMARY KEY (ClassId),
    FOREIGN KEY (TeacherId) REFERENCES `USER`(UserId)
);

CREATE TABLE FLASHCARDSET
(
    FlashcardSetId INT NOT NULL AUTO_INCREMENT,
    Subject        VARCHAR(100) NOT NULL,
    ClassId        INT NOT NULL,
    PRIMARY KEY (FlashcardSetId),
    FOREIGN KEY (ClassId) REFERENCES `CLASS`(ClassId)
);

CREATE TABLE FLASHCARD
(
    FlashcardId    INT NOT NULL AUTO_INCREMENT,
    Term           VARCHAR(255) NOT NULL,
    Definition     TEXT NOT NULL,
    FlashcardSetId INT NOT NULL,
    UserId         INT NOT NULL,
    PRIMARY KEY (FlashcardId),
    FOREIGN KEY (FlashcardSetId) REFERENCES FLASHCARDSET(FlashcardSetId),
    FOREIGN KEY (UserId) REFERENCES `USER`(UserId)
);

CREATE TABLE QUIZ
(
    QuizId        INT NOT NULL AUTO_INCREMENT,
    NoOfQuestions INT NOT NULL,
    UserId        INT NOT NULL,
    PRIMARY KEY (QuizId),
    FOREIGN KEY (UserId) REFERENCES `USER`(UserId)
);

CREATE TABLE CLASS_DETAILS
(
    ClassDetailsId INT NOT NULL AUTO_INCREMENT,
    UserId  INT NOT NULL,
    ClassId INT NOT NULL,

    PRIMARY KEY (ClassDetailsId),
    UNIQUE (UserId, ClassId),

    FOREIGN KEY (UserId) REFERENCES `USER`(UserId),
    FOREIGN KEY (ClassId) REFERENCES `CLASS`(ClassId)
);

CREATE TABLE QUIZ_DETAILS
(
    QuizDetailsId INT NOT NULL AUTO_INCREMENT,
    QuizId        INT NOT NULL,
    FlashcardId   INT NOT NULL,
    PRIMARY KEY (QuizDetailsId),
    UNIQUE (QuizId, FlashcardId),
    FOREIGN KEY (QuizId) REFERENCES QUIZ(QuizId),
    FOREIGN KEY (FlashcardId) REFERENCES FLASHCARD(FlashcardId)
);

CREATE TABLE STUDY
(
    StudyId        INT NOT NULL AUTO_INCREMENT,
    Statistic      INT NOT NULL,
    UserId         INT NOT NULL,
    FlashcardSetId INT NOT NULL,

    PRIMARY KEY (StudyId),
    UNIQUE (UserId, FlashcardSetId),

    FOREIGN KEY (UserId) REFERENCES `USER`(UserId),
    FOREIGN KEY (FlashcardSetId) REFERENCES FLASHCARDSET(FlashcardSetId)
);

