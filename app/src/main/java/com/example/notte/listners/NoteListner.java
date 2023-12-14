package com.example.notte.listners;

import com.example.notte.entries.Note;

public interface NoteListner {
    void onNoteClicked(Note note, int position);
}
