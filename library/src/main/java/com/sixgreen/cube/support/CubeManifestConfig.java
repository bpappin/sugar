package com.sixgreen.cube.support;

import com.sixgreen.cube.CubeConfig;

/**
 * Created by bpappin on 16-08-11.
 */
public class CubeManifestConfig extends CubeConfig {
    private String databaseName;
    private int version;
    private String authority;

    @Override
    public String getDatabaseName() {
        return databaseName;
    }

    /**
     * @return Class<?>[] an array of the entity classes for the schema.
     */
    @Override
    public Class<?>[] getEntityClasses() {
        return new Class<?>[0];
    }

    /**
     * The content provider authority.
     *
     * @return String
     */
    @Override
    public String getAuthority() {
        return authority;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    @Override
    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

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
