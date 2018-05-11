package com.data;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface DestinationDAO {
    @Query("SELECT * FROM destination")
    List<Destination> getAll();

    @Insert
    long insertDestination(Destination dest);

    @Delete
    void delete(Destination dest);

    @Update
    void update(Destination dest);

    @Query("DELETE FROM destination")
    void nukeTable();

    @Query("SELECT COUNT(*) from destination")
    int getNumberOfRows();
}