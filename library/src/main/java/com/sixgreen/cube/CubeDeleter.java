package com.sixgreen.cube;

import com.sixgreen.cube.support.CubeDataManager;

/**
 * Convenience base class for objects that need to be able to delete themselves.
 * There is no requirement for persistent objects to extend this
 * class; it just provides a delete() method.
 * <p/>
 * There are two versions of the delete() method. The no-args version does the
 * delete operation inside its own transaction - it's the simplest way of deleteing
 * an object. The other version takes a CubeDataManager object and adds the delete operation to the
 * mangers transaction.
 * <p/>
 * Created by bpappin on 16-08-23.
 */
public abstract class CubeDeleter {

    public void delete() throws Exception {
        Cube.executeDeleter(this);
    }

    public abstract void delete(CubeDataManager manager);
}
