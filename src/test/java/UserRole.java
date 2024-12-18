import org.gabrielgavrilov.macchiato.annotations.*;

@Entity
@Table(name = "user_roles")
public class UserRole {

    @Id
    @Column(name = "user_role_id")
    public int userRoleId;

    @Column(name = "user_id")
    public int userId;

    @Column(name = "role_id")
    public int roleId;

    @OneToOne
    @JoinTable(tableName = "users", columnName = "user_id")
    public User user;

    @OneToOne
    @JoinTable(tableName = "roles", columnName = "role_id")
    public Role role;

    public static UserRole newInstance(int userRoleId, int userId, int roleId) {
        UserRole userRole = new UserRole();
        userRole.userRoleId = userRoleId;
        userRole.userId = userId;
        userRole.roleId = roleId;
        return userRole;
    }

}
