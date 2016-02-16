package eu.vranckaert.heart.rate.monitor.shared.dao;

import android.content.Context;
import android.util.Log;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import eu.vranckaert.heart.rate.monitor.shared.model.Measurement;

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
    public MeasurementDao(Context context) {
        super(Measurement.class, HeartRateDatabaseHelper.class, context);
    }

    @Override
    public List<Measurement> findUnique(Measurement measurement) {
        try {
            QueryBuilder<Measurement, Integer> qb = dao.queryBuilder();
            qb.where().eq(Measurement.COLUMN_UNIQUE_KEY, measurement.getUniqueKey());
            PreparedQuery<Measurement> pq = qb.prepare();
            return dao.query(pq);
        } catch (SQLException e) {
            Log.e("MeasurementDao", "Could not build the query...");
            throwFatalException(e);
        }

        return new ArrayList<>();
    }

    @Override
    public List<Measurement> findMeasurementsToSyncWithFit() {
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

    @Override
    public List<Measurement> findMeasurementsToSyncWithPhone() {
        try {
            QueryBuilder<Measurement, Integer> qb = dao.queryBuilder();
            qb.where().eq(Measurement.COLUMN_SYNCED_WITH_PHONE, false);
            PreparedQuery<Measurement> pq = qb.prepare();
            return dao.query(pq);
        } catch (SQLException e) {
            Log.e("MeasurementDao", "Could not build the query...");
            throwFatalException(e);
        }

        return new ArrayList<>();
    }

    @Override
    public List<Measurement> findAllSorted() {
        try {
            QueryBuilder<Measurement, Integer> qb = dao.queryBuilder();
            qb.orderBy(Measurement.COLUMN_START, false);
            PreparedQuery<Measurement> pq = qb.prepare();
            return dao.query(pq);
        } catch (SQLException e) {
            Log.e("MeasurementDao", "Could not build the query...");
            throwFatalException(e);
        }

        return new ArrayList<>();
    }

    @Override
    public Measurement findLatest() {
        try {
            QueryBuilder<Measurement, Integer> qb = dao.queryBuilder();
            qb.orderBy(Measurement.COLUMN_START, false);
            PreparedQuery<Measurement> pq = qb.prepare();
            return dao.queryForFirst(pq);
        } catch (SQLException e) {
            Log.e("MeasurementDao", "Could not build the query...");
            throwFatalException(e);
        }

        return null;
    }

    @Override
    public List<Measurement> findAllByUniqueKey(List<String> uniqueKeys) {
        try {
            QueryBuilder<Measurement, Integer> qb = dao.queryBuilder();
            qb.where().in(Measurement.COLUMN_UNIQUE_KEY, uniqueKeys);
            PreparedQuery<Measurement> pq = qb.prepare();
            return dao.query(pq);
        } catch (SQLException e) {
            Log.e("MeasurementDao", "Could not build the query...");
            throwFatalException(e);
        }

        return null;
    }
}
