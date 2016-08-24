package com.sixgreen.cube.support;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import com.sixgreen.cube.Cube;
import com.sixgreen.cube.CubeConfig;


/**
 * Helper class for accessing properties in the AndroidManifest
 */
public final class CubeManifest {
    //private static final String LOG_TAG = "CubeManifest";

    private static boolean queryLogEnabled;
    private static boolean debugEnabled;

    //<meta-data android:name="DATABASE" android:value="sugar_example.db" />
    //<meta-data android:name="VERSION" android:value="2" />
    //<meta-data android:name="QUERY_LOG" android:value="true" />

    /**
     * Key for the database name meta data.
     */
    public final static String METADATA_DATABASE = "CUBE_DATABASE";

    /**
     * Key for the database version meta data.
     */
    public final static String METADATA_VERSION = "CUBE_VERSION";
    public final static String METADATA_QUERY_LOG = "CUBE_QUERY_LOG";
    private static final String METADATA_DEBUG = "CUBE_DEBUG";
    private static final String METADATA_AUTHORITY = "CUBE_AUTHORITY";
    private static final String METADATA_MODEL_RESID = "CUBE_MODEL_RESID";
    private static final String METADATA_CONFIG_CLASS = "CUBE_CONFIG_CLASS";

    /**
     * The default name for the database unless specified in the AndroidManifest.
     */
    public final static String DATABASE_DEFAULT_NAME_SUFFIX = "_cube.db";


    //Prevent instantiation
    private CubeManifest() {
    }

    public static CubeConfig getConfig(Context context) {
        Class<? extends CubeConfig> cfg = getConfigurationClass(context);
        if(cfg != null){
            try {
                return cfg.newInstance();
            } catch (Exception e) {
                //e.printStackTrace();
                Log.e(Cube.TAG, "Could not create new Cube Config class.", e);
                throw new RuntimeException(e);
            }
        }else {
            return getCubeConfigFromManifest(context);
        }
    }

    private static CubeConfig getCubeConfigFromManifest(Context context) {
        final CubeManifestConfig cubeConfig = new CubeManifestConfig(
                getAuthority(context),
                getDatabaseName(context),
                getModels(context),
                getDatabaseVersion(context));
        cubeConfig.setDebug(isDebugEnabled(context));
        cubeConfig.setLogQueries(isLogQueriesEnabled(context));
        return cubeConfig;
    }

    /**
     * Grabs the database version from the manifest.
     *
     * @return the database version as specified by the {@link #METADATA_VERSION} version or 1 of
     * not present
     */
    public static int getDatabaseVersion(Context context) {
        Integer databaseVersion = getMetaDataInteger(context, METADATA_VERSION);

        if ((databaseVersion == null) || (databaseVersion == 0)) {
            databaseVersion = 1;
        }

        return databaseVersion;
    }

    /**
     * Grabs the database ContentProvider authority from the manifest.
     *
     * @return the database version as specified by the {@link #METADATA_VERSION} version or 1 of
     * not present
     */
    public static String getAuthority(Context context) {
        String authority = getMetaDataString(context, METADATA_AUTHORITY);

        //if ((authority == null) || (databaseVersion == 0)) {
        //    databaseVersion = 1;
        //}

        return authority;
    }

    ///**
    // * Grabs the domain name of the model classes from the manifest.
    // *
    // * @return the package String that Sugar uses to search for model classes
    // */
    //public static String getDomainPackageName(Context context) {
    //    String domainPackageName = getMetaDataString(context, METADATA_DOMAIN_PACKAGE_NAME);
    //
    //    if (domainPackageName == null) {
    //        domainPackageName = "";
    //    }
    //
    //    return domainPackageName;
    //}

    /**
     * Grabs the name of the database file specified in the manifest.
     *
     * @return the value for the {@value #METADATA_DATABASE} meta data in the AndroidManifest or
     * {@link #DATABASE_DEFAULT_NAME_SUFFIX} if not present
     */
    public static String getDatabaseName(Context context) {
        String databaseName = getMetaDataString(context, METADATA_DATABASE);

        if (databaseName == null) {
            databaseName = context.getPackageName() + DATABASE_DEFAULT_NAME_SUFFIX;
        }

        return databaseName;
    }

    //public static String getDbName() {
    //    return getDatabaseName();
    //}

    /**
     * @return true if the query log flag is enabled
     */
    public static boolean isLogQueriesEnabled(Context context) {
        return getMetaDataBoolean(context, METADATA_QUERY_LOG);
    }

    /**
     * @return true if the debug flag is enabled
     */
    public static boolean isDebugEnabled(Context context) {
        return getMetaDataBoolean(context, METADATA_DEBUG);
    }

    /**
     * Returns the list of classes defined in the StringArray resource specified with
     * CUBE_MODEL_RESID.
     *
     * @param context
     * @return
     */
    public static Class<?>[] getModels(Context context) {
        boolean debug = isDebugEnabled(context);


        Integer resId = getMetaDataInteger(context, METADATA_MODEL_RESID);
        if (debug) {
            Log.d(Cube.TAG, "Model data resource id: " + resId);
        }

        if (resId == null || resId == 0) {
            return new Class<?>[0];
        }

        String[] classNames = context.getResources().getStringArray(resId);


        if (classNames == null) {
            if (debug) {
                Log.d(Cube.TAG, "Model resource array not found, creating zero length array.");
            }
            classNames = new String[0];
        }

        if (debug) {
            Log.d(Cube.TAG, "Model resource array length: " + classNames.length);
        }

        Class<?>[] modelTypes = new Class<?>[classNames.length];
        for (int i = 0; i < classNames.length; i++) {
            if (debug) {
                Log.d(Cube.TAG, "Model resource processing: " + classNames[i]);
            }
            try {
                modelTypes[i] = Class
                        .forName(classNames[i], true, context.getClass().getClassLoader());
            } catch (Throwable e) {
                Log.e(Cube.TAG, "Can't create class: " + classNames[i], e);
            }
        }

        return modelTypes;
    }

    public static Class<? extends CubeConfig> getConfigurationClass(Context context) {
        String className = getMetaDataString(context, METADATA_CONFIG_CLASS);
        try {
            if (className != null) {
                Class<?> c = Class.forName(className);
                if (CubeConfig.class.isAssignableFrom(c)) {
                    return (Class<? extends CubeConfig>) c;
                }
            }
        } catch (ClassNotFoundException e) {
            //throw new RuntimeException(e);
            Log.e(Cube.TAG, "Cube Config class was not found.", e);
        }

        return null;

    }

    private static String getMetaDataString(Context context, String name) {
        PackageManager pm = context.getPackageManager();
        String value = null;

        try {
            ApplicationInfo ai = pm
                    .getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            value = ai.metaData.getString(name);
        } catch (Exception e) {
            Log.d(Cube.TAG, "Couldn't find Cube manifest config value: " + name);
        }

        return value;
    }

    private static int getMetaDataInteger(Context context, String name) {
        PackageManager pm = context.getPackageManager();
        int value = 0;

        try {
            ApplicationInfo ai = pm
                    .getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            value = ai.metaData.getInt(name);
        } catch (Exception e) {
            Log.d(Cube.TAG, "Couldn't find Cube manifest config value: " + name);
        }

        return value;
    }

    private static boolean getMetaDataBoolean(Context context, String name) {
        PackageManager pm = context.getPackageManager();
        boolean value = false;

        try {
            ApplicationInfo ai = pm
                    .getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            value = ai.metaData.getBoolean(name);
        } catch (Exception e) {
            Log.d(Cube.TAG, "Couldn't find Cube manifest config value: " + name);
        }

        return value;
    }
}
