package com.data;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface PlaceDAO {
    @Query("SELECT * FROM place")
    List<Place> getAll();

    @Insert
    long insertPlace(Place place);

    @Delete
    void delete(Place place);

    @Update
    void update(Place place);

    @Query("DELETE FROM place")
    void nukeTable();
}