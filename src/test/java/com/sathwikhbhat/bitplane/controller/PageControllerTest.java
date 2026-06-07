package com.sathwikhbhat.bitplane.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PageController.class)
class PageControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    void index_returnsIndexView() throws Exception {
        mockMvc.perform(get("/")).andExpect(status().isOk()).andExpect(view().name("index"));
    }

    @Test
    void fileSizeError_returnsExceptionErrorView() throws Exception {
        mockMvc.perform(get("/error/file-size").param("operation", "encode"))
                .andExpect(status().isOk())
                .andExpect(view().name("exception-error"));
    }
}
