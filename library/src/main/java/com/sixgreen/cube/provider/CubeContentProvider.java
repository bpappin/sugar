package com.sixgreen.cube.provider;

import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;
import android.util.SparseArray;

import com.sixgreen.cube.Cube;
import com.sixgreen.cube.CubeConfig;
import com.sixgreen.cube.util.NameUtil;

/**
 * To use this class, you must use the manifest version for configuration, because the content
 * provider may be created before the application.
 * <p/>
 * Created by bpappin on 16-03-29.
 */
public class CubeContentProvider extends android.content.ContentProvider {
    private static final String TAG = "Cube.ContentProvider";
    //private static boolean DEBUG;

    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    private static final SparseArray<Class<?>> typeCodes = new SparseArray<Class<?>>();
    private static SparseArray<String> mimeTypeCache = new SparseArray<String>();
    //private CubeConfig config;

    @Override
    public boolean onCreate() {
//config = CubeConfig.manifest(getContext());
        // XXX This must always happen first.
        //Cube.setup(getContext(),CubeConfig.manifest(getContext()));
        Cube.setup(getContext());
        //SugarContext.init(CubeConfig.manifest(getContext()));

        //final SugarConfiguration configuration = SugarContext.getSugarContext().getConfiguration();
        //DEBUG = configuration.isDebug();

        Log.d(TAG, "Debug mode enabled: " + Cube.getConfig().isDebug());

        final String authority = Cube.getConfig()
                .getAuthority();

        if(Cube.getConfig().isDebug()) {
            Log.d(TAG, "Content provider authority: " + authority);
        }

        Class<?>[] classList = Cube.getConfig().getEntityClasses();

        if(Cube.getConfig().isDebug()) {
            Log.d(TAG, "Entity classes found: " + classList.length);
        }

        //final int size = classList.size();
        for (int i = 0; i < classList.length; i++) {
            Class<?> tableClass = classList[i];
            if (Cube.getConfig().isDebug()) {
                Log.d(TAG, "Registering table for: " + tableClass.getSimpleName());
            }
            final int tableKey = (i * 2) + 1;
            final int itemKey = (i * 2) + 2;

            // content://<authority>/<table>
            uriMatcher
                    .addURI(authority, NameUtil.toTableName(tableClass)
                                               .toLowerCase(), tableKey);
            typeCodes.put(tableKey, tableClass);
            if (Cube.getConfig().isDebug()) {
                Log.d(TAG, "Registering table key: " + tableKey + " for class " +
                           tableClass.getSimpleName());
            }

            // content://<authority>/<table>/<id>
            uriMatcher.addURI(authority,
                    NameUtil.toTableName(tableClass).toLowerCase() +
                    "/#", itemKey);
            typeCodes.put(itemKey, tableClass);
            if (Cube.getConfig().isDebug()) {
                Log.d(TAG, "Registering item key: " + itemKey + " for class " +
                           tableClass.getSimpleName());
            }
        }

        return true;
    }

    @Override
    public String getType(Uri uri) {
        final int match = uriMatcher.match(uri);

        String cachedMimeType = mimeTypeCache.get(match);
        if (cachedMimeType != null) {
            return cachedMimeType;
        }

        final Class<?> type = getModelType(uri);
        final boolean single = ((match % 2) == 0);

        String mimeType = getMimeType(type, single);

        mimeTypeCache.append(match, mimeType);

        return mimeType;
    }

    private String getMimeType(Class<?> type, boolean single) {
        StringBuilder mimeTypeBufer = new StringBuilder();
        mimeTypeBufer.append("vnd");
        mimeTypeBufer.append(".");
        mimeTypeBufer.append(Cube.getConfig().getAuthority());
        mimeTypeBufer.append(".");
        mimeTypeBufer.append(single ? "item" : "dir");
        mimeTypeBufer.append("/");
        mimeTypeBufer.append("vnd");
        mimeTypeBufer.append(".");
        mimeTypeBufer.append(Cube.getConfig().getAuthority());
        mimeTypeBufer.append(".");
        mimeTypeBufer.append(NameUtil.toTableName(type));
        return mimeTypeBufer.toString();
    }


    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final Class<?> type = getModelType(uri);
        final Long id = Cube.getCubeDataManager().getWritableDatabase()
                .insert(NameUtil.toTableName(type), null, values);

        if (id != null && id > 0) {
            Uri retUri = createUri(type, id);
            notifyChange(retUri);

            return retUri;
        }

        return null;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final Class<?> type = getModelType(uri);
        final int count = Cube.getCubeDataManager().getWritableDatabase()
                .update(NameUtil.toTableName(type), values, selection, selectionArgs);

        notifyChange(uri);

        return count;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final Class<?> type = getModelType(uri);
        final int count = Cube.getCubeDataManager().getWritableDatabase()
                .delete(NameUtil.toTableName(type), selection, selectionArgs);

        notifyChange(uri);

        return count;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        final Class<?> type = getModelType(uri);

        final Cursor cursor = Cube.getCubeDataManager().getReadableDatabase().query(
                NameUtil.toTableName(type),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder);

        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }


    public static Uri createUri(Class<?> type, Long id) {
        final StringBuilder uri = new StringBuilder();
        uri.append("content://");
        //final SugarConfiguration configuration = SugarContext.getSugarContext().getConfiguration();
        uri.append(Cube.getConfig().getAuthority());
        uri.append("/");
        uri.append(NameUtil.toTableName(type).toLowerCase());

        if (id != null) {
            uri.append("/");
            uri.append(id.toString());
        }

        return Uri.parse(uri.toString());
    }


    private Class<?> getModelType(Uri uri) {
        if (Cube.getConfig().isDebug()) {
            Log.d(TAG, "Getting model type for URI: " + uri);
        }
        final int code = uriMatcher.match(uri);
        if (Cube.getConfig().isDebug()) {
            Log.d(TAG, "\tGot matcher type code: " + code);
        }
        if (code != UriMatcher.NO_MATCH) {
            if (Cube.getConfig().isDebug()) {
                Log.d(TAG, "\tType code found...");
            }
            return typeCodes.get(code);
        }
        if (Cube.getConfig().isDebug()) {
            Log.d(TAG, "\tType code NO_MATCH.");
        }
        return null;
    }

    private void notifyChange(Uri uri) {
        getContext().getContentResolver().notifyChange(uri, null);
    }


    //public SQLiteDatabase getDatabase() {
    //    return Cube.getCubeDataManager().getDatabase();
    //    //return SugarContext.getSugarContext().getSugarDb().getDB();
    //}

    public CubeConfig getConfig() {
        return Cube.getConfig();
    }
}