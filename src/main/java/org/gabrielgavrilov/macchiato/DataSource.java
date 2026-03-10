package org.gabrielgavrilov.macchiato;

import java.sql.*;

public class DataSource {

    private final String databaseUrl;

    /**
     * Initializes the DataSource and connects to the given database.
     */
    public DataSource(String databaseUrl) {
        this.databaseUrl = databaseUrl;
    }

    public Connection getConnection() {
        try {
            return DriverManager.getConnection(this.databaseUrl);
        } catch (SQLException e) {
            // Todo: fix this
            throw new RuntimeException(e);
        }
    }

    /**
     * Executes a SQL query and returns a ResultSet.
     * @param query
     * @return ResultSet
     */
    public ResultSet executeQuery(String query) {
        try {
            Statement stmt = this.getConnection().createStatement();
            return stmt.executeQuery(query);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Executes a SQL query without returning a ResultSet.
     * @param query
     */
    public void execute(String query) {
        try {
            Statement stmt = this.getConnection().createStatement();
            stmt.execute(query);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
}
