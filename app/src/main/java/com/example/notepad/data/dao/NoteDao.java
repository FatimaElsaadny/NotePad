package com.example.notepad.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.notepad.data.model.Note;

import java.util.List;

@Dao
public interface NoteDao {

    @Query("SELECT * FROM notes WHERE id = :noteId LIMIT 1")
    Note getNoteById(long noteId);
    @Insert
    void insert(Note note);

    @Update
    void update(Note note);

    @Delete
    void delete(Note note);

    @Query("SELECT * FROM notes")
    List<Note> getAllNotes();
}
