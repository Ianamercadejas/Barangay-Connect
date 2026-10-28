
package com.barangayconnect;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DocumentRequest {

    public static void showForm(JPanel content, int residentId, Runnable backAction) {
        content.removeAll();
        content.setLayout(new BorderLayout(10, 10));

        JPanel top = new JPanel(new BorderLayout());
        JButton backButton = new JButton("Back");
        JLabel title = new JLabel("Request Documents", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 22));

        backButton.addActionListener(e -> backAction.run());
        top.add(backButton, BorderLayout.WEST);
        top.add(title, BorderLayout.CENTER);
        content.add(top, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(15, 25, 15, 25));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 6, 8, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        JComboBox<String> documentType = new JComboBox<>(new String[]{
                "BARANGAY_ID",
                "CERTIFICATE_OF_RESIDENCY",
                "CERTIFICATE_OF_INDIFFIENCY"
        });

        JTextField purposeField = new JTextField(25);
        JLabel photoLabel = new JLabel("No photo selected");
        JButton choosePhotoButton = new JButton("Choose Photo");
        JButton submitButton = new JButton("Submit Request");

        final File[] selectedPhoto = {null};

        int row = 0;
        addRow(form, gbc, row++, "Document Type:", documentType);
        addRow(form, gbc, row++, "Purpose:", purposeField);

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        form.add(new JLabel("Recipient Photo:"), gbc);

        JPanel photoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        photoPanel.add(choosePhotoButton);
        photoPanel.add(photoLabel);

        gbc.gridx = 1;
        gbc.weightx = 1;
        form.add(photoPanel, gbc);

        gbc.gridx = 0;
        gbc.gridy = row + 1;
        gbc.gridwidth = 2;
        form.add(submitButton, gbc);

        choosePhotoButton.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Choose a Photo");
            chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                    "Image Files (JPG, JPEG, PNG)", "jpg", "jpeg", "png"));

            if (chooser.showOpenDialog(content) == JFileChooser.APPROVE_OPTION) {
                selectedPhoto[0] = chooser.getSelectedFile();
                photoLabel.setText(selectedPhoto[0].getName());
            }
        });

        documentType.addActionListener(e -> {
            boolean needsPhoto = "BARANGAY_ID".equals(
                    documentType.getSelectedItem());
            choosePhotoButton.setEnabled(true);
            photoLabel.setText(selectedPhoto[0] == null
                    ? (needsPhoto ? "Photo required for Barangay ID" : "No photo selected")
                    : selectedPhoto[0].getName());
        });

        submitButton.addActionListener(e -> {
            String type = (String) documentType.getSelectedItem();
            String purpose = purposeField.getText().trim();

            if (purpose.isEmpty()) {
                JOptionPane.showMessageDialog(content, "Please enter the purpose.");
                return;
            }

            if ("BARANGAY_ID".equals(type) && selectedPhoto[0] == null) {
                JOptionPane.showMessageDialog(content,
                        "Please select a photo for your Barangay ID request.");
                return;
            }

            if (!isProfileComplete(residentId)) {
                int choice = JOptionPane.showConfirmDialog(
                        content,
                        "Please complete your personal information before requesting a document.\nOpen My Profile now?",
                        "Incomplete Profile",
                        JOptionPane.YES_NO_OPTION
                );

                if (choice == JOptionPane.YES_OPTION) {
                    ResidentPersonalInformation.showWindow(
                            content, residentId, backAction);
                }
                return;
            }

            String savedPhotoPath = null;

            try {
                if (selectedPhoto[0] != null) {
                    File photo = selectedPhoto[0];
                    String fileName = photo.getName().toLowerCase();

                    if (!(fileName.endsWith(".jpg")
                            || fileName.endsWith(".jpeg")
                            || fileName.endsWith(".png"))) {
                        JOptionPane.showMessageDialog(content,
                                "Please choose a JPG, JPEG, or PNG image.");
                        return;
                    }

                    File folder = new File("document_request_photos");
                    if (!folder.exists() && !folder.mkdirs()) {
                        JOptionPane.showMessageDialog(content,
                                "Could not create the photo folder.");
                        return;
                    }

                    String extension = fileName.substring(fileName.lastIndexOf('.'));
                    String timestamp = LocalDateTime.now().format(
                            DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS"));

                    File destination = new File(folder,
                            "resident_" + residentId + "_" + timestamp + extension);

                    Files.copy(photo.toPath(), destination.toPath(),
                            StandardCopyOption.REPLACE_EXISTING);

                    savedPhotoPath = "document_request_photos/"
                            + destination.getName();
                }

                String sql = "INSERT INTO document_requests " +
                        "(resident_id, document_type, purpose, status, recipient_photo_path) " +
                        "VALUES (?, ?, ?, 'PENDING', ?)";

                try (Connection conn = DBConnection.getConnection();
                     PreparedStatement stmt = conn.prepareStatement(sql)) {

                    stmt.setInt(1, residentId);
                    stmt.setString(2, type);
                    stmt.setString(3, purpose);
                    stmt.setString(4, savedPhotoPath);
                    stmt.executeUpdate();
                }

                JOptionPane.showMessageDialog(content,
                        "Document request submitted successfully!");
                backAction.run();

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(content,
                        "Error submitting request: " + ex.getMessage());
            }
        });

        content.add(form, BorderLayout.CENTER);
        content.revalidate();
        content.repaint();
    }

    private static void addRow(JPanel panel, GridBagConstraints gbc,
                               int row, String label, JComponent field) {
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        panel.add(new JLabel(label), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        panel.add(field, gbc);
    }

    private static boolean isProfileComplete(int residentId) {
        String sql = "SELECT birth_date, gender, civil_status, complete_address, " +
                "contact_number, occupation, citizenship " +
                "FROM resident_records WHERE user_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, residentId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    return false;
                }

                String[] fields = {
                        rs.getString("birth_date"),
                        rs.getString("gender"),
                        rs.getString("civil_status"),
                        rs.getString("complete_address"),
                        rs.getString("contact_number"),
                        rs.getString("occupation"),
                        rs.getString("citizenship")
                };

                for (String field : fields) {
                    if (field == null || field.trim().isEmpty()) {
                        return false;
                    }
                }

                return true;
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null,
                    "Error checking profile: " + ex.getMessage());
            return false;
        }
    }
}
