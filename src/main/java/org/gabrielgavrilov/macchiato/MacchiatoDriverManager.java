package org.gabrielgavrilov.macchiato;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MacchiatoDriverManager {

    private final String databaseUrl;

    public MacchiatoDriverManager(String databaseUrl) {
        this.databaseUrl = databaseUrl;
    }

    public Connection getConnection() {
        try {
            return DriverManager.getConnection(databaseUrl);
        } catch (SQLException e) {
            // Todo: throw custom
            throw new RuntimeException(e);
        }
    }

}
