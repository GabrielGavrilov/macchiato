package org.gabrielgavrilov.macchiato;

import java.sql.*;

public class DataSource {

    private Connection connection;

    /**
     * Initializes the DataSource and connects to the given database.
     */
    public DataSource() {
        try {
            this.connection = DriverManager.getConnection(Macchiato.DATABASE);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Executes a SQL query and returns a ResultSet.
     * @param query
     * @return ResultSet
     */
    public ResultSet executeQuery(String query) {
        try {
            Statement stmt = this.connection.createStatement();
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
            Statement stmt = this.connection.createStatement();
            stmt.execute(query);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
}
