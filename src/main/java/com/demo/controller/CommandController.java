package com.demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/system")
public class CommandController {

    private static final Pattern VALID_DOMAIN = Pattern.compile("^[a-zA-Z0-9][a-zA-Z0-9.\\-]*$");
    private static final Pattern VALID_FILENAME = Pattern.compile("^[a-zA-Z0-9][a-zA-Z0-9._\\-]*$");

    @GetMapping("/nslookup")
    public ResponseEntity<String> nslookup(@RequestParam String domain) throws Exception {
        if (!VALID_DOMAIN.matcher(domain).matches()) {
            return ResponseEntity.badRequest().body("Invalid domain");
        }
        Process process = new ProcessBuilder("nslookup", domain).start();
        String output = readProcessOutput(process);
        return ResponseEntity.ok(output);
    }

    @GetMapping("/digest")
    public ResponseEntity<String> fileDigest(@RequestParam String filename) throws Exception {
        if (!VALID_FILENAME.matcher(filename).matches()) {
            return ResponseEntity.badRequest().body("Invalid filename");
        }
        Process process = new ProcessBuilder("md5sum", "/tmp/" + filename).start();
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
