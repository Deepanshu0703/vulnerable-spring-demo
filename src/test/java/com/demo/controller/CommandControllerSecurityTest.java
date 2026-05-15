package com.demo.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.containsString;

@SpringBootTest
@AutoConfigureMockMvc
class CommandControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void nslookup_rejectsCommandInjection() throws Exception {
        mockMvc.perform(get("/api/system/nslookup")
                .param("domain", "example.com; cat /etc/passwd"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(not(containsString("root:"))));
    }

    @Test
    void nslookup_allowsValidDomain() throws Exception {
        int status = mockMvc.perform(get("/api/system/nslookup")
                .param("domain", "example.com"))
                .andReturn().getResponse().getStatus();
        assertNotEquals(400, status);
    }

    @Test
    void digest_rejectsCommandInjection() throws Exception {
        mockMvc.perform(get("/api/system/digest")
                .param("filename", "report.txt cat/etc/shadow"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(not(containsString("root:"))));
    }

    @Test
    void digest_allowsValidFilename() throws Exception {
        int status = mockMvc.perform(get("/api/system/digest")
                .param("filename", "report.txt"))
                .andReturn().getResponse().getStatus();
        assertNotEquals(400, status);
    }
}
