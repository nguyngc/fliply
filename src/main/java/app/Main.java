package app;

import model.dao.*;
import model.entity.*;

public class Main {
    public static void main(String[] args) {
        UserDao userDao = new UserDao();
        ClassModelDao classDao = new ClassModelDao();

        User u = new User();
        u.setFirstName("Nhut");
        u.setLastName("Vo");
        u.setEmail("nhut@test.com");
        u.setGoogleId("google-sub-123");
        u.setRole(1);
        userDao.persist(u);

        ClassModel c = new ClassModel();
        c.setClassName("ICT23-SW");
        c.setTeacher(u);
        classDao.persist(c);

        System.out.println("Teacher email = " + c.getTeacher().getEmail());
        System.out.println("Classes count = " + classDao.findByTeacherId(u.getUserId()).size());

        FlashcardSetDao setDao = new FlashcardSetDao();

        FlashcardSet fs = new FlashcardSet();
        fs.setSubject("Math");
        fs.setClassModel(c);

        setDao.persist(fs);

        System.out.println("Set: " + fs);
        System.out.println("Teacher email from set: " + fs.getClassModel().getTeacher().getEmail());
        System.out.println("Sets in class: " + setDao.findByClassId(c.getClassId()).size());

        FlashcardDao flashDao = new FlashcardDao();

        Flashcard f = new Flashcard();
        f.setTerm("BFS");
        f.setDefinition("Breadth-first search");
        f.setFlashcardSet(fs);
        f.setUser(u);

        flashDao.persist(f);

        System.out.println("Flashcards in set: " + flashDao.findByFlashcardSetId(fs.getFlashcardSetId()).size());
        System.out.println("Flashcards by user: " + flashDao.findByUserId(u.getUserId()).size());
        System.out.println("Exists term BFS in set? " + flashDao.existsByTermInSet("BFS", fs.getFlashcardSetId()));

        QuizDao quizDao = new QuizDao();

        Quiz q = new Quiz();
        q.setNoOfQuestions(2);
        q.setUser(u);
        quizDao.persist(q);

        System.out.println("Quiz: " + q);
        System.out.println("Quizzes by user: " + quizDao.findByUserId(u.getUserId()).size());
        System.out.println("Exists same quiz? " + quizDao.existsByUserAndQuestionCount(u.getUserId(), 2));


        QuizDetailsDao qdDao = new QuizDetailsDao();

        QuizDetails qd = new QuizDetails(q, f);
        qdDao.persist(qd);

        System.out.println("QuizDetails: " + qd);
        System.out.println("Details in quiz: " + qdDao.findByQuizId(q.getQuizId()).size());
        System.out.println("Exists pair? " + qdDao.exists(q.getQuizId(), f.getFlashcardId()));
        ClassDetailsDao cdDao = new ClassDetailsDao();

        ClassDetails cd = new ClassDetails();
        cd.setUser(u);
        cd.setClassModel(c);
        cdDao.persist(cd);

        System.out.println("ClassDetails: " + cd);
        System.out.println("Students in class: " + cdDao.findByClassId(c.getClassId()).size());
        System.out.println("Classes joined by user: " + cdDao.findByUserId(u.getUserId()).size());
        System.out.println("Exists enrollment? " + cdDao.existsByUserAndClass(u.getUserId(), c.getClassId()));


        StudyDao studyDao = new StudyDao();

        Study s = new Study();
        s.setStatistic(10);
        s.setUser(u);
        s.setFlashcardSet(fs);
        studyDao.persist(s);

        System.out.println("Study: " + s);
        System.out.println("Study by user: " + studyDao.findByUserId(u.getUserId()).size());
        System.out.println("Study by set: " + studyDao.findByFlashcardSetId(fs.getFlashcardSetId()).size());
        System.out.println("Exists study record? " + studyDao.existsByUserAndSet(u.getUserId(), fs.getFlashcardSetId()));
    }
}
