package com.example.usermanagement.controller;

import com.example.usermanagement.dto.ApiResponse;
import com.example.usermanagement.dto.CheckInDto;
import com.example.usermanagement.service.CheckInService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/checkin")
public class CheckInController {

    @Autowired
    private CheckInService checkInService;

    @PostMapping
    public ResponseEntity<?> checkIn(@RequestBody CheckInDto.CheckInRequest request) {
        try {
            var resp = checkInService.checkIn(request.getUserId(), request.getSugarIntake(), request.getNotes());
            return ResponseEntity.ok(ApiResponse.success(resp));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/history")
    public ResponseEntity<?> getHistory(@RequestParam Long userId) {
        try {
            return ResponseEntity.ok(ApiResponse.success(checkInService.getCheckInHistory(userId)));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/streak")
    public ResponseEntity<?> getStreak(@RequestParam Long userId) {
        try {
            return ResponseEntity.ok(ApiResponse.success(checkInService.getStreak(userId)));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/badges")
    public ResponseEntity<?> getBadges(@RequestParam Long userId) {
        try {
            return ResponseEntity.ok(ApiResponse.success(checkInService.getBadges(userId)));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/ranking")
    public ResponseEntity<?> getRanking(@RequestParam(defaultValue = "20") int top) {
        try {
            return ResponseEntity.ok(ApiResponse.success(checkInService.getRanking(top)));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error(e.getMessage()));
        }
    }
}
