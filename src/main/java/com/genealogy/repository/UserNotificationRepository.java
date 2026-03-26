package com.genealogy.repository;

import com.genealogy.entity.UserNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

/**
 * 用户通知 Repository
 */
public interface UserNotificationRepository extends JpaRepository<UserNotification, Long>,
        JpaSpecificationExecutor<UserNotification> {

    /**
     * 查询用户未读通知
     */
    List<UserNotification> findByUserIdAndIsReadFalseOrderByCreatedAtDesc(Long userId);

    /**
     * 查询用户所有通知
     */
    List<UserNotification> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * 统计未读数量
     */
    long countByUserIdAndIsReadFalse(Long userId);
}
