package eu.vranckaert.heart.rate.monitor.shared.dao;

/**
 * Date: 02/04/14
 * Time: 15:16
 *
 * @author Dirk Vranckaert
 */
public interface IDatabaseUpgrade {
    int getToVersion();
    String[] getSqlQueries();
}
