import core.Teacher;
import core.TeacherRepository;
import org.gabrielgavrilov.macchiato.DataSource;
import org.gabrielgavrilov.macchiato.Macchiato;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import testutil.datafactory.TeacherTestDataFactory;

import java.sql.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class MacchiatoTest {

    private TeacherRepository teacherRepository;

    @BeforeEach
    public void before() {
        Macchiato.DATABASE = "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1";
        teacherRepository = new TeacherRepository();
        execute("""
            CREATE TABLE IF NOT EXISTS teachers (
                teacher_id INT PRIMARY KEY,
                first_name VARCHAR(255),
                last_name VARCHAR(255)
            );
        """);
    }

    @Test
    public void testMacchiatoRepository_save() {
        Teacher entity = TeacherTestDataFactory.createValidTeacher();
        teacherRepository.save(entity);
        Teacher teacher = teacherRepository.getAll().get(0);
        assertNotNull(teacher);
        assertAll(
                () -> assertEquals(entity.teacherId, teacher.teacherId),
                () -> assertEquals(entity.firstName, teacher.firstName),
                () -> assertEquals(entity.lastName, teacher.lastName)
        );
    }

    private void execute(String query) {
        try {
            Connection conn = DriverManager.getConnection(Macchiato.DATABASE);

            Statement stmt = conn.createStatement();
            stmt.executeUpdate(query);

            stmt.close();
            conn.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


}
