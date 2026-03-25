package com.genealogy.repository;

import com.genealogy.entity.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PersonRepository extends JpaRepository<Person, Long>, org.springframework.data.jpa.repository.JpaSpecificationExecutor<Person> {
    
    /**
     * 根据姓名模糊搜索
     */
    List<Person> findByNameContainingIgnoreCase(String name);
    
    /**
     * 根据支系查找
     */
    List<Person> findByBranchContainingIgnoreCase(String branch);
    
    /**
     * 根据父亲ID查找子女
     */
    List<Person> findByFatherId(Long fatherId);
    
    /**
     * 根据母亲ID查找子女
     */
    List<Person> findByMotherId(Long motherId);
    
    /**
     * 统计某世代人数
     */
    long countByGeneration(Integer generation);
    
    /**
     * 获取最大世代数（某个家谱）
     */
    @Query("SELECT MAX(p.generation) FROM Person p WHERE :genealogyId IS NULL OR p.genealogyId = :genealogyId")
    Integer findMaxGeneration(@Param("genealogyId") Long genealogyId);
    
    /**
     * 根据性别统计
     */
    long countByGender(String gender);
    
    /**
     * 根据家谱ID查找所有人
     */
    List<Person> findByGenealogyId(Long genealogyId);
    
    /**
     * 根据用户ID查找
     */
    List<Person> findByUserId(Long userId);
    
    /**
     * 根据家谱ID和用户ID查找
     */
    List<Person> findByGenealogyIdAndUserId(Long genealogyId, Long userId);
    
    /**
     * 统计家谱人数
     */
    long countByGenealogyId(Long genealogyId);
    
    /**
     * 用户范围内姓名搜索
     */
    List<Person> findByUserIdAndNameContainingIgnoreCase(Long userId, String name);
}
