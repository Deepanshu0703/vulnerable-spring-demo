package com.demo.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class XssControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void echo_xssPayload_isEscaped() throws Exception {
        mockMvc.perform(get("/api/content/echo")
                        .param("message", "<script>alert(document.cookie)</script>"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.containsString("<script>"))));
    }

    @Test
    void search_xssPayload_isEscaped() throws Exception {
        mockMvc.perform(get("/api/content/search")
                        .param("q", "<img src=x onerror=alert(1)>"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.containsString("<img src=x"))));
    }

    @Test
    void greet_xssPayload_isEscaped() throws Exception {
        mockMvc.perform(get("/api/content/greet")
                        .param("name", "<svg/onload=alert('xss')>"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.containsString("<svg/onload"))));
    }
}
