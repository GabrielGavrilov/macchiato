package org.gabrielgavrilov.macchiato;

public class Macchiato {

    private static MacchiatoDriverManager macchiatoDriverManager;

    public static void initialize(String databaseUrl) {
        macchiatoDriverManager = new MacchiatoDriverManager(databaseUrl);
    }

    public static MacchiatoDriverManager getDriverManager() {
        if (macchiatoDriverManager == null) {
            // fix this
            throw new IllegalStateException("DataSource has not been initialized");
        }
        return macchiatoDriverManager;
    }

}
