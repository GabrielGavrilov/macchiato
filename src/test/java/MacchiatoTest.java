import org.gabrielgavrilov.macchiato.DataSource;
import org.gabrielgavrilov.macchiato.Macchiato;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MacchiatoTest {
    public static void main(String[] args) {

        UserRepository userRepository = new UserRepository();
        RoleRepository roleRepository = new RoleRepository();
        UserRoleRepository userRoleRepository = new UserRoleRepository();


        User user = User.newInstance(3, "Mark", "Fishbach");
        Role role = Role.newInstance(1, "ADMIN");
        UserRole userRole = UserRole.newInstance(1, 1, 1);

        userRepository.deleteById(Integer.toString(2));
        userRepository.save(user);

        UserRole r = userRoleRepository.findById(Integer.toString(1));

        System.out.println(r.user.firstName + " " + r.user.lastName + " has role " + r.role.roleName);

    }
}
