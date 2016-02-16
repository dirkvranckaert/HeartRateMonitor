/*
 * Copyright 2013 Dirk Vranckaert
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.vranckaert.heart.rate.monitor.shared.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.db.SqliteAndroidDatabaseType;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * A utility class to be used to setup and interact with a database.
 */
public abstract class DatabaseHelper extends OrmLiteSqliteOpenHelper {
    /**
     * Logging
     */
    private static final String LOG_TAG = DatabaseHelper.class.getSimpleName();

    /**
     * The database type.
     */
    private DatabaseType databaseType = new SqliteAndroidDatabaseType();

    /**
     * The context.
     */
    private Context context = null;

    /**
     * Create a new database helper.
     * @param context The context.
     */
    public DatabaseHelper(Context context, String databaseName, int databaseVersion) {
        super(context, databaseName, null, databaseVersion);
        this.context = context;
        Log.i(LOG_TAG, "Installing database, databasename = " + databaseName + ", version = " + databaseVersion);
    }

    /**
     * Create a new database helper.
     * @param context The context.
     * @param databaseName The database name.
     * @param factory The factory.
     * @param databaseVersion The database version.
     */
    public DatabaseHelper(Context context, String databaseName, SQLiteDatabase.CursorFactory factory, int databaseVersion) {
        super(context, databaseName, factory, databaseVersion);
        this.context = context;
        Log.i(LOG_TAG, "Installing database, databasename = " + databaseName + ", version = " + databaseVersion);
    }

    @Override
    public void onCreate(SQLiteDatabase database, ConnectionSource connectionSource) {
        try {
            Log.d(LOG_TAG, "Creating the database");
            Log.d(LOG_TAG, "Database path: " + database.getPath());
            List<ITable> tables = getTables();
            if (tables == null) {
                tables = new ArrayList<>();
            }
            tables.add(0, new ITable() {
                @Override
                public Class getTableClass() {
                    return Setup.class;
                }
            });
            for(ITable table : tables) {
                TableUtils.createTable(connectionSource, table.getTableClass());
            }
        } catch (SQLException e) {
            Log.e(LOG_TAG, "Excpetion while creating the database", e);
            throw new RuntimeException("Excpetion while creating the database", e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, int oldVersion, int newVersion) {
        if (newVersion < oldVersion) {
            Log.w(LOG_TAG, "Trying to install an older database over a more recent one. Not executing update...");
            Log.d(LOG_TAG, "Database path: " + database.getPath());
            return;
        }

        Log.d(LOG_TAG, "Updating the database from version " + oldVersion + " to " + newVersion);
        Log.d(LOG_TAG, "Database path: " + database.getPath());

        List<IDatabaseUpgrade> databaseUpgrades = getDatabaseUpgrades();
        Collections.sort(databaseUpgrades, new Comparator<IDatabaseUpgrade>() {
            @Override
            public int compare(IDatabaseUpgrade lhs, IDatabaseUpgrade rhs) {
                if (lhs.getToVersion() < rhs.getToVersion()) {
                    return -1;
                } else if (lhs.getToVersion() > rhs.getToVersion()) {
                    return +1;
                }

                return 0;
            }
        });


        int upgradeSqlCount = 0;
        int upgradeSqlBlockCount = 0;
        for (IDatabaseUpgrade databaseUpgrade : databaseUpgrades) {
            if (oldVersion < databaseUpgrade.getToVersion()) {
                String[] queries = databaseUpgrade.getSqlQueries();
                for (String query : queries) {
                    try {
                        database.execSQL(query);
                    } catch (android.database.SQLException e) {
                        Log.d(LOG_TAG, "Exception while executing upgrade queries (toVersion: "
                                + databaseUpgrade.getToVersion() + ") during query: " + query, e);
                        throw new RuntimeException("Exception while executing upgrade queries (toVersion: "
                                + databaseUpgrade.getToVersion() + ") during query: " + query, e);
                    }
                    Log.d(LOG_TAG, "Executed an upgrade query to version " + databaseUpgrade.getToVersion()
                            + " with success: " + query);
                    upgradeSqlCount++;
                }
                Log.d(LOG_TAG, "Upgrade queries for version " + databaseUpgrade.getToVersion()
                        + " executed with success");
                upgradeSqlBlockCount++;
            }
        }
        if (upgradeSqlCount > 0) {
            Log.d(LOG_TAG, "All upadate queries exected with success. Total number of upgrade queries executed: "
                    + upgradeSqlCount + " in " + upgradeSqlBlockCount + " blocks");
        } else {
            Log.d(LOG_TAG, "No database upgrade queries where necessary!");
        }


        /* This is the old code for upgrading a database: dropping the old one and creating a new one...
        try {

            for(ITable table : ITable.values()) {
                TableUtils.dropTable(databaseType, connectionSource, table.getTableClass(), true);
            }
            onCreate(database);
        } catch (SQLException e) {
            Log.e(LOG_TAG, "Excpetion while updating the database from version " + oldVersion + "to " + newVersion, e);
            throw new RuntimeException("Excpetion while updating the database from version " + oldVersion + "to " + newVersion, e);
        }*/
    }

    @Override
    public void close() {
        Log.d(LOG_TAG, "Closing connection");
        super.close();
    }

    public static Date convertDateToSqliteDate(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        cal.set(Calendar.HOUR, 0);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        return cal.getTime();
    }

    public abstract List<ITable> getTables();
    public abstract List<IDatabaseUpgrade> getDatabaseUpgrades();
}
