
package com.barangayconnect;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.Arrays;

public class ResidentManagement {

    public static void showWindow() {
        JFrame frame = new JFrame("Manage Accounts");
        frame.setSize(460, 520);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLocationRelativeTo(null);

        JPanel content = new JPanel(new BorderLayout());
        showWindow(content, () -> frame.dispose());

        frame.add(content);
        frame.setVisible(true);
    }

    public static void showWindow(JPanel content, Runnable backAction) {
        content.removeAll();
        content.setLayout(new BorderLayout());

        JPanel panel = new JPanel(new GridLayout(0, 1, 8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));
        panel.setBackground(new Color(245, 247, 246));

        JLabel title = new JLabel("Manage Accounts");
        title.setFont(new Font("Arial", Font.BOLD, 22));

        JTextField nameField = new JTextField();
        JTextField emailField = new JTextField();
        JPasswordField passwordField = new JPasswordField();

        JComboBox<String> roleBox = new JComboBox<>(
                new String[]{"RESIDENT", "OFFICIAL"});

        JButton saveButton = new JButton("Create Account");
        JButton editButton = new JButton("Edit Resident Information");
        JButton deactivateButton = new JButton("Deactivate Resident Account");
        JButton reactivateButton = new JButton("Reactivate Resident Account");
        JButton backButton = new JButton("Back to Dashboard");

        panel.add(title);
        panel.add(new JLabel("Full Name:"));
        panel.add(nameField);
        panel.add(new JLabel("Email:"));
        panel.add(emailField);
        panel.add(new JLabel("Role:"));
        panel.add(roleBox);
        panel.add(new JLabel("Temporary Password (minimum 8 characters):"));
        panel.add(passwordField);
        panel.add(saveButton);
        panel.add(editButton);
        panel.add(deactivateButton);
        panel.add(reactivateButton);
        panel.add(backButton);

        saveButton.addActionListener(e -> {
            String name = nameField.getText().trim();
            String email = emailField.getText().trim();
            String role = (String) roleBox.getSelectedItem();
            char[] passwordChars = passwordField.getPassword();

            try {
                String password = new String(passwordChars);

                if (name.isEmpty() || email.isEmpty()
                        || password.isEmpty() || role == null) {
                    JOptionPane.showMessageDialog(
                            content, "Please fill in all fields.");
                    return;
                }

                if (password.length() < 8) {
                    JOptionPane.showMessageDialog(
                            content,
                            "Temporary password must be at least 8 characters.");
                    return;
                }

                String hashedPassword = PasswordUtil.hashPassword(password);

                String sql = "INSERT INTO users "
                        + "(full_name, email, password_hash, role, "
                        + "account_status, must_change_password, created_at) "
                        + "VALUES (?, ?, ?, ?, 'ACTIVE', 1, NOW())";

                try (Connection conn = DBConnection.getConnection();
                     PreparedStatement stmt = conn.prepareStatement(sql)) {

                    stmt.setString(1, name);
                    stmt.setString(2, email);
                    stmt.setString(3, hashedPassword);
                    stmt.setString(4, role);

                    int rows = stmt.executeUpdate();

                    if (rows > 0) {
                        JOptionPane.showMessageDialog(
                                content,
                                role + " account created successfully!\n"
                                + "The user must change the temporary password "
                                + "on first login.");

                        nameField.setText("");
                        emailField.setText("");
                        passwordField.setText("");
                        roleBox.setSelectedIndex(0);
                    }

                } catch (SQLException ex) {
                    if ("23000".equals(ex.getSQLState())) {
                        JOptionPane.showMessageDialog(
                                content,
                                "This email may already be registered.",
                                "Account Error",
                                JOptionPane.WARNING_MESSAGE);
                    } else {
                        ex.printStackTrace();
                        showError(content, "Could not create account: "
                                + ex.getMessage());
                    }
                }

            } catch (RuntimeException ex) {
                ex.printStackTrace();
                showError(content,
                        "Could not securely process the password.");
            } finally {
                Arrays.fill(passwordChars, '\0');
            }
        });

        editButton.addActionListener(e -> editResident(content));
        deactivateButton.addActionListener(
                e -> changeResidentStatus(content, false));
        reactivateButton.addActionListener(
                e -> changeResidentStatus(content, true));
        backButton.addActionListener(e -> backAction.run());

        content.add(panel, BorderLayout.CENTER);
        content.revalidate();
        content.repaint();
    }

    private static void editResident(Component parent) {
        String email = JOptionPane.showInputDialog(
                parent, "Enter the current email of the resident:");

        if (email == null || email.trim().isEmpty()) return;
        email = email.trim();

        String findSql = "SELECT user_id, full_name, email FROM users "
                + "WHERE email = ? AND role = 'RESIDENT'";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement find = conn.prepareStatement(findSql)) {

            find.setString(1, email);

            try (ResultSet rs = find.executeQuery()) {
                if (!rs.next()) {
                    JOptionPane.showMessageDialog(
                            parent, "Resident account not found.");
                    return;
                }

                int userId = rs.getInt("user_id");
                JTextField name = new JTextField(rs.getString("full_name"));
                JTextField newEmail = new JTextField(rs.getString("email"));

                JPanel form = new JPanel(new GridLayout(0, 1, 5, 5));
                form.add(new JLabel("Full Name:"));
                form.add(name);
                form.add(new JLabel("Email:"));
                form.add(newEmail);

                int choice = JOptionPane.showConfirmDialog(
                        parent, form, "Edit Resident Information",
                        JOptionPane.OK_CANCEL_OPTION);

                if (choice != JOptionPane.OK_OPTION) return;

                if (name.getText().trim().isEmpty()
                        || newEmail.getText().trim().isEmpty()) {
                    JOptionPane.showMessageDialog(
                            parent, "Name and email cannot be empty.");
                    return;
                }

                String sql = "UPDATE users SET full_name = ?, email = ? "
                        + "WHERE user_id = ? AND role = 'RESIDENT'";

                try (PreparedStatement update = conn.prepareStatement(sql)) {
                    update.setString(1, name.getText().trim());
                    update.setString(2, newEmail.getText().trim());
                    update.setInt(3, userId);

                    if (update.executeUpdate() > 0) {
                        JOptionPane.showMessageDialog(
                                parent, "Resident information updated!");
                    }
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            if ("23000".equals(ex.getSQLState())) {
                showError(parent, "That email is already used.");
            } else {
                showError(parent,
                        "Could not edit resident: " + ex.getMessage());
            }
        }
    }

    private static void changeResidentStatus(
            Component parent, boolean reactivate) {

        String email = JOptionPane.showInputDialog(
                parent,
                reactivate
                        ? "Enter the email of the inactive resident:"
                        : "Enter the email of the resident to deactivate:");

        if (email == null || email.trim().isEmpty()) return;
        email = email.trim();

        try (Connection conn = DBConnection.getConnection()) {
            String findSql = "SELECT user_id, full_name, account_status "
                    + "FROM users WHERE email = ? AND role = 'RESIDENT'";

            try (PreparedStatement find = conn.prepareStatement(findSql)) {
                find.setString(1, email);

                try (ResultSet rs = find.executeQuery()) {
                    if (!rs.next()) {
                        JOptionPane.showMessageDialog(
                                parent, "Resident account not found.");
                        return;
                    }

                    int userId = rs.getInt("user_id");
                    String name = rs.getString("full_name");
                    String status = rs.getString("account_status");

                    if (reactivate && !"INACTIVE".equalsIgnoreCase(status)) {
                        JOptionPane.showMessageDialog(
                                parent,
                                "This resident account is not inactive.");
                        return;
                    }

                    if (!reactivate && "INACTIVE".equalsIgnoreCase(status)) {
                        JOptionPane.showMessageDialog(
                                parent,
                                "This account is already inactive.");
                        return;
                    }

                    String action = reactivate ? "reactivate" : "deactivate";
                    int choice = JOptionPane.showConfirmDialog(
                            parent,
                            "Are you sure you want to " + action
                                    + " the account for " + name + "?",
                            "Confirm " + action,
                            JOptionPane.YES_NO_OPTION);

                    if (choice != JOptionPane.YES_OPTION) return;

                    String newStatus = reactivate ? "ACTIVE" : "INACTIVE";
                    String updateSql = "UPDATE users SET account_status = ? "
                            + "WHERE user_id = ? AND role = 'RESIDENT'";

                    try (PreparedStatement update =
                                 conn.prepareStatement(updateSql)) {
                        update.setString(1, newStatus);
                        update.setInt(2, userId);

                        JOptionPane.showMessageDialog(
                                parent,
                                update.executeUpdate() > 0
                                        ? "Resident account " + newStatus + "."
                                        : "No account was changed.");
                    }
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            showError(parent, "Could not change account status: "
                    + ex.getMessage());
        }
    }

    private static void showError(Component parent, String message) {
        JOptionPane.showMessageDialog(
                parent, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
