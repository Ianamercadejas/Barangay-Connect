package com.barangayconnect;

import javax.swing.*;
import java.awt.*;

public class App {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
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
                "BarangayConnect", SwingConstants.CENTER
            );
            title.setFont(new Font("Arial", Font.BOLD, 25));
            title.setForeground(new Color(20, 75, 130));

            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.gridwidth = 2;
            panel.add(title, gbc);

            gbc.gridy = 1;
            JLabel subtitle = new JLabel(
                "Login to your account", SwingConstants.CENTER
            );
            panel.add(subtitle, gbc);

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
                String password =
                    new String(passwordField.getPassword());

                if (email.isEmpty() || password.isEmpty()) {
                    JOptionPane.showMessageDialog(
                        frame, "Please enter your email and password."
                    );
                    return;
                }

                String result = LoginService.login(email, password);

                if (result.startsWith("LOGIN_SUCCESS:")) {
                    String[] parts = result.split(":", 3);
                    String role = parts[1];
                    String name = parts[2];

                  frame.dispose();

if ("OFFICIAL".equalsIgnoreCase(role)) {
    Dashboard.showDashboard(name, role);
} else if ("RESIDENT".equalsIgnoreCase(role)) {
    ResidentDashboard.showDashboard(name, role);
} else {
    JOptionPane.showMessageDialog(
            null,
            "Unknown account role. Please contact the administrator."
    );
}
                } else {
                    JOptionPane.showMessageDialog(
                        frame, result, "Login",
                        JOptionPane.WARNING_MESSAGE
                    );
                }
            });

            frame.add(panel);
            frame.setVisible(true);
        });
    }
}