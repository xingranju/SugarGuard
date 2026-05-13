package com.example.usermanagement.controller;

import com.example.usermanagement.dto.ApiResponse;
import com.example.usermanagement.dto.FamilyDto.*;
import com.example.usermanagement.service.FamilyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/family")
public class FamilyController {

    @Autowired
    private FamilyService familyService;

    @PostMapping("/create")
    public ResponseEntity<?> createFamily(@RequestParam Long userId,
                                           @RequestBody CreateFamilyRequest request) {
        try {
            return ResponseEntity.ok(ApiResponse.success(familyService.createFamily(userId, request.getName())));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/join")
    public ResponseEntity<?> joinFamily(@RequestParam Long userId,
                                         @RequestBody JoinFamilyRequest request) {
        try {
            return ResponseEntity.ok(ApiResponse.success(familyService.joinFamily(userId, request.getInviteCode())));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/my")
    public ResponseEntity<?> getMyFamilies(@RequestParam Long userId) {
        try {
            return ResponseEntity.ok(ApiResponse.success(familyService.getMyFamilies(userId)));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/{groupId}/members")
    public ResponseEntity<?> getFamilyMembers(@PathVariable Long groupId) {
        try {
            return ResponseEntity.ok(ApiResponse.success(familyService.getFamilyMembers(groupId)));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{groupId}/members/{targetUserId}")
    public ResponseEntity<?> removeMember(@PathVariable Long groupId,
                                           @PathVariable Long targetUserId,
                                           @RequestParam Long operatorId) {
        try {
            familyService.removeMember(groupId, targetUserId, operatorId);
            return ResponseEntity.ok(ApiResponse.success("成员已移除"));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/{groupId}/alerts")
    public ResponseEntity<?> getAlerts(@PathVariable Long groupId) {
        try {
            return ResponseEntity.ok(ApiResponse.success(familyService.getAlerts(groupId)));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/{groupId}/check-alerts")
    public ResponseEntity<?> checkAlerts(@PathVariable Long groupId) {
        try {
            return ResponseEntity.ok(ApiResponse.success(familyService.checkHealthAlerts(groupId)));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/alerts/{alertId}/read")
    public ResponseEntity<?> markAlertRead(@PathVariable Long alertId) {
        try {
            familyService.markAlertRead(alertId);
            return ResponseEntity.ok(ApiResponse.success("已标记为已读"));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error(e.getMessage()));
        }
    }
}
