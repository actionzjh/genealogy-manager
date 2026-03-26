package com.genealogy.service;

import com.genealogy.entity.OperationLog;
import com.genealogy.repository.OperationLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 操作日志服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OperationLogService {

    private final OperationLogRepository operationLogRepository;

    /**
     * 记录操作
     */
    public void log(Long userId, Long genealogyId, String targetType, Long targetId, String operationType, String description, String ipAddress) {
        OperationLog opLog = new OperationLog();
        opLog.setUserId(userId);
        opLog.setGenealogyId(genealogyId);
        opLog.setTargetType(targetType);
        opLog.setTargetId(targetId);
        opLog.setOperationType(operationType);
        opLog.setDescription(description);
        opLog.setIpAddress(ipAddress);
        operationLogRepository.save(opLog);
        log.debug("操作日志: userId={}, genealogy={}, target={}-{}, operation={}",
                userId, genealogyId, targetType, targetId, operationType);
    }

    /**
     * 获取家谱操作日志
     */
    public List<OperationLog> getLogs(Long genealogyId) {
        return operationLogRepository.findByGenealogyIdOrderByCreatedAtDesc(genealogyId);
    }
}
