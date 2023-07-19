package com.solovev.repositories;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.solovev.model.IdHolder;
import com.solovev.util.Constants;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Class to store values as a repository
 * @param <T> must be serializable via Jackson library
 */
public abstract class AbstractRepository<T extends IdHolder> implements Repository<T> {
    private ObjectMapper objectMapper = new ObjectMapper();
    private Set<T> values = new HashSet<>();
    private File fileToStoreData;

    public AbstractRepository(Path path) throws IOException {
        if(Files.exists(path)) {
            fileToStoreData = path.toFile();
            values = objectMapper.readValue(fileToStoreData, new TypeReference<>() {
            });
        }
    }

    /**
     * Adds student to the repo;
     * Only unique students will be added
     * Note: it changes initial student ID!
     * @param elem student to add
     * @return true if student was successfully added, false otherwise;
     */
    @Override
    public boolean add(T elem) {
        int maxId = values.stream().mapToInt(IdHolder::getId).max().orElse(-1);
        boolean addSuccess = values.add(elem);
        if (addSuccess) {
            elem.setId(maxId + 1);
            save();
        }
        return addSuccess;
    }

    public boolean delete(int elemId) {
        Optional<T> foundStudent = values
                .stream()
                .filter(idHolder -> idHolder.getId() == elemId)
                .findFirst();

        foundStudent.ifPresent(
                idHolder -> {
                    values.remove(idHolder);
                    save();
                }
        );
        return foundStudent.isPresent();

    }


    @Override
    public Collection<T> takeData() {
        return values;
    }
    /**
     * Finds object in the collection by this id
     * @return first found Object with this id or null if nothing has been found
     */
    @Override
    public T takeData(int id) {
        return values
                .stream()
                .filter(idHolder -> idHolder.getId() == id)
                .findAny()
                .orElse(null);
    }

    /**
     * Replace first found object with the ID of the given object with the given object;
     * @param newElem student to add to the collection to replace the old one with the new student ID!
     * @return true if object with new id was found and replaced, false otherwise
     */
    @Override
    public boolean replace(T newElem) {
        return delete(newElem.getId())
                && values.add(newElem);
    }

    @Override
    public void save() {
        try {
            objectMapper.writeValue(fileToStoreData, values);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
