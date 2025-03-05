import org.gabrielgavrilov.macchiato.DataSource;
import org.gabrielgavrilov.macchiato.Macchiato;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MacchiatoTest {
    public static void main(String[] args) {

        // SELECT * FROM courses JOIN teachers ON (teachers.teacher_id = courses.teacher_id) WHERE teachers.first_name = "John" ;

        Macchiato.DATABASE = "jdbc:sqlite:test.db";

        TeacherRepository teacherRepository = new TeacherRepository();
        CourseRepository courseRepository = new CourseRepository();

        List<Teacher> teachers = teacherRepository.getAll();
        List<Course> courses = courseRepository.getAll();

        List<Course> johnCourses = teacherRepository.findById(Integer.toString(101)).courses;

        for (Course c : johnCourses) {
            System.out.println(String.format("(%s, %s)", c.courseId, c.courseName));
        }

//        System.out.println("Teachers");
//        teachers.forEach((teacher) -> {
//            System.out.println(String.format("(%s, %s, %s)", teacher.teacherId, teacher.firstName, teacher.lastName));
//        });
//
//        System.out.println("Courses");
//        courses.forEach((course) -> {
//            System.out.println(String.format("(%s, %s, %s)", course.courseId, course.courseName, course.teacherId));
//        });



    }
}
