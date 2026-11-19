package dev.demo.observatory;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DemoRecordController.class)
class DemoApiUnavailableTest {
    @Autowired
    MockMvc mvc;

    @MockitoBean
    DemoRecordService service;

    @Test
    void reportsUnavailableSourceInsteadOfAnEmptyResult() throws Exception {
        when(service.listRecords()).thenThrow(new DemoSourceUnavailableException());

        mvc.perform(get("/api/demo/records"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.code").value("SOURCE_UNAVAILABLE"));
    }
}
