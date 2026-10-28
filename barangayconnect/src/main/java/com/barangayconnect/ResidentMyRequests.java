
package com.barangayconnect;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class ResidentMyRequests {

    public static void showWindow(JPanel content, int userId, Runnable backAction) {
        content.removeAll();
        content.setLayout(new BorderLayout(10, 10));

        JPanel top = new JPanel(new BorderLayout());
        JButton backButton = new JButton("Back");
        JLabel title = new JLabel("My Document Requests", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 22));
        JButton refreshButton = new JButton("Refresh");

        backButton.addActionListener(e -> backAction.run());
        top.add(backButton, BorderLayout.WEST);
        top.add(title, BorderLayout.CENTER);
        top.add(refreshButton, BorderLayout.EAST);
        content.add(top, BorderLayout.NORTH);

        String[] columns = {
                "Request ID", "Document Type", "Purpose",
                "Status", "Pickup Date", "Date Requested"
        };

        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(model);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        JScrollPane scrollPane = new JScrollPane(table);
        content.add(scrollPane, BorderLayout.CENTER);

        JLabel messageLabel = new JLabel(" ", SwingConstants.CENTER);
        content.add(messageLabel, BorderLayout.SOUTH);

        Runnable loadRequests = () -> {
            model.setRowCount(0);
            messageLabel.setText("Loading requests...");

            String sql = "SELECT request_id, document_type, purpose, status, " +
                    "pickup_date, requested_at FROM document_requests " +
                    "WHERE resident_id = ? ORDER BY requested_at DESC";

            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setInt(1, userId);

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        model.addRow(new Object[]{
                                rs.getInt("request_id"),
                                rs.getString("document_type"),
                                rs.getString("purpose"),
                                rs.getString("status"),
                                rs.getString("pickup_date"),
                                rs.getString("requested_at")
                        });
                    }
                }

                messageLabel.setText(model.getRowCount() == 0
                        ? "You have no document requests yet."
                        : "Total requests: " + model.getRowCount());

            } catch (SQLException ex) {
                messageLabel.setText("Could not load requests.");
                JOptionPane.showMessageDialog(content,
                        "Error loading requests: " + ex.getMessage());
            }
        };

        refreshButton.addActionListener(e -> loadRequests.run());

        loadRequests.run();
        content.revalidate();
        content.repaint();
    }
}
