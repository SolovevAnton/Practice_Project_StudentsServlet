package com.solovev.repositories;

import java.util.Collection;

public interface Repository<T> {
    boolean add(T elem);
    T delete(int elemId);
    Collection<T> takeData();
    T takeData(int elemId);
    boolean replace(T newElem);
    void save();
    int size();
    /**
     * Finds max id for collection in this repo
     * @return max ID of this repo,
     */
    int lastId();
}
