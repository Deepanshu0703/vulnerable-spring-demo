package com.demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/fetch")
public class SsrfController {

    @GetMapping("/url")
    public ResponseEntity<String> fetchUrl(@RequestParam String target) {
        try {
            URL url = new URL(target);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String body = reader.lines().collect(Collectors.joining("\n"));
            reader.close();

            return ResponseEntity.ok(body);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error fetching URL: " + e.getMessage());
        }
    }

    @PostMapping("/preview")
    public ResponseEntity<String> previewLink(@RequestParam String link) {
        try {
            URL url = new URL(link);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(3000);
            connection.setReadTimeout(3000);

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String firstLine = reader.readLine();
            reader.close();

            return ResponseEntity.ok("Preview: " + firstLine);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Preview failed: " + e.getMessage());
        }
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> registerWebhook(@RequestParam String callbackUrl,
                                                   @RequestParam(defaultValue = "test") String event) {
        try {
            URL url = new URL(callbackUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(3000);
            connection.setReadTimeout(3000);
            connection.setDoOutput(true);
            connection.getOutputStream().write(("event=" + event).getBytes());
            int status = connection.getResponseCode();
            return ResponseEntity.ok("Webhook registered, server responded: " + status);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Webhook error: " + e.getMessage());
        }
    }
}
