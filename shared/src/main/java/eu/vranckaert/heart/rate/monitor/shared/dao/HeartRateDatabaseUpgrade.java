package eu.vranckaert.heart.rate.monitor.shared.dao;

/**
 * Date: 20/04/15
 * Time: 15:52
 *
 * @author Dirk Vranckaert
 */
public enum HeartRateDatabaseUpgrade implements IDatabaseUpgrade {
    ;

    int toVersion;
    String[] sqlQueries;

    HeartRateDatabaseUpgrade(int toVersion, String[] sqlQueries) {
        this.toVersion = toVersion;
        this.sqlQueries = sqlQueries;
    }

    public int getToVersion() {
        return toVersion;
    }

    public String[] getSqlQueries() {
        return sqlQueries;
    }

    private class DataTypes {
        private static final String SMALLINT = "SMALLINT";
        private static final String BIGINT = "BIGINT";
        private static final String INTEGER = "INTEGER";
        private static final String BOOLEAN = "SMALLINT";
        private static final String TEXT = "TEXT";
        private static final String VARCHAR = "VARCHAR";
        private static final String FLOAT = "FLOAT";
        private static final String DOUBLE = "DOUBLE";
    }
}
