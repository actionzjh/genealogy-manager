package com.genealogy.service;

import com.genealogy.entity.FamilyActivity;
import com.genealogy.entity.Genealogy;
import com.genealogy.repository.FamilyActivityRepository;
import com.genealogy.repository.GenealogyRepository;
import lombok.RequiredArgsConstructor;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 家族活动服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FamilyActivityService {

    private final FamilyActivityRepository activityRepository;
    private final GenealogyRepository genealogyRepository;
    private final GenealogyService genealogyService;

    /**
     * 创建活动
     */
    @Transactional
    public Result create(FamilyActivity activity, Long userId) {
        // 检查家谱是否存在，用户是否有权限
        Optional<Genealogy> genealogyOpt = genealogyRepository.findById(activity.getGenealogyId());
        if (genealogyOpt.isEmpty()) {
            return Result.error("家谱不存在");
        }
        Genealogy genealogy = genealogyOpt.get();
        if (!genealogy.getUserId().equals(userId)) {
            // 检查协作者编辑权限
            if (!genealogyService.canEdit(activity.getGenealogyId(), userId)) {
                return Result.error("无权在此家谱创建活动");
            }
        }

        FamilyActivity saved = activityRepository.save(activity);
        log.info("创建家族活动成功: id={}, 家谱={}", saved.getId(), activity.getGenealogyId());
        return Result.success(saved);
    }

    /**
     * 更新活动
     */
    @Transactional
    public Result update(Long id, FamilyActivity activity, Long userId) {
        Optional<FamilyActivity> existingOpt = activityRepository.findById(id);
        if (existingOpt.isEmpty()) {
            return Result.error("活动不存在");
        }
        FamilyActivity existing = existingOpt.get();
        // 检查权限：家谱所有者/编辑才能修改
        Optional<Genealogy> genealogyOpt = genealogyRepository.findById(existing.getGenealogyId());
        if (genealogyOpt.isEmpty()) {
            return Result.error("家谱不存在");
        }
        Genealogy genealogy = genealogyOpt.get();
        if (!genealogy.getUserId().equals(userId)) {
            if (!genealogyService.canEdit(existing.getGenealogyId(), userId)) {
                return Result.error("无权修改此活动");
            }
        }

        activity.setId(id);
        activity.setGenealogyId(existing.getGenealogyId());
        FamilyActivity saved = activityRepository.save(activity);
        return Result.success(saved);
    }

    /**
     * 删除活动
     */
    @Transactional
    public Result delete(Long id, Long userId) {
        Optional<FamilyActivity> existingOpt = activityRepository.findById(id);
        if (existingOpt.isEmpty()) {
            return Result.error("活动不存在");
        }
        FamilyActivity existing = existingOpt.get();
        Optional<Genealogy> genealogyOpt = genealogyRepository.findById(existing.getGenealogyId());
        if (genealogyOpt.isEmpty()) {
            return Result.error("家谱不存在");
        }
        Genealogy genealogy = genealogyOpt.get();
        if (!genealogy.getUserId().equals(userId)) {
            if (!genealogyService.canEdit(existing.getGenealogyId(), userId)) {
                return Result.error("无权删除此活动");
            }
        }
        activityRepository.deleteById(id);
        return Result.success(null);
    }

    /**
     * 获取家谱公开活动列表
     */
    public Page<FamilyActivity> listPublic(Long genealogyId, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return activityRepository.findByGenealogyIdAndIsPublicTrueOrderByCreatedAtDesc(genealogyId, pageable);
    }

    /**
     * 获取家谱所有活动（管理用）
     */
    public List<FamilyActivity> listAll(Long genealogyId) {
        return activityRepository.findByGenealogyIdOrderByCreatedAtDesc(genealogyId);
    }

    /**
     * 获取活动详情
     */
    public Optional<FamilyActivity> getById(Long id) {
        return activityRepository.findById(id);
    }

    // 返回结果封装
    @Data
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
}
