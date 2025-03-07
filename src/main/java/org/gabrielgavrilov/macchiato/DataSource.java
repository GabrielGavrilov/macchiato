package org.gabrielgavrilov.macchiato;

import java.sql.*;

public class DataSource {

    private Connection connection;
    private Statement statement;

    /**
     * Initializes the DataSource and connects to the given database.
     */
    public DataSource() {
        try {
            this.connection = DriverManager.getConnection(Macchiato.DATABASE);
            this.statement = this.connection.createStatement();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Executes a SQL query and returns a ResultSet.
     * @param statement
     * @return ResultSet
     */
    public ResultSet executeQuery(String statement) {
        try {
            return this.statement.executeQuery(statement);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Executes a SQL query without returning a ResultSet.
     * @param statement
     */
    public void execute(String statement) {
        try {
            this.statement.execute(statement);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
}
