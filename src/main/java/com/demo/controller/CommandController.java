package com.demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/system")
public class CommandController {

    private static final Pattern SAFE_DOMAIN = Pattern.compile("^[a-zA-Z0-9][a-zA-Z0-9.\\-]{0,253}[a-zA-Z0-9]$");
    private static final Pattern SAFE_FILENAME = Pattern.compile("^[a-zA-Z0-9][a-zA-Z0-9._\\-]{0,99}$");

    @GetMapping("/nslookup")
    public ResponseEntity<String> nslookup(@RequestParam String domain) throws Exception {
        if (!SAFE_DOMAIN.matcher(domain).matches()) {
            return ResponseEntity.badRequest().body("Error: Invalid domain name");
        }
        ProcessBuilder pb = new ProcessBuilder("nslookup", domain);
        pb.redirectErrorStream(true);
        Process process = pb.start();
        String output = readProcessOutput(process);
        return ResponseEntity.ok(output);
    }

    @GetMapping("/digest")
    public ResponseEntity<String> fileDigest(@RequestParam String filename) throws Exception {
        if (!SAFE_FILENAME.matcher(filename).matches()) {
            return ResponseEntity.badRequest().body("Error: Invalid filename");
        }
        ProcessBuilder pb = new ProcessBuilder("md5sum", "/tmp/" + filename);
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
