package com.demo.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.containsString;

@SpringBootTest
@AutoConfigureMockMvc
class XssControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void echoShouldEscapeXssPayload() throws Exception {
        mockMvc.perform(get("/api/content/echo").param("message", "<script>alert(document.cookie)</script>"))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("<script>alert(document.cookie)</script>"))));
    }

    @Test
    void searchShouldEscapeXssPayload() throws Exception {
        mockMvc.perform(get("/api/content/search").param("q", "<img src=x onerror=alert(1)>"))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("<img src=x onerror=alert(1)>"))));
    }

    @Test
    void greetShouldEscapeXssPayload() throws Exception {
        mockMvc.perform(get("/api/content/greet").param("name", "<svg/onload=alert('xss')>"))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("<svg/onload=alert('xss')>"))));
    }
}
