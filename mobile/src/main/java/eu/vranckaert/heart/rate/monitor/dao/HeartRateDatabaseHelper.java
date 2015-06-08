package eu.vranckaert.heart.rate.monitor.dao;

import android.content.Context;
import eu.vranckaert.hear.rate.monitor.shared.dao.dao.DatabaseHelper;
import eu.vranckaert.hear.rate.monitor.shared.dao.dao.IDatabaseUpgrade;
import eu.vranckaert.hear.rate.monitor.shared.dao.dao.ITable;
import eu.vranckaert.hear.rate.monitor.shared.model.Measurement;
import eu.vranckaert.heart.rate.monitor.HeartRateApplication;

import java.util.ArrayList;
import java.util.List;

/**
 * Date: 08/06/15
 * Time: 17:49
 *
 * @author Dirk Vranckaert
 */
public class HeartRateDatabaseHelper extends DatabaseHelper {
    public static final String DB_NAME = "HearRateDatabase";
    public static final int DB_VERSION = 1;

    public HeartRateDatabaseHelper() {
        this(HeartRateApplication.getContext());
    }

    public HeartRateDatabaseHelper(Context context) {
        super(context, DB_NAME, DB_VERSION);
    }

    @Override
    public List<ITable> getTables() {
        List<ITable> tables = new ArrayList<>();
        tables.add(new ITable() {
            @Override
            public Class getTableClass() {
                return Measurement.class;
            }
        });
        return tables;
    }

    @Override
    public List<IDatabaseUpgrade> getDatabaseUpgrades() {
        List<IDatabaseUpgrade> databaseUpgrades = new ArrayList<>();

        for (HeartRateDatabaseUpgrade upgrade : HeartRateDatabaseUpgrade.values()) {
            databaseUpgrades.add(upgrade);
        }

        return databaseUpgrades;
    }
}
