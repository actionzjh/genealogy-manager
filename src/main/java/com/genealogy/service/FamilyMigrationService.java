package com.genealogy.service;

import com.genealogy.entity.FamilyMigration;
import com.genealogy.repository.FamilyMigrationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 家族迁徙服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FamilyMigrationService {

    private final FamilyMigrationRepository migrationRepository;

    /**
     * 添加迁徙记录
     */
    @Transactional
    public Result create(FamilyMigration migration, Long userId) {
        if (migration.getGenealogyId() == null) {
            return Result.error("家谱ID不能为空");
        }
        if (migration.getName() == null || migration.getName().isEmpty()) {
            return Result.error("迁徙名称不能为空");
        }
        migration.setUserId(userId);
        FamilyMigration saved = migrationRepository.save(migration);
        log.info("添加家族迁徙成功: genealogyId={}, name={}", migration.getGenealogyId(), migration.getName());
        return Result.success(saved);
    }

    /**
     * 更新迁徙记录
     */
    @Transactional
    public Result update(FamilyMigration migration, Long userId) {
        return migrationRepository.findById(migration.getId()).map(existing -> {
            if (!existing.getUserId().equals(userId)) {
                return Result.error("无权修改此迁徙记录");
            }
            existing.setName(migration.getName());
            existing.setDescription(migration.getDescription());
            existing.setFromLat(migration.getFromLat());
            existing.setFromLng(migration.getFromLng());
            existing.setFromPlace(migration.getFromPlace());
            existing.setToLat(migration.getToLat());
            existing.setToLng(migration.getToLng());
            existing.setToPlace(migration.getToPlace());
            existing.setYear(migration.getYear());
            existing.setSortOrder(migration.getSortOrder());
            existing.setIsPublic(migration.getIsPublic());
            migrationRepository.save(existing);
            return Result.success(existing);
        }).orElse(Result.error("迁徙记录不存在"));
    }

    /**
     * 删除迁徙记录
     */
    @Transactional
    public Result delete(Long id, Long userId) {
        return migrationRepository.findById(id).map(existing -> {
            if (!existing.getUserId().equals(userId)) {
                return Result.error("无权删除此迁徙记录");
            }
            migrationRepository.deleteById(id);
            return Result.success(null);
        }).orElse(Result.error("迁徙记录不存在"));
    }

    /**
     * 获取家谱公开迁徙记录
     */
    public List<FamilyMigration> listPublic(Long genealogyId) {
        return migrationRepository.findByGenealogyIdAndIsPublicTrueOrderBySortOrderAsc(genealogyId);
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
