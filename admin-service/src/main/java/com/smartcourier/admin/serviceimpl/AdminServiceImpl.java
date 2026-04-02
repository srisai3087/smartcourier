package com.smartcourier.admin.serviceimpl;

import com.smartcourier.admin.client.DeliveryServiceClient;
import com.smartcourier.admin.dto.*;
import com.smartcourier.admin.entity.ExceptionLog;
import com.smartcourier.admin.entity.Hub;
import com.smartcourier.admin.entity.Report;
import com.smartcourier.admin.exception.ResourceNotFoundException;
import com.smartcourier.admin.repository.ExceptionLogRepository;
import com.smartcourier.admin.repository.HubRepository;
import com.smartcourier.admin.repository.ReportRepository;
import com.smartcourier.admin.service.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final ExceptionLogRepository exceptionLogRepository;
    private final HubRepository hubRepository;
    private final ReportRepository reportRepository;
    private final DeliveryServiceClient deliveryServiceClient;
    private final ModelMapper modelMapper;

    @Override
    public DashboardResponse getDashboard() {

        long unresolved = exceptionLogRepository.countByResolvedByIsNull();
        long totalHubs  = hubRepository.countByIsActiveTrue();

        long total = 0;
        long delivered = 0;
        long failed = 0;
        long delayed = 0;
        long pending = 0;

        try {
            // ✅ Feign call (UPDATED)
            PageResponse<DeliveryDTO> page =
                    deliveryServiceClient.getAllDeliveries(0, 100);

            total = page.getTotalElements();

            for (DeliveryDTO d : page.getContent()) {

                String status = d.getStatus();

                if ("DELIVERED".equals(status)) delivered++;
                else if ("FAILED".equals(status)) failed++;
                else if ("DELAYED".equals(status)) delayed++;
                else pending++;
            }

        } catch (Exception e) {
            log.warn("Error fetching delivery data: {}", e.getMessage());
        }

        return DashboardResponse.builder()
                .totalDeliveries(total)
                .totalDelivered(delivered)
                .totalFailed(failed)
                .totalDelayed(delayed)
                .totalPending(pending)
                .unresolvedExceptions(unresolved)
                .totalHubs(totalHubs)
                .build();
    }
    // ================= EXCEPTION =================
    @Override
    @Transactional
    public ExceptionLog resolveException(Long exceptionId, ExceptionResolutionRequest req, Long adminId) {
        ExceptionLog exceptionLog = exceptionLogRepository.findById(exceptionId)
                .orElseThrow(() -> new ResourceNotFoundException("Exception log not found: " + exceptionId));

        exceptionLog.setResolvedBy(adminId);
        exceptionLog.setResolvedAt(LocalDateTime.now());
        exceptionLog.setResolution(req.getResolution());

        log.info("Exception {} resolved by admin {}", exceptionId, adminId);

        return exceptionLogRepository.save(exceptionLog);
    }

    // ================= REPORT =================
    @Override
    public List<ReportDTO> getReports() {
        return reportRepository.findAll().stream()
                .map(r -> modelMapper.map(r, ReportDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ReportDTO generateReport() {


        DashboardResponse dashboard = getDashboard();

        Report report = Report.builder()
                .reportType("DAILY")
                .reportDate(LocalDate.now())
                .totalDeliveries(dashboard.getTotalDeliveries())
                .delivered(dashboard.getTotalDelivered())
                .failed(dashboard.getTotalFailed())
                .pending(dashboard.getTotalPending())
                .build();

        Report saved = reportRepository.save(report);

        log.info("Daily report generated for date: {}", LocalDate.now());

        return modelMapper.map(saved, ReportDTO.class);
    }

    // ================= HUB =================
    @Override
    public List<Hub> getAllHubs() {
        return hubRepository.findByIsActiveTrue();
    }

    @Override
    @Transactional
    public Hub createHub(HubRequest req) {
        Hub hub = modelMapper.map(req, Hub.class);
        hub.setIsActive(true);
        return hubRepository.save(hub);
    }

    @Override
    @Transactional
    public Hub updateHub(Long id, HubRequest req) {
        Hub hub = hubRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hub not found: " + id));

        modelMapper.map(req, hub);
        return hubRepository.save(hub);
    }


    @Override
    @Transactional
    public ExceptionLog createException(ExceptionCreateRequest req) {

        ExceptionLog log = ExceptionLog.builder()
                .deliveryId(req.getDeliveryId())
                .exceptionType(req.getExceptionType())
                .reason(req.getReason())
                .createdAt(LocalDateTime.now())
                .resolvedAt(null)
                .resolvedBy(null)
                .build();

        return exceptionLogRepository.save(log);
    }

    @Override
    public List<ExceptionLogDTO> getAllExceptions() {
        return exceptionLogRepository.findAll().stream().map(e -> {
            ExceptionLogDTO dto = new ExceptionLogDTO();

            dto.setId(e.getId());
            dto.setDeliveryId(e.getDeliveryId());
            dto.setExceptionType(e.getExceptionType());
            dto.setReason(e.getReason());
            dto.setResolution(e.getResolution());
            dto.setCreatedAt(e.getCreatedAt());
            dto.setResolvedAt(e.getResolvedAt());
            dto.setResolvedBy(e.getResolvedBy());

            return dto;
        }).toList();
    }
}