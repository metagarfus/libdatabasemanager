package org.metagarfus.databasemanager;


import com.j256.ormlite.dao.Dao;

import java.util.Map;

public class DaoProviderWrapper implements DaoProvider {

    private final Map<Class<?>, Dao<?, ?>> daoMap;

    public DaoProviderWrapper(Map<Class<?>, Dao<?, ?>> daoMap) {
        this.daoMap = daoMap;
    }

    public  <D extends Dao<T, ?>, T> D getDao(Class<T> clazz) {
        synchronized (this.daoMap) {
            return (D) daoMap.get(clazz);
        }
    }
}
