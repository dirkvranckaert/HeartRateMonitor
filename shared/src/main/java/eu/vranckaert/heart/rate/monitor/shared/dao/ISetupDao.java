package eu.vranckaert.heart.rate.monitor.shared.dao;

/**
 * Date: 24/03/15
 * Time: 17:42
 *
 * @author Dirk Vranckaert
 */
public interface ISetupDao extends IDao<Setup,Integer,DatabaseHelper> {
    void setup(int dbVersion);
}
