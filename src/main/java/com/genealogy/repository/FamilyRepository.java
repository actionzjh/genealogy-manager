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
     * 根据父亲ID查找
     */
    List<Family> findByFatherId(Long fatherId);
    
    /**
     * 根据母亲ID查找
     */
    List<Family> findByMotherId(Long motherId);
    
    /**
     * 根据用户ID查找
     */
    List<Family> findByUserId(Long userId);
    
    /**
     * 根据家谱ID和用户ID查找
     */
    List<Family> findByGenealogyIdAndUserId(Long genealogyId, Long userId);
    
    /**
     * 根据父亲ID和家谱查找
     */
    List<Family> findByFatherIdAndGenealogyId(Long fatherId, Long genealogyId);
    
    /**
     * 根据母亲ID和家谱查找
     */
    List<Family> findByMotherIdAndGenealogyId(Long motherId, Long genealogyId);
}
