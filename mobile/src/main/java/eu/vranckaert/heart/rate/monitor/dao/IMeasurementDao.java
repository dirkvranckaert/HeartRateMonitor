package eu.vranckaert.heart.rate.monitor.dao;

import eu.vranckaert.hear.rate.monitor.shared.dao.dao.IDao;
import eu.vranckaert.hear.rate.monitor.shared.model.Measurement;

import java.util.List;

/**
 * Date: 08/06/15
 * Time: 17:54
 *
 * @author Dirk Vranckaert
 */
public interface IMeasurementDao extends IDao<Measurement, Integer, HeartRateDatabaseHelper> {
    List<Measurement> findExact(Measurement measurement);
    List<Measurement> findMeasurementsToSync();
}
