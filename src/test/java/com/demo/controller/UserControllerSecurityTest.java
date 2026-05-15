package com.demo.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void byIdShouldRejectUnionInjection() throws Exception {
        mockMvc.perform(get("/api/users/byId").param("id", "0 UNION SELECT id,username,password,role FROM users--"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listShouldRejectSqliInSortBy() throws Exception {
        mockMvc.perform(get("/api/users/list").param("sortBy", "id,(SELECT 1 FROM information_schema.tables)"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void searchShouldRejectSqliPayload() throws Exception {
        mockMvc.perform(get("/api/users/search").param("username", "' OR '1'='1"))
                .andExpect(status().isOk())
                .andExpect(content().string("[]"));
    }
}
