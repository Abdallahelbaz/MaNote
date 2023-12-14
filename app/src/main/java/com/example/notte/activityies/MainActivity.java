package com.example.notte.activityies;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.notte.adapters.NoteAdapter;
import com.example.notte.database.NotesDatabase;
import com.example.notte.databinding.ActivityMainBinding;
import com.example.notte.entries.Note;
import com.example.notte.listners.NoteListner;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NoteListner {


    ActivityMainBinding binding;
    private List<Note> noteList;
    private NoteAdapter adapter;
    private int noteClickePosition = -1;
    ActivityResultLauncher<Intent> arl;
    ActivityResultLauncher<Intent> arlIntent;
    static List<Note> notes;
    ActivityResultLauncher<Intent> Arl;
    private boolean isNoteDeleted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        arl = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == RESULT_OK) {
                    getNotes();
                }

            }
        });

        Arl = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == RESULT_OK && result != null) {
                    Uri selectedImage = result.getData().getData();
                    if (selectedImage != null) {
                        try {
                            String selectImagePath=getPathFromUri(selectedImage);
                            InputStream inputStream = getContentResolver().openInputStream(selectedImage);
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                        } catch (Exception e) {
                            showToast(e.getMessage());
                        }
                    }
                }
            }
        });

        binding.notesRV.setLayoutManager(
                new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        );
        noteList = new ArrayList<>();
        adapter = new NoteAdapter(noteList, this);
        binding.notesRV.setAdapter(adapter);

        binding.imageAddNoteMain.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, CreateNoteActivity.class);
            arl.launch(intent);
        });
        arlIntent = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == RESULT_OK) {
                    noteList.remove(noteClickePosition);
                    if (isNoteDeleted) {
                        adapter.notifyItemRemoved(noteClickePosition);

                    } else {
                        noteList.add(noteClickePosition, notes.get(noteClickePosition));
                        adapter.notifyItemChanged(noteClickePosition);
                    }

                    getNotes();

                }
            }
        });
        getNotes();
        binding.inputSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                adapter.cancelTimer();
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (noteList.size() != 0) {
                    adapter.searchNote(editable.toString());
                }
            }
        });
        binding.imageAddImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, CreateNoteActivity.class);
                arl.launch(intent);
            }
        });
    }


    private void getNotes() {
        this.isNoteDeleted = isNoteDeleted;
        @SuppressLint("StaticFieldLeak")
        class GetNotesTask extends AsyncTask<Void, Void, List<Note>> {
            @Override
            protected List<Note> doInBackground(Void... voids) {
                return NotesDatabase.getDatabase(getApplicationContext()).noteDao().getAllNotes();
            }

            @Override
            protected void onPostExecute(List<Note> notes) {
                super.onPostExecute(notes);
                MainActivity.notes = notes;
                if (noteList.size() == 0) {
                    noteList.addAll(notes);
                    adapter.notifyDataSetChanged();
                } else {
                    noteList.add(0, notes.get(0));
                    adapter.notifyItemInserted(0);
                }
                binding.notesRV.smoothScrollToPosition(0);
            }
        }
        new GetNotesTask().execute();
    }
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNoteClicked(Note note, int position) {
        noteClickePosition = position;
        Intent intent = new Intent(MainActivity.this, CreateNoteActivity.class);
        intent.putExtra("isViewOrUpdate", true);
        intent.putExtra("note", note);
        arlIntent.launch(intent);
    }
    private String getPathFromUri(Uri contentUri) {
        String filePath;
        Cursor cursor = getContentResolver().query(contentUri, null, null, null, null);
        if (cursor == null) {
            filePath = contentUri.getPath();
        } else {
            cursor.moveToFirst();
            int index = cursor.getColumnIndex("_data");
            filePath = cursor.getString(index);
            cursor.close();
        }
        return filePath;
    }


}