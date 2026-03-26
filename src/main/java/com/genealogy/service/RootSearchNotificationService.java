package com.genealogy.service;

import com.genealogy.entity.RootSearchNotification;
import com.genealogy.entity.RootSearch;
import com.genealogy.repository.RootSearchNotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 寻根匹配通知服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RootSearchNotificationService {

    private final RootSearchNotificationRepository notificationRepository;

    /**
     * 创建新匹配通知
     */
    @Transactional
    public void createNotification(Long userId, Long searchId, Long matchedGenealogyId, Double score) {
        RootSearchNotification notification = new RootSearchNotification();
        notification.setUserId(userId);
        notification.setSearchId(searchId);
        notification.setMatchedGenealogyId(matchedGenealogyId);
        notification.setMatchScore(score);
        notification.setIsRead(false);
        notificationRepository.save(notification);
        log.info("创建寻根匹配通知: userId={}, searchId={}, matched={}, score={}",
                userId, searchId, matchedGenealogyId, score);
    }

    /**
     * 获取用户未读通知
     */
    public List<RootSearchNotification> getUnread(Long userId) {
        return notificationRepository.findByUserIdAndIsReadFalse(userId);
    }

    /**
     * 获取用户所有通知
     */
    public List<RootSearchNotification> getAll(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    /**
     * 统计未读数量
     */
    public long countUnread(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    /**
     * 标记已读
     */
    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        notificationRepository.findById(notificationId).ifPresent(notification -> {
            if (notification.getUserId().equals(userId)) {
                notification.setIsRead(true);
                notificationRepository.save(notification);
            }
        });
    }

    /**
     * 标记所有已读
     */
    @Transactional
    public void markAllAsRead(Long userId) {
        List<RootSearchNotification> unread = notificationRepository.findByUserIdAndIsReadFalse(userId);
        unread.forEach(notification -> {
            notification.setIsRead(true);
            notificationRepository.save(notification);
        });
    }
}
