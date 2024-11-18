import org.gabrielgavrilov.macchiato.Controller;
import org.gabrielgavrilov.macchiato.Macchiato;

import java.sql.*;
import java.util.List;

public class MacchiatoTest {
    public static void main(String[] args) {

        UserRepository userRepository = new UserRepository();

        User Steve = User.newInstance("Steve", "Smith");

        userRepository.save(Steve);

        List<User> users = userRepository.getAll();

        for(User user : users) {
            System.out.println(user.firstName);
        }


//        try(
//                Connection conn = DriverManager.getConnection("jdbc:sqlite:test.db");
//                Statement statement = conn.createStatement();
//        ) {
//            statement.executeUpdate("INSERT INTO users(first_name, last_name) VALUES('John', 'Doe')");
//            statement.executeUpdate("INSERT INTO users(first_name, last_name) VALUES('Jane', 'Doe')");
//            ResultSet rs = statement.executeQuery("SELECT * FROM users");
//
//            while(rs.next()) {
//                System.out.println(rs.getString("first_name"));
//            }
//
//        }
//        catch(SQLException e) {
//            e.printStackTrace();
//        }


    }
}
