package com.example.notepad.ui.edit;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.notepad.R;
import com.example.notepad.data.dao.NoteDao;
import com.example.notepad.data.database.AppDatabase;
import com.example.notepad.data.model.Note;

public class EditNoteActivity extends AppCompatActivity {

    private EditText editTitle, editContent;
    private NoteDao noteDao;
    private int noteId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_note);

        editTitle = findViewById(R.id.edit_title);
        editContent = findViewById(R.id.edit_content);

        noteDao = AppDatabase.getInstance(this).noteDao();

        Toolbar toolbar = findViewById(R.id.toolbarEdit);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        ImageView btnClose = toolbar.findViewById(R.id.btnClose);
        ImageView btnSave = toolbar.findViewById(R.id.btnSave);

        btnClose.setOnClickListener(v -> finish());

        noteId = getIntent().getIntExtra("note_id", -1);
        if (noteId != -1) {
            loadNote(noteId);
        }

        btnSave.setOnClickListener(v -> saveNote());
    }

    private void loadNote(int noteId) {
        Note currentNote = noteDao.getNoteById(noteId);

        if (currentNote != null) {
            editTitle.setText(currentNote.getTitle());
            editContent.setText(currentNote.getContent());
        } else {
            Toast.makeText(this, "Note not found", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveNote() {

        String title = editTitle.getText().toString().trim();
        String content = editContent.getText().toString().trim();

        if (TextUtils.isEmpty(title) && TextUtils.isEmpty(content)) {
            Toast.makeText(this, "Cannot save empty note", Toast.LENGTH_SHORT).show();
            return;
        }

        long now = System.currentTimeMillis();

        if (noteId > -1) {
            // UPDATE
            Note note = noteDao.getNoteById(noteId);
            note.setTitle(title);
            note.setContent(content);
            note.setCreatedAt(now);
            noteDao.update(note);

        } else {
            // INSERT
            Note newNote = new Note(title, content);
            newNote.setCreatedAt(now);
            noteDao.insert(newNote);
        }

        Toast.makeText(this, "Note saved!", Toast.LENGTH_SHORT).show();
        finish();
    }
}
