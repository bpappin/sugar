package com.sixgreen.cube.support;

import android.content.ContentValues;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.orm.helper.NamingHelper;
import com.sixgreen.cube.Cube;
import com.sixgreen.cube.CubeConfig;
import com.sixgreen.cube.annotation.Table;
import com.sixgreen.cube.annotation.Unique;
import com.sixgreen.cube.util.NameUtil;
import com.sixgreen.cube.util.ReflectionUtil;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Created by bpappin on 16-08-23.
 */
public class CubeDataManager extends SQLiteOpenHelper {
    private final CubeConfig config;
    private final Map<Object, Long> entitiesMap = new HashMap<Object, Long>();
    private SQLiteDatabase database;
    private Context context;

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
        this.context = context;
    }

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

    //public void close() {
    //    if (database != null) {
    //        database.close();
    //    }
    //}

    public SQLiteDatabase getDatabase() {
        if (database == null) {
            database = getWritableDatabase();
        }

        return database;
    }

    public Map<Object, Long> getEntitiesMap() {
        return entitiesMap;
    }

    public CubeConfig getConfig() {
        return config;
    }

    public Context getContext() {
        return context;
    }

    public <T> Cursor getCursor(Class<T> type, String whereClause, String[] whereArgs, String groupBy, String orderBy, String limit) {
        Cursor raw = getDatabase()
                .query(NameUtil.toTableName(type), null, whereClause, whereArgs,
                        groupBy, null, orderBy, limit);
        return raw;
    }

    private void inflate(Cursor cursor, Object entity, Map<Object, Long> entitiesMap) {
        //if (DEBUG_CURSOR) {
        //    Log.d(TAG, "Row Dump (inflate): " + DatabaseUtils.dumpCurrentRowToString(cursor));
        //}
        List<Field> columns = ReflectionUtil.getTableFields(getConfig(), entity.getClass());
        if (!entitiesMap.containsKey(entity)) {
            entitiesMap.put(entity, cursor.getLong(cursor.getColumnIndex(Cube._ID)));
        }

        for (Field field : columns) {
            field.setAccessible(true);
            Class<?> fieldType = field.getType();
            if (Cube.isEntity(fieldType)) {
                // This is a joined entity.
                try {
                    long id = cursor.getLong(cursor
                            .getColumnIndex(Cube._ID/*NameUtil.toColumnName(field)*/));
                    field.set(entity, (id > 0) ? load(fieldType, id) : null);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            } else {
                ReflectionUtil.setFieldValueFromCursor(getConfig(), cursor, field, entity);
            }
        }
    }

    public <T> List<T> getEntitiesFromCursor(Cursor cursor, Class<T> type) {
        T entity;
        List<T> result = new ArrayList<T>();
        try {
            while (cursor.moveToNext()) {
                entity = type.getDeclaredConstructor().newInstance();
                inflate(cursor, entity, getEntitiesMap());
                result.add(entity);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            cursor.close();
        }

        return result;
    }

    public void executeQuery(String query, String... arguments) {
        getDatabase().execSQL(query, arguments);
    }

    public <T> T first(Class<T> type) {
        List<T> list = findWithQuery(type,
                "SELECT * FROM " +
                NameUtil.toTableName(type) + " ORDER BY " +
                Cube._ID +
                " ASC LIMIT 1");
        if (list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }

    public <T> T last(Class<T> type) {
        List<T> list = findWithQuery(type,
                "SELECT * FROM " +
                NameUtil.toTableName(type) + " ORDER BY " +
                Cube._ID + " DESC LIMIT 1");
        if (list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }

    public <T> T load(Class<T> type, long id) {
        List<T> list = find(type, QueryBuilder
                .id(), new String[]{String.valueOf(id)}, null, null, 1);
        if (list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }

    public <T> List<T> load(Class<T> type, String[] ids) {
        String whereClause =
                Cube._ID + " IN (" + QueryBuilder.generatePlaceholders(ids.length) + ")";
        return find(type, whereClause, ids);
    }

    public <T> List<T> find(Class<T> type, String whereClause, String... whereArgs) {
        return find(type, whereClause, whereArgs, null, null, 0);
    }

    public <T> List<T> find(Class<T> type, String whereClause, String[] whereArgs, String groupBy, String orderBy, int limit) {
        Cursor cursor = null;
        if (limit > 0) {
            cursor = getDatabase()
                    .query(NameUtil.toTableName(type), null, whereClause, whereArgs,
                            groupBy, null, orderBy, Integer.toString(limit));
            return getEntitiesFromCursor(cursor, type);
        } else {
            return find(type, whereClause, whereArgs, groupBy, orderBy);
        }
    }

    public <T> List<T> find(Class<T> type, String whereClause, String[] whereArgs, String groupBy, String orderBy) {
        Cursor cursor = getDatabase()
                .query(NameUtil.toTableName(type), null, whereClause, whereArgs,
                        groupBy, null, orderBy);

        return getEntitiesFromCursor(cursor, type);
    }

    //public <T> T load(Class<T> type, Integer id) {
    //    return findById(type, Long.valueOf(id));
    //}

    public long save(Object object) {
        return save(getDatabase(), object);
    }

    long save(SQLiteDatabase db, Object object) {
        Map<Object, Long> entitiesMap = getEntitiesMap();

        List<Field> columns = ReflectionUtil.getTableFields(config, object.getClass());

        ContentValues values = new ContentValues(columns.size());

        Field idField = null;
        for (Field column : columns) {
            ReflectionUtil.addFieldValueToColumn(config, values, column, object, entitiesMap);
            if (column.getName().equals("id")) {
                idField = column;
            }
        }

        boolean isSugarEntity = ReflectionUtil.isEntity(object.getClass());
        if (isSugarEntity && entitiesMap.containsKey(object)) {
            values.put("id", entitiesMap.get(object));
        }

        long id = db.insertWithOnConflict(NameUtil.toTableName(object.getClass()), null, values,
                SQLiteDatabase.CONFLICT_REPLACE);

        if (object.getClass().isAnnotationPresent(Table.class)) {
            if (idField != null) {
                idField.setAccessible(true);
                try {
                    idField.set(object, id);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            } else {
                entitiesMap.put(object, id);
            }
        }
        //else if (SugarRecord.class.isAssignableFrom(object.getClass())) {
        //    ((SugarRecord) object).setId(id);
        //}

        if (getConfig().isDebug()) {
            Log.i(Cube.TAG, object.getClass().getSimpleName() + " saved : " + id);
        }

        return id;
    }


    /**
     * Update does not automatically create a new object of the update object does not match.
     *
     * @param object
     * @return long the number of records changed by this update.
     */
    public long update(Object object) {
        return update(getDatabase(), object);
    }

    long update(SQLiteDatabase db, Object object) {
        Map<Object, Long> entitiesMap = getEntitiesMap();
        List<Field> fields = ReflectionUtil.getTableFields(getConfig(), object.getClass());
        ContentValues values = new ContentValues(fields.size());

        StringBuilder whereClause = new StringBuilder();
        List<String> whereArgs = new ArrayList<>();

        for (Field field : fields) {
            if (field.isAnnotationPresent(Unique.class)) {
                try {
                    field.setAccessible(true);
                    String columnName = NameUtil.toColumnName(field);
                    Object columnValue = field.get(object);

                    whereClause.append(QueryBuilder.column(columnName));
                    whereArgs.add(String.valueOf(columnValue));
                } catch (IllegalAccessException e) {
                    //e.printStackTrace();
                    Log.w(Cube.TAG, "Could not access field: " + field.getName(), e);
                }
            } else {
                if (!field.getName().equals("id")) {
                    ReflectionUtil
                            .addFieldValueToColumn(getConfig(), values, field, object, entitiesMap);
                }
            }
        }

        String[] whereArgsArray = whereArgs.toArray(new String[whereArgs.size()]);
        // Get SugarRecord based on Unique values
        long rowsEffected = db.update(NameUtil.toTableName(object
                .getClass()), values, whereClause
                .toString(), whereArgsArray);

        //if (rowsEffected == 0) {
        //    // FIXME This is WRONG. The save method returns an ID, but this update method should
        //    // FIXME return a count. I am not sure of the implications of changing the count to 1
        //    // FIXME at this moment, so I'll have to come back to it. -bpappin
        //    final long recordId = save(db, object);
        //    return recordId;
        //} else {
        if (rowsEffected > 0) {
            notifyChange(object.getClass());
        }
        return rowsEffected;
        //}
    }

    public <T> void notifyChange(Class<T> type) {
        notifyChange(Cube.createUri(type, null), null, false);
    }

    /**
     * Shortcut to ContentResolver.notifyChange(Uri uri, ContentObserver observer, boolean
     * syncToNetwork)
     *
     * @param uri
     * @param observer
     * @param syncToNetwork
     */
    public void notifyChange(Uri uri, ContentObserver observer, boolean syncToNetwork) {
        if (getContext() != null) {
            if (getConfig().isDebug()) {
                Log.d(Cube.TAG, "Notify of data change: " + uri);
            }
            getContext().getApplicationContext().getContentResolver()
                        .notifyChange(uri, observer, syncToNetwork);
        } else {
            Log.w(Cube.TAG, "Context not set. Unable to notify of data change: " + uri);
        }
    }


    public boolean delete(Object object) {
        Class<?> type = object.getClass();
        if (type.isAnnotationPresent(Table.class)) {
            try {
                Field field = type.getDeclaredField("id");
                field.setAccessible(true);
                Long id = (Long) field.get(object);
                if (id != null && id > 0L) {

                    boolean deleted = getDatabase().delete(NameUtil.toTableName(type), QueryBuilder
                            .id(), new String[]{
                            id.toString()
                    }) == 1;
                    if (deleted) {
                        Log.d(Cube.TAG, type.getSimpleName() + " deleted : " + id);
                        notifyChange(type, id);

                    } else {
                        Log.w(Cube.TAG, type.getSimpleName() + " was not deleted : " + id);
                    }
                    return deleted;
                } else {
                    Log.i(Cube.TAG, "Cannot delete object: " + object.getClass().getSimpleName() +
                                    " - object has not been saved");
                    return false;
                }
            } catch (NoSuchFieldException e) {
                Log.i(Cube.TAG, "Cannot delete object: " + object.getClass().getSimpleName() +
                                " - annotated object has no id");
                return false;
            } catch (IllegalAccessException e) {
                Log.i(Cube.TAG, "Cannot delete object: " + object.getClass().getSimpleName() +
                                " - can't access id");
                return false;
            }
            //} else if (SugarRecord.class.isAssignableFrom(type)) {
            //    return ((SugarRecord) object).delete();
        } else {
            Log.i(Cube.TAG, "Cannot delete object: " + object.getClass().getSimpleName() +
                            " - not an entity");
            return false;
        }
    }

    public <T> void notifyChange(Class<T> type, Long id) {
        notifyChange(Cube.createUri(type, id), null, false);
    }

    public <T> long count(Class<?> type) {
        return count(type, null, null, null, null, null);
    }

    public <T> long count(Class<?> type, String whereClause, String[] whereArgs, String groupBy, String orderBy, String limit) {
        long result = -1;
        String filter = (!TextUtils.isEmpty(whereClause)) ? " where " + whereClause : "";
        SQLiteStatement sqliteStatement;
        try {
            sqliteStatement = getDatabase().compileStatement(
                    "SELECT count(*) FROM " +
                    NameUtil.toTableName(type) + filter);
        } catch (SQLiteException e) {
            //e.printStackTrace();
            Log.w(Cube.TAG, "Could not get entity count.", e);
            return result;
        }

        if (whereArgs != null) {
            for (int i = whereArgs.length; i != 0; i--) {
                sqliteStatement.bindString(i, whereArgs[i - 1]);
            }
        }

        try {
            result = sqliteStatement.simpleQueryForLong();
        } finally {
            sqliteStatement.close();
        }

        return result;
    }

    public <T> long count(Class<?> type, String whereClause, String[] whereArgs) {
        return count(type, whereClause, whereArgs, null, null, null);
    }

    public <T> void notifyChange(Class<T> type, ContentObserver observer, boolean syncToNetwork) {
        notifyChange(Cube.createUri(type, null), observer, syncToNetwork);
    }

    public <T> void notifyChange(Class<T> type, Long id, ContentObserver observer, boolean syncToNetwork) {
        notifyChange(Cube.createUri(type, id), observer, syncToNetwork);
    }

    public <T> List<T> findWithQuery(Class<T> type, String query, String... arguments) {
        Cursor cursor = getDatabase().rawQuery(query, arguments);

        return getEntitiesFromCursor(cursor, type);
    }

    public <T> Iterator<T> findAll(Class<T> type) {
        return findAsIterator(type, null, null, null, null, null);
    }

    public <T> Iterator<T> findAsIterator(Class<T> type, String whereClause, String... whereArgs) {
        return findAsIterator(type, whereClause, whereArgs, null, null, null);
    }

    public <T> Iterator<T> findWithQueryAsIterator(Class<T> type, String query, String... arguments) {
        Cursor cursor = getDatabase().rawQuery(query, arguments);
        return new CursorIterator<>(type, cursor);
    }

    public <T> Iterator<T> findAsIterator(Class<T> type, String whereClause, String[] whereArgs, String groupBy, String orderBy, String limit) {
        Cursor cursor = getDatabase()
                .query(NamingHelper.toTableName(type), null, whereClause, whereArgs,
                        groupBy, null, orderBy, limit);
        return new CursorIterator<>(type, cursor);
    }

    class CursorIterator<E> implements Iterator<E> {
        Class<E> type;
        Cursor cursor;

        public CursorIterator(Class<E> type, Cursor cursor) {
            this.type = type;
            this.cursor = cursor;
        }

        @Override
        public boolean hasNext() {
            return cursor != null && !cursor.isClosed() && !cursor.isAfterLast();
        }

        @Override
        public E next() {
            E entity = null;
            if (cursor == null || cursor.isAfterLast()) {
                throw new NoSuchElementException();
            }

            if (cursor.isBeforeFirst()) {
                cursor.moveToFirst();
            }

            try {
                entity = type.getDeclaredConstructor().newInstance();
                inflate(cursor, entity, getEntitiesMap());
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                cursor.moveToNext();
                if (cursor.isAfterLast()) {
                    cursor.close();
                }
            }

            return entity;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
