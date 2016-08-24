package com.sixgreen.cube;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase.CursorFactory;

import com.sixgreen.cube.support.CubeCursorFactory;
import com.sixgreen.cube.support.CubeDataManager;
import com.sixgreen.cube.support.CubeErrorHandler;
import com.sixgreen.cube.support.CubeManifest;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by bpappin on 16-08-11.
 */
public abstract class CubeConfig {
    private boolean debug;
    private boolean logQueries;
    private int version;
    private boolean writeAheadLoggingEnabled = true;
    private DatabaseErrorHandler databaseErrorHandler = new CubeErrorHandler();
    private CursorFactory cursorFactory;

    private final static Map<Class<?>, List<Field>> fieldCache = new HashMap<>();

    public static final CubeConfig manifest(Context context) {
        return CubeManifest.getConfig(context);
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public boolean isLogQueries() {
        return logQueries;
    }

    public void setLogQueries(boolean logQueries) {
        this.logQueries = logQueries;
    }

    /**
     * @return
     * @see {https://developer.android.com/reference/android/database/sqlite/SQLiteDatabase.html#enableWriteAheadLogging()}
     */
    public boolean isWriteAheadLoggingEnabled() {
        return writeAheadLoggingEnabled;
    }

    /**
     * @param writeAheadLoggingEnabled
     * @see {https://developer.android.com/reference/android/database/sqlite/SQLiteDatabase.html#enableWriteAheadLogging()}
     */
    public void setWriteAheadLoggingEnabled(boolean writeAheadLoggingEnabled) {
        this.writeAheadLoggingEnabled = writeAheadLoggingEnabled;
    }

    /**
     * the {@link DatabaseErrorHandler} to be used when sqlite reports database
     */
    public DatabaseErrorHandler getDatabaseErrorHandler() {
        return databaseErrorHandler;
    }

    /**
     * the {@link DatabaseErrorHandler} to be used when sqlite reports database
     */
    public void setDatabaseErrorHandler(DatabaseErrorHandler databaseErrorHandler) {
        this.databaseErrorHandler = databaseErrorHandler;
    }

    /**
     * The class to use for creating cursor objects, or null for the default
     *
     * @return CursorFactory
     */
    public CursorFactory getCursorFactory() {
        if (this.cursorFactory != null) {
            return this.cursorFactory;
        }
        return new CubeCursorFactory(isLogQueries());
    }

    /**
     * The class to use for creating cursor objects, or null for the default
     *
     * @param cursorFactory
     *         CursorFactory
     */
    public void setCursorFactory(CursorFactory cursorFactory) {
        this.cursorFactory = cursorFactory;
    }

    public static List<Field> getFieldCache(Class<?> clazz) {
        if (fieldCache.containsKey(clazz)) {
            List<Field> list = fieldCache.get(clazz);
            return Collections.synchronizedList(list);
        }

        return null;
    }

    public void setFieldCache(Class<?> table, List<Field> toStore) {
        fieldCache.put(table, toStore);
    }

    /**
     * The version number of the database (starting at 1); if the database is older,
     * {@link CubeDataManager#onUpgrade} will be used to upgrade the database; if the database is
     * newer, {@link CubeDataManager#onDowngrade} will be used to downgrade the database
     *
     * @return int the version of this database
     */
    public abstract int getVersion();

    /**
     * The name of the database file, or null for an in-memory database.
     *
     * @return String the name of this database
     */
    public abstract String getDatabaseName();

    /**
     * @return Class<?>[] an array of the entity classes for the schema.
     */
    public abstract Class<?>[] getEntityClasses();

    /**
     * The content provider authority.
     *
     * @return String
     */
    public abstract String getAuthority();

    ///**
    // * Key for the database name meta data.
    // */
    //public final static String METADATA_DATABASE = "DATABASE";
    //
    ///**
    // * Key for the database version meta data.
    // */
    //public final static String METADATA_VERSION = "VERSION";
    //public final static String METADATA_DOMAIN_PACKAGE_NAME = "DOMAIN_PACKAGE_NAME";
    //public final static String METADATA_QUERY_LOG = "QUERY_LOG";
    //
    ///**
    // * The default name for the database unless specified in the AndroidManifest.
    // */
    //public final static String DATABASE_DEFAULT_NAME = "Sugar.db";

}
