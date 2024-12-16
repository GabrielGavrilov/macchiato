import org.gabrielgavrilov.macchiato.Controller;
import org.gabrielgavrilov.macchiato.DataSource;
import org.gabrielgavrilov.macchiato.Macchiato;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MacchiatoTest {
    public static void main(String[] args) {

        UserRepository userRepository = new UserRepository();
        UserClassRepository userClassRepository = new UserClassRepository();

        userClassRepository.findById(Integer.toString(1));


    }
}
