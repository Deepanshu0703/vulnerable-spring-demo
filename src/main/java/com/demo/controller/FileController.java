package com.demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private static final String BASE_DIR = "/tmp/app-files/";
    private static final Path BASE_PATH = Paths.get(BASE_DIR).normalize().toAbsolutePath();

    @GetMapping("/read")
    public ResponseEntity<String> readFile(@RequestParam String filename) {
        try {
            Path resolved = Paths.get(BASE_DIR + filename).normalize().toAbsolutePath();
            if (!resolved.startsWith(BASE_PATH)) {
                return ResponseEntity.badRequest().body("Access denied");
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
            Path resolved = Paths.get(BASE_DIR + path).normalize().toAbsolutePath();
            if (!resolved.startsWith(BASE_PATH)) {
                return ResponseEntity.badRequest().body("Access denied");
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
            Path resolved = Paths.get(BASE_DIR + dir).normalize().toAbsolutePath();
            if (!resolved.startsWith(BASE_PATH)) {
                return ResponseEntity.badRequest().body("Access denied");
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
