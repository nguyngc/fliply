CREATE TABLE USER
(
    UserId INT NOT NULL,
    FirstName INT NOT NULL,
    LastName INT NOT NULL,
    Email INT NOT NULL,
    GoogleId INT NOT NULL,
    Role INT NOT NULL,
    PRIMARY KEY (UserId)
);

CREATE TABLE CLASS
(
    ClassId INT NOT NULL,
    ClassName INT NOT NULL,
    TeacherId INT NOT NULL,
    PRIMARY KEY (ClassId),
    FOREIGN KEY (TeacherId) REFERENCES USER(UserId)
);

CREATE TABLE FLASHCARDSET
(
    FlashcardSetId INT NOT NULL,
    Subject INT NOT NULL,
    ClassId INT NOT NULL,
    PRIMARY KEY (FlashcardSetId),
    FOREIGN KEY (ClassId) REFERENCES CLASS(ClassId)
);

CREATE TABLE FLASHCARD
(
    FlashcardId INT NOT NULL,
    Term INT NOT NULL,
    Definition INT NOT NULL,
    FlashcardSetId INT NOT NULL,
    UserId INT NOT NULL,
    PRIMARY KEY (FlashcardId),
    FOREIGN KEY (FlashcardSetId) REFERENCES FLASHCARDSET(FlashcardSetId),
    FOREIGN KEY (UserId) REFERENCES USER(UserId)
);

CREATE TABLE QUIZ
(
    QuizId INT NOT NULL,
    NoOfQuestions INT NOT NULL,
    UserId INT NOT NULL,
    PRIMARY KEY (QuizId),
    FOREIGN KEY (UserId) REFERENCES USER(UserId)
);

CREATE TABLE CLASS_DETAILS
(
    UserId INT NOT NULL,
    ClassId INT NOT NULL,
    PRIMARY KEY (UserId, ClassId),
    FOREIGN KEY (UserId) REFERENCES USER(UserId),
    FOREIGN KEY (ClassId) REFERENCES CLASS(ClassId)
);

CREATE TABLE QUIZ_DETAILS
(
    QuizId INT NOT NULL,
    FlashcardId INT NOT NULL,
    PRIMARY KEY (QuizId, FlashcardId),
    FOREIGN KEY (QuizId) REFERENCES QUIZ(QuizId),
    FOREIGN KEY (FlashcardId) REFERENCES FLASHCARD(FlashcardId)
);

CREATE TABLE STUDY
(
    Statistic INT NOT NULL,
    UserId INT NOT NULL,
    FlashcardSetId INT NOT NULL,
    PRIMARY KEY (UserId, FlashcardSetId),
    FOREIGN KEY (UserId) REFERENCES USER(UserId),
    FOREIGN KEY (FlashcardSetId) REFERENCES FLASHCARDSET(FlashcardSetId)
);