package org.gabrielgavrilov.macchiato;

import java.sql.*;

public class DataSource {

    private Connection connection;
    private Statement statement;

    public DataSource() {
        try {
            this.connection = DriverManager.getConnection(Macchiato.DATABASE);
            this.statement = this.connection.createStatement();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    public ResultSet executeQuery(String statement) {
        try {
            return this.statement.executeQuery(statement);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void execute(String statement) {
        try {
            this.statement.execute(statement);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    public Connection getConnection() {
        return this.connection;
    }

    public Statement getStatement() {
        return this.statement;
    }

}
