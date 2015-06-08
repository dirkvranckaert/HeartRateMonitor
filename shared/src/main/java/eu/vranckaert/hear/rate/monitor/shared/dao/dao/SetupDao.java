package eu.vranckaert.hear.rate.monitor.shared.dao.dao;

import android.content.Context;
import eu.vranckaert.hear.rate.monitor.shared.util.ContextUtils;

/**
 * Date: 24/03/15
 * Time: 17:44
 *
 * @author Dirk Vranckaert
 */
public class SetupDao extends Dao<Setup,Integer,DatabaseHelper> implements ISetupDao {
    private static SetupDao INSTANCE;

    /**
     * This constructor should always be called in order to have a DAO!
     *
     * @param helperClazz
     * @param context
     */
    private SetupDao(Class<? extends DatabaseHelper> helperClazz, Context context) {
        super(Setup.class, helperClazz, context);
    }

    public static SetupDao getInstance(Class<? extends DatabaseHelper> helperClazz, Context context) {
        if (INSTANCE == null) {
            INSTANCE = new SetupDao(helperClazz, context);
        }

        return INSTANCE;
    }

    public void setup(int dbVersion) {
        deleteAll();
        Setup setup = new Setup();
        setup.setDatabaseVersion(dbVersion);
        setup.setAppVersionCode(ContextUtils.getCurrentApplicationVersionCode(getContext()));
        setup.setAppVersionName(ContextUtils.getCurrentApplicationVersionName(getContext()));
        save(setup);
    }
}
