package atc;

import java.sql.SQLException;

public class UserDAO {
    /** Returns the user's role, or null if credentials are wrong. Password is hashed in SQL (SHA-256). */
    public static String login(String username, String password) throws SQLException {
        Object role = DBConnection.scalar(
                "SELECT role FROM users WHERE username=? AND password_hash=SHA2(?,256)", username, password);
        return role == null ? null : role.toString();
    }
}
