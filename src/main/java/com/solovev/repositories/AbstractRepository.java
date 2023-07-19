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
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
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
     * Adds element to the repo;
     * Only unique elementwill be added
     * Note: it changes initial element ID!
     * @param elem element to add
     * @return true if element was successfully added, false otherwise;
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

    /**
     * Deletes first found object with the given ID
     * @param elemId id of the element to remove
     * @return true if element with this ID was found and removed, false otherwise
     */
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

    /**
     * method to get content of the repository
     * @return Original collection(not the copy of it)
     */
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
     * @param newElem object to add to the collection to replace the old one with the new object ID!
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
