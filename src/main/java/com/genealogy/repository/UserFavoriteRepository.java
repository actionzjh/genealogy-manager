package com.genealogy.repository;

import com.genealogy.entity.UserFavorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

/**
 * 用户收藏 Repository
 */
public interface UserFavoriteRepository extends JpaRepository<UserFavorite, Long>,
        JpaSpecificationExecutor<UserFavorite> {

    /**
     * 查询用户收藏列表
     */
    List<UserFavorite> findByUserIdAndTypeOrderByCreatedAtDesc(Long userId, String type);

    /**
     * 查询用户是否已收藏
     */
    Optional<UserFavorite> findByUserIdAndTargetId(Long userId, Long targetId);

    /**
     * 删除收藏
     */
    void deleteByUserIdAndTargetId(Long userId, Long targetId);
}
