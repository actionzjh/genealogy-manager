package com.genealogy.service;

import com.genealogy.entity.Genealogy;
import com.genealogy.entity.GenealogyCollaborator;
import com.genealogy.entity.User;
import com.genealogy.repository.GenealogyCollaboratorRepository;
import com.genealogy.repository.GenealogyRepository;
import com.genealogy.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 家谱协作者权限服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GenealogyCollaboratorService {

    private final GenealogyCollaboratorRepository collaboratorRepository;
    private final GenealogyRepository genealogyRepository;
    private final UserRepository userRepository;
    private final MembershipService membershipService;

    /**
     * 邀请协作者
     */
    @Transactional
    public Result inviteCollaborator(Long genealogyId, Long inviterUserId, String inviteeUsername, String role) {
        // 检查家谱是否存在，邀请人是否是所有者或有权限
        Optional<Genealogy> genealogyOpt = genealogyRepository.findById(genealogyId);
        if (genealogyOpt.isEmpty()) {
            return Result.error("家谱不存在");
        }
        Genealogy genealogy = genealogyOpt.get();

        // 检查邀请人权限：只有所有者能邀请协作者
        if (!genealogy.getUserId().equals(inviterUserId)) {
            // 检查是否是EDITOR，EDITOR不能邀请
            Optional<GenealogyCollaborator> inviterCollab = collaboratorRepository
                    .findByGenealogyIdAndUserId(genealogyId, inviterUserId);
            if (inviterCollab.isEmpty() || !"OWNER".equals(inviterCollab.get().getRole())) {
                return Result.error("只有所有者才能邀请协作者");
            }
        }

        // 会员额度检查：检查是否超出最大协作者数量限制
        List<GenealogyCollaborator> existing = collaboratorRepository.findByGenealogyId(genealogyId);
        MembershipService.CheckResult check = membershipService.canAddCollaborator(inviterUserId, existing.size());
        if (!check.isAllowed()) {
            return Result.error(check.getMessage());
        }

        // 查找被邀请用户
        Optional<User> inviteeOpt = userRepository.findByUsername(inviteeUsername);
        if (inviteeOpt.isEmpty()) {
            return Result.error("用户不存在: " + inviteeUsername);
        }
        User invitee = inviteeOpt.get();

        // 检查是否已经是协作者
        Optional<GenealogyCollaborator> existing = collaboratorRepository
                .findByGenealogyIdAndUserId(genealogyId, invitee.getId());
        if (existing.isPresent()) {
            return Result.error("该用户已经是协作者，请修改权限直接更新即可");
        }

        // 创建协作者记录
        GenealogyCollaborator collaborator = new GenealogyCollaborator();
        collaborator.setGenealogyId(genealogyId);
        collaborator.setUserId(invitee.getId());
        collaborator.setRole(role);
        collaborator.setInviterId(inviterUserId);
        collaboratorRepository.save(collaborator);

        log.info("邀请协作者成功: 家谱={}, 邀请人={}, 被邀请人={}, 角色={}",
                genealogyId, inviterUserId, invitee.getId(), role);
        return Result.success(collaborator);
    }

    /**
     * 更新协作者权限
     */
    @Transactional
    public Result updateRole(Long genealogyId, Long operatorUserId, Long targetUserId, String newRole) {
        // 检查操作者权限（只有所有者能修改权限）
        Optional<Genealogy> genealogyOpt = genealogyRepository.findById(genealogyId);
        if (genealogyOpt.isEmpty()) {
            return Result.error("家谱不存在");
        }
        Genealogy genealogy = genealogyOpt.get();

        if (!genealogy.getUserId().equals(operatorUserId)) {
            return Result.error("只有所有者才能修改权限");
        }

        Optional<GenealogyCollaborator> collaboratorOpt = collaboratorRepository
                .findByGenealogyIdAndUserId(genealogyId, targetUserId);
        if (collaboratorOpt.isEmpty()) {
            return Result.error("该用户不是协作者");
        }

        GenealogyCollaborator collaborator = collaboratorOpt.get();
        collaborator.setRole(newRole);
        collaboratorRepository.save(collaborator);

        return Result.success(collaborator);
    }

    /**
     * 移除协作者
     */
    @Transactional
    public Result removeCollaborator(Long genealogyId, Long operatorUserId, Long targetUserId) {
        Optional<Genealogy> genealogyOpt = genealogyRepository.findById(genealogyId);
        if (genealogyOpt.isEmpty()) {
            return Result.error("家谱不存在");
        }
        Genealogy genealogy = genealogyOpt.get();

        // 只有所有者能移除，或者自己可以退出
        if (!genealogy.getUserId().equals(operatorUserId) && !operatorUserId.equals(targetUserId)) {
            Optional<GenealogyCollaborator> operatorCollab = collaboratorRepository
                    .findByGenealogyIdAndUserId(genealogyId, operatorUserId);
            if (operatorCollab.isEmpty() || !"OWNER".equals(operatorCollab.get().getRole())) {
                return Result.error("只有所有者才能移除协作者，或者自己退出");
            }
        }

        // 不能移除所有者
        if (genealogy.getUserId().equals(targetUserId)) {
            return Result.error("不能移除家谱所有者");
        }

        collaboratorRepository.deleteByGenealogyIdAndUserId(genealogyId, targetUserId);
        log.info("移除协作者: 家谱={}, 操作者={}, 目标={}", genealogyId, operatorUserId, targetUserId);

        return Result.success(null);
    }

    /**
     * 获取家谱所有协作者列表（带用户信息）
     */
    public List<CollaboratorInfo> getCollaborators(Long genealogyId) {
        List<GenealogyCollaborator> collaborators = collaboratorRepository.findByGenealogyId(genealogyId);
        List<CollaboratorInfo> result = new ArrayList<>();
        for (GenealogyCollaborator c : collaborators) {
            Optional<User> userOpt = userRepository.findById(c.getUserId());
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                CollaboratorInfo info = new CollaboratorInfo();
                info.setId(c.getId());
                info.setUserId(user.getId());
                info.setUsername(user.getUsername());
                info.setNickname(user.getNickname());
                info.setRole(c.getRole());
                info.setInviterId(c.getInviterId());
                info.setCreatedAt(c.getCreatedAt());
                result.add(info);
            }
        }
        return result;
    }

    /**
     * 检查当前用户是否有权限编辑家谱
     */
    public boolean canEdit(Long genealogyId, Long userId) {
        Optional<Genealogy> genealogyOpt = genealogyRepository.findById(genealogyId);
        if (genealogyOpt.isEmpty()) {
            return false;
        }
        Genealogy genealogy = genealogyOpt.get();
        return collaboratorRepository.canEdit(genealogyId, userId, genealogy.getUserId());
    }

    /**
     * 检查当前用户是否有权限查看家谱
     */
    public boolean canView(Long genealogyId, Long userId) {
        Optional<Genealogy> genealogyOpt = genealogyRepository.findById(genealogyId);
        if (genealogyOpt.isEmpty()) {
            return false;
        }
        Genealogy genealogy = genealogyOpt.get();
        // 所有者公开？先简化：只有所有者和协作者能看
        return collaboratorRepository.canView(genealogyId, userId, genealogy.getUserId());
    }

    /**
     * 获取用户参与的所有家谱（自己创建的 + 被邀请的）
     */
    public List<Genealogy> getUserAccessibleGenealogies(Long userId) {
        // 先找自己创建的
        List<Genealogy> owned = genealogyRepository.findByUserId(userId);
        // 再找被邀请的
        List<Long> invitedIds = collaboratorRepository.findGenealogyIdsByUserId(userId);
        List<Genealogy> invited = genealogyRepository.findAllById(invitedIds);
        // 合并去重
        List<Genealogy> result = new ArrayList<>(owned);
        for (Genealogy g : invited) {
            if (!g.getUserId().equals(userId)) {
                result.add(g);
            }
        }
        return result;
    }

    // 返回结果封装
    @lombok.Data
    public static class Result {
        private boolean success;
        private String message;
        private Object data;

        public static Result success(Object data) {
            Result r = new Result();
            r.setSuccess(true);
            r.setMessage("成功");
            r.setData(data);
            return r;
        }

        public static Result error(String message) {
            Result r = new Result();
            r.setSuccess(false);
            r.setMessage(message);
            return r;
        }
    }

    // 协作者信息（带用户名）
    @lombok.Data
    public static class CollaboratorInfo {
        private Long id;
        private Long userId;
        private String username;
        private String nickname;
        private String role;
        private Long inviterId;
        private java.time.LocalDateTime createdAt;
    }
}
