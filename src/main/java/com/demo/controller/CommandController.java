package com.demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/system")
public class CommandController {

    private static final Pattern DOMAIN_PATTERN = Pattern.compile("^[a-zA-Z0-9][a-zA-Z0-9.\\-]{0,253}[a-zA-Z0-9]$");
    private static final Pattern FILENAME_PATTERN = Pattern.compile("^[a-zA-Z0-9][a-zA-Z0-9._\\-]{0,254}$");
    private static final String ALLOWED_DIR = "/tmp";

    @GetMapping("/nslookup")
    public ResponseEntity<String> nslookup(@RequestParam String domain) throws Exception {
        if (!DOMAIN_PATTERN.matcher(domain).matches()) {
            return ResponseEntity.badRequest().body("Invalid domain name");
        }
        ProcessBuilder pb = new ProcessBuilder("nslookup", domain);
        pb.redirectErrorStream(true);
        Process process = pb.start();
        String output = readProcessOutput(process);
        return ResponseEntity.ok(output);
    }

    @GetMapping("/digest")
    public ResponseEntity<String> fileDigest(@RequestParam String filename) throws Exception {
        if (!FILENAME_PATTERN.matcher(filename).matches()) {
            return ResponseEntity.badRequest().body("Invalid filename");
        }
        Path resolved = Path.of(ALLOWED_DIR, filename).normalize().toAbsolutePath();
        if (!resolved.startsWith(Path.of(ALLOWED_DIR).toAbsolutePath())) {
            return ResponseEntity.badRequest().body("Invalid filename");
        }
        ProcessBuilder pb = new ProcessBuilder("md5sum", resolved.toString());
        pb.redirectErrorStream(true);
        Process process = pb.start();
        String output = readProcessOutput(process);
        return ResponseEntity.ok(output);
    }

    private String readProcessOutput(Process process) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        StringBuilder output = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            output.append(line).append("\n");
        }
        process.waitFor();
        return output.toString();
    }
}
