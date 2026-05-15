package com.demo.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class XssControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testEchoXssBlocked() throws Exception {
        mockMvc.perform(get("/api/content/echo")
                .param("message", "<script>alert(document.cookie)</script>"))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("<script>"))))
                .andExpect(content().string(containsString("&lt;script&gt;")));
    }

    @Test
    public void testSearchXssBlocked() throws Exception {
        mockMvc.perform(get("/api/content/search")
                .param("q", "<img src=x onerror=alert(1)>"))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("<img src=x"))))
                .andExpect(content().string(containsString("&lt;img")));
    }

    @Test
    public void testGreetXssBlocked() throws Exception {
        mockMvc.perform(get("/api/content/greet")
                .param("name", "<svg/onload=alert('xss')>"))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("<svg"))))
                .andExpect(content().string(containsString("&lt;svg")));
    }
}
