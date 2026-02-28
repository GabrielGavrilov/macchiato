package core;

import org.gabrielgavrilov.macchiato.Macchiato;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public class Test {
    public static void main(String[] args) throws SQLException {

//        Macchiato.DATABASE = "jdbc:sqlite:test.db";
        Macchiato.DATABASE = "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1";

        Connection conn = DriverManager.getConnection(Macchiato.DATABASE);
        Statement stmt = conn.createStatement();
        stmt.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS teachers (
                        teacher_id INT PRIMARY KEY,
                        first_name VARCHAR(255),
                        last_name VARCHAR(255)
                    );
                """);

        stmt.close();
        conn.close();


        TeacherRepository teacherRepository = new TeacherRepository();
        CourseRepository courseRepository = new CourseRepository();

        teacherRepository.save(Teacher.newInstance(1, "John", "Doe"));

        List<Teacher> teachers = teacherRepository.getAll();

        for (Teacher teacher : teachers) {
            System.out.println(teacher.firstName);
        }

    }
}
