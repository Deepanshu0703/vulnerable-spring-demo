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
class UserControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void list_sqlInjectionInSortBy_isBlocked() throws Exception {
        mockMvc.perform(get("/api/users/list").param("sortBy",
                "id,(SELECT 1 FROM(SELECT COUNT(*),CONCAT(h2version(),0x3a,FLOOR(RAND(0)*2))x FROM information_schema.tables GROUP BY x)a)"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void byId_sqlInjection_isBlocked() throws Exception {
        mockMvc.perform(get("/api/users/byId").param("id", "0 UNION SELECT id,username,password,role FROM users--"))
                .andExpect(status().isBadRequest());
    }
}
