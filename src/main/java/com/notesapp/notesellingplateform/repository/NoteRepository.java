package com.notesapp.notesellingplateform.repository;

import com.notesapp.notesellingplateform.entity.Note;
import com.notesapp.notesellingplateform.entity.User;
import jakarta.annotation.Resource;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NoteRepository extends JpaRepository<Note, Long> {
    List<Note> findByTitleContainingIgnoreCase(String keyword);
    List<Note> findBySubject(String subject);
    List<Note> findAll();
    void deleteByIdAndUploader_Id(Long id, Long uploaderId);

    List<Note> findByUploader(User uploader);
}
