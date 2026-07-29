package dev.gustavo.passin.controller;

import dev.gustavo.passin.service.AttendeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/check-ins")
@RequiredArgsConstructor
public class CheckInController {

    private final AttendeeService attendeeService;

    @PostMapping("/{token}")
    public ResponseEntity<Void> checkIn(@PathVariable String token) {
        attendeeService.checkInAttendeeByToken(token);
        return ResponseEntity.noContent().build();
    }
}
