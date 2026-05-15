package com.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final Set<String> ALLOWED_SORT_COLUMNS = Set.of("id", "username", "email", "role");

    @GetMapping("/search")
    public ResponseEntity<?> searchUsers(@RequestParam String username) {
        String sql = "SELECT id, username, email, role FROM users WHERE username = ?";
        List<Map<String, Object>> results = jdbcTemplate.queryForList(sql, username);
        return ResponseEntity.ok(results);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestParam String username, @RequestParam String password) {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
        List<Map<String, Object>> results = jdbcTemplate.queryForList(sql, username, password);
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
        if (!ALLOWED_SORT_COLUMNS.contains(sortBy)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid sort column"));
        }
        String sql = "SELECT id, username, email, role FROM users ORDER BY " + sortBy;
        List<Map<String, Object>> results = jdbcTemplate.queryForList(sql);
        return ResponseEntity.ok(results);
    }
}
