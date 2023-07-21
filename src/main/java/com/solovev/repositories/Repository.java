package com.solovev.repositories;

import java.util.Collection;

public interface Repository<T> {
    boolean add(T elem);
    boolean delete(int elemId);
    Collection<T> takeData();
    T takeData(int elemId);
    boolean replace(T newElem);
    void save();
}
