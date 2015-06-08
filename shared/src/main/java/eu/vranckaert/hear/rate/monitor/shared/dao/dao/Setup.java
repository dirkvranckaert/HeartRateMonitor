package eu.vranckaert.hear.rate.monitor.shared.dao.dao;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Date: 24/03/15
 * Time: 17:41
 *
 * @author Dirk Vranckaert
 */
@DatabaseTable(tableName = "DATABASESTARTUPCONFIGURATIONTABLE")
public class Setup {
    @DatabaseField
    int databaseVersion;
    @DatabaseField
    int appVersionCode;
    @DatabaseField
    String appVersionName;

    public int getDatabaseVersion() {
        return databaseVersion;
    }

    public void setDatabaseVersion(int databaseVersion) {
        this.databaseVersion = databaseVersion;
    }

    public int getAppVersionCode() {
        return appVersionCode;
    }

    public void setAppVersionCode(int appVersionCode) {
        this.appVersionCode = appVersionCode;
    }

    public String getAppVersionName() {
        return appVersionName;
    }

    public void setAppVersionName(String appVersionName) {
        this.appVersionName = appVersionName;
    }
}
