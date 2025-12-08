package com.example.notepad.data.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.notepad.data.dao.NoteDao;
import com.example.notepad.data.model.Note;

@Database(
        entities = {Note.class},
        version = 4,
        exportSchema = false   // Best solution → removes warning
)
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase INSTANCE;

    public abstract NoteDao noteDao();

    public static synchronized AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "notepad_db"
                    )
                    .fallbackToDestructiveMigration()
                    .allowMainThreadQueries() // TEMP: okay for small apps; remove later
                    .build();
        }
        return INSTANCE;
    }
}
