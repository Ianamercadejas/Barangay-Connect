package com.barangayconnect;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ResidentDashboard {

    public static void showDashboard(String name, String role, int userId) {

        JFrame frame = new JFrame("BarangayConnect - Resident");
        frame.setSize(800, 500);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout());

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(30, 90, 70));
        header.setBorder(
                BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel title = new JLabel("BarangayConnect");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Arial", Font.BOLD, 24));

        JLabel welcome = new JLabel("Welcome, " + name + "!");
        welcome.setForeground(Color.WHITE);
        welcome.setFont(new Font("Arial", Font.PLAIN, 14));

        header.add(title, BorderLayout.WEST);
        header.add(welcome, BorderLayout.EAST);

        // Resident menu
        JPanel content = new JPanel(new GridLayout(3, 2, 15, 15));
        content.setBorder(
                BorderFactory.createEmptyBorder(25, 25, 25, 25));
        content.setBackground(new Color(245, 247, 246));

        // Announcements card
        JLabel announcementsCard = createCard("Announcements");
        announcementsCard.setCursor(
                new Cursor(Cursor.HAND_CURSOR));

        announcementsCard.addMouseListener(
                new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                showAnnouncements(content, userId);
            }
        });

        content.add(announcementsCard);

        // Request Documents card
        JLabel requestCard = createCard("Request Documents");
        requestCard.setCursor(new Cursor(Cursor.HAND_CURSOR));

        requestCard.addMouseListener(
                new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                DocumentRequest.showForm(userId);
            }
        });

        content.add(requestCard);

        content.add(createCard("My Requests"));
        content.add(createCard("Ayuda Programs"));
        content.add(createCard("Barangay Events"));
        content.add(createCard("My Profile"));

        frame.add(header, BorderLayout.NORTH);
        frame.add(content, BorderLayout.CENTER);

        frame.setVisible(true);
    }

    private static JLabel createCard(String text) {
        JLabel card = new JLabel(text, SwingConstants.CENTER);
        card.setFont(new Font("Arial", Font.BOLD, 16));
        card.setOpaque(true);
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createLineBorder(
                new Color(210, 220, 215), 1));

        return card;
    }

    private static void showAnnouncements(
            JPanel content, int userId) {

        content.removeAll();
        content.setLayout(new BorderLayout(10, 10));

        JButton backButton = new JButton("Back");

        JLabel heading = new JLabel("Barangay Announcements");
        heading.setFont(new Font("Arial", Font.BOLD, 22));

        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.add(backButton, BorderLayout.WEST);
        topPanel.add(heading, BorderLayout.CENTER);

        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setFont(new Font("Arial", Font.PLAIN, 14));

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT title, content, created_at FROM announcements "
                     + "WHERE status = 'PUBLISHED' "
                     + "ORDER BY created_at DESC");
             ResultSet rs = stmt.executeQuery()) {

            boolean found = false;

            while (rs.next()) {
                found = true;

                area.append("Title: "
                        + rs.getString("title") + "\n");
                area.append("Date: "
                        + rs.getString("created_at") + "\n");
                area.append(rs.getString("content") + "\n\n");
                area.append("--------------------------------\n\n");
            }

            if (!found) {
                area.setText("No published announcements yet.");
            }

        } catch (Exception ex) {
            area.setText(
                    "Unable to load announcements:\n"
                    + ex.getMessage());
        }

        // Return to the resident dashboard
        backButton.addActionListener(e -> {
            content.removeAll();
            content.setLayout(new GridLayout(3, 2, 15, 15));

            // Announcements card
            JLabel announcementsCard = createCard("Announcements");
            announcementsCard.setCursor(
                    new Cursor(Cursor.HAND_CURSOR));

            announcementsCard.addMouseListener(
                    new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(
                        java.awt.event.MouseEvent e) {
                    showAnnouncements(content, userId);
                }
            });

            content.add(announcementsCard);

            // Request Documents card
            JLabel requestCard = createCard("Request Documents");
            requestCard.setCursor(
                    new Cursor(Cursor.HAND_CURSOR));

            requestCard.addMouseListener(
                    new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(
                        java.awt.event.MouseEvent e) {
                    DocumentRequest.showForm(userId);
                }
            });

            content.add(requestCard);

            content.add(createCard("My Requests"));
            content.add(createCard("Ayuda Programs"));
            content.add(createCard("Barangay Events"));
            content.add(createCard("My Profile"));

            content.revalidate();
            content.repaint();
        });

        content.add(topPanel, BorderLayout.NORTH);
        content.add(new JScrollPane(area), BorderLayout.CENTER);

        content.revalidate();
        content.repaint();
    }
}