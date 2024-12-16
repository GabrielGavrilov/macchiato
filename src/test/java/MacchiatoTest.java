import org.gabrielgavrilov.macchiato.Controller;
import org.gabrielgavrilov.macchiato.DataSource;
import org.gabrielgavrilov.macchiato.Macchiato;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MacchiatoTest {
    public static void main(String[] args) {

        UserRepository userRepository = new UserRepository();

        User user1 = User.newInstance(2, "Mark", "Fishbach");

        userRepository.save(user1);

        List<User> users = userRepository.getAll();

        for(User user : users) {
            System.out.println(user.id + " " + user.firstName);
        }


    }
}
