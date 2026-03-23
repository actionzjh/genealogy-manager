package com.genealogy.service;

import com.genealogy.entity.Family;
import com.genealogy.repository.FamilyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class FamilyService {

    @Autowired
    private FamilyRepository familyRepository;

    /**
     * 保存或更新家庭关系
     */
    public Family save(Family family) {
        return familyRepository.save(family);
    }

    /**
     * 根据ID查找
     */
    public Optional<Family> findById(Long id) {
        return familyRepository.findById(id);
    }

    /**
     * 根据家谱ID查找所有家庭
     */
    public List<Family> findByGenealogyId(Long genealogyId) {
        return familyRepository.findByGenealogyId(genealogyId);
    }

    /**
     * 根据丈夫ID查找
     */
    public List<Family> findByHusbandId(Long husbandId) {
        return familyRepository.findByHusbandId(husbandId);
    }

    /**
     * 根据妻子ID查找
     */
    public List<Family> findByWifeId(Long wifeId) {
        return familyRepository.findByWifeId(wifeId);
    }

    /**
     * 删除
     */
    public void deleteById(Long id) {
        familyRepository.deleteById(id);
    }

    /**
     * 统计总数
     */
    public long count() {
        return familyRepository.count();
    }
}
