package com.barangayconnect;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DocumentRequest {

    public static void showForm(int residentId) {
        JFrame frame = new JFrame("Request Barangay Document");
        frame.setSize(450, 350);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel(new GridLayout(4, 2, 10, 10));
        panel.setBorder(
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        );

        JLabel documentLabel = new JLabel("Document Type:");
        String[] documents = {
                "BARANGAY_ID",
                "CERTIFICATE_OF_RESIDENCY",
                "CERTIFICATE_OF_INDIFFENCY"
        };
        JComboBox<String> documentBox = new JComboBox<>(documents);

        JLabel purposeLabel = new JLabel("Purpose:");
        JTextField purposeField = new JTextField();

        JButton submitButton = new JButton("Submit Request");

        panel.add(documentLabel);
        panel.add(documentBox);
        panel.add(purposeLabel);
        panel.add(purposeField);
        panel.add(new JLabel(""));
        panel.add(new JLabel(""));
        panel.add(new JLabel(""));
        panel.add(submitButton);

        submitButton.addActionListener(e -> {
            String documentType =
                    (String) documentBox.getSelectedItem();
            String purpose = purposeField.getText().trim();

            if (purpose.isEmpty()) {
                JOptionPane.showMessageDialog(
                        frame,
                        "Please enter the purpose of your request."
                );
                return;
            }

            String sql = "INSERT INTO document_requests "
                    + "(resident_id, document_type, purpose, status) "
                    + "VALUES (?, ?, ?, 'PENDING')";

            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement stmt =
                         conn.prepareStatement(sql)) {

                stmt.setInt(1, residentId);
                stmt.setString(2, documentType);
                stmt.setString(3, purpose);

                stmt.executeUpdate();

                JOptionPane.showMessageDialog(
                        frame,
                        "Document request submitted successfully!"
                );

                frame.dispose();

            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(
                        frame,
                        "Unable to submit request:\n"
                                + ex.getMessage(),
                        "Database Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        });

        frame.add(panel);
        frame.setVisible(true);
    }

}