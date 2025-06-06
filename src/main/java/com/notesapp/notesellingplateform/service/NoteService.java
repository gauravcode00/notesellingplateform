package com.notesapp.notesellingplateform.service;

import com.notesapp.notesellingplateform.entity.Note;
import com.notesapp.notesellingplateform.entity.User;
import com.notesapp.notesellingplateform.repository.NoteRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class NoteService {
    private final NoteRepository noteRepo;

    public NoteService(NoteRepository noteRepo) {
        this.noteRepo = noteRepo;
    }

    public Note create(Note note) {
        return noteRepo.save(note);
    }

    public Optional<Note> findById(Long id) {
        return noteRepo.findById(id);
    }

    public List<Note> searchByTitle(String keyword) {
        return noteRepo.findByTitleContainingIgnoreCase(keyword);
    }

    public List<Note> findBySubject(String subject) {
        return noteRepo.findBySubject(subject);
    }

    public List<Note> findAll(){
        return noteRepo.findAll();
    }

    @Transactional
    public void deleteByIdAndUploader(Long id, User user) {
        Note note = noteRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Note not found"));

        // Check uploader
        if (!note.getUploader().getId().equals(user.getId())) {
            throw new SecurityException("You are not authorized to delete this note.");
        }

        noteRepo.delete(note);
    }

    public List<Note> findByUploader(User user) {
        return noteRepo.findByUploader(user);
    }

    // update, delete, etc.
}
