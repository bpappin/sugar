package com.sixgreen.cube;

import com.sixgreen.cube.support.CubeDataManager;

/**
 * Created by bpappin on 16-08-23.
 */
public abstract class Loader<T> {
    public abstract T load(CubeDataManager manager);

    public T load() throws Exception {
        return Cube.executeLoader(this);
    }
}
