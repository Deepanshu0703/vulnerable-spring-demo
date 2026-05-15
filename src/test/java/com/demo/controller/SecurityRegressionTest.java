package com.demo.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityRegressionTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void xss_echo_payload_is_escaped() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/content/echo")
                        .param("message", "<script>alert(document.cookie)</script>"))
                .andExpect(status().isOk())
                .andReturn();
        String body = result.getResponse().getContentAsString();
        assertFalse(body.contains("<script>alert(document.cookie)</script>"));
    }

    @Test
    void xss_search_payload_is_escaped() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/content/search")
                        .param("q", "<img src=x onerror=alert(1)>"))
                .andExpect(status().isOk())
                .andReturn();
        String body = result.getResponse().getContentAsString();
        assertFalse(body.contains("<img src=x onerror=alert(1)>"));
    }

    @Test
    void xss_greet_payload_is_escaped() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/content/greet")
                        .param("name", "<svg/onload=alert('xss')>"))
                .andExpect(status().isOk())
                .andReturn();
        String body = result.getResponse().getContentAsString();
        assertFalse(body.contains("<svg/onload=alert('xss')>"));
    }

    @Test
    void sqli_users_byId_parameterized() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/users/byId")
                        .param("id", "0 UNION SELECT id,username,password,role FROM users--"))
                .andExpect(status().isOk())
                .andReturn();
        String body = result.getResponse().getContentAsString();
        assertFalse(body.contains("password"));
    }

    @Test
    void sqli_users_list_rejects_invalid_sort() throws Exception {
        mockMvc.perform(get("/api/users/list")
                        .param("sortBy", "id,(SELECT 1 FROM(SELECT COUNT(*),CONCAT(h2version(),0x3a,FLOOR(RAND(0)*2))x FROM information_schema.tables GROUP BY x)a)"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void lfi_read_rejects_traversal() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/files/read")
                        .param("filename", "../../etc/passwd"))
                .andExpect(status().isBadRequest())
                .andReturn();
        String body = result.getResponse().getContentAsString();
        assertTrue(body.contains("Invalid file path"));
    }

    @Test
    void lfi_view_rejects_absolute_path() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/files/view")
                        .param("path", "/etc/passwd"))
                .andExpect(status().isBadRequest())
                .andReturn();
        String body = result.getResponse().getContentAsString();
        assertTrue(body.contains("Access denied"));
    }

    @Test
    void cmdi_nslookup_rejects_injection() throws Exception {
        mockMvc.perform(get("/api/system/nslookup")
                        .param("domain", "example.com; cat /etc/passwd"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void cmdi_digest_rejects_injection() throws Exception {
        mockMvc.perform(get("/api/system/digest")
                        .param("filename", "report.txt cat/etc/shadow"))
                .andExpect(status().isBadRequest());
    }
}
