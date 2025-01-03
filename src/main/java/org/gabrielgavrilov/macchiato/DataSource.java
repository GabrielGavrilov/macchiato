package org.gabrielgavrilov.macchiato;

import java.sql.*;

public class DataSource {

    private Connection connection;
    private Statement statement;

    public DataSource() {
        try {
            this.connection = DriverManager.getConnection("jdbc:sqlite:test.db");
            this.statement = this.connection.createStatement();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    // TODO: Might not always return a ResultSet
    public ResultSet executeQuery(String statement) {
        try {
            return this.statement.executeQuery(statement);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public Connection getConnection() {
        return this.connection;
    }

    public Statement getStatement() {
        return this.statement;
    }

}
