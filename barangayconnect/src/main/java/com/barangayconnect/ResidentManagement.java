package com.barangayconnect;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ResidentManagement {

    public static void showWindow() {

        JFrame frame = new JFrame("Manage Resident Accounts");
        frame.setSize(450, 400);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridLayout(0, 1, 8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        JLabel title = new JLabel("Create Resident Account");
        title.setFont(new Font("Arial", Font.BOLD, 20));

        JTextField nameField = new JTextField();
        JTextField emailField = new JTextField();
        JPasswordField passwordField = new JPasswordField();

        JButton saveButton = new JButton("Create Account");

        panel.add(title);
        panel.add(new JLabel("Resident Full Name:"));
        panel.add(nameField);
        panel.add(new JLabel("Resident Email:"));
        panel.add(emailField);
        panel.add(new JLabel("Temporary Password:"));
        panel.add(passwordField);
        panel.add(saveButton);

        saveButton.addActionListener(e -> {
            String name = nameField.getText().trim();
            String email = emailField.getText().trim();
            String password = new String(passwordField.getPassword());

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(
                        frame, "Please fill in all fields.");
                return;
            }

            String sql = "INSERT INTO users "
                    + "(full_name, email, password_hash, role, "
                    + "account_status, created_at) "
                    + "VALUES (?, ?, ?, 'RESIDENT', 'ACTIVE', NOW())";

            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, name);
                stmt.setString(2, email);
                stmt.setString(3, password);

                int rows = stmt.executeUpdate();

                if (rows > 0) {
                    JOptionPane.showMessageDialog(
                            frame, "Resident account created successfully!");

                    nameField.setText("");
                    emailField.setText("");
                    passwordField.setText("");
                }

            } catch (SQLException ex) {
                if ("23000".equals(ex.getSQLState())) {
                    JOptionPane.showMessageDialog(
                            frame,
                            "This email may already be registered.",
                            "Account Error",
                            JOptionPane.WARNING_MESSAGE);
                } else {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(
                            frame,
                            "Could not create account: " + ex.getMessage(),
                            "Database Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        frame.add(panel);
        frame.setVisible(true);
    }
}