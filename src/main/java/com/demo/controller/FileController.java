package com.demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private static final String BASE_DIR = "/tmp/app-files/";

    @GetMapping("/read")
    public ResponseEntity<String> readFile(@RequestParam String filename) {
        try {
            Path basePath = Paths.get(BASE_DIR).toAbsolutePath().normalize();
            Path resolvedPath = basePath.resolve(filename).normalize();
            if (!resolvedPath.startsWith(basePath)) {
                return ResponseEntity.badRequest().body("Error: Invalid file path");
            }
            File file = resolvedPath.toFile();
            if (!file.exists()) {
                return ResponseEntity.notFound().build();
            }
            String content = new String(Files.readAllBytes(resolvedPath));
            return ResponseEntity.ok(content);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/view")
    public ResponseEntity<String> viewFile(@RequestParam String path) {
        try {
            Path basePath = Paths.get(BASE_DIR).toAbsolutePath().normalize();
            Path resolvedPath = Paths.get(path).toAbsolutePath().normalize();
            if (!resolvedPath.startsWith(basePath)) {
                return ResponseEntity.badRequest().body("Error: Access denied - path outside allowed directory");
            }
            File file = resolvedPath.toFile();
            if (!file.exists() || file.isDirectory()) {
                return ResponseEntity.notFound().build();
            }
            String content = new String(Files.readAllBytes(resolvedPath));
            return ResponseEntity.ok(content);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/list")
    public ResponseEntity<?> listDirectory(@RequestParam(defaultValue = "") String dir) {
        try {
            Path basePath = Paths.get(BASE_DIR).toAbsolutePath().normalize();
            Path resolvedPath = basePath.resolve(dir).normalize();
            if (!resolvedPath.startsWith(basePath)) {
                return ResponseEntity.badRequest().body("Error: Invalid directory path");
            }
            File directory = resolvedPath.toFile();
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
