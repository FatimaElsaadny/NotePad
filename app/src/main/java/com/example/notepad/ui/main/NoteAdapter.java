package com.example.notepad.ui.main;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SeekBar;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NoteAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_TEXT = 0;
    private static final int TYPE_VOICE = 1;

    private Context context;
    private List<Note> notes;
    private List<Note> notesFull;
    private NoteDao noteDao;

    public NoteAdapter(Context context, List<Note> notes) {
        this.context = context;
        this.notes = notes;
        this.notesFull = new ArrayList<>(notes);
        this.noteDao = AppDatabase.getInstance(context).noteDao();
    }

    @Override
    public int getItemViewType(int position) {
        return notes.get(position).isVoiceNote() ? TYPE_VOICE : TYPE_TEXT;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {

        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        if (viewType == TYPE_VOICE) {
            View view = inflater.inflate(R.layout.item_voice_note, parent, false);
            return new VoiceNoteViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_note, parent, false);
            return new TextNoteViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(
            @NonNull RecyclerView.ViewHolder holder, int position) {

        Note note = notes.get(position);

        if (holder.getItemViewType() == TYPE_TEXT) {
            ((TextNoteViewHolder) holder).bind(note);
        } else {
            ((VoiceNoteViewHolder) holder).bind(note);
        }

        holder.itemView.setOnClickListener(v -> {
            if (!note.isVoiceNote()) {
                Intent intent = new Intent(context, EditNoteActivity.class);
                intent.putExtra("note_id", note.getId());
                context.startActivity(intent);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Delete Note")
                    .setMessage("Are you sure?")
                    .setPositiveButton("Yes", (dialog, which) -> {

                        int pos = holder.getAdapterPosition();
                        if (pos == RecyclerView.NO_POSITION) return;

                        Note selectedNote = notes.get(pos);

                        noteDao.delete(selectedNote);
                        notes.remove(pos);
                        notesFull.remove(selectedNote);

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

    // ======================================================
    // TEXT NOTE VIEW HOLDER
    // ======================================================
    static class TextNoteViewHolder extends RecyclerView.ViewHolder {

        TextView textTitle, textContent, textDate;

        public TextNoteViewHolder(@NonNull View itemView) {
            super(itemView);
            textTitle = itemView.findViewById(R.id.textTitle);
            textContent = itemView.findViewById(R.id.textContent);
            textDate = itemView.findViewById(R.id.textDateTime);
        }

        public void bind(Note note) {
            textTitle.setText(note.getTitle());
            textContent.setText(note.getContent());
            textDate.setText(formatDate(note.getCreatedAt()));
        }
    }

    // ======================================================
    // VOICE NOTE VIEW HOLDER
    // ======================================================
    static class VoiceNoteViewHolder extends RecyclerView.ViewHolder {

        ImageButton playButton;
        TextView duration, voiceDate;
        SeekBar seekBar;
        MediaPlayer mediaPlayer;
        Handler handler = new Handler();

        public VoiceNoteViewHolder(@NonNull View itemView) {
            super(itemView);
            playButton = itemView.findViewById(R.id.btnPlayAudio);
            voiceDate = itemView.findViewById(R.id.audioDateTime);
            duration = itemView.findViewById(R.id.audioDuration);
            seekBar = itemView.findViewById(R.id.seekBarAudio);
        }

        public void bind(Note note) {

            // --- Show Date ---
            voiceDate.setText(formatDate(note.getCreatedAt()));

            playButton.setImageResource(R.drawable.ic_play_button);
            seekBar.setProgress(0);

            // =====================================================
            // ⭐ LOAD REAL AUDIO DURATION BEFORE PLAYING
            // =====================================================
            try {
                MediaPlayer tempPlayer = new MediaPlayer();
                tempPlayer.setDataSource(note.getAudioPath());
                tempPlayer.prepare();
                int dur = tempPlayer.getDuration();
                duration.setText(formatDuration(dur));
                tempPlayer.release();
            } catch (Exception e) {
                duration.setText("00:00");
            }

            // =====================================================
            // PLAY / PAUSE BUTTON
            // =====================================================
            playButton.setOnClickListener(v -> {
                try {
                    if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                        mediaPlayer.pause();
                        playButton.setImageResource(R.drawable.ic_play_button);
                        return;
                    }

                    if (mediaPlayer != null) {
                        mediaPlayer.start();
                        playButton.setImageResource(R.drawable.ic_pause_icone);
                        updateSeekBar();
                        return;
                    }

                    mediaPlayer = new MediaPlayer();
                    mediaPlayer.setDataSource(note.getAudioPath());
                    mediaPlayer.prepare();
                    mediaPlayer.start();

                    seekBar.setMax(mediaPlayer.getDuration());
                    playButton.setImageResource(R.drawable.ic_pause_icone);
                    updateSeekBar();

                    mediaPlayer.setOnCompletionListener(mp -> {
                        seekBar.setProgress(0);
                        playButton.setImageResource(R.drawable.ic_play_button);
                        mediaPlayer.release();
                        mediaPlayer = null;
                    });

                } catch (Exception e) {
                    Toast.makeText(itemView.getContext(), "Cannot play audio", Toast.LENGTH_SHORT).show();
                }
            });

            // User scrubs seek bar manually
            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                    if (fromUser && mediaPlayer != null) {
                        mediaPlayer.seekTo(progress);
                    }
                }
                @Override public void onStartTrackingTouch(SeekBar seekBar) {}
                @Override public void onStopTrackingTouch(SeekBar seekBar) {}
            });
        }

        private void updateSeekBar() {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                        seekBar.setProgress(mediaPlayer.getCurrentPosition());
                        handler.postDelayed(this, 150);
                    }
                }
            }, 150);
        }

        // Format duration mm:ss
        private String formatDuration(int ms) {
            int sec = ms / 1000;
            int mins = sec / 60;
            int s = sec % 60;
            return String.format(Locale.getDefault(), "%02d:%02d", mins, s);
        }
    }

    // ======================================================
    // DATE FORMATTER
    // ======================================================
    private static String formatDate(long time) {
        if (time == 0) return "";
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM • hh:mm a", Locale.getDefault());
        return sdf.format(new Date(time));
    }
}
