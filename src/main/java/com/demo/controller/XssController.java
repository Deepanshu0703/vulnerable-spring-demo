package com.demo.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/content")
public class XssController {

    @GetMapping(value = "/echo", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> echo(@RequestParam String message) {
        String html = "<html><body>"
                + "<h2>Echo</h2>"
                + "<p>You said: " + message + "</p>"
                + "</body></html>";
        return ResponseEntity.ok(html);
    }

    @GetMapping(value = "/search", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> search(@RequestParam String q) {
        String html = "<html><body>"
                + "<h2>Search Results</h2>"
                + "<p>Showing results for: <b>" + q + "</b></p>"
                + "<p>No results found.</p>"
                + "</body></html>";
        return ResponseEntity.ok(html);
    }

    @GetMapping(value = "/greet", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> greet(@RequestParam String name) {
        String html = "<html><body>"
                + "<h1>Welcome, " + name + "!</h1>"
                + "</body></html>";
        return ResponseEntity.ok(html);
    }

    @PostMapping(value = "/comment", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> postComment(@RequestParam String comment,
                                               @RequestParam(defaultValue = "Anonymous") String author) {
        String html = "<html><body>"
                + "<h2>Comment Posted</h2>"
                + "<div class='comment'>"
                + "<strong>" + author + "</strong>: " + comment
                + "</div>"
                + "</body></html>";
        return ResponseEntity.ok(html);
    }
}
