import org.gabrielgavrilov.macchiato.Controller;
import org.gabrielgavrilov.macchiato.Macchiato;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MacchiatoTest {
    public static void main(String[] args) {

        UserRepository userRepository = new UserRepository();

        User user = User.newInstance("John", "Doe");
        User user2 = User.newInstance("Jane", "Doe");

        userRepository.save(user);
        userRepository.save(user2);

    }
}
