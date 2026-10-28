
package com.barangayconnect;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AnnouncementManagement {

    public static void showWindow() {
        JFrame frame = new JFrame("Manage Announcements");
        frame.setSize(900, 550);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel content = new JPanel(new BorderLayout());
        showWindow(content, 1, frame::dispose);

        frame.add(content);
        frame.setVisible(true);
    }

    public static void showWindow(
            JPanel content, int officialUserId, Runnable backAction) {

        showAnnouncementForm(content, officialUserId, backAction);
    }

    private static void showAnnouncementForm(
            JPanel content, int officialUserId, Runnable backAction) {

        content.removeAll();
        content.setLayout(new BorderLayout(10, 10));

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
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1;
        formPanel.add(new JLabel("Announcement Content:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        formPanel.add(new JScrollPane(contentArea), gbc);

        JButton publishButton = new JButton("Publish Announcement");
        JButton viewButton = new JButton("View Announcements");
        JButton editButton = new JButton("Edit Announcement");
        JButton deleteButton = new JButton("Delete Announcement");
        JButton backButton = new JButton("Back to Dashboard");

        JPanel actionPanel = new JPanel(
                new FlowLayout(FlowLayout.CENTER, 8, 8));

        actionPanel.add(publishButton);
        actionPanel.add(viewButton);
        actionPanel.add(editButton);
        actionPanel.add(deleteButton);

        // Dedicated row for the Dashboard button.
        JPanel backPanel = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 10, 8));

        backPanel.add(backButton);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(actionPanel, BorderLayout.CENTER);
        bottomPanel.add(backPanel, BorderLayout.SOUTH);

        content.add(titlePanel, BorderLayout.NORTH);
        content.add(formPanel, BorderLayout.CENTER);
        content.add(bottomPanel, BorderLayout.SOUTH);

        publishButton.addActionListener(e -> {
            String title = titleField.getText().trim();
            String announcementContent = contentArea.getText().trim();

            if (title.isEmpty() || announcementContent.isEmpty()) {
                JOptionPane.showMessageDialog(
                        content,
                        "Please enter both the title and content.");
                return;
            }

            String sql = "INSERT INTO announcements "
                    + "(title, content, posted_by) VALUES (?, ?, ?)";

            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, title);
                stmt.setString(2, announcementContent);
                stmt.setInt(3, officialUserId);

                if (stmt.executeUpdate() > 0) {
                    JOptionPane.showMessageDialog(
                            content,
                            "Announcement published successfully!");

                    titleField.setText("");
                    contentArea.setText("");
                }

            } catch (SQLException ex) {
                ex.printStackTrace();

                JOptionPane.showMessageDialog(
                        content,
                        "Unable to publish announcement:\n"
                                + ex.getMessage(),
                        "Database Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        viewButton.addActionListener(e ->
                showAnnouncementsTable(
                        content, officialUserId, backAction));

        editButton.addActionListener(e -> editAnnouncement(content));

        deleteButton.addActionListener(e -> deleteAnnouncement(content));

        backButton.addActionListener(e -> {
            if (backAction != null) {
                backAction.run();
            }
        });

        content.revalidate();
        content.repaint();
    }

    private static void showAnnouncementsTable(
            JPanel content, int officialUserId, Runnable backAction) {

        content.removeAll();
        content.setLayout(new BorderLayout(10, 10));

        JLabel title = new JLabel(
                "ALL ANNOUNCEMENTS", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 22));
        title.setBorder(
                BorderFactory.createEmptyBorder(15, 10, 10, 10));

        String[] columns = {
            "ID", "Title", "Content", "Posted By", "Created At"
        };

        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(model);
        table.setAutoCreateRowSorter(true);
        table.setRowHeight(24);

        String sql = "SELECT announcement_id, title, content, posted_by, "
                + "created_at FROM announcements ORDER BY created_at DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("announcement_id"),
                    rs.getString("title"),
                    rs.getString("content"),
                    rs.getInt("posted_by"),
                    rs.getTimestamp("created_at")
                });
            }

        } catch (SQLException ex) {
            ex.printStackTrace();

            JOptionPane.showMessageDialog(
                    content,
                    "Unable to load announcements:\n" + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }

        JButton refreshButton = new JButton("Refresh");
        JButton formButton = new JButton("Back to Announcement Form");
        JButton dashboardButton = new JButton("Back to Dashboard");

        JPanel actionPanel = new JPanel(
                new FlowLayout(FlowLayout.CENTER, 8, 8));

        actionPanel.add(refreshButton);
        actionPanel.add(formButton);

        JPanel dashboardPanel = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 10, 8));

        dashboardPanel.add(dashboardButton);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(actionPanel, BorderLayout.CENTER);
        bottomPanel.add(dashboardPanel, BorderLayout.SOUTH);

        refreshButton.addActionListener(e ->
                showAnnouncementsTable(
                        content, officialUserId, backAction));

        formButton.addActionListener(e ->
                showAnnouncementForm(
                        content, officialUserId, backAction));

        dashboardButton.addActionListener(e -> {
            if (backAction != null) {
                backAction.run();
            }
        });

        content.add(title, BorderLayout.NORTH);
        content.add(new JScrollPane(table), BorderLayout.CENTER);
        content.add(bottomPanel, BorderLayout.SOUTH);

        content.revalidate();
        content.repaint();
    }

    private static void editAnnouncement(Component parent) {
        String input = JOptionPane.showInputDialog(
                parent,
                "Enter the Announcement ID you want to edit:");

        if (input == null || input.trim().isEmpty()) {
            return;
        }

        int id;

        try {
            id = Integer.parseInt(input.trim());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(
                    parent,
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
                    JOptionPane.showMessageDialog(
                            parent, "Announcement not found.");
                    return;
                }

                JTextField newTitle =
                        new JTextField(rs.getString("title"));

                JTextArea newContent = new JTextArea(
                        rs.getString("content"), 8, 30);

                newContent.setLineWrap(true);
                newContent.setWrapStyleWord(true);

                JPanel form = new JPanel(
                        new GridLayout(0, 1, 5, 5));

                form.add(new JLabel("Announcement Title:"));
                form.add(newTitle);
                form.add(new JLabel("Announcement Content:"));
                form.add(new JScrollPane(newContent));

                int choice = JOptionPane.showConfirmDialog(
                        parent,
                        form,
                        "Edit Announcement",
                        JOptionPane.OK_CANCEL_OPTION,
                        JOptionPane.PLAIN_MESSAGE);

                if (choice != JOptionPane.OK_OPTION) {
                    return;
                }

                if (newTitle.getText().trim().isEmpty()
                        || newContent.getText().trim().isEmpty()) {
                    JOptionPane.showMessageDialog(
                            parent,
                            "Title and content cannot be empty.");
                    return;
                }

                String updateSql = "UPDATE announcements "
                        + "SET title = ?, content = ? "
                        + "WHERE announcement_id = ?";

                try (PreparedStatement update =
                             conn.prepareStatement(updateSql)) {

                    update.setString(1, newTitle.getText().trim());
                    update.setString(2, newContent.getText().trim());
                    update.setInt(3, id);

                    if (update.executeUpdate() > 0) {
                        JOptionPane.showMessageDialog(
                                parent,
                                "Announcement updated successfully!");
                    } else {
                        JOptionPane.showMessageDialog(
                                parent,
                                "Announcement was not updated.");
                    }
                }
            }

        } catch (SQLException ex) {
            ex.printStackTrace();

            JOptionPane.showMessageDialog(
                    parent,
                    "Unable to edit announcement:\n" + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void deleteAnnouncement(Component parent) {
        String input = JOptionPane.showInputDialog(
                parent,
                "Enter the Announcement ID you want to delete:");

        if (input == null || input.trim().isEmpty()) {
            return;
        }

        int id;

        try {
            id = Integer.parseInt(input.trim());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(
                    parent,
                    "Please enter a valid Announcement ID.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                parent,
                "Are you sure you want to delete Announcement ID "
                        + id + "?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        String sql =
                "DELETE FROM announcements WHERE announcement_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            int rowsDeleted = stmt.executeUpdate();

            if (rowsDeleted > 0) {
                JOptionPane.showMessageDialog(
                        parent,
                        "Announcement deleted successfully!");
            } else {
                JOptionPane.showMessageDialog(
                        parent,
                        "Announcement not found.");
            }

        } catch (SQLException ex) {
            ex.printStackTrace();

            JOptionPane.showMessageDialog(
                    parent,
                    "Unable to delete announcement:\n"
                            + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
