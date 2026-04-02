package com.smartcourier.admin.repository;
import com.smartcourier.admin.entity.ExceptionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
@Repository
public interface ExceptionLogRepository extends JpaRepository<ExceptionLog, Long> {
    List<ExceptionLog> findByDeliveryId(Long deliveryId);
    List<ExceptionLog> findByResolvedByIsNull();
    long countByResolvedByIsNull();
}
