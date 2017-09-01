package org.metagarfus.databasemanager.tests;

import com.j256.ormlite.dao.Dao;
import org.metagarfus.databasemanager.tests.entity.Account;
import org.metagarfus.databasemanager.DatabaseManager;

import java.sql.SQLException;

public class Main  {

    public static void main(String[] args) {
        try {
            final DatabaseManager manager = new DatabaseManager("org.metagarfus.databasemanager.tests.entity", (daoProvider, oldVersion, currentVersion) -> System.err.println(oldVersion + " -> " + currentVersion));
            manager.openSQLite("testa", 1);
            String name = "Jim Smith";
            final Dao<Account, String> accountDao = manager.getDao(Account.class);
            final Account account = new Account(name, "pst");
            accountDao.createOrUpdate(account);
            Account account2 = accountDao.queryForId(name);
            System.out.println("Account: " + account2.password);
            manager.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
