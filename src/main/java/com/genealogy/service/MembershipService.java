package com.genealogy.service;

import com.genealogy.entity.User;
import com.genealogy.repository.GenealogyRepository;
import com.genealogy.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 会员权限服务 - 增值变现功能
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MembershipService {

    private final UserRepository userRepository;
    private final GenealogyRepository genealogyRepository;

    /**
     * 会员等级配置
     */
    public enum MembershipLevel {
        FREE("免费版", 1, 500, 2),
        BASIC("基础会员", 10, 5000, 10),
        PREMIUM("高级会员", 100, 50000, 100);

        public final String label;
        public final int maxGenealogies;
        public final int maxPersonsPerGenealogy;
        public final int maxCollaborators;

        MembershipLevel(String label, int maxGenealogies, int maxPersons, int maxCollaborators) {
            this.label = label;
            this.maxGenealogies = maxGenealogies;
            this.maxPersonsPerGenealogy = maxPersons;
            this.maxCollaborators = maxCollaborators;
        }
    }

    /**
     * 检查用户是否可以创建新家谱
     */
    public CheckResult canCreateGenealogy(Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return CheckResult.error("用户不存在", false);
        }
        User user = userOpt.get();

        // 获取额度
        int max = getUserMaxGenealogies(user);
        long current = genealogyRepository.countByUserId(userId);

        if (current >= max) {
            String level = getMembershipLevel(user);
            return CheckResult.error(
                String.format("您当前%s会员最多可创建%d个家谱，已使用%d，请升级会员创建更多家谱",
                        MembershipLevel.valueOf(level).label, max, current),
                false
            );
        }
        return CheckResult.ok();
    }

    /**
     * 检查新增人物是否超出额度
     */
    public CheckResult canAddPerson(Long userId, Long genealogyId, int currentCount) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return CheckResult.error("用户不存在", false);
        }
        User user = userOpt.get();
        int max = getUserMaxPersons(user);

        if (currentCount >= max) {
            String level = getMembershipLevel(user);
            return CheckResult.error(
                String.format("您当前%s会员单家谱最多可容纳%d个人物，当前已有%d，请升级会员增加人数限制",
                        MembershipLevel.valueOf(level).label, max, currentCount),
                false
            );
        }
        return CheckResult.ok();
    }

    /**
     * 检查添加协作者是否超出额度
     */
    public CheckResult canAddCollaborator(Long userId, int currentCount) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return CheckResult.error("用户不存在", false);
        }
        User user = userOpt.get();
        // 这里简化：每个家谱的协作者额度由用户会员等级决定
        int max = getUserMaxCollaborators(user);

        if (currentCount >= max) {
            String level = getMembershipLevel(user);
            return CheckResult.error(
                String.format("您当前%s会员最多可添加%d个协作者，当前已有%d，请升级会员增加协作者数量",
                        MembershipLevel.valueOf(level).label, max, currentCount),
                false
            );
        }
        return CheckResult.ok();
    }

    /**
     * 获取用户对应当前等级的最大家谱数量
     */
    private int getUserMaxGenealogies(User user) {
        if (user.getMaxGenealogies() != null) {
            return user.getMaxGenealogies();
        }
        return getDefaultConfig(user).maxGenealogies;
    }

    /**
     * 获取用户对应当前等级的单家谱最大人数
     */
    private int getUserMaxPersons(User user) {
        if (user.getMaxPersonsPerGenealogy() != null) {
            return user.getMaxPersonsPerGenealogy();
        }
        return getDefaultConfig(user).maxPersonsPerGenealogy;
    }

    /**
     * 获取用户对应当前等级的最大协作者
     */
    private int getUserMaxCollaborators(User user) {
        if (user.getMaxCollaborators() != null) {
            return user.getMaxCollaborators();
        }
        return getDefaultConfig(user).maxCollaborators;
    }

    /**
     * 获取默认配置
     */
    private MembershipLevel getDefaultConfig(User user) {
        String level = getMembershipLevel(user);
        try {
            return MembershipLevel.valueOf(level);
        } catch (IllegalArgumentException e) {
            return MembershipLevel.FREE;
        }
    }

    /**
     * 获取会员等级
     */
    private String getMembershipLevel(User user) {
        String level = user.getMembershipLevel();
        return level == null ? "FREE" : level;
    }

    /**
     * 检查会员是否过期
     */
    public boolean isMembershipValid(User user) {
        if (user.getMembershipExpireAt() == null) {
            return true; // 永久有效
        }
        return LocalDateTime.now().isBefore(user.getMembershipExpireAt());
    }

    /**
     * 升级会员
     */
    public void upgradeMembership(Long userId, String newLevel, LocalDateTime expireAt) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return;
        }
        User user = userOpt.get();
        MembershipLevel config = MembershipLevel.valueOf(newLevel);

        user.setMembershipLevel(newLevel);
        user.setMembershipExpireAt(expireAt);
        user.setMaxGenealogies(config.maxGenealogies);
        user.setMaxPersonsPerGenealogy(config.maxPersonsPerGenealogy);
        user.setMaxCollaborators(config.maxCollaborators);

        userRepository.save(user);
        log.info("用户{}升级会员到{}，过期时间{}", userId, newLevel, expireAt);
    }

    /**
     * 检查结果
     */
    @Data
    @AllArgsConstructor
    public static class CheckResult {
        private boolean allowed;
        private String message;

        public static CheckResult ok() {
            return new CheckResult(true, "");
        }

        public static CheckResult error(String message, boolean allowed) {
            return new CheckResult(allowed, message);
        }
    }
}
