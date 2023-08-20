package com.solovev.repositories;

import java.sql.SQLException;
import java.util.Collection;

public interface Repository<T> extends AutoCloseable{
    boolean add (T elem) throws SQLException;
    T delete(int elemId);
    Collection<T> takeData();
    T takeData(int elemId);
    boolean replace(T newElem);
    int size();
    /**
     * Finds max id for collection in this repo
     * @return max ID of this repo,
     */
    int lastId();

}
