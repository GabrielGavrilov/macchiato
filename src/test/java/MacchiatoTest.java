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

        User user = User.newInstance(1, "John", "Doe");
        Role role = Role.newInstance(1, "ADMIN");
        UserRole userRole = UserRole.newInstance(1, 1, 1);

//        userRepository.save(user);
//        roleRepository.save(role);
//        userRoleRepository.save(userRole);

        UserRole findUserRole = userRoleRepository.findById(Integer.toString(1));

        System.out.println(String.format("%s %s", findUserRole.user.firstName, findUserRole.user.lastName));

    }
}
