package com.example.notepad.data.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.notepad.data.dao.NoteDao;
import com.example.notepad.data.model.Note;

@Database(entities = {Note.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase INSTANCE;
    public abstract NoteDao noteDao();

    public static synchronized AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "notepad_db")
                    .allowMainThreadQueries() // For testing only! Remove in production.
                    .build();
        }
        return INSTANCE;
    }
}
