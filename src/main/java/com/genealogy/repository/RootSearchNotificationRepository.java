package com.genealogy.repository;

import com.genealogy.entity.RootSearchNotification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 寻根通知Repository
 */
public interface RootSearchNotificationRepository extends JpaRepository<RootSearchNotification, Long> {

    /**
     * 查询用户未读通知
     */
    List<RootSearchNotification> findByUserIdAndIsReadFalse(Long userId);

    /**
     * 查询用户所有通知，按时间倒序
     */
    List<RootSearchNotification> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * 统计用户未读数量
     */
    long countByUserIdAndIsReadFalse(Long userId);
}
