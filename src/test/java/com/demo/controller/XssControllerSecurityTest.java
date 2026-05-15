package com.demo.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class XssControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    // --- /api/content/echo ---

    @Test
    void echo_shouldEscapeScriptTag() throws Exception {
        String payload = "<script>alert(document.cookie)</script>";
        MvcResult result = mockMvc.perform(get("/api/content/echo")
                        .param("message", payload))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andExpect(content().string(not(containsString("<script>"))))
                .andExpect(content().string(not(containsString("</script>"))))
                .andExpect(content().string(containsString("&lt;script&gt;")))
                .andReturn();
    }

    @Test
    void echo_shouldPreserveSafeContent() throws Exception {
        mockMvc.perform(get("/api/content/echo")
                        .param("message", "Hello World"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Hello World")));
    }

    // --- /api/content/search ---

    @Test
    void search_shouldEscapeImgOnerror() throws Exception {
        String payload = "<img src=x onerror=alert(1)>";
        mockMvc.perform(get("/api/content/search")
                        .param("q", payload))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andExpect(content().string(not(containsString("<img"))))
                .andExpect(content().string(containsString("&lt;img")));
    }

    @Test
    void search_shouldPreserveSafeContent() throws Exception {
        mockMvc.perform(get("/api/content/search")
                        .param("q", "spring boot"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("spring boot")));
    }

    // --- /api/content/greet ---

    @Test
    void greet_shouldEscapeSvgOnload() throws Exception {
        String payload = "<svg/onload=alert('xss')>";
        mockMvc.perform(get("/api/content/greet")
                        .param("name", payload))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andExpect(content().string(not(containsString("<svg"))))
                .andExpect(content().string(containsString("&lt;svg")));
    }

    @Test
    void greet_shouldPreserveSafeContent() throws Exception {
        mockMvc.perform(get("/api/content/greet")
                        .param("name", "Alice"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Welcome, Alice!")));
    }

    // --- /api/content/comment ---

    @Test
    void comment_shouldEscapeCommentPayload() throws Exception {
        String commentPayload = "<script>alert('xss')</script>";
        String authorPayload = "<b onmouseover=alert(1)>hacker</b>";
        mockMvc.perform(post("/api/content/comment")
                        .param("comment", commentPayload)
                        .param("author", authorPayload))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andExpect(content().string(not(containsString("<script>"))))
                .andExpect(content().string(not(containsString("<b onmouseover"))))
                .andExpect(content().string(containsString("&lt;script&gt;")))
                .andExpect(content().string(containsString("&lt;b onmouseover")));
    }

    @Test
    void comment_shouldPreserveSafeContent() throws Exception {
        mockMvc.perform(post("/api/content/comment")
                        .param("comment", "Great post!")
                        .param("author", "Bob"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Bob")))
                .andExpect(content().string(containsString("Great post!")));
    }

    @Test
    void comment_shouldEscapeHtmlEntities() throws Exception {
        String payload = "\"quotes\" & <angles> 'apostrophes'";
        mockMvc.perform(post("/api/content/comment")
                        .param("comment", payload)
                        .param("author", "Test"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("&amp;")))
                .andExpect(content().string(containsString("&lt;angles&gt;")))
                .andExpect(content().string(containsString("&quot;quotes&quot;")));
    }
}
