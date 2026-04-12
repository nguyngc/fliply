package app;

import model.dao.*;
import model.entity.*;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {
    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        UserDao userDao = new UserDao();
        ClassModelDao classDao = new ClassModelDao();

        User u = new User();
        u.setFirstName("Nhut");
        u.setLastName("Vo");
        u.setEmail("nhut@test.com");
        u.setPassword("12345");
        u.setRole(1);
        userDao.persist(u);

        ClassModel c = new ClassModel();
        c.setClassName("ICT23-SW");
        c.setTeacher(u);
        classDao.persist(c);

        LOGGER.log(Level.INFO, () -> "Teacher email = " + c.getTeacher().getEmail());
        LOGGER.log(Level.INFO, () -> "Classes count = " + classDao.findByTeacherId(u.getUserId()).size());

        FlashcardSetDao setDao = new FlashcardSetDao();

        FlashcardSet fs = new FlashcardSet();
        fs.setSubject("Math");
        fs.setClassModel(c);

        setDao.persist(fs);

        LOGGER.log(Level.INFO, () -> "Set: " + fs);
        LOGGER.log(Level.INFO, () -> "Teacher email from set: " + fs.getClassModel().getTeacher().getEmail());
        LOGGER.log(Level.INFO, () -> "Sets in class: " + setDao.findByClassId(c.getClassId()).size());

        FlashcardDao flashDao = new FlashcardDao();

        Flashcard f = new Flashcard();
        f.setTerm("BFS");
        f.setDefinition("Breadth-first search");
        f.setFlashcardSet(fs);
        f.setUser(u);

        flashDao.persist(f);

        LOGGER.log(Level.INFO, () -> "Flashcards in set: " + flashDao.findByFlashcardSetId(fs.getFlashcardSetId()).size());
        LOGGER.log(Level.INFO, () -> "Flashcards by user: " + flashDao.findByUserId(u.getUserId()).size());
        LOGGER.log(Level.INFO, () -> "Exists term BFS in set? " + flashDao.existsByTermInSet("BFS", fs.getFlashcardSetId()));

        QuizDao quizDao = new QuizDao();

        Quiz q = new Quiz();
        q.setNoOfQuestions(2);
        q.setUser(u);
        quizDao.persist(q);

        LOGGER.log(Level.INFO, () -> "Quiz: " + q);
        LOGGER.log(Level.INFO, () -> "Quizzes by user: " + quizDao.findByUserId(u.getUserId()).size());
        LOGGER.log(Level.INFO, () -> "Exists same quiz? " + quizDao.existsByUserAndQuestionCount(u.getUserId(), 2));


        QuizDetailsDao qdDao = new QuizDetailsDao();

        QuizDetails qd = new QuizDetails(q, f);
        qdDao.persist(qd);

        LOGGER.log(Level.INFO, () -> "QuizDetails: " + qd);
        LOGGER.log(Level.INFO, () -> "Details in quiz: " + qdDao.findByQuizId(q.getQuizId()).size());
        LOGGER.log(Level.INFO, () -> "Exists pair? " + qdDao.exists(q.getQuizId(), f.getFlashcardId()));
        ClassDetailsDao cdDao = new ClassDetailsDao();

        ClassDetails cd = new ClassDetails();
        cd.setStudent(u);
        cd.setClassModel(c);
        cdDao.persist(cd);

        LOGGER.log(Level.INFO, () -> "ClassDetails: " + cd);
        LOGGER.log(Level.INFO, () -> "Students in class: " + cdDao.findByClassId(c.getClassId()).size());
        LOGGER.log(Level.INFO, () -> "Classes joined by user: " + cdDao.findByStudentId(u.getUserId()).size());
        LOGGER.log(Level.INFO, () -> "Exists enrollment? " + cdDao.existsByUserAndClass(u.getUserId(), c.getClassId()));


        StudyDao studyDao = new StudyDao();

        Study s = new Study();
        s.setStatistic(10);
        s.setUser(u);
        s.setFlashcardSet(fs);
        studyDao.persist(s);

        LOGGER.log(Level.INFO, () -> "Study: " + s);
        LOGGER.log(Level.INFO, () -> "Study by user: " + studyDao.findByUserId(u.getUserId()).size());
        LOGGER.log(Level.INFO, () -> "Study by set: " + studyDao.findByFlashcardSetId(fs.getFlashcardSetId()).size());
        LOGGER.log(Level.INFO, () -> "Exists study record? " + studyDao.existsByUserAndSet(u.getUserId(), fs.getFlashcardSetId()));
    }
}
