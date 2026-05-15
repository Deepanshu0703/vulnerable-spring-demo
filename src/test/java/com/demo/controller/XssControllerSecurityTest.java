package com.demo.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class XssControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testXssBlockedOnGreet() throws Exception {
        // Test that XSS payload is HTML-escaped on /greet endpoint
        mockMvc.perform(get("/api/content/greet")
                .param("name", "<svg/onload=alert('xss')>"))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("<svg/onload="))))
                .andExpect(content().string(containsString("&lt;svg")));
    }

    @Test
    public void testXssBlockedOnEcho() throws Exception {
        // Test that XSS payload is HTML-escaped on /echo endpoint
        mockMvc.perform(get("/api/content/echo")
                .param("message", "<script>alert(document.cookie)</script>"))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("<script>"))))
                .andExpect(content().string(containsString("&lt;script&gt;")));
    }

    @Test
    public void testXssBlockedOnSearch() throws Exception {
        // Test that XSS payload is HTML-escaped on /search endpoint
        mockMvc.perform(get("/api/content/search")
                .param("q", "<img src=x onerror=alert(1)>"))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("<img src=x"))))
                .andExpect(content().string(containsString("&lt;img")));
    }
}
