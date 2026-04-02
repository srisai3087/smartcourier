package com.smartcourier.admin.controller;

import com.smartcourier.admin.dto.*;
import com.smartcourier.admin.entity.ExceptionLog;
import com.smartcourier.admin.entity.Hub;
import com.smartcourier.admin.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    // All admin endpoints — ROLE_ADMIN only
    // If CUSTOMER tries to call any of these → 403 Forbidden

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/dashboard")
    public ResponseEntity<DashboardResponse> getDashboard() {
        return ResponseEntity.ok(adminService.getDashboard());
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PutMapping("/deliveries/{id}/resolve")
    public ResponseEntity<ExceptionLog> resolveException(
            @PathVariable Long id,
            @RequestBody ExceptionResolutionRequest request,
            @RequestHeader("X-User-Id") Long adminId) {
        return ResponseEntity.ok(
                adminService.resolveException(id, request, adminId));
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/reports")
    public ResponseEntity<List<ReportDTO>> getReports() {
        return ResponseEntity.ok(adminService.getReports());
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PostMapping("/reports/generate")
    public ResponseEntity<ReportDTO> generateReport() {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(adminService.generateReport());
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/hubs")
    public ResponseEntity<List<Hub>> getAllHubs() {
        return ResponseEntity.ok(adminService.getAllHubs());
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PostMapping("/hubs")
    public ResponseEntity<Hub> createHub(
            @RequestBody HubRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(adminService.createHub(request));
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PutMapping("/hubs/{id}")
    public ResponseEntity<Hub> updateHub(
            @PathVariable Long id,
            @RequestBody HubRequest request) {
        return ResponseEntity.ok(adminService.updateHub(id, request));
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/exceptions")
    public ResponseEntity<List<ExceptionLogDTO>> getAllExceptions() {
        return ResponseEntity.ok(adminService.getAllExceptions());
    }

    @PreAuthorize("permitAll()")
    @PostMapping("/exceptions")
    public ResponseEntity<ExceptionLog> createException(
            @RequestBody ExceptionCreateRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(adminService.createException(request));
    }
}