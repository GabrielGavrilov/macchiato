import core.entities.Todo;
import core.entities.User;
import core.repositories.TodoRepository;
import core.repositories.UserRepository;
import org.gabrielgavrilov.macchiato.Macchiato;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.*;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class MacchiatoTest {

    private UserRepository userRepository;
    private TodoRepository todoRepository;

    private final User john = User.newInstance("johndoe@gmail.com");
    private final User jane = User.newInstance("janedoe@gmail.com");

    private final Todo foo = Todo.newInstance(john.userId, "foo");
    private final Todo bar = Todo.newInstance(john.userId, "bar");

    @BeforeEach
    public void before() {
        Macchiato.DATABASE = "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1";

        userRepository = new UserRepository();
        todoRepository = new TodoRepository();

        execute("DROP TABLE IF EXISTS todos;");
        execute("DROP TABLE IF EXISTS users;");
        execute("""
            CREATE TABLE users (
                user_id UUID PRIMARY KEY,
                email VARCHAR(255)
            );
        """);
        execute("""
            CREATE TABLE todos (
                todo_id UUID PRIMARY KEY NOT NULL,
                user_id UUID NOT NULL,
                title VARCHAR(255),
                FOREIGN KEY (user_id) REFERENCES users(user_id)
            );
        """);
    }

    @Test
    public void testMacchiatoRepository_save_shouldReturnSavedEntity() {
        User user = userRepository.save(john);
        assertNotNull(user);
        assertAll(
                () -> assertEquals(john.userId, user.userId),
                () -> assertEquals(john.email, user.email)
        );
    }

    @Test
    public void testMacchiatoRepository_save_withJoinColumn_shouldReturnSavedEntity() {
        userRepository.save(john);
        Todo entity = todoRepository.save(foo);
        assertNotNull(entity);
        assertAll(
                () -> assertEquals(foo.todoId, entity.todoId),
                () -> assertEquals(foo.userId, entity.userId),
                () -> assertEquals(foo.title, entity.title),
                () -> assertEquals(john.userId, entity.user.userId),
                () -> assertEquals(john.email, entity.user.email)
        );
    }

    @Test
    public void testMacchiatoRepository_getAll_shouldReturnAListOfAllEntities() {
        userRepository.save(john);
        userRepository.save(jane);

        List<User> users = userRepository.getAll();

        assertEquals(2, users.size());
        assertAll(
                () -> assertEquals(jane.userId, users.get(1).userId),
                () -> assertEquals(jane.email, users.get(1).email)
        );
    }

    @Test
    public void testMacchiatoRepository_getAll_withJoinColumn_shouldReturnAListOfAllEntities() {
        userRepository.save(john);
        todoRepository.save(foo);
        todoRepository.save(bar);

        List<Todo> entities = todoRepository.getAll();
        Todo entity = entities.get(1);

        assertEquals(2, entities.size());
        assertAll(
                () -> assertEquals(bar.todoId, entity.todoId),
                () -> assertEquals(bar.userId, entity.userId),
                () -> assertEquals(bar.title, entity.title),
                () -> assertEquals(john.userId, entity.user.userId),
                () -> assertEquals(john.email, entity.user.email)
        );
    }

    @Test
    public void testMacchiatoRepository_getAll_withNoEntities_shouldReturnAnEmptyList() {
        List<User> users = userRepository.getAll();
        assertEquals(0, users.size());
    }

    @Test
    public void testMacchiatoRepository_findById_shouldReturnEntity() {
        userRepository.save(john);
        User entity = userRepository.findById(String.valueOf(john.userId));
        assertNotNull(entity);
        assertAll(
                () -> assertEquals(john.userId, entity.userId),
                () -> assertEquals(john.email, entity.email)
        );
    }

    @Test
    public void testMacchiatoRepository_findById_withJoinColumn_shouldReturnEntity() {
        userRepository.save(john);
        todoRepository.save(foo);
        Todo entity = todoRepository.findById(String.valueOf(foo.todoId));
        assertNotNull(entity);
        assertAll(
                () -> assertEquals(foo.todoId, entity.todoId),
                () -> assertEquals(foo.userId, entity.userId),
                () -> assertEquals(foo.title, entity.title),
                () -> assertEquals(john.userId, entity.user.userId),
                () -> assertEquals(john.email, entity.user.email)
        );
    }

    @Test
    public void testMacchiatoRepository_findById_withNoEntity_shouldReturnNullEntity() {
        User entity = userRepository.findById(String.valueOf(new UUID(0,1)));
        assertNull(entity);
    }

    @Test
    public void testMacchiatoRepository_update_shouldReturnUpdatedEntity() {
        userRepository.save(john);

        User oldEntity = userRepository.findById(String.valueOf(john.userId));
        User updatedEntity = john.setEmail("johndoe1@gmail.com");
        assertNotNull(oldEntity);

        User entity = userRepository.update(updatedEntity);
        assertNotNull(entity);

        assertAll(
                () -> assertEquals(john.userId, entity.userId),
                () -> assertEquals(updatedEntity.email, entity.email)
        );
    }

    @Test
    public void testMacchiatoRepository_update_withJoinColumn_shouldReturnUpdatedEntity() {
        userRepository.save(john);
        todoRepository.save(foo);

        Todo oldEntity = todoRepository.findById(String.valueOf(foo.todoId));
        Todo updatedEntity = foo.setTitle("baz");
        assertNotNull(oldEntity);

        Todo entity = todoRepository.update(updatedEntity);
        assertNotNull(entity);

        assertAll(
                () -> assertEquals(foo.todoId, entity.todoId),
                () -> assertEquals(foo.userId, entity.userId),
                () -> assertEquals(updatedEntity.title, entity.title),
                () -> assertEquals(john.userId, entity.user.userId),
                () -> assertEquals(john.email, entity.user.email)
        );
    }

    @Test
    public void testMacchiatoRepository_update_withNoEntity_shouldThrowRuntimeException() {
        assertThrows(RuntimeException.class, () -> userRepository.update(john));
    }

    @Test
    public void testMacchiatoRepository_delete_shouldDeleteEntity() {
        userRepository.save(john);
        User entity = userRepository.findById(String.valueOf(john.userId));
        assertNotNull(entity);
        userRepository.delete(john);
        entity = userRepository.findById(String.valueOf(john.userId));
        assertNull(entity);
    }

    @Test
    public void testMacchiatoRepository_delete_withJoinColumn_shouldDeleteEntity() {
        userRepository.save(john);
        todoRepository.save(foo);
        assertNotNull(todoRepository.findById(String.valueOf(foo.todoId)));
        todoRepository.delete(foo);
        assertNull(todoRepository.findById(String.valueOf(foo.todoId)));
    }

    @Test
    public void testMacchiatoRepository_delete_withJoinColumn_deleteForeignEntity_shouldNotDeleteForeignEntity() {
        // TODO: maybe throw error?
        userRepository.save(john);
        todoRepository.save(foo);
        assertNotNull(todoRepository.findById(String.valueOf(foo.todoId)));
        userRepository.delete(john);
        assertNotNull(todoRepository.findById(String.valueOf(foo.todoId)));
        assertNotNull(userRepository.findById(String.valueOf(john.userId)));
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
