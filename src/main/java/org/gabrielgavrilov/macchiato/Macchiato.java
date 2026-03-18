package org.gabrielgavrilov.macchiato;

import org.gabrielgavrilov.macchiato.exceptions.MacchiatoConfigurationException;

public class Macchiato {

    private static MacchiatoDriverManager macchiatoDriverManager;

    public static void initialize(String databaseUrl) {
        macchiatoDriverManager = new MacchiatoDriverManager(databaseUrl);
    }

    public static MacchiatoDriverManager getDriverManager() {
        if (macchiatoDriverManager == null) {
            throw new MacchiatoConfigurationException("DataSource has not been initialized");
        }
        return macchiatoDriverManager;
    }

}
