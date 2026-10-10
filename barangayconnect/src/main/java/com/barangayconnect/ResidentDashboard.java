package com.barangayconnect;

import javax.swing.*;
import java.awt.*;

public class ResidentDashboard {

    public static void showDashboard(String name, String role) {

        JFrame frame = new JFrame("BarangayConnect - Resident");
        frame.setSize(800, 500);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout());

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(30, 90, 70));
        header.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

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
        content.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
        content.setBackground(new Color(245, 247, 246));

        content.add(createCard("Announcements"));
        content.add(createCard("Request Documents"));
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
}
