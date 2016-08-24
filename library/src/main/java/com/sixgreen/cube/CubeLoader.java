package com.sixgreen.cube;

import com.sixgreen.cube.support.CubeDataManager;

/**
 * Convenience base class for objects that need to be able to load themselves.
 * There is no requirement for persistent objects to extend this
 * class; it just provides a load() method.
 * <p/>
 * There are two versions of the load() method. The no-args version does the
 * load operation inside its own transaction - it's the simplest way of loading
 * an object. The other version takes a CubeDataManager object and adds the load operation to the
 * mangers transaction.
 * <p/>
 * Created by bpappin on 16-08-23.
 */
public abstract class CubeLoader<T> {
    public abstract T load(CubeDataManager manager);

    public T load() throws Exception {
        return Cube.executeLoader(this);
    }
}
