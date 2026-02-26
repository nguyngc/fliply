package model.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ClassDetailsTest {

    @Test
    void testDefaultConstructor() {
        ClassDetails cd = new ClassDetails();

        assertNull(cd.getClassDetailsId());
        assertNull(cd.getClassModel());
        assertNull(cd.getStudent());
    }

    @Test
    void testFullConstructor() {
        ClassModel cm = new ClassModel();
        User student = new User();

        ClassDetails cd = new ClassDetails(cm, student);

        assertEquals(cm, cd.getClassModel());
        assertEquals(student, cd.getStudent());
    }

    @Test
    void testSettersAndGetters() {
        ClassDetails cd = new ClassDetails();

        ClassModel cm = new ClassModel();
        User student = new User();

        cd.setClassModel(cm);
        cd.setStudent(student);

        assertEquals(cm, cd.getClassModel());
        assertEquals(student, cd.getStudent());
    }

    @Test
    void testGetClassDetailsId_withReflection() {
        ClassDetails cd = new ClassDetails();

        setId(cd, "classDetailsId", 99);

        assertEquals(99, cd.getClassDetailsId());
    }

    @Test
    void testToString_withValues() {
        ClassModel cm = new ClassModel();
        User student = new User();

        setId(cm, "classId", 10);
        setId(student, "userId", 5);
        setId(student, "role", 1);

        ClassDetails cd = new ClassDetails(cm, student);
        setId(cd, "classDetailsId", 7);

        String str = cd.toString();

        assertTrue(str.contains("classDetailsId=7"));
        assertTrue(str.contains("StudentId=5"));
        assertTrue(str.contains("classId=10"));
    }

    @Test
    void testToString_nullValues() {
        ClassDetails cd = new ClassDetails();

        String str = cd.toString();

        assertTrue(str.contains("StudentId=null"));
        assertTrue(str.contains("classId=null"));
    }

    // ---------------- Reflection helper ----------------
    private void setId(Object obj, String field, Integer value) {
        try {
            var f = obj.getClass().getDeclaredField(field);
            f.setAccessible(true);
            f.set(obj, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
