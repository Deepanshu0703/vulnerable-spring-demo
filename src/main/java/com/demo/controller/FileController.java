package com.demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private static final String BASE_DIR = "/tmp/app-files/";

    private static String validateWithinBaseDir(File file) throws IOException {
        String canonicalBase = new File(BASE_DIR).getCanonicalPath();
        String canonicalPath = file.getCanonicalPath();
        if (!canonicalPath.startsWith(canonicalBase + File.separator) && !canonicalPath.equals(canonicalBase)) {
            return null;
        }
        return canonicalPath;
    }

    @GetMapping("/read")
    public ResponseEntity<String> readFile(@RequestParam String filename) {
        try {
            File file = new File(BASE_DIR, filename);
            String canonicalPath = validateWithinBaseDir(file);
            if (canonicalPath == null) {
                return ResponseEntity.badRequest().body("Invalid file path");
            }
            File safeFile = new File(canonicalPath);
            if (!safeFile.exists()) {
                return ResponseEntity.notFound().build();
            }
            String content = new String(Files.readAllBytes(safeFile.toPath()));
            return ResponseEntity.ok(content);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/view")
    public ResponseEntity<String> viewFile(@RequestParam String path) {
        try {
            File file = new File(path);
            String canonicalPath = validateWithinBaseDir(file);
            if (canonicalPath == null) {
                return ResponseEntity.badRequest().body("Invalid file path");
            }
            File safeFile = new File(canonicalPath);
            if (!safeFile.exists() || safeFile.isDirectory()) {
                return ResponseEntity.notFound().build();
            }
            String content = new String(Files.readAllBytes(safeFile.toPath()));
            return ResponseEntity.ok(content);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/list")
    public ResponseEntity<?> listDirectory(@RequestParam(defaultValue = "") String dir) {
        try {
            File directory = new File(BASE_DIR, dir);
            String canonicalPath = validateWithinBaseDir(directory);
            if (canonicalPath == null) {
                return ResponseEntity.badRequest().body("Invalid directory path");
            }
            File safeDir = new File(canonicalPath);
            if (!safeDir.exists() || !safeDir.isDirectory()) {
                return ResponseEntity.badRequest().body("Not a directory");
            }
            String[] files = safeDir.list();
            return ResponseEntity.ok(files);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }
}
