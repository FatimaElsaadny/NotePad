package com.example.notepad.ui.voice;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.notepad.R;
import com.example.notepad.data.database.AppDatabase;
import com.example.notepad.data.model.Note;
import com.google.android.material.appbar.MaterialToolbar;

import java.io.File;
import java.io.IOException;

public class VoiceRecordActivity extends AppCompatActivity {

    private static final int REQ_RECORD_AUDIO = 101;

    private MediaRecorder mediaRecorder;
    private String audioPath;

    private ImageButton btnRecord, btnStop;
    private TextView recordTimer;

    private boolean isRecording = false;
    private Handler timerHandler = new Handler();
    private int seconds = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_record);

        // ✅ Toolbar back button
        MaterialToolbar toolbar = findViewById(R.id.toolbarVoice);
        toolbar.setNavigationOnClickListener(v -> finish());

        btnRecord = findViewById(R.id.btnRecord);
        btnStop = findViewById(R.id.btnStop);
        recordTimer = findViewById(R.id.recordTimer);

        btnRecord.setOnClickListener(v -> {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                    != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.RECORD_AUDIO},
                        REQ_RECORD_AUDIO
                );
            } else {
                startRecording();
            }
        });

        btnStop.setOnClickListener(v -> stopRecording());
    }

    // ✅ START RECORDING
    private void startRecording() {
        try {
            File folder = new File(getExternalFilesDir("VoiceNotes"), "");
            if (!folder.exists()) folder.mkdirs();

            String fileName = "voice_" + System.currentTimeMillis() + ".m4a";
            File audioFile = new File(folder, fileName);
            audioPath = audioFile.getAbsolutePath();

            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mediaRecorder.setOutputFile(audioPath);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);

            mediaRecorder.prepare();
            mediaRecorder.start();

            isRecording = true;
            btnRecord.setVisibility(ImageButton.GONE);
            btnStop.setVisibility(ImageButton.VISIBLE);

            startTimer();

            Toast.makeText(this, "Recording started...", Toast.LENGTH_SHORT).show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ✅ STOP RECORDING
    private void stopRecording() {
        if (isRecording) {
            mediaRecorder.stop();
            mediaRecorder.release();
            mediaRecorder = null;

            isRecording = false;

            saveVoiceNoteToDatabase();

            Toast.makeText(this, "Voice note saved", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    // ✅ SAVE WITH REAL SYSTEM DATE/TIME
    private void saveVoiceNoteToDatabase() {
        Note voiceNote = new Note(audioPath);
        voiceNote.setCreatedAt(System.currentTimeMillis()); // ✅ REAL TIME SAVED
        AppDatabase.getInstance(this).noteDao().insert(voiceNote);
    }

    // ✅ TIMER
    private void startTimer() {
        seconds = 0;
        timerHandler.post(new Runnable() {
            @Override
            public void run() {
                if (isRecording) {
                    int mins = seconds / 60;
                    int secs = seconds % 60;
                    recordTimer.setText(String.format("%02d:%02d", mins, secs));
                    seconds++;
                    timerHandler.postDelayed(this, 1000);
                }
            }
        });
    }

    // ✅ PERMISSION RESULT
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        if (requestCode == REQ_RECORD_AUDIO) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startRecording();
            } else {
                Toast.makeText(this, "Microphone permission denied", Toast.LENGTH_SHORT).show();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
