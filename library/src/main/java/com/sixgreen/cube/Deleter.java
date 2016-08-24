package com.sixgreen.cube;

import com.sixgreen.cube.support.CubeDataManager;

/**
 * Created by bpappin on 16-08-23.
 */
public abstract class Deleter {

    public void delete() throws Exception {
        Cube.executeDeleter(this);
    }

    public abstract void delete(CubeDataManager manager);
}
