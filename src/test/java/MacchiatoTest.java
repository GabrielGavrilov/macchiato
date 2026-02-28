import core.Teacher;
import core.TeacherRepository;
import org.gabrielgavrilov.macchiato.Macchiato;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class MacchiatoTest {

    private TeacherRepository teacherRepository;

    private final Teacher john = Teacher.newInstance(1, "John", "Doe");
    private final Teacher jane = Teacher.newInstance(2, "Jane", "Doe");

    @BeforeEach
    public void before() {
        Macchiato.DATABASE = "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1";
        teacherRepository = new TeacherRepository();
        execute("DROP TABLE IF EXISTS teachers;");
        execute("""
            CREATE TABLE teachers (
                teacher_id INT PRIMARY KEY,
                first_name VARCHAR(255),
                last_name VARCHAR(255)
            );
        """);
    }

    @Test
    public void testMacchiatoRepository_save_shouldReturnSavedEntity() {
        Teacher teacher = teacherRepository.save(john);
        assertNotNull(teacher);
        assertAll(
                () -> assertEquals(john.teacherId, teacher.teacherId),
                () -> assertEquals(john.firstName, teacher.firstName),
                () -> assertEquals(john.lastName, teacher.lastName)
        );
    }

    @Test
    public void testMacchiatoRepository_getAll_shouldReturnAListOfAllEntities() {
        teacherRepository.save(john);
        teacherRepository.save(jane);

        List<Teacher> teachers = teacherRepository.getAll();

        assertEquals(2, teachers.size());
        assertAll(
                () -> assertEquals(jane.teacherId, teachers.get(1).teacherId),
                () -> assertEquals(jane.firstName, teachers.get(1).firstName),
                () -> assertEquals(jane.lastName, teachers.get(1).lastName)
        );
    }

    @Test
    public void testMacchiatoRepository_getAll_withNoEntities_shouldReturnAnEmptyList() {
        List<Teacher> teachers = teacherRepository.getAll();
        assertEquals(0, teachers.size());
    }

    @Test
    public void testMacchiatoRepository_findById_shouldReturnEntity() {
        teacherRepository.save(john);
        Teacher entity = teacherRepository.findById(String.valueOf(john.teacherId));
        assertNotNull(entity);
        assertAll(
                () -> assertEquals(john.teacherId, entity.teacherId),
                () -> assertEquals(john.firstName, entity.firstName),
                () -> assertEquals(john.lastName, entity.lastName)
        );
    }

    @Test
    public void testMacchiatoRepository_findById_withNoEntity_shouldReturnNullEntity() {
        Teacher entity = teacherRepository.findById(String.valueOf(1));
        assertNull(entity);
    }

    @Test
    public void testMacchiatoRepository_update_shouldReturnUpdatedEntity() {
        teacherRepository.save(john);
        Teacher mark = Teacher.newInstance(1, "Mark", "Brown");
        Teacher entity = teacherRepository.update(mark);
        assertNotNull(entity);
        assertAll(
                () -> assertEquals(john.teacherId, entity.teacherId),
                () -> assertEquals(mark.firstName, entity.firstName),
                () -> assertEquals(mark.lastName, entity.lastName)
        );
    }

    @Test
    public void testMacchiatoRepository_delete_shouldDeleteEntity() {
        teacherRepository.save(john);
        Teacher savedEntity = teacherRepository.findById(String.valueOf(john.teacherId));
        assertNotNull(savedEntity);
        teacherRepository.delete(john);
        savedEntity = teacherRepository.findById(String.valueOf(john.teacherId));
        assertNull(savedEntity);
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
