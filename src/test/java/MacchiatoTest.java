import org.gabrielgavrilov.macchiato.DataSource;
import org.gabrielgavrilov.macchiato.Macchiato;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MacchiatoTest {
    public static void main(String[] args) {

        UserRepository userRepository = new UserRepository();

        User savedUser = User.newInstance(3, "Mark", "Fishbach");
        userRepository.save(savedUser);



        User user = userRepository.findById(Integer.toString(3));
//
        System.out.println(String.format("(%s, %s, %s)", user.id, user.firstName, user.lastName));

    }
}
