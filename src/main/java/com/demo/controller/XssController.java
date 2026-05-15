package com.demo.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.HtmlUtils;

import java.util.Map;

@RestController
@RequestMapping("/api/content")
public class XssController {

    @GetMapping(value = "/echo", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> echo(@RequestParam String message) {
        String safeMessage = HtmlUtils.htmlEscape(message);
        String html = "<html><body>"
                + "<h2>Echo</h2>"
                + "<p>You said: " + safeMessage + "</p>"
                + "</body></html>";
        return ResponseEntity.ok(html);
    }

    @GetMapping(value = "/search", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> search(@RequestParam String q) {
        String safeQ = HtmlUtils.htmlEscape(q);
        String html = "<html><body>"
                + "<h2>Search Results</h2>"
                + "<p>Showing results for: <b>" + safeQ + "</b></p>"
                + "<p>No results found.</p>"
                + "</body></html>";
        return ResponseEntity.ok(html);
    }

    @GetMapping(value = "/greet", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> greet(@RequestParam String name) {
        String safeName = HtmlUtils.htmlEscape(name);
        String html = "<html><body>"
                + "<h1>Welcome, " + safeName + "!</h1>"
                + "</body></html>";
        return ResponseEntity.ok(html);
    }

    @PostMapping(value = "/comment", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> postComment(@RequestParam String comment,
                                               @RequestParam(defaultValue = "Anonymous") String author) {
        String safeComment = HtmlUtils.htmlEscape(comment);
        String safeAuthor = HtmlUtils.htmlEscape(author);
        String html = "<html><body>"
                + "<h2>Comment Posted</h2>"
                + "<div class='comment'>"
                + "<strong>" + safeAuthor + "</strong>: " + safeComment
                + "</div>"
                + "</body></html>";
        return ResponseEntity.ok(html);
    }
}
