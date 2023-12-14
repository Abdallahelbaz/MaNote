package com.example.notte.activityies;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.notte.R;
import com.example.notte.database.NotesDatabase;
import com.example.notte.databinding.ActivityCreateNoteBinding;
import com.example.notte.databinding.LayoutAddUrlBinding;
import com.example.notte.databinding.LayoutDeleteNoteBinding;
import com.example.notte.entries.Note;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CreateNoteActivity extends AppCompatActivity {

    ActivityCreateNoteBinding binding;
    private String selectdNoteColor;
    ActivityResultLauncher<Intent> Arl;

    private AlertDialog dialogAddUrl;
    private AlertDialog dialogDeleteNote;
    ActivityResultLauncher<String> arl;
    private String selectImagePath;
    private Note alreadyAvailable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCreateNoteBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.imageBack.setOnClickListener(view -> onBackPressed());

        binding.textDateTime.setText(
                new SimpleDateFormat("EEEE, dd MMMM yyyy HH:mm a", Locale.getDefault())
                        .format(new Date())
        );
        binding.imageSave.setOnClickListener(view -> saveNote());

        selectdNoteColor = "#333333";
        selectImagePath = "";
        if (getIntent().getBooleanExtra("isViewOrUpdate", false)) {
            alreadyAvailable = (Note) getIntent().getSerializableExtra("note");
            setViewOrUpdate();
        }
        binding.imageRemoveUrl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.textWebUrl.setText(null);
                binding.layoutWebUrl.setVisibility(View.GONE);
            }
        });

        binding.imageRemoveImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.imageNote.setImageBitmap(null);
                binding.imageNote.setVisibility(View.GONE);
                binding.imageRemoveImage.setVisibility(View.GONE);
                selectImagePath="";
            }
        });

        arl = registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
            @Override
            public void onActivityResult(Boolean result) {
                if (result) {
                    setImage();
                } else {
                    showToast("Permission Denied");
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
                            InputStream inputStream = getContentResolver().openInputStream(selectedImage);
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            binding.imageNote.setImageBitmap(bitmap);
                            binding.imageNote.setVisibility(View.VISIBLE);
                            binding.imageRemoveImage.setVisibility(View.VISIBLE);
                            selectImagePath = getPathFromUri(selectedImage);

                        } catch (Exception e) {
                            showToast(e.getMessage());
                        }
                    }
                }
            }
        });
        initMiscell();
        setSubtitleIndicator();
    }

    private void setViewOrUpdate() {
        binding.inputNoteTitle.setText(alreadyAvailable.getTitle());
        binding.inputNote.setText(alreadyAvailable.getNoteText());
        binding.inputNoteSubtitle.setText(alreadyAvailable.getSubTilte());
        binding.textDateTime.setText(alreadyAvailable.getDateTime());
        if (alreadyAvailable.getImagePath() != null && !alreadyAvailable.getImagePath().trim().isEmpty()) {
            binding.imageNote.setImageBitmap(BitmapFactory.decodeFile(alreadyAvailable.getImagePath()));
            binding.imageNote.setVisibility(View.VISIBLE);
            binding.imageRemoveImage.setVisibility(View.VISIBLE);
            selectImagePath = alreadyAvailable.getImagePath();
        }
        if (alreadyAvailable.getWebLink() != null && !alreadyAvailable.getWebLink().trim().isEmpty()) {
            binding.textWebUrl.setText(alreadyAvailable.getWebLink());
            binding.layoutWebUrl.setVisibility(View.VISIBLE);
        }

    }

    private void saveNote() {
        if (binding.inputNoteTitle.getText().toString().trim().isEmpty()) {
            showToast("Note title cant be empty");
            return;
        } else if (binding.inputNoteTitle.getText().toString().trim().isEmpty() &&
                binding.inputNoteSubtitle.getText().toString().trim().isEmpty()) {
            showToast("Note title cant be empty");
            return;
        }

        final Note note = new Note();
        note.setTitle(binding.inputNoteTitle.getText().toString());
        note.setSubTilte(binding.inputNoteSubtitle.getText().toString());
        note.setNoteText(binding.inputNote.getText().toString());
        note.setDateTime(binding.textDateTime.getText().toString());
        note.setColor(selectdNoteColor);
        note.setImagePath(selectImagePath);
        if (binding.layoutWebUrl.getVisibility() == View.VISIBLE) {
            note.setWebLink(binding.textWebUrl.getText().toString());
        }

        if(alreadyAvailable!=null){
            note.setId(alreadyAvailable.getId());
        }


        @SuppressLint("StaticFieldLeak")
        class SaveNoteTask extends AsyncTask<Void, Void, Void> {

            @Override
            protected Void doInBackground(Void... voids) {
                NotesDatabase.getDatabase(getApplicationContext()).noteDao().insertNote(note);
                return null;
            }

            @Override
            protected void onPostExecute(Void unused) {
                super.onPostExecute(unused);
                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                finish();
            }
        }
        new SaveNoteTask().execute();
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void initMiscell() {

        BottomSheetBehavior<LinearLayout> bottomSheetBehavior = BottomSheetBehavior.from(binding.layoutMiscell2.getRoot());

        binding.layoutMiscell2.textMiscell.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED) {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                } else {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }

            }
        });
        binding.layoutMiscell2.layoutAddImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

                arl.launch(Manifest.permission.READ_EXTERNAL_STORAGE);

            }
        });
        binding.layoutMiscell2.viewColor1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectdNoteColor = "#333333";
                binding.layoutMiscell2.imageColor1.setImageResource(R.drawable.ic_done);
                binding.layoutMiscell2.imageColor2.setImageResource(0);
                binding.layoutMiscell2.imageColor3.setImageResource(0);
                binding.layoutMiscell2.imageColor4.setImageResource(0);
                binding.layoutMiscell2.imageColor5.setImageResource(0);
                setSubtitleIndicator();
            }
        });
        binding.layoutMiscell2.viewColor2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectdNoteColor = "#FDBE3B";
                binding.layoutMiscell2.imageColor2.setImageResource(R.drawable.ic_done);
                binding.layoutMiscell2.imageColor1.setImageResource(0);
                binding.layoutMiscell2.imageColor3.setImageResource(0);
                binding.layoutMiscell2.imageColor4.setImageResource(0);
                binding.layoutMiscell2.imageColor5.setImageResource(0);
                setSubtitleIndicator();
            }
        });
        binding.layoutMiscell2.viewColor3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectdNoteColor = "#FF4842";
                binding.layoutMiscell2.imageColor3.setImageResource(R.drawable.ic_done);
                binding.layoutMiscell2.imageColor2.setImageResource(0);
                binding.layoutMiscell2.imageColor1.setImageResource(0);
                binding.layoutMiscell2.imageColor4.setImageResource(0);
                binding.layoutMiscell2.imageColor5.setImageResource(0);
                setSubtitleIndicator();
            }
        });
        binding.layoutMiscell2.viewColor4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectdNoteColor = "#3A52Fc";
                binding.layoutMiscell2.imageColor4.setImageResource(R.drawable.ic_done);
                binding.layoutMiscell2.imageColor2.setImageResource(0);
                binding.layoutMiscell2.imageColor3.setImageResource(0);
                binding.layoutMiscell2.imageColor1.setImageResource(0);
                binding.layoutMiscell2.imageColor5.setImageResource(0);
                setSubtitleIndicator();
            }
        });
        binding.layoutMiscell2.viewColor5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectdNoteColor = "#000000";
                binding.layoutMiscell2.imageColor5.setImageResource(R.drawable.ic_done);
                binding.layoutMiscell2.imageColor2.setImageResource(0);
                binding.layoutMiscell2.imageColor3.setImageResource(0);
                binding.layoutMiscell2.imageColor4.setImageResource(0);
                binding.layoutMiscell2.imageColor1.setImageResource(0);
                setSubtitleIndicator();
            }
        });

        if (alreadyAvailable != null && alreadyAvailable.getColor() != null && alreadyAvailable.getColor().trim().isEmpty()) {
            switch (alreadyAvailable.getColor()) {
                case "#FDBE3B":
                    binding.layoutMiscell2.viewColor2.performClick();
                    break;
                case "#FF4842":
                    binding.layoutMiscell2.viewColor3.performClick();
                    break;
                case "#3A52Fc":
                    binding.layoutMiscell2.viewColor4.performClick();
                    break;
                case "#000000":
                    binding.layoutMiscell2.viewColor5.performClick();
                    break;
            }
        }

        binding.layoutMiscell2.layoutAddUrl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                showAddUrlDialog();
            }
        });

        if(alreadyAvailable!=null){
            binding.layoutMiscell2.layoutDeleteNote.setVisibility(View.VISIBLE);
            binding.layoutMiscell2.layoutDeleteNote.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    showDeleteNoteDialog();
                }
            });
        }

    }


    private void showDeleteNoteDialog(){
        LayoutDeleteNoteBinding bbinding=LayoutDeleteNoteBinding.inflate(getLayoutInflater());
        if(dialogDeleteNote==null){
            AlertDialog.Builder builder=new AlertDialog.Builder(CreateNoteActivity.this);
            builder.setView(bbinding.getRoot());
            dialogDeleteNote=builder.create();
            if (dialogDeleteNote.getWindow() != null) {
                dialogDeleteNote.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            }
            bbinding.textDeletNote.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    @SuppressLint("StaticFieldLeak")
                    class DeleteNoteTask extends AsyncTask<Void,Void,Void>{

                        @Override
                        protected Void doInBackground(Void... voids) {
                            NotesDatabase.getDatabase(getApplicationContext()).noteDao().deletNote(alreadyAvailable);
                            return null;
                        }

                        @Override
                        protected void onPostExecute(Void unused) {
                            super.onPostExecute(unused);
                            Intent intent=new Intent(CreateNoteActivity.this,MainActivity.class);
                            intent.putExtra("isNoteDeleted",true);
                            setResult(RESULT_OK,intent);
                            finish();
                        }
                    }
                    new DeleteNoteTask().execute();
                }
            });
            bbinding.textCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialogDeleteNote.dismiss();
                }
            });
        }
        dialogDeleteNote.show();
    }

    private void setImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if (intent.resolveActivity(getPackageManager()) != null) {
            Arl.launch(intent);
        }
    }

    private void setSubtitleIndicator() {
        GradientDrawable gradientDrawable = (GradientDrawable) binding.viewSubtitleIndicator.getBackground();
        gradientDrawable.setColor(Color.parseColor(selectdNoteColor));
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

    private void showAddUrlDialog() {
        LayoutAddUrlBinding lbindin = LayoutAddUrlBinding.inflate(getLayoutInflater());
        if (dialogAddUrl == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(CreateNoteActivity.this);
            builder.setView(lbindin.getRoot());
            dialogAddUrl = builder.create();
            if (dialogAddUrl.getWindow() != null) {
                dialogAddUrl.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            }

            lbindin.inputUrl.requestFocus();
            lbindin.textAdd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (lbindin.inputUrl.getText().toString().trim().isEmpty()) {
                        showToast("Enter URL");
                    } else if (!Patterns.WEB_URL.matcher(lbindin.inputUrl.getText().toString()).matches()) {
                        showToast("Enter Valid URL");
                    } else {
                        binding.textWebUrl.setText(lbindin.inputUrl.getText().toString());
                        binding.layoutWebUrl.setVisibility(View.VISIBLE);
                        dialogAddUrl.dismiss();
                    }
                }
            });
            lbindin.textCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialogAddUrl.dismiss();
                    lbindin.inputUrl.setText("");
                }
            });

        }
        dialogAddUrl.show();

    }
}