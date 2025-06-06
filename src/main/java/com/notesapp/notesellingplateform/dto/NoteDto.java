package com.notesapp.notesellingplateform.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NoteDto {
    private Long id;
    private String title;
    private String subject;
    private Double price;
    private String description;
    private String fileKey;
    private String uploaderEmail; // or just uploader id or name

    // getters/setters
}
