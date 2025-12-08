package com.example.notepad.data.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.Ignore;

@Entity(tableName = "notes")
public class Note {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String title;
    private String content;
    private String audioPath;

    // ✅ NEW: Store date & time as timestamp
    private long createdAt;

    // ✅ Required empty constructor for Room
    public Note() {}

    // ✅ Constructor for TEXT notes
    @Ignore
    public Note(String title, String content) {
        this.title = title;
        this.content = content;
        this.audioPath = null;
        this.createdAt = System.currentTimeMillis(); // ✅ set current time
    }

    // ✅ Constructor for VOICE notes
    @Ignore
    public Note(String audioPath) {
        this.audioPath = audioPath;
        this.title = null;
        this.content = null;
        this.createdAt = System.currentTimeMillis(); // ✅ set current time
    }

    public boolean isVoiceNote() {
        return audioPath != null && !audioPath.isEmpty();
    }

    // -------------------------
    // Getters & Setters
    // -------------------------
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getAudioPath() { return audioPath; }
    public void setAudioPath(String audioPath) { this.audioPath = audioPath; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long dateTime) { this.createdAt = dateTime; }
}
