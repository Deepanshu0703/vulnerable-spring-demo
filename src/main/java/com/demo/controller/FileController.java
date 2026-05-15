package com.demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private static final Path BASE_DIR = Path.of("/tmp/app-files").toAbsolutePath().normalize();

    @GetMapping("/read")
    public ResponseEntity<String> readFile(@RequestParam String filename) {
        try {
            Path resolved = BASE_DIR.resolve(filename).normalize().toAbsolutePath();
            if (!resolved.startsWith(BASE_DIR)) {
                return ResponseEntity.badRequest().body("Invalid filename");
            }
            File file = resolved.toFile();
            if (!file.exists()) {
                return ResponseEntity.notFound().build();
            }
            String content = new String(Files.readAllBytes(resolved));
            return ResponseEntity.ok(content);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/view")
    public ResponseEntity<String> viewFile(@RequestParam String path) {
        try {
            Path resolved = BASE_DIR.resolve(path).normalize().toAbsolutePath();
            if (!resolved.startsWith(BASE_DIR)) {
                return ResponseEntity.badRequest().body("Invalid path");
            }
            File file = resolved.toFile();
            if (!file.exists() || file.isDirectory()) {
                return ResponseEntity.notFound().build();
            }
            String content = new String(Files.readAllBytes(resolved));
            return ResponseEntity.ok(content);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/list")
    public ResponseEntity<?> listDirectory(@RequestParam(defaultValue = "") String dir) {
        try {
            Path resolved = BASE_DIR.resolve(dir).normalize().toAbsolutePath();
            if (!resolved.startsWith(BASE_DIR)) {
                return ResponseEntity.badRequest().body("Invalid directory");
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
