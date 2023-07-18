package com.solovev.repositories;

import com.solovev.model.IdHolder;

import java.util.Collection;
import java.util.function.ToIntFunction;

public interface Repository<T> {
    boolean add(T elem);
    boolean delete(int elemId);
    Collection<T> takeData();
    T takeData(int elemId);
    boolean replace(T newElem);
    void save();
}
