package com.sixgreen.cube.support;

import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.sixgreen.cube.Cube;

/**
 * Created by bpappin on 16-08-23.
 */
public class CubeErrorHandler implements DatabaseErrorHandler {

    /**
     * The method invoked when database corruption is detected.
     *
     * @param dbObj
     *         the {@link SQLiteDatabase} object representing the database on which corruption
     *         is detected.
     */
    @Override
    public void onCorruption(SQLiteDatabase dbObj) {
        Log.e(Cube.TAG, "Database corruption has been detected at: " + dbObj.getPath());
    }
}
