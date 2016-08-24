package com.sixgreen.cube.support;

import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.sixgreen.cube.Cube;
import com.sixgreen.cube.CubeConfig;
import com.sixgreen.cube.util.NameUtil;

/**
 * Based on the SchemaGenerator from Sugar ORM.
 * <p/>
 * Created by bpappin on 16-08-23.
 */
public class SchemaGenerator {

    private CubeConfig config;

    public SchemaGenerator(CubeConfig config) {
        super();
        this.config = config;
    }

    public static SchemaGenerator get(CubeConfig config) {
        final SchemaGenerator schemaGenerator = new SchemaGenerator(config);
        return schemaGenerator;
    }

    public void doCreate(SQLiteDatabase db) {
        if (config.isDebug()) {
            Log.i(Cube.TAG, "Creating database tables from entities...");
        }
        Class<?>[] entityClasses = config.getEntityClasses();
        for (Class<?> entity : entityClasses) {
            createTable(entity, db);
            //afterTableCreated(domain,sqLiteDatabase);
        }
    }
    

    public void doUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (config.isDebug()) {
            Log.i(Cube.TAG, "Upgrading database from " + oldVersion + " to " + newVersion + "...");
        }
        dropAllTables(db);
        doCreate(db);
    }


    public void doDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (config.isDebug()) {
            Log.i(Cube.TAG,
                    "Downgrading database from " + oldVersion + " to " + newVersion + "...");
        }
        dropAllTables(db);
        doCreate(db);
    }

    public void doManualReset(SQLiteDatabase db) {
        if (config.isDebug()) {
            Log.i(Cube.TAG, "Resetting database manually...");
        }
        dropAllTables(db);
        doCreate(db);
    }

    public void dropAllTables(SQLiteDatabase db) {
        if (config.isDebug()) {
            Log.i(Cube.TAG, "Dropping all database tables...");
        }
        Class<?>[] entityClasses = config.getEntityClasses();
        for (Class<?> entity : entityClasses) {
            final String sql = "DROP TABLE IF EXISTS " + NameUtil.toTableName(entity);
            if (config.isLogQueries()) {
                Log.d(Cube.TAG, sql);
            }
            db.execSQL(sql);
        }
    }

    private void createTable(Class<?> entity, SQLiteDatabase db) {
        String createSQL = SqlBuilder.createTableSQL(config, entity);

        if (!createSQL.isEmpty()) {
            try {
                if (config.isLogQueries()) {
                    Log.d(Cube.TAG, createSQL);
                }
                db.execSQL(createSQL);
            } catch (SQLException e) {
                Log.e(Cube.TAG, "Unable to create table from entity.", e);
                //e.printStackTrace();
            }
        }
    }
    

}
