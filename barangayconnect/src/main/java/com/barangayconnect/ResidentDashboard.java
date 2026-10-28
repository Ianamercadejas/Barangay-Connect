
package com.barangayconnect;

import javax.swing.*;
import java.awt.*;

public class ResidentDashboard {

    private static JFrame frame;
    private static JPanel content;
    private static int currentUserId;
    private static String currentName;

    public static void showDashboard(
            String name, String role, int userId) {

        currentName = name;
        currentUserId = userId;

        frame = new JFrame("BarangayConnect - Resident");
        frame.setSize(800, 500);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout());

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(30, 90, 70));
        header.setBorder(
                BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel title = new JLabel("BarangayConnect");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Arial", Font.BOLD, 24));

        JPanel rightPanel = new JPanel(new FlowLayout(
                FlowLayout.RIGHT, 15, 0));
        rightPanel.setOpaque(false);

        JLabel welcome = new JLabel("Welcome, " + currentName + "!");
        welcome.setForeground(Color.WHITE);
        welcome.setFont(new Font("Arial", Font.PLAIN, 14));

        JButton logoutButton = new JButton("Logout");
        logoutButton.setFont(new Font("Arial", Font.BOLD, 12));
        logoutButton.addActionListener(e -> logout());

        rightPanel.add(welcome);
        rightPanel.add(logoutButton);

        header.add(title, BorderLayout.WEST);
        header.add(rightPanel, BorderLayout.EAST);

        content = new JPanel(new BorderLayout());
        content.setBorder(
                BorderFactory.createEmptyBorder(25, 25, 25, 25));
        content.setBackground(new Color(245, 247, 246));

        frame.add(header, BorderLayout.NORTH);
        frame.add(content, BorderLayout.CENTER);

        showMenu();
        frame.setVisible(true);
    }

    private static void logout() {
        int choice = JOptionPane.showConfirmDialog(
                frame,
                "Are you sure you want to logout?",
                "Confirm Logout",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (choice == JOptionPane.YES_OPTION) {
            frame.dispose();
            App.showLoginWindow();
        }
    }

    private static void showMenu() {
        content.removeAll();
        content.setLayout(new GridLayout(3, 2, 15, 15));

        addCard("Announcements",
                () -> showAnnouncements());

        addCard("Request Documents",
                () -> DocumentRequest.showForm(
                        content, currentUserId,
                        ResidentDashboard::showMenu));

        addCard("My Requests",
                () -> ResidentMyRequests.showWindow(
                        content, currentUserId,
                        ResidentDashboard::showMenu));

        addCard("Barangay Officials",
                () -> ResidentBarangayOfficials.showWindow(
                        content, ResidentDashboard::showMenu));

        addCard("My Profile",
                () -> ResidentPersonalInformation.showWindow(
                        content, currentUserId,
                        ResidentDashboard::showMenu));

        content.revalidate();
        content.repaint();
    }

    private static void addCard(String text, Runnable action) {
        JLabel card = new JLabel(text, SwingConstants.CENTER);
        card.setFont(new Font("Arial", Font.BOLD, 16));
        card.setOpaque(true);
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createLineBorder(
                new Color(210, 220, 215), 1));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        card.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(
                    java.awt.event.MouseEvent e) {
                action.run();
            }
        });

        content.add(card);
    }

    public static void showAnnouncements() {
        content.removeAll();
        content.setLayout(new BorderLayout(10, 10));

        JButton backButton = new JButton("Back");

        JLabel heading =
                new JLabel("Barangay Announcements");
        heading.setFont(new Font("Arial", Font.BOLD, 22));

        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.add(backButton, BorderLayout.WEST);
        topPanel.add(heading, BorderLayout.CENTER);

        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setFont(new Font("Arial", Font.PLAIN, 14));

        String sql = "SELECT title, content, created_at "
                + "FROM announcements ORDER BY created_at DESC";

        try (java.sql.Connection conn =
                     DBConnection.getConnection();
             java.sql.PreparedStatement stmt =
                     conn.prepareStatement(sql);
             java.sql.ResultSet rs = stmt.executeQuery()) {

            boolean found = false;

            while (rs.next()) {
                found = true;
                area.append("Title: "
                        + rs.getString("title") + "\n");
                area.append("Date: "
                        + rs.getString("created_at") + "\n");
                area.append(rs.getString("content") + "\n\n");
                area.append(
                        "------------------------------\n\n");
            }

            if (!found) {
                area.setText("No announcements yet.");
            }

        } catch (Exception ex) {
            area.setText("Unable to load announcements:\n"
                    + ex.getMessage());
        }

        backButton.addActionListener(e -> showMenu());

        content.add(topPanel, BorderLayout.NORTH);
        content.add(new JScrollPane(area), BorderLayout.CENTER);
        content.revalidate();
        content.repaint();
    }
}
