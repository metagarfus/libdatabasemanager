package org.metagarfus.databasemanager.tests.entity;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.UUID;

@DatabaseTable(tableName = "Task")
public class Task {
    @DatabaseField(id = true)
    public UUID id;

    @DatabaseField
    public String description;
}
