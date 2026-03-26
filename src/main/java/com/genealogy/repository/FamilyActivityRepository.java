package com.genealogy.repository;

import com.genealogy.entity.FamilyActivity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

/**
 * 家族活动Repository
 */
public interface FamilyActivityRepository extends JpaRepository<FamilyActivity, Long>,
        JpaSpecificationExecutor<FamilyActivity> {

    /**
     * 查询某家谱公开活动
     */
    Page<FamilyActivity> findByGenealogyIdAndIsPublicTrueOrderByCreatedAtDesc(Long genealogyId, Pageable pageable);

    /**
     * 查询某家谱所有活动
     */
    List<FamilyActivity> findByGenealogyIdOrderByCreatedAtDesc(Long genealogyId);
}
