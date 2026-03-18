import core.entities.Todo;
import core.entities.User;
import core.repositories.TodoRepository;
import core.repositories.UserRepository;
import org.gabrielgavrilov.macchiato.Macchiato;
import org.gabrielgavrilov.macchiato.MacchiatoQueryExecutor;
import org.gabrielgavrilov.macchiato.exceptions.MacchiatoConstraintViolationException;
import org.gabrielgavrilov.macchiato.exceptions.MacchiatoEntityDoesNotExistException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.*;
import java.util.List;
import java.util.Optional;
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
        Macchiato.initialize( "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1");

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
        Optional<User> user = userRepository.save(john);
        assertTrue(user.isPresent());
        assertAll(
                () -> assertEquals(john.userId, user.get().userId),
                () -> assertEquals(john.email, user.get().email)
        );
    }

    @Test
    public void testMacchiatoRepository_save_withJoinColumn_shouldReturnSavedEntity() {
        userRepository.save(john);
        Optional<Todo> entity = todoRepository.save(foo);
        assertTrue(entity.isPresent());
        assertAll(
                () -> assertEquals(foo.todoId, entity.get().todoId),
                () -> assertEquals(foo.userId, entity.get().userId),
                () -> assertEquals(foo.title, entity.get().title),
                () -> assertEquals(john.userId, entity.get().user.userId),
                () -> assertEquals(john.email, entity.get().user.email)
        );
    }

    @Test
    public void testMacchiatoRepository_getAll_shouldReturnAListOfAllEntities() {
        userRepository.save(john);
        userRepository.save(jane);

        List<User> users = userRepository.findAll();

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

        List<Todo> entities = todoRepository.findAll();
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
        List<User> users = userRepository.findAll();
        assertEquals(0, users.size());
    }

    @Test
    public void testMacchiatoRepository_findById_shouldReturnEntity() {
        userRepository.save(john);
        Optional<User> entity = userRepository.findById(String.valueOf(john.userId));
        assertTrue(entity.isPresent());
        assertAll(
                () -> assertEquals(john.userId, entity.get().userId),
                () -> assertEquals(john.email, entity.get().email)
        );
    }

    @Test
    public void testMacchiatoRepository_findById_withJoinColumn_shouldReturnEntity() {
        userRepository.save(john);
        todoRepository.save(foo);
        Optional<Todo> entity = todoRepository.findById(String.valueOf(foo.todoId));
        assertTrue(entity.isPresent());
        assertAll(
                () -> assertEquals(foo.todoId, entity.get().todoId),
                () -> assertEquals(foo.userId, entity.get().userId),
                () -> assertEquals(foo.title, entity.get().title),
                () -> assertEquals(john.userId, entity.get().user.userId),
                () -> assertEquals(john.email, entity.get().user.email)
        );
    }

    @Test
    public void testMacchiatoRepository_findById_withNoEntity_shouldReturnEmptyOptionalEntity() {
        Optional<User> entity = userRepository.findById(String.valueOf(new UUID(0,1)));
        assertFalse(entity.isPresent());
    }

    @Test
    public void testMacchiatoRepository_update_shouldReturnUpdatedEntity() {
        userRepository.save(john);

        Optional<User> oldEntity = userRepository.findById(String.valueOf(john.userId));
        User updatedEntity = john.setEmail("johndoe1@gmail.com");
        assertTrue(oldEntity.isPresent());

        Optional<User> entity = userRepository.update(updatedEntity);
        assertTrue(entity.isPresent());

        assertAll(
                () -> assertEquals(john.userId, entity.get().userId),
                () -> assertEquals(updatedEntity.email, entity.get().email)
        );
    }

    @Test
    public void testMacchiatoRepository_update_withJoinColumn_shouldReturnUpdatedEntity() {
        userRepository.save(john);
        todoRepository.save(foo);

        Optional<Todo> oldEntity = todoRepository.findById(String.valueOf(foo.todoId));
        Todo updatedEntity = foo.setTitle("baz");
        assertNotNull(oldEntity);

        Optional<Todo> entity = todoRepository.update(updatedEntity);
        assertTrue(entity.isPresent());

        assertAll(
                () -> assertEquals(foo.todoId, entity.get().todoId),
                () -> assertEquals(foo.userId, entity.get().userId),
                () -> assertEquals(updatedEntity.title, entity.get().title),
                () -> assertEquals(john.userId, entity.get().user.userId),
                () -> assertEquals(john.email, entity.get().user.email)
        );
    }

    @Test
    public void testMacchiatoRepository_update_withNoEntity_shouldThrowRuntimeException() {
        assertThrows(MacchiatoEntityDoesNotExistException.class, () -> userRepository.update(john));
//        assertFalse(userRepository.update(john).isPresent());
    }

    @Test
    public void testMacchiatoRepository_delete_shouldDeleteEntity() {
        userRepository.save(john);
        Optional<User> entity = userRepository.findById(String.valueOf(john.userId));
        assertNotNull(entity);
        userRepository.delete(john);
        entity = userRepository.findById(String.valueOf(john.userId));
        assertTrue(entity.isEmpty());
    }

    @Test
    public void testMacchiatoRepository_delete_withJoinColumn_shouldDeleteEntity() {
        userRepository.save(john);
        todoRepository.save(foo);
        assertTrue(todoRepository.findById(String.valueOf(foo.todoId)).isPresent());
        todoRepository.delete(foo);
        assertFalse(todoRepository.findById(String.valueOf(foo.todoId)).isPresent());
    }

    @Test
    public void testMacchiatoRepository_delete_withJoinColumn_deleteForeignEntity_shouldNotDeleteForeignEntity() {
        userRepository.save(john);
        todoRepository.save(foo);
        assertTrue(todoRepository.findById(String.valueOf(foo.todoId)).isPresent());
        assertThrows(MacchiatoConstraintViolationException.class, () -> userRepository.delete(john));
        assertTrue(todoRepository.findById(String.valueOf(foo.todoId)).isPresent());
    }

    private void execute(String query) {
        new MacchiatoQueryExecutor(Macchiato.getDriverManager()).execute(query);
    }


}
