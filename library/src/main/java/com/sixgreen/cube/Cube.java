package com.sixgreen.cube;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.util.Log;

import com.sixgreen.cube.support.CubeDataManager;

/**
 * Created by bpappin on 16-08-11.
 */
public class Cube {
    // XXX As a library, it is beneficial for the client to get log messages from a consistent tag.
    public static final String TAG = "Cube";
    public static final String DEFAULT_ID_COLUMN = "_id";
    private static Cube instance;

    private Context context;
    private CubeConfig config;
    private CubeDataManager cubeDataManager;

    public static void setup(Context context) {
        setup(context, CubeConfig.manifest(context));
    }

    public static void setup(Context context, CubeConfig config) {
        if (instance == null) {
            instance = new Cube();
            instance.config = config;
            instance.cubeDataManager = new CubeDataManager(context, config);
        }
    }

    // XXX We don't appear to need this.
    //public static void teardown(Context context) {
    //    if (instance != null) {
    //        //instance = new Cube();
    //    }
    //
    //    // Once this is called, setup must be called again.
    //    instance = null;
    //}

    /**
     * Wipes a database, for cases where the current users needs the data to be reset.
     */
    public static void wipe() {
        if (get().cubeDataManager != null) {
            get().cubeDataManager.onReset();
        }
    }

    public static Cube get() {
        if (instance == null) {
            Log.w(TAG, "Cube has not been initialized.");
        }
        return instance;
    }

    public static CubeConfig getConfig() {
        return get().config;
    }
    
    public static CubeDataManager getCubeDataManager() {
        return get().cubeDataManager;
    }


    public static <T> T executeLoader(final Loader<T> loader) throws Exception {
        T result = null;
        CubeDataManager manager = null;
        try {
            manager = getCubeDataManager();
            beginTransaction(manager);
            result = loader.load(manager);
        } catch (final Exception e) {
            Log.e(TAG, "Unable to execute loader: "
                       + loader.getClass().getSimpleName(), e);
            throw e;
        } finally {
            finishTransaction(manager);
        }
        return result;
    }

    public static void executeDeleter(final Deleter deleter) throws Exception {
        CubeDataManager manager = null;
        try {
            manager = getCubeDataManager();
            beginTransaction(manager);
            deleter.delete(manager);
        } catch (final Exception e) {
            Log.e(TAG, "Unable to execute deleter: "
                       + deleter.getClass().getSimpleName(), e);
            throw e;
        } finally {
            finishTransaction(manager);
        }
    }

    public static void executeSaver(final Saver saver) throws Exception {
        //EntityTransaction transaction = null;
        CubeDataManager manager = null;
        try {
            manager = getCubeDataManager();
            beginTransaction(manager);
            saver.save(manager);
        } catch (final Exception e) {
            Log.e(TAG, "Unable to execute saver: "
                       + saver.getClass().getSimpleName(), e);
            throw e;
        } finally {
            finishTransaction(manager);
        }
    }
    

    public static void executeUpdater(final Updater updater) throws Exception {
        CubeDataManager manager = null;
        try {
            manager = getCubeDataManager();
            beginTransaction(manager);
            updater.update(manager);
            successfulTransaction(manager);
        } catch (final Exception e) {
            Log.e(TAG, "Unable to execute updater: "
                       + updater.getClass().getSimpleName(), e);
            throw e;
        } finally {
            finishTransaction(manager);
        }
    }
    
    private static void successfulTransaction(CubeDataManager manager) throws
                                                                       IllegalStateException {
        SQLiteDatabase sqLiteDatabase = manager.getDatabase();
        sqLiteDatabase.setTransactionSuccessful();
    }
    
    private static void beginTransaction(CubeDataManager manager) {
        SQLiteDatabase sqLiteDatabase = manager.getDatabase();
        if (VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN) {
            if (sqLiteDatabase.isWriteAheadLoggingEnabled()) {
                sqLiteDatabase.beginTransactionNonExclusive();
            } else {
                sqLiteDatabase.beginTransaction();
            }
        } else {
            sqLiteDatabase.beginTransaction();
            sqLiteDatabase.setLockingEnabled(false);
        }
    }


    private static void finishTransaction(CubeDataManager manager) {
        SQLiteDatabase sqLiteDatabase = manager.getDatabase();
        //sqLiteDatabase.yieldIfContendedSafely()
        sqLiteDatabase.endTransaction();
        sqLiteDatabase.setLockingEnabled(true);
    }
    
}
