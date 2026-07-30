package dev.gustavo.passin.controller;

import dev.gustavo.passin.exception.AttendeeAlreadyCheckedInException;
import dev.gustavo.passin.exception.InvalidCheckInTokenException;
import dev.gustavo.passin.repository.OrganizerRepository;
import dev.gustavo.passin.security.JwtService;
import dev.gustavo.passin.service.AttendeeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CheckInController.class)
@AutoConfigureMockMvc(addFilters = false)
class CheckInControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AttendeeService attendeeService;

    @MockitoBean
    private OrganizerRepository organizerRepository;

    @MockitoBean
    private JwtService jwtService;

    @Test
    void shouldCheckInByToken() throws Exception {
        mockMvc.perform(post("/check-ins/signed-token"))
                .andExpect(status().isNoContent());

        verify(attendeeService).checkInAttendeeByToken("signed-token");
    }

    @Test
    void shouldReturnBadRequestWhenTokenIsInvalid() throws Exception {
        doThrow(new InvalidCheckInTokenException("Invalid check-in token"))
                .when(attendeeService).checkInAttendeeByToken("invalid-token");

        mockMvc.perform(post("/check-ins/invalid-token"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid check-in token"));
    }

    @Test
    void shouldReturnConflictWhenAttendeeIsAlreadyCheckedIn() throws Exception {
        doThrow(new AttendeeAlreadyCheckedInException("This attendee is already checked in"))
                .when(attendeeService).checkInAttendeeByToken("signed-token");

        mockMvc.perform(post("/check-ins/signed-token"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("This attendee is already checked in"));
    }
}
