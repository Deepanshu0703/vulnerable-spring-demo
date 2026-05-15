package com.demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private static final String BASE_DIR = "/tmp/app-files/";

    @GetMapping("/read")
    public ResponseEntity<String> readFile(@RequestParam String filename) {
        try {
            Path basePath = Path.of(BASE_DIR).toAbsolutePath().normalize();
            Path filePath = basePath.resolve(filename).normalize();
            if (!filePath.startsWith(basePath)) {
                return ResponseEntity.badRequest().body("Access denied");
            }
            File file = filePath.toFile();
            if (!file.exists()) {
                return ResponseEntity.notFound().build();
            }
            String content = new String(Files.readAllBytes(filePath));
            return ResponseEntity.ok(content);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/view")
    public ResponseEntity<String> viewFile(@RequestParam String path) {
        try {
            Path basePath = Path.of(BASE_DIR).toAbsolutePath().normalize();
            Path filePath = basePath.resolve(path).normalize();
            if (!filePath.startsWith(basePath)) {
                return ResponseEntity.badRequest().body("Access denied");
            }
            File file = filePath.toFile();
            if (!file.exists() || file.isDirectory()) {
                return ResponseEntity.notFound().build();
            }
            String content = new String(Files.readAllBytes(filePath));
            return ResponseEntity.ok(content);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/list")
    public ResponseEntity<?> listDirectory(@RequestParam(defaultValue = "") String dir) {
        try {
            Path basePath = Path.of(BASE_DIR).toAbsolutePath().normalize();
            Path dirPath = basePath.resolve(dir).normalize();
            if (!dirPath.startsWith(basePath)) {
                return ResponseEntity.badRequest().body("Access denied");
            }
            File directory = dirPath.toFile();
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
