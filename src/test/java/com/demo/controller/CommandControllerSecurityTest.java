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
class CommandControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void nslookupShouldRejectCommandInjection() throws Exception {
        mockMvc.perform(get("/api/system/nslookup").param("domain", "example.com; cat /etc/passwd"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void digestShouldRejectCommandInjection() throws Exception {
        mockMvc.perform(get("/api/system/digest").param("filename", "report.txt cat/etc/shadow"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void nslookupShouldAcceptValidDomain() throws Exception {
        try {
            mockMvc.perform(get("/api/system/nslookup").param("domain", "example.com"))
                    .andExpect(result -> {
                        int status = result.getResponse().getStatus();
                        assert status != 400 : "Valid domain should not be rejected as invalid";
                    });
        } catch (Exception e) {
            // nslookup binary may not exist in test env - IOException is acceptable
            assert e.getCause() == null || e.getCause() instanceof java.io.IOException;
        }
    }
}
