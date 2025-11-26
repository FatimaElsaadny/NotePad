package com.example.notepad.ui.edit;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;

import androidx.appcompat.app.AppCompatActivity;

import com.example.notepad.R;
import com.example.notepad.data.dao.NoteDao;
import com.example.notepad.data.database.AppDatabase;
import com.example.notepad.data.model.Note;


public class EditNoteActivity extends AppCompatActivity {

    private EditText editTitle, editContent;
    private NoteDao noteDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_note);

        editTitle = findViewById(R.id.edit_title);
        editContent = findViewById(R.id.edit_content);

        noteDao = AppDatabase.getInstance(this).noteDao();

        // Setup toolbar icons
        Toolbar toolbar = findViewById(R.id.toolbarEdit);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        ImageView btnClose = toolbar.findViewById(R.id.btnClose);
        ImageView btnSave = toolbar.findViewById(R.id.btnSave);

        btnClose.setOnClickListener(v -> finish());

        // Check if we have a note id
        int noteId = getIntent().getIntExtra("note_id", -1);
        if (noteId != -1) {
            loadNote(noteId);
        }

        // Save icon (right)

        btnSave.setOnClickListener(v -> saveNote(noteId));
    }

    private void loadNote(int noteId) {
        // Get the note from the database
        Note currentNote = noteDao.getNoteById(noteId);

        if (currentNote != null) {
            // Set the existing title and content in the EditText fields
            editTitle.setText(currentNote.getTitle());
            editContent.setText(currentNote.getContent());
        } else {
            // Note not found, you can show a message or leave fields empty
            editTitle.setText("");
            editContent.setText("");
            Toast.makeText(this, "Note not found", Toast.LENGTH_SHORT).show();
        }
    }


    private void saveNote(long noteId) {

        String title = editTitle.getText().toString().trim();
        String content = editContent.getText().toString().trim();

        if (TextUtils.isEmpty(title) && TextUtils.isEmpty(content)) {
            Toast.makeText(this, "Cannot save empty note", Toast.LENGTH_SHORT).show();
            return;
        }

        Note note;
        if (noteId > -1) {
            note = noteDao.getNoteById(noteId);
            note.setTitle(title);
            note.setContent(content);
            noteDao.update(note);

        } else {
            Note new_note = new Note(title, content);
            noteDao.insert(new_note);
        }

        Toast.makeText(this, "Note saved!", Toast.LENGTH_SHORT).show();
        finish();
    }
}
