package com.genealogy.repository;

import com.genealogy.entity.GenealogyCollaborator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * 家谱协作者Repository
 */
public interface GenealogyCollaboratorRepository extends JpaRepository<GenealogyCollaborator, Long> {

    /**
     * 查询某个家谱的所有协作者
     */
    List<GenealogyCollaborator> findByGenealogyId(Long genealogyId);

    /**
     * 查询用户在某个家谱的协作信息
     */
    Optional<GenealogyCollaborator> findByGenealogyIdAndUserId(Long genealogyId, Long userId);

    /**
     * 查询用户参与的所有家谱ID列表
     */
    @Query("SELECT gc.genealogyId FROM GenealogyCollaborator gc WHERE gc.userId = :userId")
    List<Long> findGenealogyIdsByUserId(@Param("userId") Long userId);

    /**
     * 删除某个协作者
     */
    void deleteByGenealogyIdAndUserId(Long genealogyId, Long userId);

    /**
     * 删除某个家谱的所有协作者
     */
    void deleteByGenealogyId(Long genealogyId);

    /**
     * 检查用户是否对某个家谱有指定权限
     * 权限判断：OWNER > EDITOR > VIEWER
     */
    default boolean hasPermission(List<String> allowedRoles, Long genealogyId, Long userId, Long ownerId) {
        // 所有者本身就有全部权限
        if (ownerId != null && ownerId.equals(userId)) {
            return true;
        }
        Optional<GenealogyCollaborator> collaborator = findByGenealogyIdAndUserId(genealogyId, userId);
        if (collaborator.isEmpty()) {
            return false;
        }
        String role = collaborator.get().getRole();
        return allowedRoles.contains(role);
    }

    /**
     * 检查是否可以编辑（OWNER或EDITOR都可以）
     */
    default boolean canEdit(Long genealogyId, Long userId, Long ownerId) {
        return hasPermission(List.of("OWNER", "EDITOR"), genealogyId, userId, ownerId);
    }

    /**
     * 检查是否可以查看（OWNER、EDITOR、VIEWER都可以）
     */
    default boolean canView(Long genealogyId, Long userId, Long ownerId) {
        return hasPermission(List.of("OWNER", "EDITOR", "VIEWER"), genealogyId, userId, ownerId);
    }
}
