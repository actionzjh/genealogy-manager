package com.genealogy.repository;

import com.genealogy.entity.OperationLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 操作日志Repository
 */
public interface OperationLogRepository extends JpaRepository<OperationLog, Long> {

    /**
     * 查询某家谱的所有操作日志
     */
    List<OperationLog> findByGenealogyIdOrderByCreatedAtDesc(Long genealogyId);
}
