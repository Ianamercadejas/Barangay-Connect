package com.barangayconnect;

import java.sql.Connection;
import java.sql.SQLException;

public class TestConnection {

    public static void main(String[] args) {

        try (Connection connection = DBConnection.getConnection()) {
            System.out.println("BarangayConnect database connected successfully!");

        } catch (SQLException e) {
            System.out.println("Database connection failed.");
            System.out.println(e.getMessage());
        }
    }
}