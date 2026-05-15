package com.demo.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.io.File;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@WebMvcTest(CommandController.class)
class CommandControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    static boolean isNslookupAvailable() {
        for (String dir : System.getenv("PATH").split(File.pathSeparator)) {
            if (new File(dir, "nslookup").canExecute()) {
                return true;
            }
        }
        return false;
    }

    // --- nslookup: malicious inputs should be rejected with 400 ---

    @Test
    void nslookup_commandInjectionWithSemicolon_returns400() throws Exception {
        mockMvc.perform(get("/api/system/nslookup").param("domain", "example.com; cat /etc/passwd"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Invalid domain")));
    }

    @Test
    void nslookup_commandInjectionWithPipe_returns400() throws Exception {
        mockMvc.perform(get("/api/system/nslookup").param("domain", "example.com | ls"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Invalid domain")));
    }

    @Test
    void nslookup_commandInjectionWithBacktick_returns400() throws Exception {
        mockMvc.perform(get("/api/system/nslookup").param("domain", "`whoami`.example.com"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Invalid domain")));
    }

    @Test
    void nslookup_commandInjectionWithAmpersand_returns400() throws Exception {
        mockMvc.perform(get("/api/system/nslookup").param("domain", "example.com && cat /etc/shadow"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Invalid domain")));
    }

    @Test
    void nslookup_commandInjectionWithSubshell_returns400() throws Exception {
        mockMvc.perform(get("/api/system/nslookup").param("domain", "$(cat /etc/passwd)"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Invalid domain")));
    }

    @Test
    void nslookup_emptyDomain_returns400() throws Exception {
        mockMvc.perform(get("/api/system/nslookup").param("domain", ""))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Invalid domain")));
    }

    @Test
    @EnabledIf("isNslookupAvailable")
    void nslookup_validDomain_notRejectedByValidation() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/system/nslookup").param("domain", "example.com"))
                .andReturn();
        assertNotEquals(400, result.getResponse().getStatus(),
                "Valid domain should not be rejected by input validation");
    }

    @Test
    @EnabledIf("isNslookupAvailable")
    void nslookup_validDomainWithHyphen_notRejectedByValidation() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/system/nslookup").param("domain", "my-site.example.com"))
                .andReturn();
        assertNotEquals(400, result.getResponse().getStatus(),
                "Valid domain with hyphens should not be rejected by input validation");
    }

    // --- digest: malicious inputs should be rejected with 400 ---

    @Test
    void digest_commandInjectionWithSemicolon_returns400() throws Exception {
        mockMvc.perform(get("/api/system/digest").param("filename", "report.txt; cat /etc/shadow"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Invalid filename")));
    }

    @Test
    void digest_commandInjectionWithPipe_returns400() throws Exception {
        mockMvc.perform(get("/api/system/digest").param("filename", "report.txt | ls"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Invalid filename")));
    }

    @Test
    void digest_pathTraversalWithSlash_returns400() throws Exception {
        mockMvc.perform(get("/api/system/digest").param("filename", "../../etc/passwd"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Invalid filename")));
    }

    @Test
    void digest_commandInjectionWithBacktick_returns400() throws Exception {
        mockMvc.perform(get("/api/system/digest").param("filename", "`whoami`.txt"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Invalid filename")));
    }

    @Test
    void digest_commandInjectionWithSubshell_returns400() throws Exception {
        mockMvc.perform(get("/api/system/digest").param("filename", "$(cat /etc/passwd)"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Invalid filename")));
    }

    @Test
    void digest_emptyFilename_returns400() throws Exception {
        mockMvc.perform(get("/api/system/digest").param("filename", ""))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Invalid filename")));
    }

    @Test
    void digest_observedPayload_returns400() throws Exception {
        mockMvc.perform(get("/api/system/digest").param("filename", "report.txt cat/etc/shadow"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Invalid filename")));
    }

    @Test
    void digest_validFilename_returns200() throws Exception {
        new java.io.File("/tmp/test-report.txt").createNewFile();
        mockMvc.perform(get("/api/system/digest").param("filename", "test-report.txt"))
                .andExpect(status().isOk());
    }

    @Test
    void digest_validFilenameWithUnderscore_returns200() throws Exception {
        new java.io.File("/tmp/my_report_2024.txt").createNewFile();
        mockMvc.perform(get("/api/system/digest").param("filename", "my_report_2024.txt"))
                .andExpect(status().isOk());
    }
}
