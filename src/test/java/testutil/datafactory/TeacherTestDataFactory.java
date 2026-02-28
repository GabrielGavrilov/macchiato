package testutil.datafactory;

import core.Teacher;

public class TeacherTestDataFactory {
    public static Teacher createValidTeacher() {
        return Teacher.newInstance(1, "John", "Doe");
    }
}
