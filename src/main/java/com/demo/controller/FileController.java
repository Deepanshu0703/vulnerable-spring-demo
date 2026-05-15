package com.demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private static final String BASE_DIR = "/tmp/app-files/";

    @GetMapping("/read")
    public ResponseEntity<?> readFile(@RequestParam String filename) {
        try {
            Path basePath = Path.of(BASE_DIR).toAbsolutePath().normalize();
            Path resolved = basePath.resolve(filename).normalize();
            if (!resolved.startsWith(basePath)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid path"));
            }
            File file = resolved.toFile();
            if (!file.exists()) {
                return ResponseEntity.notFound().build();
            }
            String content = new String(Files.readAllBytes(resolved));
            return ResponseEntity.ok(content);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/view")
    public ResponseEntity<?> viewFile(@RequestParam String path) {
        try {
            Path basePath = Path.of(BASE_DIR).toAbsolutePath().normalize();
            Path resolved = Path.of(path).toAbsolutePath().normalize();
            if (!resolved.startsWith(basePath)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid path"));
            }
            File file = resolved.toFile();
            if (!file.exists() || file.isDirectory()) {
                return ResponseEntity.notFound().build();
            }
            String content = new String(Files.readAllBytes(resolved));
            return ResponseEntity.ok(content);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/list")
    public ResponseEntity<?> listDirectory(@RequestParam(defaultValue = "") String dir) {
        try {
            Path basePath = Path.of(BASE_DIR).toAbsolutePath().normalize();
            Path resolved = basePath.resolve(dir).normalize();
            if (!resolved.startsWith(basePath)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid path"));
            }
            File directory = resolved.toFile();
            if (!directory.exists() || !directory.isDirectory()) {
                return ResponseEntity.badRequest().body("Not a directory");
            }
            String[] files = directory.list();
            return ResponseEntity.ok(files);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }
}
