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
class UserControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getUserByIdShouldRejectSqliPayload() throws Exception {
        try {
            mockMvc.perform(get("/api/users/byId").param("id", "0 UNION SELECT id,username,password,role FROM users--"))
                    .andExpect(result -> {
                        int status = result.getResponse().getStatus();
                        String body = result.getResponse().getContentAsString();
                        assert status != 200 || !body.contains("password") :
                                "SQLi payload should not return password data";
                    });
        } catch (org.springframework.web.util.NestedServletException e) {
            // Parameterized query rejects the non-numeric input - SQLi is neutralized
            assert !e.getMessage().contains("UNION") || e.getCause() instanceof org.springframework.dao.DataIntegrityViolationException;
        }
    }

    @Test
    void listUsersShouldRejectInvalidSortColumn() throws Exception {
        mockMvc.perform(get("/api/users/list").param("sortBy", "id,(SELECT 1 FROM(SELECT COUNT(*),CONCAT(h2version(),0x3a,FLOOR(RAND(0)*2))x FROM information_schema.tables GROUP BY x)a)"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listUsersWithValidSortShouldWork() throws Exception {
        mockMvc.perform(get("/api/users/list").param("sortBy", "username"))
                .andExpect(status().isOk());
    }
}
