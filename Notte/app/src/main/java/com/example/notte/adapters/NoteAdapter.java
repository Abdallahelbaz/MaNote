package com.example.notte.adapters;

import android.annotation.SuppressLint;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.notte.databinding.ItemContainerNoteBinding;
import com.example.notte.entries.Note;
import com.example.notte.listners.NoteListner;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {
    private List<Note> notes;

    private NoteListner listner;
    private Timer timer;
    private List<Note> noteSource;

    public NoteAdapter(List<Note> notes, NoteListner listner) {
        this.notes = notes;
        this.listner = listner;
        noteSource = notes;
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new NoteViewHolder(ItemContainerNoteBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @SuppressLint("RecyclerView")
    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {

        holder.setNote(notes.get(position));
        holder.binding.layoutNotes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listner.onNoteClicked(notes.get(position), position);
            }
        });

    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    static class NoteViewHolder extends RecyclerView.ViewHolder {
        ItemContainerNoteBinding binding;

        NoteViewHolder(ItemContainerNoteBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void setNote(Note note) {
            binding.textDateTime.setText(note.getDateTime());
            binding.textTitle.setText(note.getTitle());
            if (note.getSubTilte().trim().isEmpty()) {
                binding.textSubtitle.setVisibility(View.GONE);
            } else {
                binding.textSubtitle.setText(note.getSubTilte());
            }
            if (note.getImagePath() != null) {
                binding.imgaeNote.setVisibility(View.VISIBLE);
                binding.imgaeNote.setImageBitmap(BitmapFactory.decodeFile(note.getImagePath()));
            } else {
                binding.imgaeNote.setVisibility(View.GONE);
            }
            GradientDrawable gradientDrawable = (GradientDrawable) binding.layoutNotes.getBackground();
            if (note.getColor() != null) {
                gradientDrawable.setColor(Color.parseColor(note.getColor()));
            } else {
                gradientDrawable.setColor(Color.parseColor("#333333"));
            }
        }
    }

    public void searchNote(final String searchWord) {
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (searchWord.trim().isEmpty()) {
                    notes = noteSource;
                } else {
                    ArrayList<Note> temp = new ArrayList<>();
                    for (Note note : noteSource) {
                        if (note.getTitle().toLowerCase().contains(searchWord.toLowerCase())
                                || note.getSubTilte().toLowerCase().contains(searchWord.toLowerCase())
                                || note.getSubTilte().toLowerCase().contains(searchWord.toLowerCase())) {
                            temp.add(note);
                        }
                    }
                    notes = temp;
                }
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        notifyDataSetChanged();
                    }
                });
            }
        }, 500);
    }

    public void cancelTimer() {
        if (timer != null)
            timer.cancel();
    }
}
