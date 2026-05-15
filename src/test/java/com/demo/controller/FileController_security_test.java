package com.demo.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class FileController_security_test {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void testPathTraversalBlocked() {
        ResponseEntity<String> response = restTemplate.getForEntity(
            "/api/files/read?filename=../../etc/passwd",
            String.class
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid path", response.getBody());
    }

    @Test
    public void testPathTraversalVariantBlocked() {
        ResponseEntity<String> response = restTemplate.getForEntity(
            "/api/files/read?filename=../../../etc/shadow",
            String.class
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
}
