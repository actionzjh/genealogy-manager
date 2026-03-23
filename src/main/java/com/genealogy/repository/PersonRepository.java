package com.genealogy.repository;

import com.genealogy.entity.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PersonRepository extends JpaRepository<Person, Long> {
    
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
     * 获取最大世代数
     */
    @Query("SELECT MAX(p.generation) FROM Person p")
    Integer findMaxGeneration();
    
    /**
     * 根据性别统计
     */
    long countByGender(String gender);
}
