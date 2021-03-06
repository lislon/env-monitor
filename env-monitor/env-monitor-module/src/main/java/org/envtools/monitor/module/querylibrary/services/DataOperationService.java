package org.envtools.monitor.module.querylibrary.services;

import org.envtools.monitor.module.exception.DataOperationException;

import java.beans.IntrospectionException;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

/**
 * Created: 11.04.16 16:02
 *
 * @author Anastasiya Plotnikova
 */
public interface DataOperationService<T> {

    DataOperationResult create(String entity, Map<String, String> fields) throws ClassNotFoundException, NoSuchMethodException, IntrospectionException, IllegalAccessException, InstantiationException, InvocationTargetException, DataOperationException;

    DataOperationResult update(String entity, T id, Map<String, String> fields) throws ClassNotFoundException;

    DataOperationResult delete(String entity, T id) throws ClassNotFoundException;
}
