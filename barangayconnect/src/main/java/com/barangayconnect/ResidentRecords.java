
package com.barangayconnect;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ResidentRecords {

    public static void showWindow() {
        JFrame frame = new JFrame("Resident Records");
        frame.setSize(900, 500);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel content = new JPanel(new BorderLayout());
        showWindow(content, frame::dispose);

        frame.add(content);
        frame.setVisible(true);
    }

    public static void showWindow(JPanel content, Runnable backAction) {

        content.removeAll();
        content.setLayout(new BorderLayout(10, 10));
        content.setBackground(new Color(245, 247, 246));

        JLabel title = new JLabel("Resident Records");
        title.setFont(new Font("Arial", Font.BOLD, 24));
        title.setBorder(BorderFactory.createEmptyBorder(15, 15, 5, 15));

        JTextField searchField = new JTextField(25);
        JButton searchButton = new JButton("Search");
        JButton refreshButton = new JButton("Refresh");
        JButton backButton = new JButton("Back to Dashboard");

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("Search by name or email:"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        searchPanel.add(refreshButton);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(title, BorderLayout.NORTH);
        topPanel.add(searchPanel, BorderLayout.CENTER);

        String[] columns = {
            "Resident ID",
            "Full Name",
            "Email",
            "Role",
            "Account Status",
            "Date Created"
        };

        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(model);
        table.setAutoCreateRowSorter(true);

        JScrollPane scrollPane = new JScrollPane(table);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottomPanel.add(backButton);

        content.add(topPanel, BorderLayout.NORTH);
        content.add(scrollPane, BorderLayout.CENTER);
        content.add(bottomPanel, BorderLayout.SOUTH);

        Runnable loadResidents = () -> {
            model.setRowCount(0);

            String keyword = searchField.getText().trim();

            String sql =
                "SELECT user_id, full_name, email, role, " +
                "account_status, created_at " +
                "FROM users " +
                "WHERE role = 'RESIDENT' " +
                "AND (full_name LIKE ? OR email LIKE ?) " +
                "ORDER BY full_name";

            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, "%" + keyword + "%");
                stmt.setString(2, "%" + keyword + "%");

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        model.addRow(new Object[]{
                            rs.getInt("user_id"),
                            rs.getString("full_name"),
                            rs.getString("email"),
                            rs.getString("role"),
                            rs.getString("account_status"),
                            rs.getTimestamp("created_at")
                        });
                    }
                }

            } catch (SQLException e) {
                JOptionPane.showMessageDialog(
                    content,
                    "Unable to load resident records.\n" + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        };

        searchButton.addActionListener(e -> loadResidents.run());
        searchField.addActionListener(e -> loadResidents.run());

        refreshButton.addActionListener(e -> {
            searchField.setText("");
            loadResidents.run();
        });

        backButton.addActionListener(e -> backAction.run());

        loadResidents.run();

        content.revalidate();
        content.repaint();
    }
}
