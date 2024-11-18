package org.gabrielgavrilov.macchiato;

import java.sql.*;

public class Controller {

    private Statement _STATEMENT;

    public Controller(String dbUrl) {
        try(
                Connection conn = DriverManager.getConnection(dbUrl);
                Statement statement = conn.createStatement();
        ) {
            this._STATEMENT = statement;
        }
        catch(SQLException e) {
            e.printStackTrace();
        }
    }

    public ResultSet executeQuery(String query) {
        try {
            ResultSet rs = this._STATEMENT.executeQuery(query);
            return rs;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
