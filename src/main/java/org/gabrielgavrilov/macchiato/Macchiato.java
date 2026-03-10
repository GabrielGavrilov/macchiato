package org.gabrielgavrilov.macchiato;

public class Macchiato {

    // TODO: Error handling

    private static DataSource dataSource;

    public static void initialize(String databaseUrl) {
        dataSource = new DataSource(databaseUrl);
    }

    public static DataSource getDataSource() {
        if (dataSource == null) {
            // fix this
            throw new IllegalStateException("DataSource has not been initialized");
        }
        return dataSource;
    }

}
