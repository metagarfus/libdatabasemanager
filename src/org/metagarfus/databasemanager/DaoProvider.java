package org.metagarfus.databasemanager;

import com.j256.ormlite.dao.Dao;

public interface DaoProvider {
    <D extends Dao<T, ?>, T> D getDao(Class<T> clazz);
}
