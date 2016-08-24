package com.sixgreen.cube.support;

import android.database.Cursor;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteCursorDriver;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQuery;
import android.util.Log;

import com.sixgreen.cube.Cube;

public class CubeCursorFactory implements SQLiteDatabase.CursorFactory {

    private boolean debugEnabled;

    public CubeCursorFactory() {
        this.debugEnabled = false;
    }

    public CubeCursorFactory(boolean debugEnabled) {
        this.debugEnabled = debugEnabled;
    }

    /**
     * See {@link SQLiteCursor#SQLiteCursor(SQLiteCursorDriver, String, SQLiteQuery)}.
     *
     * @param db
     * @param masterQuery
     * @param editTable
     * @param query
     */
    @Override
    public Cursor newCursor(SQLiteDatabase db, SQLiteCursorDriver masterQuery, String editTable, SQLiteQuery query) {
        if (debugEnabled) {
            Log.d(Cube.TAG, query.toString());
        }

        return new SQLiteCursor(masterQuery, editTable, query);
    }
}
