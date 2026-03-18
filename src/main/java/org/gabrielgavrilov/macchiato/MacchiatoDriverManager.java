package org.gabrielgavrilov.macchiato;

import org.gabrielgavrilov.macchiato.exceptions.MacchiatoConnectionException;

import java.sql.*;

public class MacchiatoDriverManager {

    private final String databaseUrl;

    public MacchiatoDriverManager(String databaseUrl) {
        this.databaseUrl = databaseUrl;
    }

    public Connection getConnection() {
        try {
            return DriverManager.getConnection(databaseUrl);
        } catch (SQLException e) {
            throw new MacchiatoConnectionException(e.getMessage());
        }
    }

}
