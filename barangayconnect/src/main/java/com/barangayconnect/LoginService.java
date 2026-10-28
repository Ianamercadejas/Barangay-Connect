
package com.barangayconnect;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginService {

    public static String login(String email, String password) {
        String sql = "SELECT user_id, full_name, password_hash, role, "
                + "account_status, must_change_password "
                + "FROM users WHERE email = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);

            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    return "Invalid email or password.";
                }

                int userId = rs.getInt("user_id");
                String fullName = rs.getString("full_name");
                String storedPassword = rs.getString("password_hash");
                String role = rs.getString("role");
                String status = rs.getString("account_status");
                boolean mustChangePassword =
                        rs.getInt("must_change_password") == 1;

                if (storedPassword == null || password == null) {
                    return "Invalid email or password.";
                }

                boolean matches;

                if (storedPassword.startsWith("PBKDF2$")) {
                    matches = PasswordUtil.verifyPassword(
                            password, storedPassword);
                } else {
                    matches = storedPassword.equals(password);

                    if (matches) {
                        String hashed = PasswordUtil.hashPassword(password);
                        String updateSql = "UPDATE users SET password_hash = ? "
                                + "WHERE user_id = ?";

                        try (PreparedStatement update =
                                     conn.prepareStatement(updateSql)) {
                            update.setString(1, hashed);
                            update.setInt(2, userId);
                            update.executeUpdate();
                        }
                    }
                }

                if (!matches) {
                    return "Invalid email or password.";
                }

                if (!"ACTIVE".equalsIgnoreCase(status)
                        && !"VERIFIED".equalsIgnoreCase(status)) {
                    return "Your account is not verified or active yet.";
                }

                if ("RESIDENT".equalsIgnoreCase(role)) {
                    return "LOGIN_SUCCESS:RESIDENT:"
                            + fullName + ":" + userId + ":"
                            + (mustChangePassword ? "1" : "0");
                }

                if ("OFFICIAL".equalsIgnoreCase(role)) {
    return "LOGIN_SUCCESS:OFFICIAL:"
            + fullName + ":" + userId + ":"
            + (mustChangePassword ? "1" : "0");
}

                return "This account has an unrecognized role.";
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "Database error: " + e.getMessage();
        } catch (RuntimeException e) {
            e.printStackTrace();
            return "Unable to verify password securely.";
        }
    }
}
