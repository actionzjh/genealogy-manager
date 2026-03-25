package com.genealogy.repository;

import com.genealogy.entity.Genealogy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface GenealogyRepository extends JpaRepository<Genealogy, Long> {
    
    /**
     * 根据姓氏查找
     */
    List<Genealogy> findBySurnameContainingIgnoreCase(String surname);
    
    /**
     * 根据名称模糊搜索
     */
    List<Genealogy> findByNameContainingIgnoreCase(String name);
    
    /**
     * 根据用户ID查找所有家谱
     */
    List<Genealogy> findByUserId(Long userId);
    
    /**
     * 根据用户ID分页查找
     */
    Page<Genealogy> findByUserId(Long userId, Pageable pageable);
    
    /**
     * 用户范围内搜索
     */
    List<Genealogy> findByUserIdAndNameContainingIgnoreCase(Long userId, String name);
    
    /**
     * 统计用户家谱数量
     */
    long countByUserId(Long userId);
}
