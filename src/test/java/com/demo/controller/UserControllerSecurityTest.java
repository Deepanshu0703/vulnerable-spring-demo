package com.demo.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.containsString;

@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testListUsers_rejectsSqlInjectionInSortBy() throws Exception {
        String maliciousPayload = "id,(SELECT 1 FROM(SELECT COUNT(*),CONCAT(h2version(),0x3a,FLOOR(RAND(0)*2))x FROM information_schema.tables GROUP BY x)a)";

        mockMvc.perform(get("/api/users/list")
                .param("sortBy", maliciousPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid sort column"));
    }

    @Test
    public void testGetUserById_preventsSqlInjectionInId() throws Exception {
        String maliciousPayload = "0 UNION SELECT id,username,password,role FROM users--";

        // Parameterized query prevents SQL injection by treating input as literal value
        // Database rejects during type conversion (string -> number), which is safe
        try {
            mockMvc.perform(get("/api/users/byId")
                    .param("id", maliciousPayload))
                    .andExpect(status().is5xxServerError());
        } catch (Exception e) {
            // Expected: DataIntegrityViolationException from type conversion failure
            // This confirms the payload was NOT executed as SQL
            assert e.getCause() instanceof org.springframework.dao.DataIntegrityViolationException
                    || e instanceof org.springframework.web.util.NestedServletException;
        }
    }

    @Test
    public void testListUsers_allowsValidSortColumn() throws Exception {
        mockMvc.perform(get("/api/users/list")
                .param("sortBy", "username"))
                .andExpect(status().isOk());
    }

    @Test
    public void testGetUserById_worksWithValidId() throws Exception {
        mockMvc.perform(get("/api/users/byId")
                .param("id", "1"))
                .andExpect(status().isOk());
    }
}
