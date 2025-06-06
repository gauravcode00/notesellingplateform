package com.notesapp.notesellingplateform.controller;

import com.notesapp.notesellingplateform.dto.NoteDto;
import com.notesapp.notesellingplateform.entity.Note;
import com.notesapp.notesellingplateform.entity.User;
import com.notesapp.notesellingplateform.service.FileStorageService;
import com.notesapp.notesellingplateform.service.NoteService;
import lombok.Getter;
import lombok.Setter;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notes")
public class NoteController {

    private final NoteService noteService;
    private final FileStorageService fileStorageService;

    public NoteController(NoteService noteService,
                          FileStorageService fileStorageService) {
        this.noteService = noteService;
        this.fileStorageService = fileStorageService;
    }

    /**
     * Create a new Note by uploading a PDF and associated metadata.
     * The authenticated user (via JWT) becomes the "uploader."
     *
     * Request must be multipart/form-data:
     * - title (String)
     * - description (String)
     * - subject (String)
     * - price (Double)
     * - file (MultipartFile)  <-- the PDF to store
     *
     * Header: Authorization: Bearer <jwt>
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createNote(
            @RequestParam("title") String title,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam("subject") String subject,
            @RequestParam("price") Double price,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal User loggedInUser
    ) {
        // 1) Store the PDF on disk, get back a generated filename:
        String storedFilename = fileStorageService.storeFile(file);

        // 2) Build and save the Note entity:
        Note note = new Note();
        note.setTitle(title);
        note.setDescription(description);
        note.setSubject(subject);
        note.setPrice(price);
        note.setFileKey(storedFilename);
        note.setUploader(loggedInUser);

        Note saved = noteService.create(note);
        return ResponseEntity.ok(saved);
    }

    /**
     * Download a Note's PDF. Only the original uploader may download here.
     * (Later you can expand this to also allow purchasers.)
     *
     * Header: Authorization: Bearer <jwt>
     */

    @GetMapping("/{id}/download")
    public ResponseEntity<?> downloadNote(@PathVariable Long id, @AuthenticationPrincipal User loggedInUser) {
        Note note = noteService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Note not found"));

        String fileUrl = fileStorageService.getPublicFileUrl(note.getFileKey());
        // Option 1: redirect
//        return ResponseEntity.status(302).header("Location", fileUrl).build();
        // Option 2: just return the URL
         return ResponseEntity.ok(Map.of("url", fileUrl));
    }



    @GetMapping("/all")
    public ResponseEntity<List<NoteDto>> getAllNotes() {
        List<Note> notes = noteService.findAll();
        List<NoteDto> noteDtos = notes.stream()
                .map(note -> {
                    NoteDto dto = new NoteDto();
                    dto.setId(note.getId());
                    dto.setTitle(note.getTitle());
                    dto.setSubject(note.getSubject());
                    dto.setPrice(note.getPrice());
                    dto.setDescription(note.getDescription());
                    dto.setFileKey(note.getFileKey());
                    dto.setUploaderEmail(note.getUploader() != null ? note.getUploader().getEmail() : null);
                    return dto;
                })
                .toList();
        return ResponseEntity.ok(noteDtos);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteNote(@PathVariable Long id, @AuthenticationPrincipal User user) {
        Note note = noteService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Note not found"));

        // Only uploader can delete, if you have this rule
        if (!note.getUploader().getId().equals(user.getId())) {
            return ResponseEntity.status(403).build();
        }

        // 1. Delete file from Supabase
        fileStorageService.deleteFile(note.getFileKey());

        // 2. Delete from DB
        noteService.deleteByIdAndUploader(id, user);

        return ResponseEntity.ok().build();
    }


    @GetMapping("/my")
    public ResponseEntity<List<NoteDto>> getMyNotes(@AuthenticationPrincipal User user) {
        List<Note> notes = noteService.findByUploader(user);
        List<NoteDto> noteDtos = notes.stream()
                .map(note -> {
                    NoteDto dto = new NoteDto();
                    dto.setId(note.getId());
                    dto.setTitle(note.getTitle());
                    dto.setSubject(note.getSubject());
                    dto.setPrice(note.getPrice());
                    dto.setDescription(note.getDescription());
                    dto.setFileKey(note.getFileKey());
                    dto.setUploaderEmail(note.getUploader() != null ? note.getUploader().getEmail() : null);
                    return dto;
                })
                .toList();
        return ResponseEntity.ok(noteDtos);
    }


}
