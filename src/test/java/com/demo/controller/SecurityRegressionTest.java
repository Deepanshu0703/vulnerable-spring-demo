package com.demo.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityRegressionTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void cmdiNslookup_injectionBlocked() throws Exception {
        mockMvc.perform(get("/api/system/nslookup")
                .param("domain", "example.com; cat /etc/passwd"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void cmdiDigest_injectionBlocked() throws Exception {
        mockMvc.perform(get("/api/system/digest")
                .param("filename", "report.txt cat/etc/shadow"))
                .andExpect(status().isBadRequest());
    }
}
