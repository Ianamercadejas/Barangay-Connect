package com.barangayconnect;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import javax.swing.table.DefaultTableModel;

public class AnnouncementManagement {

    public static void showWindow() {

        JFrame frame = new JFrame("Manage Announcements");
        frame.setSize(850, 500);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLayout(new BorderLayout(10, 10));

        JLabel titleLabel = new JLabel("Create Announcement");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));

        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        titlePanel.add(titleLabel);

        JTextField titleField = new JTextField(30);
        JTextArea contentArea = new JTextArea(12, 30);
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);

        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;

        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("Announcement Title:"), gbc);

        gbc.gridx = 1;
        formPanel.add(titleField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(new JLabel("Announcement Content:"), gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1;
        gbc.weighty = 1;
        formPanel.add(new JScrollPane(contentArea), gbc);

        JButton publishButton = new JButton("Publish Announcement");
        JButton viewButton = new JButton("View Announcements");
        JButton editButton = new JButton("Edit Announcement");
        JButton deleteButton = new JButton("Delete Announcement");

        JPanel buttonPanel = new JPanel();
buttonPanel.add(publishButton);
buttonPanel.add(viewButton);
buttonPanel.add(editButton);
buttonPanel.add(deleteButton);

        frame.add(titlePanel, BorderLayout.NORTH);
        frame.add(formPanel, BorderLayout.CENTER);
        frame.add(buttonPanel, BorderLayout.SOUTH);

        publishButton.addActionListener(e -> {

            String title = titleField.getText().trim();
            String content = contentArea.getText().trim();

            if (title.isEmpty() || content.isEmpty()) {
                JOptionPane.showMessageDialog(
                    frame,
                    "Please enter both the title and content."
                );
                return;
            }

            String sql = "INSERT INTO announcements "
                       + "(title, content, posted_by, status) "
                       + "VALUES (?, ?, ?, 'PUBLISHED')";

            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, title);
                stmt.setString(2, content);
                stmt.setInt(3, 1); 

                stmt.executeUpdate();

                JOptionPane.showMessageDialog(
                    frame,
                    "Announcement published successfully!"
                );

                titleField.setText("");
                contentArea.setText("");

            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(
                    frame,
                    "Unable to publish announcement:\n" + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        });
viewButton.addActionListener(e -> {
            showAnnouncementsTable();
        });
        editButton.addActionListener(e -> {
    editAnnouncement();
});
 deleteButton.addActionListener(e -> {
    deleteAnnouncement();
});


        frame.setVisible(true);
    }
    private static void showAnnouncementsTable() {
        JFrame tableFrame = new JFrame("All Announcements");
        tableFrame.setSize(750, 450);
        tableFrame.setLocationRelativeTo(null);

        String[] columns = {
                "ID", "Title", "Content", "Posted By", "Created At", "Status"
        };

        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(model);

        String sql = "SELECT announcement_id, title, content, posted_by, "
                   + "created_at, status FROM announcements "
                   + "ORDER BY created_at DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                model.addRow(new Object[] {
                        rs.getInt("announcement_id"),
                        rs.getString("title"),
                        rs.getString("content"),
                        rs.getInt("posted_by"),
                        rs.getTimestamp("created_at"),
                        rs.getString("status")
                });
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(
                    tableFrame,
                    "Unable to load announcements:\n" + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }

        tableFrame.add(new JScrollPane(table));
        tableFrame.setVisible(true);
    }
   private static void editAnnouncement() {
    String input = JOptionPane.showInputDialog(
            null,
            "Enter the Announcement ID you want to edit:"
    );

    if (input == null || input.trim().isEmpty()) {
        return;
    }

    int id;

    try {
        id = Integer.parseInt(input.trim());
    } catch (NumberFormatException ex) {
        JOptionPane.showMessageDialog(null,
                "Please enter a valid Announcement ID.");
        return;
    }

    String sql = "SELECT title, content FROM announcements "
               + "WHERE announcement_id = ?";

    try (Connection conn = DBConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

        stmt.setInt(1, id);

        try (ResultSet rs = stmt.executeQuery()) {
            if (!rs.next()) {
                JOptionPane.showMessageDialog(null,
                        "Announcement not found.");
                return;
            }

            String oldTitle = rs.getString("title");
            String oldContent = rs.getString("content");

            String newTitle = JOptionPane.showInputDialog(
                    null, "Edit Announcement Title:", oldTitle);

            if (newTitle == null || newTitle.trim().isEmpty()) {
                return;
            }

            String newContent = JOptionPane.showInputDialog(
                    null, "Edit Announcement Content:", oldContent);

            if (newContent == null || newContent.trim().isEmpty()) {
                return;
            }

            String updateSql = "UPDATE announcements "
                             + "SET title = ?, content = ? "
                             + "WHERE announcement_id = ?";

            try (PreparedStatement updateStmt =
                         conn.prepareStatement(updateSql)) {

                updateStmt.setString(1, newTitle.trim());
                updateStmt.setString(2, newContent.trim());
                updateStmt.setInt(3, id);

                updateStmt.executeUpdate();

                JOptionPane.showMessageDialog(null,
                        "Announcement updated successfully!");
            }
        }

    } catch (SQLException ex) {
        JOptionPane.showMessageDialog(null,
                "Unable to edit announcement:\n" + ex.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
    }
} 
private static void deleteAnnouncement() {
    String input = JOptionPane.showInputDialog(
            null,
            "Enter the Announcement ID you want to delete:"
    );

    if (input == null || input.trim().isEmpty()) {
        return;
    }

    int id;

    try {
        id = Integer.parseInt(input.trim());
    } catch (NumberFormatException ex) {
        JOptionPane.showMessageDialog(null,
                "Please enter a valid Announcement ID.");
        return;
    }

    int confirm = JOptionPane.showConfirmDialog(
            null,
            "Are you sure you want to delete Announcement ID " + id + "?",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION
    );

    if (confirm != JOptionPane.YES_OPTION) {
        return;
    }

    String sql = "DELETE FROM announcements WHERE announcement_id = ?";

    try (Connection conn = DBConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

        stmt.setInt(1, id);

        int rowsDeleted = stmt.executeUpdate();

        if (rowsDeleted > 0) {
            JOptionPane.showMessageDialog(null,
                    "Announcement deleted successfully!");
        } else {
            JOptionPane.showMessageDialog(null,
                    "Announcement not found.");
        }

    } catch (SQLException ex) {
        JOptionPane.showMessageDialog(null,
                "Unable to delete announcement:\n" + ex.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
    }
}
}