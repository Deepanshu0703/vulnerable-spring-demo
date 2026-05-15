package com.demo.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    // ---- /api/users/search ----

    @Test
    void search_legitimateQuery_returnsResults() throws Exception {
        mockMvc.perform(get("/api/users/search").param("username", "admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].USERNAME", is("admin")));
    }

    @Test
    void search_sqlInjection_returnsEmptyNotAllRows() throws Exception {
        mockMvc.perform(get("/api/users/search").param("username", "' OR '1'='1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void search_unionInjection_returnsEmpty() throws Exception {
        mockMvc.perform(get("/api/users/search")
                        .param("username", "' UNION SELECT id,username,password,role FROM users--"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    // ---- /api/users/login ----

    @Test
    void login_validCredentials_succeeds() throws Exception {
        mockMvc.perform(post("/api/users/login")
                        .param("username", "admin")
                        .param("password", "admin123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("login_success")));
    }

    @Test
    void login_invalidCredentials_fails() throws Exception {
        mockMvc.perform(post("/api/users/login")
                        .param("username", "admin")
                        .param("password", "wrongpassword"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is("login_failed")));
    }

    @Test
    void login_sqlInjectionBypass_fails() throws Exception {
        mockMvc.perform(post("/api/users/login")
                        .param("username", "' OR '1'='1")
                        .param("password", "' OR '1'='1"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is("login_failed")));
    }

    @Test
    void login_sqlInjectionCommentBypass_fails() throws Exception {
        mockMvc.perform(post("/api/users/login")
                        .param("username", "admin'--")
                        .param("password", "anything"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is("login_failed")));
    }

    // ---- /api/users/byId ----

    @Test
    void byId_legitimateQuery_returnsResult() throws Exception {
        mockMvc.perform(get("/api/users/byId").param("id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].USERNAME", is("admin")));
    }

    @Test
    void byId_unionInjection_doesNotReturnExtraData() throws Exception {
        org.junit.jupiter.api.Assertions.assertThrows(
                org.springframework.web.util.NestedServletException.class,
                () -> mockMvc.perform(get("/api/users/byId")
                        .param("id", "0 UNION SELECT id,username,password,role FROM users--"))
        );
    }

    // ---- /api/users/list ----

    @Test
    void list_validSortColumn_returnsResults() throws Exception {
        mockMvc.perform(get("/api/users/list").param("sortBy", "username"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(4)));
    }

    @Test
    void list_defaultSort_returnsResults() throws Exception {
        mockMvc.perform(get("/api/users/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(4)));
    }

    @Test
    void list_sqlInjectionInSortBy_rejected() throws Exception {
        mockMvc.perform(get("/api/users/list")
                        .param("sortBy", "id,(SELECT 1 FROM(SELECT COUNT(*),CONCAT(h2version(),0x3a,FLOOR(RAND(0)*2))x FROM information_schema.tables GROUP BY x)a)"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Invalid sort column")));
    }

    @Test
    void list_arbitraryColumnNameRejected() throws Exception {
        mockMvc.perform(get("/api/users/list").param("sortBy", "password"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Invalid sort column")));
    }

    @Test
    void list_sortByCaseInsensitive_accepted() throws Exception {
        mockMvc.perform(get("/api/users/list").param("sortBy", "USERNAME"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(4)));
    }
}
