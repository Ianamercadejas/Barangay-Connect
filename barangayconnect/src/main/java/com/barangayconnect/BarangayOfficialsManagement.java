
package com.barangayconnect;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.*;

public class BarangayOfficialsManagement {

    private static String selectedPhotoPath = "";

    // Standalone window for compatibility with older callers
    public static void showWindow() {
        JFrame frame = new JFrame("Manage Barangay Officials");
        frame.setSize(1000, 550);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLocationRelativeTo(null);

        JPanel content = new JPanel(new BorderLayout());
        frame.setContentPane(content);

        showWindow(content, frame::dispose);

        frame.setVisible(true);
    }

    // Display inside the existing Official Dashboard window
    public static void showWindow(JPanel content, Runnable backAction) {
        content.removeAll();
        content.setLayout(new BorderLayout(10, 10));

        JLabel title = new JLabel(
                "Manage Barangay Officials",
                SwingConstants.CENTER
        );
        title.setFont(new Font("Arial", Font.BOLD, 22));
        title.setBorder(BorderFactory.createEmptyBorder(15, 10, 10, 10));

        String[] columns = {
                "ID", "Full Name", "Position",
                "Contact Number", "Photo Path"
        };

        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(model);
        table.setRowHeight(28);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JButton addButton = new JButton("Add Official");
        JButton editButton = new JButton("Edit Official");
        JButton deleteButton = new JButton("Delete Official");
        JButton refreshButton = new JButton("Refresh");
        JButton backButton = new JButton("Back to Dashboard");

        JPanel buttons = new JPanel(new FlowLayout());
        buttons.add(addButton);
        buttons.add(editButton);
        buttons.add(deleteButton);
        buttons.add(refreshButton);
        buttons.add(backButton);

        Runnable loadOfficials = () -> {
            model.setRowCount(0);

            String sql = """
                    SELECT official_id, full_name, position,
                           contact_number, photo_path
                    FROM barangay_officials
                    ORDER BY display_order, official_id
                    """;

            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    model.addRow(new Object[]{
                            rs.getInt("official_id"),
                            rs.getString("full_name"),
                            rs.getString("position"),
                            rs.getString("contact_number"),
                            rs.getString("photo_path")
                    });
                }

            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(
                        content,
                        "Unable to load officials:\n" + ex.getMessage(),
                        "Database Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        };

        // Choose a photo and copy it into uploads/officials
        java.util.function.Supplier<String> choosePhoto = () -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Choose Official Photo");

            chooser.setFileFilter(
                    new javax.swing.filechooser.FileNameExtensionFilter(
                            "Image files (JPG, PNG, GIF)",
                            "jpg", "jpeg", "png", "gif"
                    )
            );

            if (chooser.showOpenDialog(content)
                    != JFileChooser.APPROVE_OPTION) {
                return null;
            }

            File source = chooser.getSelectedFile();

            try {
                String fileName = java.util.UUID.randomUUID() + "_"
                        + source.getName().replaceAll(
                                "[^a-zA-Z0-9._-]", "_"
                        );

                Path folder = Path.of(
                        System.getProperty("user.dir"),
                        "uploads", "officials"
                );

                Files.createDirectories(folder);

                Path destination = folder.resolve(fileName);

                Files.copy(
                        source.toPath(),
                        destination,
                        StandardCopyOption.REPLACE_EXISTING
                );

                return Path.of(
                        "uploads", "officials", fileName
                ).toString().replace("\\", "/");

            } catch (IOException ex) {
                JOptionPane.showMessageDialog(
                        content,
                        "Unable to save photo:\n" + ex.getMessage(),
                        "Photo Error",
                        JOptionPane.ERROR_MESSAGE
                );
                return null;
            }
        };

        // ADD OFFICIAL
        addButton.addActionListener(e -> {
            JTextField nameField = new JTextField(20);
            JTextField positionField = new JTextField(20);
            JTextField contactField = new JTextField(20);
            JLabel photoLabel = new JLabel("No photo selected");
            JButton photoButton = new JButton("Choose Photo");

            selectedPhotoPath = "";

            photoButton.addActionListener(ev -> {
                String path = choosePhoto.get();

                if (path != null) {
                    selectedPhotoPath = path;
                    photoLabel.setText(new File(path).getName());
                }
            });

            JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
            form.add(new JLabel("Full Name:"));
            form.add(nameField);
            form.add(new JLabel("Position:"));
            form.add(positionField);
            form.add(new JLabel("Contact Number (optional):"));
            form.add(contactField);
            form.add(new JLabel("Official Photo:"));

            JPanel photoPanel = new JPanel(new BorderLayout(5, 0));
            photoPanel.add(photoButton, BorderLayout.WEST);
            photoPanel.add(photoLabel, BorderLayout.CENTER);
            form.add(photoPanel);

            int result = JOptionPane.showConfirmDialog(
                    content,
                    form,
                    "Add Barangay Official",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE
            );

            if (result != JOptionPane.OK_OPTION) {
                return;
            }

            String name = nameField.getText().trim();
            String position = positionField.getText().trim();

            if (name.isEmpty() || position.isEmpty()) {
                JOptionPane.showMessageDialog(
                        content,
                        "Full Name and Position are required."
                );
                return;
            }

            String sql = """
                    INSERT INTO barangay_officials
                    (full_name, position, contact_number, photo_path)
                    VALUES (?, ?, ?, ?)
                    """;

            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, name);
                stmt.setString(2, position);
                stmt.setString(3, contactField.getText().trim());
                stmt.setString(
                        4,
                        selectedPhotoPath.isBlank()
                                ? null : selectedPhotoPath
                );

                stmt.executeUpdate();

                JOptionPane.showMessageDialog(
                        content,
                        "Barangay official added successfully."
                );

                loadOfficials.run();

            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(
                        content,
                        "Unable to add official:\n" + ex.getMessage(),
                        "Database Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        });

        // EDIT OFFICIAL
        editButton.addActionListener(e -> {
            int row = table.getSelectedRow();

            if (row == -1) {
                JOptionPane.showMessageDialog(
                        content,
                        "Please select an official to edit."
                );
                return;
            }

            int officialId = (int) model.getValueAt(row, 0);

            JTextField nameField = new JTextField(
                    String.valueOf(model.getValueAt(row, 1))
            );

            JTextField positionField = new JTextField(
                    String.valueOf(model.getValueAt(row, 2))
            );

            Object contactValue = model.getValueAt(row, 3);
            Object photoValue = model.getValueAt(row, 4);

            JTextField contactField = new JTextField(
                    contactValue == null ? "" : contactValue.toString()
            );

            selectedPhotoPath = photoValue == null
                    ? "" : photoValue.toString();

            JLabel photoLabel = new JLabel(
                    selectedPhotoPath.isBlank()
                            ? "No photo selected"
                            : new File(selectedPhotoPath).getName()
            );

            JButton photoButton = new JButton("Change Photo");

            photoButton.addActionListener(ev -> {
                String path = choosePhoto.get();

                if (path != null) {
                    selectedPhotoPath = path;
                    photoLabel.setText(new File(path).getName());
                }
            });

            JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
            form.add(new JLabel("Full Name:"));
            form.add(nameField);
            form.add(new JLabel("Position:"));
            form.add(positionField);
            form.add(new JLabel("Contact Number:"));
            form.add(contactField);
            form.add(new JLabel("Official Photo:"));

            JPanel photoPanel = new JPanel(new BorderLayout(5, 0));
            photoPanel.add(photoButton, BorderLayout.WEST);
            photoPanel.add(photoLabel, BorderLayout.CENTER);
            form.add(photoPanel);

            int result = JOptionPane.showConfirmDialog(
                    content,
                    form,
                    "Edit Barangay Official",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE
            );

            if (result != JOptionPane.OK_OPTION) {
                return;
            }

            String name = nameField.getText().trim();
            String position = positionField.getText().trim();

            if (name.isEmpty() || position.isEmpty()) {
                JOptionPane.showMessageDialog(
                        content,
                        "Full Name and Position are required."
                );
                return;
            }

            String sql = """
                    UPDATE barangay_officials
                    SET full_name = ?, position = ?, contact_number = ?,
                        photo_path = ?
                    WHERE official_id = ?
                    """;

            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, name);
                stmt.setString(2, position);
                stmt.setString(3, contactField.getText().trim());
                stmt.setString(
                        4,
                        selectedPhotoPath.isBlank()
                                ? null : selectedPhotoPath
                );
                stmt.setInt(5, officialId);

                stmt.executeUpdate();

                JOptionPane.showMessageDialog(
                        content,
                        "Barangay official updated successfully."
                );

                loadOfficials.run();

            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(
                        content,
                        "Unable to update official:\n" + ex.getMessage(),
                        "Database Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        });

        // DELETE OFFICIAL
        deleteButton.addActionListener(e -> {
            int row = table.getSelectedRow();

            if (row == -1) {
                JOptionPane.showMessageDialog(
                        content,
                        "Please select an official to delete."
                );
                return;
            }

            int officialId = (int) model.getValueAt(row, 0);

            int confirm = JOptionPane.showConfirmDialog(
                    content,
                    "Are you sure you want to delete this official?",
                    "Confirm Delete",
                    JOptionPane.YES_NO_OPTION
            );

            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }

            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                         "DELETE FROM barangay_officials WHERE official_id = ?"
                 )) {

                stmt.setInt(1, officialId);
                stmt.executeUpdate();

                JOptionPane.showMessageDialog(
                        content,
                        "Barangay official deleted successfully."
                );

                loadOfficials.run();

            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(
                        content,
                        "Unable to delete official:\n" + ex.getMessage(),
                        "Database Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        });

        refreshButton.addActionListener(e -> loadOfficials.run());

        backButton.addActionListener(e -> {
            if (backAction != null) {
                backAction.run();
            }
        });

        content.add(title, BorderLayout.NORTH);
        content.add(new JScrollPane(table), BorderLayout.CENTER);
        content.add(buttons, BorderLayout.SOUTH);

        loadOfficials.run();

        content.revalidate();
        content.repaint();
    }
}
