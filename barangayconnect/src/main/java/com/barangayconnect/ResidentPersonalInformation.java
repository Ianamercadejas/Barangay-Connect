
package com.barangayconnect;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;

public class ResidentPersonalInformation {

    public static void showWindow(JPanel content, int userId, Runnable backAction) {
        content.removeAll();
        content.setLayout(new BorderLayout(10, 10));

        JPanel top = new JPanel(new BorderLayout());
        JButton backButton = new JButton("Back");
        JLabel title = new JLabel("My Personal Information", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 22));

        backButton.addActionListener(e -> backAction.run());
        top.add(backButton, BorderLayout.WEST);
        top.add(title, BorderLayout.CENTER);
        content.add(top, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 25));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        JTextField nameField = new JTextField();
        JTextField emailField = new JTextField();
        JTextField birthDateField = new JTextField();
        JComboBox<String> genderBox = new JComboBox<>(
                new String[]{"", "Male", "Female", "Prefer not to say"});
        JComboBox<String> civilStatusBox = new JComboBox<>(
                new String[]{"", "Single", "Married", "Widowed", "Separated", "Other"});
        JTextField addressField = new JTextField();
        JTextField contactField = new JTextField();
        JTextField occupationField = new JTextField();
        JTextField citizenshipField = new JTextField();
        JButton saveButton = new JButton("Save Information");

        nameField.setEditable(false);
        emailField.setEditable(false);

        JComponent[] fields = {
                nameField, emailField, birthDateField, genderBox, civilStatusBox,
                addressField, contactField, occupationField, citizenshipField
        };

        String[] labels = {
                "Full Name:", "Email:", "Birth Date (YYYY-MM-DD):",
                "Gender:", "Civil Status:", "Complete Address:",
                "Contact Number:", "Occupation:", "Citizenship:"
        };

        for (int i = 0; i < labels.length; i++) {
            gbc.gridy = i;
            gbc.gridx = 0;
            gbc.weightx = 0;
            form.add(new JLabel(labels[i]), gbc);

            gbc.gridx = 1;
            gbc.weightx = 1;
            form.add(fields[i], gbc);
        }

        gbc.gridx = 0;
        gbc.gridy = labels.length;
        gbc.gridwidth = 2;
        form.add(saveButton, gbc);

        JScrollPane scrollPane = new JScrollPane(form);
        content.add(scrollPane, BorderLayout.CENTER);

        String loadSql = "SELECT u.full_name, u.email, r.birth_date, r.gender, " +
                "r.civil_status, r.complete_address, r.contact_number, " +
                "r.occupation, r.citizenship " +
                "FROM users u LEFT JOIN resident_records r ON u.user_id = r.user_id " +
                "WHERE u.user_id = ? AND u.role = 'RESIDENT'";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(loadSql)) {

            stmt.setInt(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    nameField.setText(valueOrEmpty(rs.getString("full_name")));
                    emailField.setText(valueOrEmpty(rs.getString("email")));

                    Date birthDate = rs.getDate("birth_date");
                    birthDateField.setText(birthDate == null
                            ? "" : birthDate.toString());

                    genderBox.setSelectedItem(valueOrEmpty(rs.getString("gender")));
                    civilStatusBox.setSelectedItem(
                            valueOrEmpty(rs.getString("civil_status")));
                    addressField.setText(valueOrEmpty(rs.getString("complete_address")));
                    contactField.setText(valueOrEmpty(rs.getString("contact_number")));
                    occupationField.setText(valueOrEmpty(rs.getString("occupation")));
                    citizenshipField.setText(valueOrEmpty(rs.getString("citizenship")));
                } else {
                    JOptionPane.showMessageDialog(content,
                            "Resident account information was not found.");
                }
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(content,
                    "Error loading profile: " + ex.getMessage());
        }

        saveButton.addActionListener(e -> {
            String birthDateText = birthDateField.getText().trim();

            Date birthDate = null;

            if (!birthDateText.isEmpty()) {
                try {
                    birthDate = Date.valueOf(LocalDate.parse(birthDateText));
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(content,
                            "Invalid birth date. Use YYYY-MM-DD, for example 2005-04-25.");
                    return;
                }
            }

            String saveSql = "INSERT INTO resident_records " +
                    "(user_id, birth_date, gender, civil_status, complete_address, " +
                    "contact_number, occupation, citizenship) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE birth_date = VALUES(birth_date), " +
                    "gender = VALUES(gender), civil_status = VALUES(civil_status), " +
                    "complete_address = VALUES(complete_address), " +
                    "contact_number = VALUES(contact_number), " +
                    "occupation = VALUES(occupation), citizenship = VALUES(citizenship)";

            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(saveSql)) {

                stmt.setInt(1, userId);

                if (birthDate == null) {
                    stmt.setNull(2, Types.DATE);
                } else {
                    stmt.setDate(2, birthDate);
                }

                stmt.setString(3, emptyToNull((String) genderBox.getSelectedItem()));
                stmt.setString(4, emptyToNull((String) civilStatusBox.getSelectedItem()));
                stmt.setString(5, emptyToNull(addressField.getText()));
                stmt.setString(6, emptyToNull(contactField.getText()));
                stmt.setString(7, emptyToNull(occupationField.getText()));
                stmt.setString(8, emptyToNull(citizenshipField.getText()));

                stmt.executeUpdate();

                JOptionPane.showMessageDialog(content,
                        "Personal information saved successfully.");

            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(content,
                        "Error saving profile: " + ex.getMessage());
            }
        });

        content.revalidate();
        content.repaint();
    }

    private static String valueOrEmpty(String value) {
        return value == null ? "" : value;
    }

    private static String emptyToNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }
}
