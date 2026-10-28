
package com.barangayconnect;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ChangePassword {

    public static boolean showWindow(int userId) {

        JPasswordField newPasswordField = new JPasswordField(18);
        JPasswordField confirmPasswordField = new JPasswordField(18);

        JPanel panel = new JPanel(new GridLayout(0, 1, 8, 8));
        panel.setBorder(
                BorderFactory.createEmptyBorder(15, 20, 15, 20));

        panel.add(new JLabel("Enter New Password:"));
        panel.add(newPasswordField);
        panel.add(new JLabel("Confirm New Password:"));
        panel.add(confirmPasswordField);

        while (true) {

            int choice = JOptionPane.showConfirmDialog(
                    null,
                    panel,
                    "Change Your Temporary Password",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE
            );

            if (choice != JOptionPane.OK_OPTION) {
                return false;
            }

            char[] newPasswordChars = newPasswordField.getPassword();
            char[] confirmPasswordChars =
                    confirmPasswordField.getPassword();

            String newPassword = new String(newPasswordChars);
            String confirmPassword = new String(confirmPasswordChars);

            if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
                JOptionPane.showMessageDialog(
                        null,
                        "Please fill in both password fields.");

                newPasswordField.setText("");
                confirmPasswordField.setText("");
                continue;
            }

            if (newPassword.length() < 8) {
                JOptionPane.showMessageDialog(
                        null,
                        "Your new password must be at least 8 characters.");

                newPasswordField.setText("");
                confirmPasswordField.setText("");
                continue;
            }

            if (!newPassword.equals(confirmPassword)) {
                JOptionPane.showMessageDialog(
                        null,
                        "The passwords do not match.");

                confirmPasswordField.setText("");
                continue;
            }

            // Hash the new password before saving it.
            String hashedPassword =
                    PasswordUtil.hashPassword(newPassword);

            String sql = "UPDATE users "
                    + "SET password_hash = ?, must_change_password = 0 "
                    + "WHERE user_id = ?";

            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, hashedPassword);
                stmt.setInt(2, userId);

                if (stmt.executeUpdate() == 1) {
                    JOptionPane.showMessageDialog(
                            null,
                            "Password changed successfully!");

                    return true;
                }

                JOptionPane.showMessageDialog(
                        null,
                        "Account not found. Please contact an official.");

                return false;

            } catch (SQLException | RuntimeException ex) {
                ex.printStackTrace();

                JOptionPane.showMessageDialog(
                        null,
                        "Could not change password. Please try again.",
                        "Password Error",
                        JOptionPane.ERROR_MESSAGE);

                return false;

            } finally {
                java.util.Arrays.fill(newPasswordChars, '\0');
                java.util.Arrays.fill(confirmPasswordChars, '\0');
            }
        }
    }
}
