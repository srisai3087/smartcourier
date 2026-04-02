package com.smartcourier.admin.service;
import com.smartcourier.admin.dto.*;
import com.smartcourier.admin.entity.ExceptionLog;
import com.smartcourier.admin.entity.Hub;
import java.util.List;

public interface AdminService {
    DashboardResponse getDashboard();
    com.smartcourier.admin.entity.ExceptionLog resolveException(Long exceptionId, ExceptionResolutionRequest req, Long adminId);
    List<ReportDTO> getReports();
    ReportDTO generateReport();
    List<Hub> getAllHubs();
    Hub createHub(HubRequest request);
    Hub updateHub(Long id, HubRequest request);
    ExceptionLog createException(ExceptionCreateRequest req);
    List<ExceptionLogDTO> getAllExceptions();
}
