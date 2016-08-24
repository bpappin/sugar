package com.sixgreen.cube.support;

import com.sixgreen.cube.CubeConfig;

/**
 * Created by bpappin on 16-08-11.
 */
public class CubeManifestConfig extends CubeConfig {
    private String databaseName;
    private int version;
    private String authority;
    private Class<?>[] entities;

    public CubeManifestConfig(String authority, String databaseName, Class<?>[] entities, int version) {
        this.authority = authority;
        this.databaseName = databaseName;
        this.entities = entities;
        this.version = version;
    }

    @Override
    public String getDatabaseName() {
        return databaseName;
    }

    /**
     * @return Class<?>[] an array of the entity classes for the schema.
     */
    @Override
    public Class<?>[] getEntityClasses() {
        return entities;
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


    @Override
    public int getVersion() {
        return version;
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
