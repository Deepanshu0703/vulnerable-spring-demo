package com.demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;

@RestController
@RequestMapping("/api/system")
public class CommandController {

    @GetMapping("/ping")
    public ResponseEntity<String> pingHost(@RequestParam String host) throws Exception {
        String command = "ping -c 2 " + host;
        Process process = Runtime.getRuntime().exec(new String[]{"sh", "-c", command});
        String output = readProcessOutput(process);
        return ResponseEntity.ok(output);
    }

    @GetMapping("/nslookup")
    public ResponseEntity<String> nslookup(@RequestParam String domain) throws Exception {
        String command = "nslookup " + domain;
        Process process = Runtime.getRuntime().exec(new String[]{"sh", "-c", command});
        String output = readProcessOutput(process);
        return ResponseEntity.ok(output);
    }

    @GetMapping("/digest")
    public ResponseEntity<String> fileDigest(@RequestParam String filename) throws Exception {
        String command = "md5sum /tmp/" + filename;
        Process process = Runtime.getRuntime().exec(new String[]{"sh", "-c", command});
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
