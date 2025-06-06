package com.notesapp.notesellingplateform.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.apikey}")
    private String supabaseApiKey;

    @Value("${supabase.bucket}")
    private String supabaseBucket;

    private final RestTemplate restTemplate = new RestTemplate();

    // Upload file to Supabase Storage
    public String storeFile(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf('.'));
        }
        String uuid = UUID.randomUUID().toString();
        String newFilename = uuid + extension;

        String url = supabaseUrl + "/storage/v1/object/" + supabaseBucket + "/" + newFilename;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + supabaseApiKey); // DO NOT USE setBearerAuth
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.set("x-upsert", "true");

        HttpEntity<byte[]> requestEntity;
        try {
            requestEntity = new HttpEntity<>(file.getBytes(), headers);
        } catch (Exception e) {
            throw new RuntimeException("Failed to read file bytes", e);
        }

        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.PUT, // Use PUT for Supabase Storage
                requestEntity,
                String.class
        );

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Supabase Storage upload failed: " + response.getBody());
        }

        return newFilename;
    }

    // Generate public URL (assuming bucket is public)
    public String getPublicFileUrl(String filename) {
        return supabaseUrl + "/storage/v1/object/public/" + supabaseBucket + "/" + filename;
    }

    public void deleteFile(String filename) {
        String url = supabaseUrl + "/storage/v1/object/" + supabaseBucket + "/" + filename;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + supabaseApiKey);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.DELETE,
                entity,
                String.class
        );

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Supabase Storage file delete failed: " + response.getBody());
        }
    }

}
