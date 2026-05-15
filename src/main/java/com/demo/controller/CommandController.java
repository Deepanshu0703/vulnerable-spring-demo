package com.demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/system")
public class CommandController {

    private static final Pattern DOMAIN_PATTERN = Pattern.compile("^[a-zA-Z0-9][a-zA-Z0-9.\\-]{0,253}[a-zA-Z0-9]$");
    private static final Pattern FILENAME_PATTERN = Pattern.compile("^[a-zA-Z0-9][a-zA-Z0-9._\\-]{0,254}$");

    @GetMapping("/nslookup")
    public ResponseEntity<String> nslookup(@RequestParam String domain) throws Exception {
        if (!DOMAIN_PATTERN.matcher(domain).matches()) {
            return ResponseEntity.badRequest().body("Invalid domain name");
        }
        Process process = new ProcessBuilder("nslookup", domain).start();
        String output = readProcessOutput(process);
        return ResponseEntity.ok(output);
    }

    @GetMapping("/digest")
    public ResponseEntity<String> fileDigest(@RequestParam String filename) throws Exception {
        if (!FILENAME_PATTERN.matcher(filename).matches()) {
            return ResponseEntity.badRequest().body("Invalid filename");
        }
        Path filePath = Path.of("/tmp").resolve(filename).normalize();
        if (!filePath.startsWith("/tmp")) {
            return ResponseEntity.badRequest().body("Invalid filename");
        }
        Process process = new ProcessBuilder("md5sum", filePath.toString()).start();
        String output = readProcessOutput(process);
        return ResponseEntity.ok(output);
    }

    private String readProcessOutput(Process process) throws Exception {
        BufferedReader stdOut = new BufferedReader(new InputStreamReader(process.getInputStream()));
        BufferedReader stdErr = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        StringBuilder output = new StringBuilder();
        String line;
        while ((line = stdOut.readLine()) != null) {
            output.append(line).append("\n");
        }
        while ((line = stdErr.readLine()) != null) {
            output.append("[err] ").append(line).append("\n");
        }
        process.waitFor();
        return output.toString();
    }
}
