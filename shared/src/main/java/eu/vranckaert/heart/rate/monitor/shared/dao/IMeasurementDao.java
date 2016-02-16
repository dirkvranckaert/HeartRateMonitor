package eu.vranckaert.heart.rate.monitor.shared.dao;

import eu.vranckaert.heart.rate.monitor.shared.model.Measurement;

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
    List<Measurement> findAllSorted();
}
