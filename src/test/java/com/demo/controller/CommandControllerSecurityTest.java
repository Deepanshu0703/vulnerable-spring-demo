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
    void nslookup_commandInjectionPayload_isRejected() throws Exception {
        mockMvc.perform(get("/api/system/nslookup")
                        .param("domain", "example.com; cat /etc/passwd"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.containsString("root:"))));
    }

    @Test
    void digest_commandInjectionPayload_isRejected() throws Exception {
        mockMvc.perform(get("/api/system/digest")
                        .param("filename", "report.txt cat/etc/shadow"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.containsString("root:"))));
    }

    @Test
    void nslookup_validDomain_isNotRejectedByValidation() throws Exception {
        mockMvc.perform(get("/api/system/nslookup")
                        .param("domain", "example.com"))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    assert status != 400 : "Valid domain must not be rejected by input validation";
                });
    }
}
