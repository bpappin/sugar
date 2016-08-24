package com.sixgreen.cube;

import com.sixgreen.cube.support.CubeDataManager;

/**
 * Convenience base class for objects that need to be able to save themselves.
 * There is no requirement for persistent objects to extend this
 * class; it just provides a update() method.
 *
 * <p>
 * There are two versions of the update() method. The no-args version does the
 * save operation inside its own transaction - it's the simplest way of saving
 * an object. The other version takes a Session object (see HibernateManager for
 * ways of acquiring one) and adds the save operation to the session's
 * transaction.
 *
 * Created by bpappin on 16-08-23.
 */
public abstract class Updater {

    /**
     * Save the object using a new Hibernate session. The session will be
     * created, committed and closed during the lifetime of this call.
     *
     * @throws Exception
     *
     * @throws Exception
     */
    public void update() throws Exception {
        Cube.executeUpdater(this);
    }

    /**
     * Save the object in an existing session.
     *
     * @param manager
     *            JPA EntityManager manager.
     */
    public abstract void update(CubeDataManager manager);
}
