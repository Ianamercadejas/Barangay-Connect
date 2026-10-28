package com.barangayconnect;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private static final String URL =
            "jdbc:mariadb://localhost:3306/barangayconnect";

    private static final String USER = "root";
    private static final String PASSWORD = "";

    public static Connection getConnection() throws SQLException {
    try {
        Class.forName("org.mariadb.jdbc.Driver");
    } catch (ClassNotFoundException e) {
        throw new SQLException(
                "MariaDB driver was not found. Check the Maven dependencies.",
                e
        );
    }

    return DriverManager.getConnection(URL, USER, PASSWORD);
}
}