import org.gabrielgavrilov.macchiato.DataSource;
import org.gabrielgavrilov.macchiato.Macchiato;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MacchiatoTest {
    public static void main(String[] args) {

        Macchiato.DATABASE = "jdbc:sqlite:test.db";

        TeacherRepository teacherRepository = new TeacherRepository();
        CourseRepository courseRepository = new CourseRepository();


        Teacher teacher = courseRepository.findById(Integer.toString(201)).teacher;
        System.out.println(teacher.firstName);


    }
}
