package com.genealogy.repository;

import com.genealogy.entity.FamilyPhoto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

/**
 * 家族相册 Repository
 */
public interface FamilyPhotoRepository extends JpaRepository<FamilyPhoto, Long>,
        JpaSpecificationExecutor<FamilyPhoto> {

    /**
     * 查询某家谱公开照片，按排序降序
     */
    List<FamilyPhoto> findByGenealogyIdAndIsPublicTrueOrderBySortOrderDescCreatedAtDesc(Long genealogyId);

    /**
     * 分页查询
     */
    Page<FamilyPhoto> findByGenealogyId(Long genealogyId, Pageable pageable);

    /**
     * 统计照片数
     */
    long countByGenealogyId(Long genealogyId);
}
