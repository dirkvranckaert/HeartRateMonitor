package eu.vranckaert.heart.rate.monitor.dao;

import android.util.Log;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import eu.vranckaert.hear.rate.monitor.shared.dao.dao.Dao;
import eu.vranckaert.hear.rate.monitor.shared.model.Measurement;
import eu.vranckaert.heart.rate.monitor.HeartRateApplication;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Date: 08/06/15
 * Time: 17:54
 *
 * @author Dirk Vranckaert
 */
public class MeasurementDao extends Dao<Measurement, Integer, HeartRateDatabaseHelper> implements IMeasurementDao {
    public MeasurementDao() {
        super(Measurement.class, HeartRateDatabaseHelper.class, HeartRateApplication.getContext());
    }

    @Override
    public List<Measurement> findExact(Measurement measurement) {
        try {
            QueryBuilder<Measurement, Integer> qb = dao.queryBuilder();
            qb.where().eq(Measurement.COLUMN_START, measurement.getStartMeasurement())
                    .and().eq(Measurement.COLUMN_END, measurement.getEndMeasurement())
                    .and().eq(Measurement.COLUMN_AVERAGE, measurement.getAverageHeartBeat())
                    .and().eq(Measurement.COLUMN_MIN, measurement.getMinimumHeartBeat())
                    .and().eq(Measurement.COLUMN_MAX, measurement.getMaximumHeartBeat());
            PreparedQuery<Measurement> pq = qb.prepare();
            return dao.query(pq);
        } catch (SQLException e) {
            Log.e("MeasurementDao", "Could not build the query...");
            throwFatalException(e);
        }

        return new ArrayList<>();
    }

    @Override
    public List<Measurement> findMeasurementsToSync() {
        try {
            QueryBuilder<Measurement, Integer> qb = dao.queryBuilder();
            qb.where().eq(Measurement.COLUMN_SYNCED_WITH_GOOGLE_FIT, false);
            PreparedQuery<Measurement> pq = qb.prepare();
            return dao.query(pq);
        } catch (SQLException e) {
            Log.e("MeasurementDao", "Could not build the query...");
            throwFatalException(e);
        }

        return new ArrayList<>();
    }
}
