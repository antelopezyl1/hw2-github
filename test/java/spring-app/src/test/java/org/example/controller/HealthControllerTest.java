package org.example.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(HealthController.class)
class HealthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void healthzTest() throws Exception {
        // a http get /healthz
        mockMvc.perform(get("/healthz")
                        .contentType(MediaType.APPLICATION_JSON))

                // assert: expect 200 ok
                .andExpect(status().isOk())

                // assert：expect "status" be "up"
                .andExpect(jsonPath("$.status").value("UP"));
    }
}
