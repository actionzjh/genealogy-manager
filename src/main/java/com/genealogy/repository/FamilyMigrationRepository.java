package com.genealogy.repository;

import com.genealogy.entity.FamilyMigration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

/**
 * 家族迁徙 Repository
 */
public interface FamilyMigrationRepository extends JpaRepository<FamilyMigration, Long>,
        JpaSpecificationExecutor<FamilyMigration> {

    /**
     * 查询家谱公开迁徙记录，按排序
     */
    List<FamilyMigration> findByGenealogyIdAndIsPublicTrueOrderBySortOrderAsc(Long genealogyId);
}
