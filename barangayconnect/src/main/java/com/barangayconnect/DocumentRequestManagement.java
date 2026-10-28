
package com.barangayconnect;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Date;

public class DocumentRequestManagement {

    private static DefaultTableModel tableModel;
    private static JTable requestTable;

    public static void showWindow(int officialUserId) {
        JFrame frame = new JFrame("Manage Document Requests");
        frame.setSize(1050, 600);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLocationRelativeTo(null);

        JPanel content = new JPanel(new BorderLayout());

        showWindow(content, officialUserId, frame::dispose);

        frame.add(content);
        frame.setVisible(true);
    }

    public static void showWindow(
            JPanel content,
            int officialUserId,
            Runnable backAction) {

        content.removeAll();
        content.setLayout(new BorderLayout(10, 10));

        JLabel title = new JLabel(
                "DOCUMENT REQUEST MANAGEMENT",
                SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 22));
        title.setBorder(
                BorderFactory.createEmptyBorder(15, 10, 10, 10));

        String[] columns = {
            "Request ID",
            "Resident ID",
            "Resident Name",
            "Document Type",
            "Purpose",
            "Status",
            "Pickup Date"
        };

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        requestTable = new JTable(tableModel);
        requestTable.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION);
        requestTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        int[] widths = {80, 85, 180, 200, 200, 100, 110};

        for (int i = 0; i < widths.length; i++) {
            requestTable.getColumnModel()
                    .getColumn(i)
                    .setPreferredWidth(widths[i]);
        }

        JScrollPane scrollPane = new JScrollPane(requestTable);

        JButton refreshButton = new JButton("Refresh Requests");
        JButton infoButton = new JButton("View Resident Information");
        JButton approveButton = new JButton("Approve Request");
        JButton denyButton = new JButton("Deny Request");
        JButton pickupButton = new JButton("Set Pickup Date");
        JButton backButton = new JButton("Back to Dashboard");

        // Keep action buttons in their own panel.
        JPanel actionButtons = new JPanel(
                new FlowLayout(FlowLayout.CENTER, 8, 8));

        actionButtons.add(refreshButton);
        actionButtons.add(infoButton);
        actionButtons.add(approveButton);
        actionButtons.add(denyButton);
        actionButtons.add(pickupButton);

        // Place Back to Dashboard on its own bottom row.
        JPanel bottomPanel = new JPanel(new BorderLayout());

        JPanel backPanel = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 10, 8));

        backPanel.add(backButton);

        bottomPanel.add(actionButtons, BorderLayout.CENTER);
        bottomPanel.add(backPanel, BorderLayout.SOUTH);

        content.add(title, BorderLayout.NORTH);
        content.add(scrollPane, BorderLayout.CENTER);
        content.add(bottomPanel, BorderLayout.SOUTH);

        refreshButton.addActionListener(e -> loadRequests(content));

        infoButton.addActionListener(e -> {
            int row = requestTable.getSelectedRow();

            if (row == -1) {
                JOptionPane.showMessageDialog(
                        content,
                        "Please select a request first.");
                return;
            }

            int residentId = Integer.parseInt(
                    tableModel.getValueAt(row, 1).toString());

            showResidentInformation(content, residentId);
        });

        approveButton.addActionListener(e ->
                updateRequestStatus(
                        content, officialUserId, "APPROVED"));

        denyButton.addActionListener(e ->
                updateRequestStatus(
                        content, officialUserId, "DENIED"));

        pickupButton.addActionListener(e ->
                setPickupDate(content));

        backButton.addActionListener(e -> {
            if (backAction != null) {
                backAction.run();
            }
        });

        loadRequests(content);

        content.revalidate();
        content.repaint();
    }

    private static void loadRequests(Component parent) {
        tableModel.setRowCount(0);

        String sql =
                "SELECT d.request_id, d.resident_id, u.full_name, "
                + "d.document_type, d.purpose, d.status, d.pickup_date "
                + "FROM document_requests d "
                + "JOIN users u ON d.resident_id = u.user_id "
                + "ORDER BY d.requested_at DESC";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getInt("request_id"),
                    rs.getInt("resident_id"),
                    rs.getString("full_name"),
                    rs.getString("document_type"),
                    rs.getString("purpose"),
                    rs.getString("status"),
                    rs.getDate("pickup_date")
                });
            }

        } catch (SQLException ex) {
            ex.printStackTrace();

            JOptionPane.showMessageDialog(
                    parent,
                    "Could not load document requests:\n"
                            + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void showResidentInformation(
            Component parent, int residentId) {

        String sql =
                "SELECT u.full_name, u.email, r.birth_date, r.gender, "
                + "r.civil_status, r.complete_address, r.contact_number, "
                + "r.occupation, r.citizenship "
                + "FROM users u "
                + "LEFT JOIN resident_records r "
                + "ON u.user_id = r.user_id "
                + "WHERE u.user_id = ? AND u.role = 'RESIDENT'";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, residentId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    JOptionPane.showMessageDialog(
                            parent,
                            "Resident information was not found.");
                    return;
                }

                String details =
                        "Full Name: " + value(rs.getString("full_name"))
                        + "\nEmail: " + value(rs.getString("email"))
                        + "\nBirth Date: " + value(rs.getString("birth_date"))
                        + "\nGender: " + value(rs.getString("gender"))
                        + "\nCivil Status: " + value(rs.getString("civil_status"))
                        + "\nComplete Address: "
                        + value(rs.getString("complete_address"))
                        + "\nContact Number: "
                        + value(rs.getString("contact_number"))
                        + "\nOccupation: " + value(rs.getString("occupation"))
                        + "\nCitizenship: " + value(rs.getString("citizenship"));

                JTextArea textArea = new JTextArea(details, 12, 35);
                textArea.setEditable(false);
                textArea.setFont(new Font("Arial", Font.PLAIN, 14));

                JOptionPane.showMessageDialog(
                        parent,
                        new JScrollPane(textArea),
                        "Resident Personal Information",
                        JOptionPane.INFORMATION_MESSAGE);
            }

        } catch (SQLException ex) {
            ex.printStackTrace();

            JOptionPane.showMessageDialog(
                    parent,
                    "Could not load resident information:\n"
                            + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void updateRequestStatus(
            Component parent,
            int officialUserId,
            String newStatus) {

        int row = requestTable.getSelectedRow();

        if (row == -1) {
            JOptionPane.showMessageDialog(
                    parent,
                    "Please select a request first.");
            return;
        }

        int requestId = Integer.parseInt(
                tableModel.getValueAt(row, 0).toString());

        String currentStatus =
                tableModel.getValueAt(row, 5).toString();

        if (!"PENDING".equalsIgnoreCase(currentStatus)) {
            JOptionPane.showMessageDialog(
                    parent,
                    "Only PENDING requests can be approved or denied.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                parent,
                "Are you sure you want to "
                        + newStatus.toLowerCase()
                        + " request #" + requestId + "?",
                "Confirm Request Review",
                JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        String sql =
                "UPDATE document_requests "
                + "SET status = ?, reviewed_by = ?, reviewed_at = NOW() "
                + "WHERE request_id = ? AND status = 'PENDING'";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, newStatus);
            stmt.setInt(2, officialUserId);
            stmt.setInt(3, requestId);

            int updated = stmt.executeUpdate();

            if (updated > 0) {
                JOptionPane.showMessageDialog(
                        parent,
                        "Request " + newStatus.toLowerCase()
                                + " successfully.");

                loadRequests(parent);
            } else {
                JOptionPane.showMessageDialog(
                        parent,
                        "The request was not changed. "
                                + "Refresh and check its status.");
            }

        } catch (SQLException ex) {
            ex.printStackTrace();

            JOptionPane.showMessageDialog(
                    parent,
                    "Could not update request:\n" + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void setPickupDate(Component parent) {
        int row = requestTable.getSelectedRow();

        if (row == -1) {
            JOptionPane.showMessageDialog(
                    parent,
                    "Please select a request first.");
            return;
        }

        int requestId = Integer.parseInt(
                tableModel.getValueAt(row, 0).toString());

        String status = tableModel.getValueAt(row, 5).toString();

        if (!"APPROVED".equalsIgnoreCase(status)) {
            JOptionPane.showMessageDialog(
                    parent,
                    "Approve the request before setting a pickup date.");
            return;
        }

        String input = JOptionPane.showInputDialog(
                parent,
                "Enter pickup date (YYYY-MM-DD):");

        if (input == null) {
            return;
        }

        input = input.trim();

        Date pickupDate;

        try {
            pickupDate = Date.valueOf(input);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(
                    parent,
                    "Invalid date. Use YYYY-MM-DD, "
                            + "for example 2026-11-05.");
            return;
        }

        String sql =
                "UPDATE document_requests "
                + "SET pickup_date = ? "
                + "WHERE request_id = ? AND status = 'APPROVED'";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, pickupDate);
            stmt.setInt(2, requestId);

            int updated = stmt.executeUpdate();

            if (updated > 0) {
                JOptionPane.showMessageDialog(
                        parent,
                        "Pickup date saved successfully.");

                loadRequests(parent);
            } else {
                JOptionPane.showMessageDialog(
                        parent,
                        "Could not update the pickup date.");
            }

        } catch (SQLException ex) {
            ex.printStackTrace();

            JOptionPane.showMessageDialog(
                    parent,
                    "Could not save pickup date:\n" + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private static String value(String text) {
        return text == null || text.trim().isEmpty()
                ? "Not provided" : text;
    }
}
