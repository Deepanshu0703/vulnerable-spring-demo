package com.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/search")
    public ResponseEntity<?> searchUsers(@RequestParam String username) {
        String sql = "SELECT id, username, email, role FROM users WHERE username = '" + username + "'";
        List<Map<String, Object>> results = jdbcTemplate.queryForList(sql);
        return ResponseEntity.ok(results);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestParam String username, @RequestParam String password) {
        String sql = "SELECT * FROM users WHERE username = '" + username + "' AND password = '" + password + "'";
        List<Map<String, Object>> results = jdbcTemplate.queryForList(sql);
        if (!results.isEmpty()) {
            return ResponseEntity.ok(Map.of("status", "login_success", "user", results.get(0)));
        }
        return ResponseEntity.status(401).body(Map.of("status", "login_failed"));
    }

    @GetMapping("/byId")
    public ResponseEntity<?> getUserById(@RequestParam String id) {
        String sql = "SELECT id, username, email, role FROM users WHERE id = ?";
        List<Map<String, Object>> results = jdbcTemplate.queryForList(sql, id);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/list")
    public ResponseEntity<?> listUsers(@RequestParam(defaultValue = "id") String sortBy) {
        // Allowlist validation to prevent SQL injection in ORDER BY clause
        List<String> allowedColumns = List.of("id", "username", "email", "role");
        if (!allowedColumns.contains(sortBy)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid sort column"));
        }
        String sql = "SELECT id, username, email, role FROM users ORDER BY " + sortBy;
        List<Map<String, Object>> results = jdbcTemplate.queryForList(sql);
        return ResponseEntity.ok(results);
    }
}
