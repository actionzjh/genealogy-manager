package com.genealogy.repository;

import com.genealogy.entity.FamilyMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

/**
 * 宗亲留言Repository
 */
public interface FamilyMessageRepository extends JpaRepository<FamilyMessage, Long>,
        JpaSpecificationExecutor<FamilyMessage> {

    /**
     * 查询家谱已通过审核的一级留言（分页）
     */
    Page<FamilyMessage> findByGenealogyIdAndParentIdAndApprovedTrue(
            Long genealogyId, Long parentId, Pageable pageable);

    /**
     * 查询回复某留言的所有回复
     */
    List<FamilyMessage> findByParentIdAndApprovedTrue(Long parentId);

    /**
     * 统计留言数
     */
    long countByGenealogyIdAndApprovedTrue(Long genealogyId);
}
