package com.example.notepad.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.notepad.R;
import com.example.notepad.data.dao.NoteDao;
import com.example.notepad.data.database.AppDatabase;
import com.example.notepad.data.model.Note;
import com.example.notepad.ui.edit.EditNoteActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private NoteAdapter adapter;
    private List<Note> notes;
    private AppDatabase db;
    protected NoteDao noteDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        db = AppDatabase.getInstance(this);
        noteDao = db.noteDao();
        Toolbar toolbar = findViewById(R.id.toolbarMain);
        setSupportActionBar(toolbar);
        recyclerView = findViewById(R.id.recyclerViewNotes);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        loadNotes();

        FloatingActionButton fab = findViewById(R.id.fabAddNote);
        fab.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, EditNoteActivity.class);
            startActivity(intent);  // Open empty edit screen
        });
        }

    @Override
    protected void onResume() {
        super.onResume();
        if (noteDao != null) {
            notes.clear();
            notes.addAll(noteDao.getAllNotes());
            adapter.notifyDataSetChanged();
        }
    }

    private void loadNotes() {
        notes = db.noteDao().getAllNotes();
        adapter = new NoteAdapter(MainActivity.this, notes);
        recyclerView.setAdapter(adapter);
    }

    private void showAddNoteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Note");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        final EditText titleInput = new EditText(this);
        titleInput.setHint("Title");
        layout.addView(titleInput);

        final EditText contentInput = new EditText(this);
        contentInput.setHint("Content");
        layout.addView(contentInput);

        builder.setView(layout);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String title = titleInput.getText().toString().trim();
            String content = contentInput.getText().toString().trim();

            if (!title.isEmpty() || !content.isEmpty()) {
                Note note = new Note(title, content);
                db.noteDao().insert(note);
                loadNotes(); // refresh list
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        builder.show();
    }
}
