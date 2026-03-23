package com.genealogy.repository;

import com.genealogy.entity.Family;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FamilyRepository extends JpaRepository<Family, Long> {
    
    /**
     * 根据家谱ID查找所有家庭
     */
    List<Family> findByGenealogyId(Long genealogyId);
    
    /**
     * 根据丈夫ID查找
     */
    List<Family> findByHusbandId(Long husbandId);
    
    /**
     * 根据妻子ID查找
     */
    List<Family> findByWifeId(Long wifeId);
}
