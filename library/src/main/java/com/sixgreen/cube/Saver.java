package com.sixgreen.cube;

import com.sixgreen.cube.support.CubeDataManager;

/**
 * Convenience base class for objects that need to be able to save themselves.
 * There is no requirement for persistent objects to extend this
 * class; it just provides a save() method.
 *
 * <p>
 * There are two versions of the save() method. The no-args version does the
 * save operation inside its own transaction - it's the simplest way of saving
 * an object. The other version takes a Session object (see HibernateManager for
 * ways of acquiring one) and adds the save operation to the session's
 * transaction.
 *
 * Created by bpappin on 16-08-23.
 */
public abstract class Saver {

    public Saver() {
        super();
    }

    public void save() throws Exception {
        Cube.executeSaver(this);
    }

    public abstract void save(CubeDataManager manager);
}