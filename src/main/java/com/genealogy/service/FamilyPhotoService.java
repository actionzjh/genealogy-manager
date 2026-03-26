package com.genealogy.service;

import com.genealogy.entity.FamilyPhoto;
import com.genealogy.repository.FamilyPhotoRepository;
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

/**
 * 家族相册服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FamilyPhotoService {

    private final FamilyPhotoRepository photoRepository;

    /**
     * 添加照片
     */
    @Transactional
    public Result create(FamilyPhoto photo, Long userId) {
        if (photo.getGenealogyId() == null) {
            return Result.error("家谱ID不能为空");
        }
        photo.setUserId(userId);
        FamilyPhoto saved = photoRepository.save(photo);
        log.info("添加家族照片成功: id={}, genealogyId={}", saved.getId(), photo.getGenealogyId());
        return Result.success(saved);
    }

    /**
     * 更新照片信息
     */
    @Transactional
    public Result update(FamilyPhoto photo, Long userId) {
        return photoRepository.findById(photo.getId()).map(existing -> {
            if (!existing.getUserId().equals(userId)) {
                return Result.error("无权修改此照片");
            }
            existing.setTitle(photo.getTitle());
            existing.setDescription(photo.getDescription());
            existing.setYear(photo.getYear());
            existing.setLocation(photo.getLocation());
            existing.setIsPublic(photo.getIsPublic());
            existing.setSortOrder(photo.getSortOrder());
            photoRepository.save(existing);
            return Result.success(existing);
        }).orElse(Result.error("照片不存在"));
    }

    /**
     * 删除照片
     */
    @Transactional
    public Result delete(Long id, Long userId) {
        return photoRepository.findById(id).map(existing -> {
            if (!existing.getUserId().equals(userId)) {
                return Result.error("无权删除此照片");
            }
            photoRepository.deleteById(id);
            return Result.success(null);
        }).orElse(Result.error("照片不存在"));
    }

    /**
     * 获取公开照片列表
     */
    @org.springframework.cache.annotation.Cacheable(value = "familyPhoto", key = "#genealogyId")
    public List<FamilyPhoto> listPublic(Long genealogyId) {
        return photoRepository.findByGenealogyIdAndIsPublicTrueOrderBySortOrderDescCreatedAtDesc(genealogyId);
    }

    /**
     * 管理后台列表
     */
    public Page<FamilyPhoto> listAll(Long genealogyId, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "sortOrder", "createdAt"));
        return photoRepository.findByGenealogyId(genealogyId, pageable);
    }

    /**
     * 获取单张照片
     */
    public FamilyPhoto getById(Long id) {
        return photoRepository.findById(id).orElse(null);
    }

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
