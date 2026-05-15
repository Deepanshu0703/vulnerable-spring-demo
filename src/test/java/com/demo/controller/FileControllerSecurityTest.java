package com.demo.controller;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FileController.class)
class FileControllerSecurityTest {

    private static final String BASE_DIR = "/tmp/app-files/";

    @Autowired
    private MockMvc mockMvc;

    @BeforeAll
    static void setUp() throws IOException {
        Path baseDir = Path.of(BASE_DIR);
        Files.createDirectories(baseDir);
        Path testFile = baseDir.resolve("test.txt");
        Files.writeString(testFile, "hello world");

        Path subDir = baseDir.resolve("subdir");
        Files.createDirectories(subDir);
        Files.writeString(subDir.resolve("nested.txt"), "nested content");
    }

    // ---- /api/files/read ----

    @Test
    void readFile_validFilename_returns200() throws Exception {
        mockMvc.perform(get("/api/files/read").param("filename", "test.txt"))
                .andExpect(status().isOk())
                .andExpect(content().string("hello world"));
    }

    @Test
    void readFile_traversalWithDotDot_returns400() throws Exception {
        mockMvc.perform(get("/api/files/read").param("filename", "../../etc/passwd"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void readFile_traversalEncoded_returns400() throws Exception {
        mockMvc.perform(get("/api/files/read").param("filename", "../../../etc/passwd"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void readFile_nestedSubdir_returns200() throws Exception {
        mockMvc.perform(get("/api/files/read").param("filename", "subdir/nested.txt"))
                .andExpect(status().isOk())
                .andExpect(content().string("nested content"));
    }

    // ---- /api/files/view ----

    @Test
    void viewFile_validPathUnderBaseDir_returns200() throws Exception {
        mockMvc.perform(get("/api/files/view").param("path", BASE_DIR + "test.txt"))
                .andExpect(status().isOk())
                .andExpect(content().string("hello world"));
    }

    @Test
    void viewFile_absolutePathOutsideBaseDir_returns400() throws Exception {
        mockMvc.perform(get("/api/files/view").param("path", "/etc/passwd"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void viewFile_traversalFromBaseDir_returns400() throws Exception {
        mockMvc.perform(get("/api/files/view").param("path", BASE_DIR + "../../etc/passwd"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void viewFile_directory_returns404() throws Exception {
        mockMvc.perform(get("/api/files/view").param("path", BASE_DIR))
                .andExpect(status().isNotFound());
    }

    // ---- /api/files/list ----

    @Test
    void listDirectory_emptyDir_returnsBaseDirListing() throws Exception {
        mockMvc.perform(get("/api/files/list").param("dir", ""))
                .andExpect(status().isOk());
    }

    @Test
    void listDirectory_validSubdir_returns200() throws Exception {
        mockMvc.perform(get("/api/files/list").param("dir", "subdir"))
                .andExpect(status().isOk());
    }

    @Test
    void listDirectory_traversalWithDotDot_returns400() throws Exception {
        mockMvc.perform(get("/api/files/list").param("dir", "../../"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listDirectory_traversalToEtc_returns400() throws Exception {
        mockMvc.perform(get("/api/files/list").param("dir", "../../etc"))
                .andExpect(status().isBadRequest());
    }
}
