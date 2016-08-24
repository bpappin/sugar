package com.sixgreen.cube.support;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import com.orm.util.ContextUtil;
import com.sixgreen.cube.Cube;
import com.sixgreen.cube.CubeConfig;

import static com.orm.util.ContextUtil.getPackageManager;
import static com.orm.util.ContextUtil.getPackageName;

/**
 * Helper class for accessing properties in the AndroidManifest
 */
public final class CubeManifest {
    //private static final String LOG_TAG = "CubeManifest";

    private static boolean queryLogEnabled;
    private static boolean debugEnabled;

    /**
     * Key for the database name meta data.
     */
    public final static String METADATA_DATABASE = "DATABASE";

    /**
     * Key for the database version meta data.
     */
    public final static String METADATA_VERSION = "VERSION";
    public final static String METADATA_DOMAIN_PACKAGE_NAME = "DOMAIN_PACKAGE_NAME";
    public final static String METADATA_QUERY_LOG = "QUERY_LOG";
    private static final String METADATA_DEBUG = "DEBUG_CUBE";

    /**
     * The default name for the database unless specified in the AndroidManifest.
     */
    public final static String DATABASE_DEFAULT_NAME_SUFFIX = "_cube.db";


    //Prevent instantiation
    private CubeManifest() {
    }

    public static CubeConfig get() {
        final CubeManifestConfig cubeConfig = new CubeManifestConfig();
        cubeConfig.setDebug(isDebugEnabled());
        cubeConfig.setLogQueries(isLogQueriesEnabled());
        cubeConfig.setDatabaseName(getDatabaseName());
        cubeConfig.setVersion(getDatabaseVersion());
        return cubeConfig;
    }

    /**
     * Grabs the database version from the manifest.
     *
     * @return the database version as specified by the {@link #METADATA_VERSION} version or 1 of
     * not present
     */
    public static int getDatabaseVersion() {
        Integer databaseVersion = getMetaDataInteger(METADATA_VERSION);

        if ((databaseVersion == null) || (databaseVersion == 0)) {
            databaseVersion = 1;
        }

        return databaseVersion;
    }

    /**
     * Grabs the domain name of the model classes from the manifest.
     *
     * @return the package String that Sugar uses to search for model classes
     */
    public static String getDomainPackageName() {
        String domainPackageName = getMetaDataString(METADATA_DOMAIN_PACKAGE_NAME);

        if (domainPackageName == null) {
            domainPackageName = "";
        }

        return domainPackageName;
    }

    /**
     * Grabs the name of the database file specified in the manifest.
     *
     * @return the value for the {@value #METADATA_DATABASE} meta data in the AndroidManifest or
     * {@link #DATABASE_DEFAULT_NAME_SUFFIX} if not present
     */
    public static String getDatabaseName() {
        String databaseName = getMetaDataString(METADATA_DATABASE);

        if (databaseName == null) {
            databaseName = ContextUtil.getPackageName() + DATABASE_DEFAULT_NAME_SUFFIX;
        }

        return databaseName;
    }

    //public static String getDbName() {
    //    return getDatabaseName();
    //}

    /**
     * @return true if the query log flag is enabled
     */
    public static boolean isLogQueriesEnabled() {
        return getMetaDataBoolean(METADATA_QUERY_LOG);
    }

    /**
     * @return true if the debug flag is enabled
     */
    public static boolean isDebugEnabled() {
        return getMetaDataBoolean(METADATA_DEBUG);
    }

    private static String getMetaDataString(String name) {
        PackageManager pm = getPackageManager();
        String value = null;

        try {
            ApplicationInfo ai = pm
                    .getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
            value = ai.metaData.getString(name);
        } catch (Exception e) {
            if (CubeManifest.isLogQueriesEnabled()) {
                Log.d(Cube.TAG, "Couldn't find manifest config value: " + name);
            }
        }

        return value;
    }

    private static int getMetaDataInteger(String name) {
        PackageManager pm = getPackageManager();
        int value = 0;

        try {
            ApplicationInfo ai = pm
                    .getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
            value = ai.metaData.getInt(name);
        } catch (Exception e) {
            if (CubeManifest.isLogQueriesEnabled()) {
                Log.d(Cube.TAG, "Couldn't find config value: " + name);
            }
        }

        return value;
    }

    private static boolean getMetaDataBoolean(String name) {
        PackageManager pm = getPackageManager();
        boolean value = false;

        try {
            ApplicationInfo ai = pm
                    .getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
            value = ai.metaData.getBoolean(name);
        } catch (Exception e) {
            Log.d(Cube.TAG, "Couldn't find manifest config value: " + name);
        }

        return value;
    }
}
