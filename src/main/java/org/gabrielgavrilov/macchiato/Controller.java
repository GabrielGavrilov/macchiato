package org.gabrielgavrilov.macchiato;

import java.sql.*;

public class Controller {

    private Connection _CONN;
    private Statement _STATEMENT;

    public Controller(String dbUrl) {

    }

    public ResultSet executeQuery(String query) {
        try {
            ResultSet rs = this._STATEMENT.executeQuery(query);
            return rs;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void close() {
        try {
            this._STATEMENT.close();
            this._CONN.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
