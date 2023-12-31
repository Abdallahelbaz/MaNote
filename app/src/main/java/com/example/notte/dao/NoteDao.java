package com.example.notte.dao;


import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.example.notte.entries.Note;
import java.util.List;


@Dao
public interface NoteDao {

    @Query("select * from notes order by id desc")
    List<Note> getAllNotes();

    @Insert (onConflict = OnConflictStrategy.REPLACE)
    void insertNote(Note note);

    @Delete
    void deletNote(Note note);
}
