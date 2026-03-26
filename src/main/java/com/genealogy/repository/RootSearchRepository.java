package com.genealogy.repository;

import com.genealogy.entity.RootSearch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * 寻根启事Repository
 */
public interface RootSearchRepository extends JpaRepository<RootSearch, Long>,
        org.springframework.data.jpa.repository.JpaSpecificationExecutor<RootSearch> {

    /**
     * 查询公开的寻根启事（分页）
     */
    Page<RootSearch> findByIsPublicTrueAndStatusOrderByCreatedAtDesc(String status, Pageable pageable);

    /**
     * 查询用户发布的所有寻根启事
     */
    List<RootSearch> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * 按姓氏搜索公开寻根
     */
    @Query("SELECT r FROM RootSearch r WHERE r.isPublic = true AND r.status = 'open' AND LOWER(r.surname) LIKE LOWER(CONCAT('%', :surname, '%'))")
    Page<RootSearch> searchBySurname(@Param("surname") String surname, Pageable pageable);
}
