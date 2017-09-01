package org.metagarfus.databasemanager.tests.entity;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "accounts")
public class Account {
    @DatabaseField(id = true)
    public String name;

    @DatabaseField(canBeNull = false)
    public String password;

    public Account() {
    }

    public Account(String name, String password) {
        this.name = name;
        this.password = password;
    }
}