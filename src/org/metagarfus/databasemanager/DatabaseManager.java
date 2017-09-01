package org.metagarfus.databasemanager;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.misc.TransactionManager;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.DatabaseTable;
import com.j256.ormlite.table.TableUtils;
import org.reflections.Reflections;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class DatabaseManager implements DaoProvider {

    @DatabaseTable(tableName = "DatabaseSystemInfo")
    private static class DatabaseSystemInfo {
        public final static String DATABASE_SYSTEM_INFO = "6c9152cb-c668-425c-97b4-91f1d4a8b706";
        @DatabaseField(id = true)
        public String id = DATABASE_SYSTEM_INFO;

        @DatabaseField(canBeNull = false)
        public long version;

        public DatabaseSystemInfo() {
        }
    }

    public interface DatabaseUpgradeListener {
        void onDatabaseUpgrade(DaoProvider daoProvider, long oldVersion, long currentVersion);
    }

    private final Reflections reflections;
    private final Map<Class<?>, Dao<?, ?>> daoMap;
    private ConnectionSource connectionSource;
    private long currentVersion;
    private DatabaseUpgradeListener listener;

    public DatabaseManager(String entityPackage, DatabaseUpgradeListener listener) {
        this.reflections = new Reflections(entityPackage);
        this.daoMap = new HashMap<>();
        this.listener = listener;
    }

    public boolean openSQLite(String database, long currentVersion) throws SQLException {
        return open("jdbc:sqlite:" + database + ".db", currentVersion);
    }

    public boolean open(String databaseURL, long currentVersion) throws SQLException {
        if (connectionSource != null)
            return false;
        final ConnectionSource connectionSource = new JdbcConnectionSource(databaseURL);
        return open(connectionSource, currentVersion);
    }

    public boolean open(ConnectionSource connectionSource, long currentVersion) {
        try {
            if (this.connectionSource != null)
                return false;
            Set<Class<?>> tables = reflections.getTypesAnnotatedWith(DatabaseTable.class);
            final Map<Class<?>, Dao<?, ?>> tempMap = new HashMap<>();
            for (Class<?> table : tables)
                tempMap.put(table, DaoManager.createDao(connectionSource, table));
            TransactionManager.callInTransaction(connectionSource, () -> {
                for (Class<?> table : tables)
                    TableUtils.createTableIfNotExists(connectionSource, table);
                final DaoProviderWrapper daoProvider = new DaoProviderWrapper(tempMap);
                updateDatabase(daoProvider, connectionSource, currentVersion);
                return null;
            });
            this.connectionSource = connectionSource;
            this.currentVersion = currentVersion;
            setDaoMap(tempMap);
            return true;
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return false;
    }

    private void setDaoMap(Map<Class<?>, Dao<?, ?>> tempMap) {
        synchronized (this.daoMap) {
            this.daoMap.clear();
            this.daoMap.putAll(tempMap);
        }
    }

    private void updateDatabase(DaoProvider daoProvider, ConnectionSource connectionSource, long currentVersion) throws SQLException {
        TableUtils.createTableIfNotExists(connectionSource, DatabaseSystemInfo.class);
        final Dao<DatabaseSystemInfo, String> dao = DaoManager.createDao(connectionSource, DatabaseSystemInfo.class);
        DatabaseSystemInfo databaseSystemInfo = dao.queryForId(DatabaseSystemInfo.DATABASE_SYSTEM_INFO);
        if (databaseSystemInfo == null)
            databaseSystemInfo = new DatabaseSystemInfo();
        final long oldVersion = databaseSystemInfo.version;
        if (oldVersion < currentVersion && listener != null)
            listener.onDatabaseUpgrade(daoProvider, oldVersion, currentVersion);
        databaseSystemInfo.version = currentVersion;
        dao.createOrUpdate(databaseSystemInfo);
    }

    public  <D extends Dao<T, ?>, T> D getDao(Class<T> clazz) {
        synchronized (this.daoMap) {
            return (D) daoMap.get(clazz);
        }
    }

    public long getVersion() {
        return currentVersion;
    }

    public void close() {
        try {
            if (this.connectionSource != null)
                this.connectionSource.close();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
