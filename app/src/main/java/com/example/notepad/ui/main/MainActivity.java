package com.example.notepad.ui.main;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.notepad.R;
import com.example.notepad.data.dao.NoteDao;
import com.example.notepad.data.database.AppDatabase;
import com.example.notepad.data.model.Note;
import com.example.notepad.ui.edit.EditNoteActivity;
import com.example.notepad.ui.voice.VoiceRecordActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private NoteAdapter adapter;
    private List<Note> notes = new ArrayList<>();
    private List<Note> filteredNotes = new ArrayList<>();

    private AppDatabase db;
    private NoteDao noteDao;

    private SearchView searchView;

    private FloatingActionButton fabMain, fabText, fabVoice;
    private boolean isFabMenuOpen = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimary));
        }

        setContentView(R.layout.activity_main);

        db = AppDatabase.getInstance(this);
        noteDao = db.noteDao();

        Toolbar toolbar = findViewById(R.id.toolbarMain);
        setSupportActionBar(toolbar);

        searchView = findViewById(R.id.searchView);

        recyclerView = findViewById(R.id.recyclerViewNotes);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        fabMain = findViewById(R.id.fabMain);
        fabText = findViewById(R.id.fabTextNote);
        fabVoice = findViewById(R.id.fabVoiceNote);

        setupFabMenu();
        loadNotes();
        setupSearchView();
    }

    // -------------------------------------------------------------
    // FAB SPEED DIAL MENU
    // -------------------------------------------------------------
    private void setupFabMenu() {
        fabMain.setOnClickListener(v -> toggleFabMenu());

        fabText.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, EditNoteActivity.class));
            toggleFabMenu();
        });

        fabVoice.setOnClickListener(v -> {
            startVoiceRecording();
            toggleFabMenu();
        });
    }

    private void toggleFabMenu() {
        if (!isFabMenuOpen) {
            fabText.show();
            fabVoice.show();

            fabText.animate().translationY(-140).alpha(1).setDuration(200);
            fabVoice.animate().translationY(-260).alpha(1).setDuration(200);

            fabMain.setImageResource(R.drawable.ic_close);
            isFabMenuOpen = true;
        } else {
            fabText.animate().translationY(0).alpha(0).setDuration(200);
            fabVoice.animate().translationY(0).alpha(0).setDuration(200);

            fabText.hide();
            fabVoice.hide();

            fabMain.setImageResource(R.drawable.ic_add);
            isFabMenuOpen = false;
        }
    }

    // -------------------------------------------------------------
    // OPEN VOICE RECORD ACTIVITY
    // -------------------------------------------------------------
    private void startVoiceRecording() {
        Intent intent = new Intent(MainActivity.this, VoiceRecordActivity.class);
        startActivity(intent);
    }

    // -------------------------------------------------------------
    // LOAD NOTES
    // -------------------------------------------------------------
    @Override
    protected void onResume() {
        super.onResume();
        notes.clear();
        notes.addAll(noteDao.getAllNotes());

        filteredNotes.clear();
        filteredNotes.addAll(notes);

        adapter.notifyDataSetChanged();
    }

    private void loadNotes() {
        notes = noteDao.getAllNotes();
        filteredNotes.clear();
        filteredNotes.addAll(notes);

        adapter = new NoteAdapter(MainActivity.this, filteredNotes);
        recyclerView.setAdapter(adapter);
    }

    // -------------------------------------------------------------
    // SEARCH FILTER
    // -------------------------------------------------------------
    private void setupSearchView() {
        searchView.clearFocus();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterNotes(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterNotes(newText);
                return true;
            }
        });
    }

    private void filterNotes(String text) {
        filteredNotes.clear();

        if (text == null || text.trim().isEmpty()) {
            filteredNotes.addAll(notes);
        } else {
            String searchText = text.toLowerCase().trim();

            for (Note note : notes) {
                if (note.getTitle() != null &&
                        note.getTitle().toLowerCase().contains(searchText)) {
                    filteredNotes.add(note);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }
}
