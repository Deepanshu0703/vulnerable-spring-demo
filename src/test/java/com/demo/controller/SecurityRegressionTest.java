package com.demo.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.jupiter.api.Assertions.assertFalse;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityRegressionTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void lfiRead_traversalBlocked() throws Exception {
        mockMvc.perform(get("/api/files/read")
                .param("filename", "../../etc/passwd"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void lfiView_absolutePathBlocked() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/files/view")
                .param("path", "/etc/passwd"))
                .andReturn();
        int statusCode = result.getResponse().getStatus();
        assertFalse(statusCode == 200 && result.getResponse().getContentAsString().contains("root:"),
                "Arbitrary file read must be blocked");
    }
}
