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
class FileControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void readFileShouldRejectPathTraversal() throws Exception {
        mockMvc.perform(get("/api/files/read").param("filename", "../../etc/passwd"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void viewFileShouldRejectAbsolutePathOutsideBase() throws Exception {
        mockMvc.perform(get("/api/files/view").param("path", "/etc/passwd"))
                .andExpect(status().isBadRequest());
    }
}
