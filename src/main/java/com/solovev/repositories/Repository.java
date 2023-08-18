package com.solovev.repositories;

import java.util.Collection;

public interface Repository<T> {
    boolean add(T elem);
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

    /**
     * Clears the repository
     * @return collection that was in this repo
     */
    Collection<T> clear();
}
