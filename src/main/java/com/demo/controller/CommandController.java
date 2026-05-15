package com.demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/system")
public class CommandController {

    private static final Pattern DOMAIN_PATTERN = Pattern.compile("^[a-zA-Z0-9.\\-]+$");
    private static final Pattern FILENAME_PATTERN = Pattern.compile("^[a-zA-Z0-9.\\-_]+$");

    @GetMapping("/nslookup")
    public ResponseEntity<String> nslookup(@RequestParam String domain) throws Exception {
        if (!DOMAIN_PATTERN.matcher(domain).matches()) {
            return ResponseEntity.badRequest().body("Invalid domain name.");
        }
        ProcessBuilder pb = new ProcessBuilder("nslookup", domain);
        String output = readProcessOutput(pb);
        return ResponseEntity.ok(output);
    }

    @GetMapping("/digest")
    public ResponseEntity<String> fileDigest(@RequestParam String filename) throws Exception {
        if (!FILENAME_PATTERN.matcher(filename).matches()) {
            return ResponseEntity.badRequest().body("Invalid filename.");
        }
        ProcessBuilder pb = new ProcessBuilder("md5sum", "/tmp/" + filename);
        String output = readProcessOutput(pb);
        return ResponseEntity.ok(output);
    }

    private String readProcessOutput(ProcessBuilder pb) throws Exception {
        Process process = pb.start();
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
