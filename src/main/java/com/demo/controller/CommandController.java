package com.demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/system")
public class CommandController {

    private static final Pattern VALID_DOMAIN = Pattern.compile("^[a-zA-Z0-9][a-zA-Z0-9.\\-]{0,253}[a-zA-Z0-9]$");
    private static final Pattern VALID_FILENAME = Pattern.compile("^[a-zA-Z0-9._\\-]{1,255}$");

    @GetMapping("/nslookup")
    public ResponseEntity<String> nslookup(@RequestParam String domain) {
        if (!VALID_DOMAIN.matcher(domain).matches()) {
            return ResponseEntity.badRequest().body("Invalid domain name");
        }
        try {
            ProcessBuilder pb = new ProcessBuilder("nslookup", domain);
            pb.redirectErrorStream(true);
            Process process = pb.start();
            String output = readProcessOutput(process);
            return ResponseEntity.ok(output);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/digest")
    public ResponseEntity<String> fileDigest(@RequestParam String filename) {
        if (!VALID_FILENAME.matcher(filename).matches()) {
            return ResponseEntity.badRequest().body("Invalid filename");
        }
        try {
            ProcessBuilder pb = new ProcessBuilder("md5sum", "/tmp/" + filename);
            pb.redirectErrorStream(true);
            Process process = pb.start();
            String output = readProcessOutput(process);
            return ResponseEntity.ok(output);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }

    private String readProcessOutput(Process process) throws Exception {
        BufferedReader stdOut = new BufferedReader(new InputStreamReader(process.getInputStream()));
        StringBuilder output = new StringBuilder();
        String line;
        while ((line = stdOut.readLine()) != null) {
            output.append(line).append("\n");
        }
        process.waitFor();
        return output.toString();
    }
}
