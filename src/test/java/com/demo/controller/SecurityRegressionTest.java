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
    void xssEcho_payloadIsEscaped() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/content/echo")
                .param("message", "<script>alert(document.cookie)</script>"))
                .andExpect(status().isOk())
                .andReturn();
        String body = result.getResponse().getContentAsString();
        assertFalse(body.contains("<script>"), "XSS payload must be escaped");
    }

    @Test
    void xssSearch_payloadIsEscaped() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/content/search")
                .param("q", "<img src=x onerror=alert(1)>"))
                .andExpect(status().isOk())
                .andReturn();
        String body = result.getResponse().getContentAsString();
        assertFalse(body.contains("<img src=x"), "XSS payload must be escaped");
    }

    @Test
    void xssGreet_payloadIsEscaped() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/content/greet")
                .param("name", "<svg/onload=alert('xss')>"))
                .andExpect(status().isOk())
                .andReturn();
        String body = result.getResponse().getContentAsString();
        assertFalse(body.contains("<svg"), "XSS payload must be escaped");
    }

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

    @Test
    void cmdiNslookup_injectionBlocked() throws Exception {
        mockMvc.perform(get("/api/system/nslookup")
                .param("domain", "example.com; cat /etc/passwd"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void cmdiDigest_injectionBlocked() throws Exception {
        mockMvc.perform(get("/api/system/digest")
                .param("filename", "report.txt cat/etc/shadow"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void sqliList_injectionBlocked() throws Exception {
        mockMvc.perform(get("/api/users/list")
                .param("sortBy", "id,(SELECT 1 FROM information_schema.tables)"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void sqliById_injectionBlocked() throws Exception {
        mockMvc.perform(get("/api/users/byId")
                .param("id", "0 UNION SELECT id,username,password,role FROM users--"))
                .andExpect(status().isBadRequest());
    }
}
