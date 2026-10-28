
package com.barangayconnect;

import javax.swing.*;
import java.awt.*;

public class Dashboard {

    private static JFrame frame;
    private static JPanel mainPanel;
    private static String officialName;
    private static int officialUserId;

    public static void showDashboard(String name, String role, int userId) {

        officialName = name;
        officialUserId = userId;

        frame = new JFrame("BarangayConnect Dashboard");
        frame.setSize(800, 500);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout());

        mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(245, 247, 246));

        showHome();

        frame.add(mainPanel, BorderLayout.CENTER);
        frame.setVisible(true);
    }

    private static void showHome() {

        mainPanel.removeAll();
        mainPanel.setLayout(new BorderLayout());

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(30, 90, 70));
        header.setBorder(
                BorderFactory.createEmptyBorder(15, 20, 15, 20)
        );

        JLabel title = new JLabel("BarangayConnect");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Arial", Font.BOLD, 24));

        JLabel welcome = new JLabel("Welcome, " + officialName + "!");
        welcome.setForeground(Color.WHITE);
        welcome.setFont(new Font("Arial", Font.PLAIN, 14));

        JButton logoutButton = new JButton("Logout");

        logoutButton.addActionListener(e -> logout());

        JPanel rightPanel = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 12, 0));
        rightPanel.setOpaque(false);
        rightPanel.add(welcome);
        rightPanel.add(logoutButton);

        header.add(title, BorderLayout.WEST);
        header.add(rightPanel, BorderLayout.EAST);

        JPanel content = new JPanel(new GridLayout(3, 2, 15, 15));
        content.setBorder(
                BorderFactory.createEmptyBorder(25, 25, 25, 25)
        );
        content.setBackground(new Color(245, 247, 246));

        JButton accounts = createButton("Manage Accounts");
        accounts.addActionListener(e ->
                ResidentManagement.showWindow(
                        mainPanel, Dashboard::showHome)
        );

        JButton records = createButton("View Resident Records");
        records.addActionListener(e ->
                ResidentRecords.showWindow(
                        mainPanel, Dashboard::showHome)
        );

        JButton requests = createButton("Document Requests");
        requests.addActionListener(e ->
                DocumentRequestManagement.showWindow(
                        mainPanel,
                        officialUserId,
                        Dashboard::showHome)
        );

        JButton announcements = createButton("Manage Announcements");
        announcements.addActionListener(e ->
                AnnouncementManagement.showWindow(
                        mainPanel,
                        officialUserId,
                        Dashboard::showHome)
        );

        JButton officials = createButton("Manage Barangay Officials");
        officials.addActionListener(e ->
                BarangayOfficialsManagement.showWindow(
                        mainPanel, Dashboard::showHome)
        );

        content.add(accounts);
        content.add(records);
        content.add(requests);
        content.add(announcements);
        content.add(officials);

        mainPanel.add(header, BorderLayout.NORTH);
        mainPanel.add(content, BorderLayout.CENTER);

        mainPanel.revalidate();
        mainPanel.repaint();
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

    private static JButton createButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 16));
        return button;
    }
}
