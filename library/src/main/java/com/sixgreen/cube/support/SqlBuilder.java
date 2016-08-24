package com.sixgreen.cube.support;

import android.util.Log;

//import com.orm.helper.ManifestHelper;
//import com.orm.helper.NamingHelper;
//import com.orm.util.KeyWordUtil;
//import com.orm.util.QueryBuilder;
//import com.orm.util.ReflectionUtil;
import com.sixgreen.cube.Cube;
import com.sixgreen.cube.CubeConfig;
import com.sixgreen.cube.annotation.Column;
import com.sixgreen.cube.annotation.MultiUnique;
import com.sixgreen.cube.annotation.NotNull;
import com.sixgreen.cube.annotation.Unique;
import com.sixgreen.cube.util.KeyWordUtil;
import com.sixgreen.cube.util.NameUtil;
import com.sixgreen.cube.util.ReflectionUtil;

import java.lang.reflect.Field;
import java.util.List;

/**
 * Created by bpappin on 16-08-23.
 */
public class SqlBuilder {
    public static final String NULL = " NULL";
    public static final String NOT_NULL = " NOT NULL";
    public static final String UNIQUE = " UNIQUE";

    public static String createTableSQL(CubeConfig config, Class<?> table) {
        if (config.isDebug()) {
            Log.i(Cube.TAG, "Create table if not exists");
        }
        List<Field> fields = ReflectionUtil.getTableFields(config, table);
        String tableName = NameUtil.toTableName(table);

        if (KeyWordUtil.isKeyword(tableName)) {
            //if (config.isDebug()) {
                Log.e(Cube.TAG, "SQLITE RESERVED WORD USED IN " + tableName);
            //}
        }

        StringBuilder sb = new StringBuilder("CREATE TABLE IF NOT EXISTS ");
        sb.append(tableName).append(" ( "+Cube.DEFAULT_ID_COLUMN+" INTEGER PRIMARY KEY AUTOINCREMENT ");

        for (Field column : fields) {
            String columnName = NameUtil.toColumnName(column);
            String columnType = QueryBuilder.getColumnType(column.getType());

            if (columnType != null) {
                if (columnName.equalsIgnoreCase(Cube.DEFAULT_ID_COLUMN)) {
                    continue;
                }

                if (column.isAnnotationPresent(Column.class)) {
                    Column columnAnnotation = column.getAnnotation(Column.class);
                    columnName = columnAnnotation.name();

                    sb.append(", ").append(columnName).append(" ").append(columnType);

                    if (columnAnnotation.notNull()) {
                        if (columnType.endsWith(NULL)) {
                            sb.delete(sb.length() - 5, sb.length());
                        }
                        sb.append(NOT_NULL);
                    }

                    if (columnAnnotation.unique()) {
                        sb.append(UNIQUE);
                    }

                } else {
                    sb.append(", ").append(columnName).append(" ").append(columnType);

                    if (column.isAnnotationPresent(NotNull.class)) {
                        if (columnType.endsWith(NULL)) {
                            sb.delete(sb.length() - 5, sb.length());
                        }
                        sb.append(NOT_NULL);
                    }

                    if (column.isAnnotationPresent(Unique.class)) {
                        sb.append(UNIQUE);
                    }
                }
            }
        }

        if (table.isAnnotationPresent(MultiUnique.class)) {
            String constraint = table.getAnnotation(MultiUnique.class).value();

            sb.append(", UNIQUE(");

            String[] constraintFields = constraint.split(",");
            for (int i = 0; i < constraintFields.length; i++) {
                String columnName = NameUtil.toSQLNameDefault(constraintFields[i]);
                sb.append(columnName);

                if (i < (constraintFields.length - 1)) {
                    sb.append(",");
                }
            }

            sb.append(") ON CONFLICT REPLACE");
        }

        sb.append(" ) ");
        if (config.isDebug()) {
            Log.i(Cube.TAG, "Creating table " + tableName);
        }

        return sb.toString();
    }
}
