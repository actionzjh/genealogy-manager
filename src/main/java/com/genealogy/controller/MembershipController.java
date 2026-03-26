package com.genealogy.controller;

import com.genealogy.entity.User;
import com.genealogy.repository.GenealogyRepository;
import com.genealogy.repository.GenealogyCollaboratorRepository;
import com.genealogy.repository.UserRepository;
import com.genealogy.service.MembershipService;
import com.genealogy.service.MembershipService.MembershipLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 会员管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/membership")
@RequiredArgsConstructor
public class MembershipController {

    private final MembershipService membershipService;
    private final GenealogyRepository genealogyRepository;
    private final GenealogyCollaboratorRepository collaboratorRepository;
    private final UserRepository userRepository;

    /**
     * 获取当前用户会员信息和额度
     */
    @GetMapping("/my")
    public ResponseEntity<Map<String, Object>> getMyMembership(Authentication authentication) {
        Map<String, Object> result = new HashMap<>();
        if (authentication == null || authentication.getPrincipal() == null) {
            result.put("success", false);
            result.put("message", "请先登录");
            return ResponseEntity.status(401).body(result);
        }
        Long userId = (Long) authentication.getPrincipal();

        // 查询用户信息
        java.util.Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            result.put("success", false);
            result.put("message", "用户不存在");
            return ResponseEntity.ok(result);
        }
        User user = userOpt.get();

        // 统计已用：用户自己创建的家谱数量
        long usedGenealogies = genealogyRepository.countByUserId(userId);

        // 统计协作者数量：简化：用户作为所有者的所有家谱协作者总和
        // 这里简化，只统计协作者总数，实际可以按家谱算
        long usedCollaborators = 0;
        List<com.genealogy.entity.Genealogy> myGenealogies = genealogyRepository.findByUserId(userId);
        for (com.genealogy.entity.Genealogy g : myGenealogies) {
            usedCollaborators += collaboratorRepository.findByGenealogyId(g.getId()).size();
        }

        // 获取配置额度
        MembershipLevel config = MembershipLevel.valueOf(user.getMembershipLevel() == null ? "FREE" : user.getMembershipLevel());

        result.put("success", true);
        result.put("membershipLevel", user.getMembershipLevel());
        result.put("membershipExpireAt", user.getMembershipExpireAt());
        result.put("usedGenealogies", usedGenealogies);
        result.put("maxGenealogies", user.getMaxGenealogies() != null ? user.getMaxGenealogies() : config.maxGenealogies);
        result.put("usedCollaborators", usedCollaborators);
        result.put("maxCollaborators", user.getMaxCollaborators() != null ? user.getMaxCollaborators() : config.maxCollaborators);

        return ResponseEntity.ok(result);
    }
}
