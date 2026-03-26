package com.genealogy.repository;

import com.genealogy.entity.FamilyCelebrity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

/**
 * 家族名人 Repository
 */
public interface FamilyCelebrityRepository extends JpaRepository<FamilyCelebrity, Long>,
        JpaSpecificationExecutor<FamilyCelebrity> {

    /**
     * 查询家谱公开名人，按排序降序
     */
    List<FamilyCelebrity> findByGenealogyIdAndIsPublicTrueOrderBySortOrderDescGenerationAsc(Long genealogyId);
}
