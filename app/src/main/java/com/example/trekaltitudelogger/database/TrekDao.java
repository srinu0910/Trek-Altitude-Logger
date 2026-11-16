package com.example.trekaltitudelogger.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface TrekDao {

    @Insert
    void insertTrek(Trek trek);

    @Update
    void updateTrek(Trek trek);

    @Delete
    void deleteTrek(Trek trek);

    @Query("DELETE FROM treks WHERE trekId = :trekId")
    void deleteTrekById(String trekId);

    @Query("SELECT * FROM treks ORDER BY startTime DESC")
    LiveData<List<Trek>> getAllTreks();

    @Query("SELECT * FROM treks WHERE trekId = :trekId LIMIT 1")
    LiveData<Trek> getTrekById(String trekId);

    @Query("SELECT * FROM treks WHERE trekId = :trekId LIMIT 1")
    Trek getTrekByIdSync(String trekId);
}