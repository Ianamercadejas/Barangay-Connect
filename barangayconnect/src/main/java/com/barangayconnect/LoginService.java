package com.barangayconnect;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginService {

    public static String login(String email, String password) {
        String sql = "SELECT user_id, full_name, password_hash, role, account_status "
                   + "FROM users WHERE email = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);

            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    return "Invalid email or password.";
                }

                String storedPassword = rs.getString("password_hash");
                String role = rs.getString("role");
                String status = rs.getString("account_status");

                // For this first test, passwords must match the stored value.
                // Hashed passwords need a proper password-verification method.
                if (storedPassword == null ||
                    !storedPassword.equals(password)) {
                    return "Invalid email or password.";
                }

                if (!"VERIFIED".equalsIgnoreCase(status)
                        && !"ACTIVE".equalsIgnoreCase(status)) {
                    return "Your account is not verified or active yet.";
                }

                if ("RESIDENT".equalsIgnoreCase(role)) {
                    return "LOGIN_SUCCESS:RESIDENT:" 
                    + rs.getString("full_name") + ":" 
                    + rs.getInt("user_id");
                }

                if ("OFFICIAL".equalsIgnoreCase(role)) {
                    return "LOGIN_SUCCESS:OFFICIAL:" 
                    + rs.getString("full_name") + ":" 
                    + rs.getInt("user_id");
                }

                return "This account has an unrecognized role.";
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return "Database error: " + e.getMessage();
        }
    }
}