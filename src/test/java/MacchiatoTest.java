import org.gabrielgavrilov.macchiato.DataSource;
import org.gabrielgavrilov.macchiato.Macchiato;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MacchiatoTest {
    public static void main(String[] args) {

        Macchiato.DATABASE = "jdbc:sqlite:test.db";

        UserRepository userRepository = new UserRepository();
        UserClassRepository classRepository = new UserClassRepository();

//        User savedUser = User.newInstance(3, "Mark", "Fishbach");
//        userRepository.save(savedUser);


        List<User> users = classRepository.findById(Integer.toString(1)).users;

        users.forEach(u -> {
            System.out.println(String.format("(%s, %s, %s, %s)", u.userId, u.firstName, u.lastName, u.classId));
        });

    }
}
