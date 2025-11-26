package com.example.notepad.ui.main;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.notepad.R;
import com.example.notepad.data.dao.NoteDao;
import com.example.notepad.data.database.AppDatabase;
import com.example.notepad.data.model.Note;
import com.example.notepad.ui.edit.EditNoteActivity;

import java.util.List;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {

    private Context context;
    private List<Note> notes;
    private NoteDao noteDao;

    public NoteAdapter(Context context, List<Note> notes) {
        this.context = context;
        this.notes = notes;

        // Correct DAO initialization
        this.noteDao = AppDatabase.getInstance(context).noteDao();
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_note, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        Note note = notes.get(position);

        holder.textTitle.setText(note.getTitle());
        holder.textContent.setText(note.getContent());

        // Click → Edit note
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, EditNoteActivity.class);
            intent.putExtra("note_id", note.getId());
            context.startActivity(intent);
        });

        // Long click → Delete note
        holder.itemView.setOnLongClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Delete Note")
                    .setMessage("Are you sure you want to delete this note?")
                    .setPositiveButton("Yes", (dialog, which) -> {

                        int pos = holder.getAdapterPosition();
                        if (pos == RecyclerView.NO_POSITION) return;

                        Note selectedNote = notes.get(pos);

                        // Delete from DB
                        noteDao.delete(selectedNote);

                        // Delete from list
                        notes.remove(pos);

                        // Update RecyclerView
                        notifyItemRemoved(pos);

                        Toast.makeText(context, "Note deleted", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("No", null)
                    .show();
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    static class NoteViewHolder extends RecyclerView.ViewHolder {
        TextView textTitle, textContent;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            textTitle = itemView.findViewById(R.id.textTitle);
            textContent = itemView.findViewById(R.id.textContent);
        }
    }
}
