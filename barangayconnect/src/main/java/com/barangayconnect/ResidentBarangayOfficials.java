
package com.barangayconnect;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.sql.*;

public class ResidentBarangayOfficials {

    public static void showWindow(JPanel content, Runnable backAction) {
        content.removeAll();
        content.setLayout(new BorderLayout(10, 10));

        JPanel top = new JPanel(new BorderLayout());
        JButton backButton = new JButton("Back");
        JLabel title = new JLabel("Barangay Officials", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 22));

        backButton.addActionListener(e -> backAction.run());
        top.add(backButton, BorderLayout.WEST);
        top.add(title, BorderLayout.CENTER);

        JButton refreshButton = new JButton("Refresh");
        top.add(refreshButton, BorderLayout.EAST);
        content.add(top, BorderLayout.NORTH);

        JPanel officialsPanel = new JPanel();
        officialsPanel.setLayout(new BoxLayout(officialsPanel, BoxLayout.Y_AXIS));

        JScrollPane scrollPane = new JScrollPane(officialsPanel);
        content.add(scrollPane, BorderLayout.CENTER);

        Runnable loadOfficials = () -> {
            officialsPanel.removeAll();

            String sql = "SELECT full_name, position, contact_number, photo_path " +
                    "FROM barangay_officials " +
                    "ORDER BY display_order ASC, official_id ASC";

            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {

                boolean found = false;

                while (rs.next()) {
                    found = true;

                    String name = rs.getString("full_name");
                    String position = rs.getString("position");
                    String contact = rs.getString("contact_number");
                    String photoPath = rs.getString("photo_path");

                    JPanel card = new JPanel(new BorderLayout(15, 5));
                    card.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createEmptyBorder(8, 8, 8, 8),
                            BorderFactory.createLineBorder(Color.LIGHT_GRAY)
                    ));
                    card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));

                    JLabel photoLabel = new JLabel("No Photo", SwingConstants.CENTER);
                    photoLabel.setPreferredSize(new Dimension(95, 105));

                    if (photoPath != null && !photoPath.trim().isEmpty()) {
                        File photoFile = new File(photoPath);
                        if (!photoFile.isAbsolute()) {
                            photoFile = new File(System.getProperty("user.dir"), photoPath);
                        }

                        if (photoFile.exists()) {
                            ImageIcon original = new ImageIcon(photoFile.getAbsolutePath());
                            Image scaled = original.getImage()
                                    .getScaledInstance(90, 100, Image.SCALE_SMOOTH);
                            photoLabel.setText("");
                            photoLabel.setIcon(new ImageIcon(scaled));
                        }
                    }

                    JPanel details = new JPanel();
                    details.setLayout(new BoxLayout(details, BoxLayout.Y_AXIS));

                    JLabel positionLabel = new JLabel(position == null ? "" : position);
                    positionLabel.setFont(new Font("Arial", Font.BOLD, 16));

                    JLabel nameLabel = new JLabel(name == null ? "" : name);
                    nameLabel.setFont(new Font("Arial", Font.PLAIN, 14));

                    details.add(positionLabel);
                    details.add(Box.createVerticalStrut(8));
                    details.add(nameLabel);

                    if (contact != null && !contact.trim().isEmpty()) {
                        details.add(Box.createVerticalStrut(5));
                        details.add(new JLabel("Contact: " + contact));
                    }

                    card.add(photoLabel, BorderLayout.WEST);
                    card.add(details, BorderLayout.CENTER);

                    officialsPanel.add(card);
                    officialsPanel.add(Box.createVerticalStrut(8));
                }

                if (!found) {
                    officialsPanel.add(new JLabel("No barangay officials found."));
                }

            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(content,
                        "Error loading barangay officials: " + ex.getMessage());
            }

            officialsPanel.revalidate();
            officialsPanel.repaint();
        };

        refreshButton.addActionListener(e -> loadOfficials.run());

        loadOfficials.run();
        content.revalidate();
        content.repaint();
    }
}
