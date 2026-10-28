
package com.barangayconnect;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;

public class App {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(App::showLoginWindow);
    }

    public static void showLoginWindow() {
        JFrame frame = new JFrame("BarangayConnect Login");
        frame.setSize(450, 350);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(240, 248, 255));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel(
                "BarangayConnect", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 25));
        title.setForeground(new Color(20, 75, 130));

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(title, gbc);

        gbc.gridy = 1;
        panel.add(new JLabel(
                "Login to your account", SwingConstants.CENTER), gbc);

        gbc.gridwidth = 1;
        gbc.gridy = 2;
        gbc.gridx = 0;
        panel.add(new JLabel("Email:"), gbc);

        gbc.gridx = 1;
        JTextField emailField = new JTextField(18);
        panel.add(emailField, gbc);

        gbc.gridy = 3;
        gbc.gridx = 0;
        panel.add(new JLabel("Password:"), gbc);

        gbc.gridx = 1;
        JPasswordField passwordField = new JPasswordField(18);
        panel.add(passwordField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;

        JButton loginButton = new JButton("Login");
        panel.add(loginButton, gbc);

        loginButton.addActionListener(e -> {
            String email = emailField.getText().trim();
            char[] chars = passwordField.getPassword();

            if (email.isEmpty() || chars.length == 0) {
                Arrays.fill(chars, '\0');
                JOptionPane.showMessageDialog(
                        frame,
                        "Please enter your email and password.");
                return;
            }

            String password = new String(chars);
            Arrays.fill(chars, '\0');

            String result = LoginService.login(email, password);
            password = null;

            if (!result.startsWith("LOGIN_SUCCESS:")) {
                JOptionPane.showMessageDialog(
                        frame,
                        result,
                        "Login",
                        JOptionPane.WARNING_MESSAGE);

                passwordField.setText("");
                return;
            }

            String[] parts = result.split(":", 5);

            if (parts.length < 4) {
                JOptionPane.showMessageDialog(
                        frame, "Invalid login response.");
                return;
            }

            String role = parts[1];
            String name = parts[2];
            int userId;

            try {
                userId = Integer.parseInt(parts[3]);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(
                        frame, "Invalid account information.");
                return;
            }

            boolean mustChangePassword =
                    parts.length > 4 && "1".equals(parts[4]);

            if (mustChangePassword) {
                boolean changed = ChangePassword.showWindow(userId);

                if (!changed) {
                    JOptionPane.showMessageDialog(
                            frame,
                            "Please change your temporary password "
                                    + "to continue.");
                    return;
                }
            }

            if ("RESIDENT".equalsIgnoreCase(role)) {
                frame.dispose();
                ResidentDashboard.showDashboard(name, role, userId);

            } else if ("OFFICIAL".equalsIgnoreCase(role)) {
                frame.dispose();
                Dashboard.showDashboard(name, role, userId);

            } else {
                JOptionPane.showMessageDialog(
                        frame, "Unknown account role.");
            }
        });

        frame.add(panel);
        frame.setVisible(true);
    }
}
