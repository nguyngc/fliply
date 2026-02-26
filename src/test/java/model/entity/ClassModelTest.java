package model.entity;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

class ClassModelTest {

    // Helper: set private ID fields via reflection
    private void setId(Object obj, String field, Integer value) {
        try {
            Field f = obj.getClass().getDeclaredField(field);
            f.setAccessible(true);
            f.set(obj, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testDefaultConstructor() {
        ClassModel cm = new ClassModel();

        assertNull(cm.getClassId());
        assertNull(cm.getClassName());
        assertNull(cm.getTeacher());
        assertNotNull(cm.getStudents());
        assertNotNull(cm.getFlashcardSets());
        assertTrue(cm.getStudents().isEmpty());
        assertTrue(cm.getFlashcardSets().isEmpty());
    }

    @Test
    void testFullConstructor() {
        User teacher = new User();
        ClassModel cm = new ClassModel("Math", teacher);

        assertEquals("Math", cm.getClassName());
        assertEquals(teacher, cm.getTeacher());
    }

    @Test
    void testSettersAndGetters() {
        ClassModel cm = new ClassModel();

        cm.setClassName("Physics");
        assertEquals("Physics", cm.getClassName());

        User teacher = new User();
        cm.setTeacher(teacher);
        assertEquals(teacher, cm.getTeacher());
    }

    @Test
    void testGetClassId() {
        ClassModel cm = new ClassModel();
        setId(cm, "classId", 123);

        assertEquals(123, cm.getClassId());
    }

    @Test
    void testGetStudents() {
        ClassModel cm = new ClassModel();
        assertNotNull(cm.getStudents());
        assertTrue(cm.getStudents().isEmpty());
    }

    @Test
    void testGetFlashcardSets() {
        ClassModel cm = new ClassModel();
        assertNotNull(cm.getFlashcardSets());
        assertTrue(cm.getFlashcardSets().isEmpty());
    }

    @Test
    void testToString_withValues() {
        ClassModel cm = new ClassModel();
        User teacher = new User();

        setId(cm, "classId", 10);
        setId(teacher, "userId", 5);

        cm.setClassName("Biology");
        cm.setTeacher(teacher);

        String str = cm.toString();

        assertTrue(str.contains("classId=10"));
        assertTrue(str.contains("className='Biology'"));
        assertTrue(str.contains("teacherId=5"));
    }

    @Test
    void testToString_nullValues() {
        ClassModel cm = new ClassModel();

        String str = cm.toString();

        assertTrue(str.contains("classId=null"));
        assertTrue(str.contains("className='null'"));
        assertTrue(str.contains("teacherId=null"));
    }

    @Test
    void testGetTeacherName_withTeacher() {
        User teacher = new User();
        teacher.setFirstName("John");
        teacher.setLastName("Doe");

        ClassModel cm = new ClassModel("History", teacher);

        assertEquals("John Doe", cm.getTeacherName());
    }

    @Test
    void testGetTeacherName_noTeacher() {
        ClassModel cm = new ClassModel();

        assertEquals("", cm.getTeacherName());
    }
}
