package com.genealogy.service;

import com.genealogy.entity.FamilyCelebrity;
import com.genealogy.repository.FamilyCelebrityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 家族名人服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FamilyCelebrityService {

    private final FamilyCelebrityRepository celebrityRepository;

    /**
     * 添加名人
     */
    @Transactional
    public Result create(FamilyCelebrity celebrity, Long userId) {
        if (celebrity.getGenealogyId() == null) {
            return Result.error("家谱ID不能为空");
        }
        if (celebrity.getName() == null || celebrity.getName().trim().isEmpty()) {
            return Result.error("名人姓名不能为空");
        }
        celebrity.setUserId(userId);
        FamilyCelebrity saved = celebrityRepository.save(celebrity);
        log.info("添加家族名人成功: id={}, genealogyId={}, name={}", saved.getId(), saved.getGenealogyId(), saved.getName());
        return Result.success(saved);
    }

    /**
     * 更新名人
     */
    @Transactional
    public Result update(FamilyCelebrity celebrity, Long userId) {
        return celebrityRepository.findById(celebrity.getId()).map(existing -> {
            if (!existing.getUserId().equals(userId)) {
                return Result.error("无权修改此名人信息");
            }
            existing.setName(celebrity.getName());
            existing.setStyleName(celebrity.getStyleName());
            existing.setHao(celebrity.getHao());
            existing.setBirthYear(celebrity.getBirthYear());
            existing.setDeathYear(celebrity.getDeathYear());
            existing.setBirthPlace(celebrity.getBirthPlace());
            existing.setTitle(celebrity.getTitle());
            existing.setBiography(celebrity.getBiography());
            existing.setPhotoUrl(celebrity.getPhotoUrl());
            existing.setGeneration(celebrity.getGeneration());
            existing.setIsPublic(celebrity.getIsPublic());
            existing.setSortOrder(celebrity.getSortOrder());
            celebrityRepository.save(existing);
            return Result.success(existing);
        }).orElse(Result.error("名人不存在"));
    }

    /**
     * 删除名人
     */
    @Transactional
    public Result delete(Long id, Long userId) {
        return celebrityRepository.findById(id).map(existing -> {
            if (!existing.getUserId().equals(userId)) {
                return Result.error("无权删除此名人");
            }
            celebrityRepository.deleteById(id);
            return Result.success(null);
        }).orElse(Result.error("名人不存在"));
    }

    /**
     * 获取家谱公开名人列表
     */
    public List<FamilyCelebrity> listPublic(Long genealogyId) {
        return celebrityRepository.findByGenealogyIdAndIsPublicTrueOrderBySortOrderDescGenerationAsc(genealogyId);
    }

    /**
     * 管理端列表
     */
    public Page<FamilyCelebrity> listAll(Long genealogyId, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        // 简化: 这里如果需要动态查询用JpaSpecificationExecutor
        return celebrityRepository.findAll(pageable);
    }

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
}
