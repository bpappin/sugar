package com.sixgreen.cube.support;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.sixgreen.cube.CubeConfig;

/**
 * Created by bpappin on 16-08-23.
 */
public class CubeDataManager extends SQLiteOpenHelper {
    private final CubeConfig config;
    private SQLiteDatabase database;

    ///**
    // * Create a helper object to create, open, and/or manage a database.
    // * This method always returns very quickly.  The database is not actually
    // * created or opened until one of {@link #getWritableDatabase} or
    // * {@link #getReadableDatabase} is called.
    // *
    // * @param context
    // *         to use to open or create the database
    // * @param name
    // *         of the database file, or null for an in-memory database
    // * @param factory
    // *         to use for creating cursor objects, or null for the default
    // * @param version
    // *         number of the database (starting at 1); if the database is older,
    // *         {@link #onUpgrade} will be used to upgrade the database; if the database is
    // *         newer, {@link #onDowngrade} will be used to downgrade the database
    // */
    //public CubeDataManager(Context context, String name, CursorFactory factory, int version) {
    //    super(context, name, factory, version);
    //}

    /**
     * Create a helper object to create, open, and/or manage a database.
     * The database is not actually created or opened until one of
     * {@link #getWritableDatabase} or {@link #getReadableDatabase} is called.
     * <p/>
     * <p>Accepts input param: a concrete instance of {@link DatabaseErrorHandler} to be
     * used to handle corruption when sqlite reports database corruption.</p>
     *
     * @param context
     *         to use to open or create the database
     * @param config
     *         the configuration of the Cube framework that contains the information for opening
     *         the database and setting up its parameters.
     */
    public CubeDataManager(Context context, CubeConfig config) {
        super(context, config.getDatabaseName(), config.getCursorFactory(), config
                .getVersion(), config.getDatabaseErrorHandler());
        this.config = config;
    }

    public SQLiteDatabase getDatabase() {
        if (database == null) {
            database = getWritableDatabase();
        }

        return database;
    }

    //public void close() {
    //    if (database != null) {
    //        database.close();
    //    }
    //}

    /**
     * Called when the database is created for the first time. This is where the
     * creation of tables and the initial population of the tables should happen.
     *
     * @param db
     *         The database.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        SchemaGenerator schemaGenerator = SchemaGenerator.get(config);
        schemaGenerator.doCreate(db);
    }

    /**
     * Called when the database needs to be upgraded. The implementation
     * should use this method to drop tables, add tables, or do anything else it
     * needs to upgrade to the new schema version.
     * <p/>
     * <p>
     * The SQLite ALTER TABLE documentation can be found
     * <a href="http://sqlite.org/lang_altertable.html">here</a>. If you add new columns
     * you can use ALTER TABLE to insert them into a live table. If you rename or remove columns
     * you can use ALTER TABLE to rename the old table, then create the new table and then
     * populate the new table with the contents of the old table.
     * </p><p>
     * This method executes within a transaction.  If an exception is thrown, all changes
     * will automatically be rolled back.
     * </p>
     *
     * @param db
     *         The database.
     * @param oldVersion
     *         The old database version.
     * @param newVersion
     *         The new database version.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        SchemaGenerator schemaGenerator = SchemaGenerator.get(config);
        schemaGenerator.doUpgrade(db, oldVersion, newVersion);
    }

    /**
     * Called when the database needs to be downgraded. This is strictly similar to
     * {@link #onUpgrade} method, but is called whenever current version is newer than requested
     * one.
     * However, this method is not abstract, so it is not mandatory for a customer to
     * implement it. If not overridden, default implementation will reject downgrade and
     * throws SQLiteException
     * <p/>
     * <p>
     * This method executes within a transaction.  If an exception is thrown, all changes
     * will automatically be rolled back.
     * </p>
     *
     * @param db
     *         The database.
     * @param oldVersion
     *         The old database version.
     * @param newVersion
     *         The new database version.
     */
    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //super.onDowngrade(db, oldVersion, newVersion);
        SchemaGenerator schemaGenerator = SchemaGenerator.get(config);
        schemaGenerator.doDowngrade(db, oldVersion, newVersion);
    }

    public void onReset() {
        SchemaGenerator schemaGenerator = SchemaGenerator.get(config);
        schemaGenerator.doManualReset(getDatabase());
    }

}
