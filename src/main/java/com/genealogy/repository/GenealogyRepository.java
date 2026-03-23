package com.genealogy.repository;

import com.genealogy.entity.Genealogy;
import org.springframework.data.jpa.repository.JpaRepository;
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
}
