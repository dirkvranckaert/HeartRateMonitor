package eu.vranckaert.heart.rate.monitor.shared.dao;

import android.content.Context;
import eu.vranckaert.heart.rate.monitor.shared.model.Measurement;

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
