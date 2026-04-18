package com.petroapp.stationtransfer;

import com.petroapp.stationtransfer.models.beans.TransferRequest;
import com.petroapp.stationtransfer.models.dtos.EventDto;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class TransferControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    private EventDto event(String id, String station, double amount) {
        EventDto dto = new EventDto();
        dto.setEvent_id(id);
        dto.setStation_id(station);
        dto.setAmount(amount);
        dto.setStatus("approved");
        dto.setCreated_at("2026-02-19T10:00:00Z");
        return dto;
    }

    private String body(List<EventDto> events) throws Exception {
        TransferRequest req = new TransferRequest();
        req.setEvents(events);
        return objectMapper.writeValueAsString(req);
    }

    @Test
    void batchInsert_countsInsertedAndDuplicates() throws Exception {
        String payload = body(List.of(
                event("e1", "s1", 100),
                event("e2", "s1", 200)
        ));

        mockMvc.perform(post("/api/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.inserted").value(2))
                .andExpect(jsonPath("$.duplicates").value(0));

        // send same again → duplicates
        mockMvc.perform(post("/api/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.inserted").value(0))
                .andExpect(jsonPath("$.duplicates").value(2));
    }

    @Test
    void duplicateEvent_doesNotAffectSummary() throws Exception {
        String payload = body(List.of(event("e1", "s1", 100)));

        mockMvc.perform(post("/api/transfers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload));

        // duplicate insert
        mockMvc.perform(post("/api/transfers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload));

        mockMvc.perform(get("/api/stations/s1/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total_approved_amount").value(100.0))
                .andExpect(jsonPath("$.events_count").value(1));
    }

    @Test
    void outOfOrderArrival_sameTotals() throws Exception {

        String payload1 = body(List.of(
                event("e1", "s1", 100),
                event("e2", "s1", 200)
        ));

        String payload2 = body(List.of(
                event("e2", "s1", 200),
                event("e1", "s1", 100)
        ));

        mockMvc.perform(post("/api/transfers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload1));

        mockMvc.perform(get("/api/stations/s1/summary"))
                .andExpect(jsonPath("$.total_approved_amount").value(300.0));

        // reset DB not needed if isolated test DB per test OR use different station
        mockMvc.perform(post("/api/transfers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload2));

        mockMvc.perform(get("/api/stations/s1/summary"))
                .andExpect(jsonPath("$.total_approved_amount").value(300.0));
    }

    @Test
    void concurrentIngestion_noDoubleInsert() throws Exception {

        String payload = body(List.of(event("e1", "s1", 100)));

        int threads = 5;
        ExecutorService executor = Executors.newFixedThreadPool(threads);

        Callable<Void> task = () -> {
            mockMvc.perform(post("/api/transfers")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(payload));
            return null;
        };

        List<Callable<Void>> tasks = List.of(task, task, task, task, task);

        executor.invokeAll(tasks);
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);

        mockMvc.perform(get("/api/stations/s1/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.events_count").value(1))
                .andExpect(jsonPath("$.total_approved_amount").value(100.0));
    }

    @Test
    void summary_perStation_correct() throws Exception {

        String payload = body(List.of(
                event("e1", "s1", 100),
                event("e2", "s1", 50),
                event("e3", "s2", 300)
        ));

        mockMvc.perform(post("/api/transfers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload));

        mockMvc.perform(get("/api/stations/s1/summary"))
                .andExpect(jsonPath("$.total_approved_amount").value(150.0))
                .andExpect(jsonPath("$.events_count").value(2));

        mockMvc.perform(get("/api/stations/s2/summary"))
                .andExpect(jsonPath("$.total_approved_amount").value(300.0))
                .andExpect(jsonPath("$.events_count").value(1));
    }

    @Test
    void validationFailure_returns400() throws Exception {

        EventDto invalid = new EventDto();
        invalid.setEvent_id(""); // invalid
        invalid.setStation_id("s1");
        invalid.setAmount(-10.0); // invalid
        invalid.setStatus("approved");
        invalid.setCreated_at("invalid-date");

        String payload = body(List.of(invalid));

        mockMvc.perform(post("/api/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest());
    }

    @Test
    void unknownStatus_notCountedInApprovedTotal() throws Exception {

        EventDto e = event("e1", "s1", 100);
        e.setStatus("pending");

        mockMvc.perform(post("/api/transfers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body(List.of(e))));

        mockMvc.perform(get("/api/stations/s1/summary"))
                .andExpect(jsonPath("$.total_approved_amount").value(0.0))
                .andExpect(jsonPath("$.events_count").value(1));
    }
}