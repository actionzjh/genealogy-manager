package com.genealogy.service;

import com.genealogy.entity.Person;
import com.genealogy.repository.PersonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

@Service
public class PersonService {

    @Autowired
    private PersonRepository personRepository;

    /**
     * 保存或更新人物
     */
    public Person save(Person person) {
        return personRepository.save(person);
    }

    /**
     * 根据ID查找
     */
    public Optional<Person> findById(Long id) {
        return personRepository.findById(id);
    }

    /**
     * 查询所有
     */
    public List<Person> findAll() {
        return personRepository.findAll();
    }

    /**
     * 分页查询
     */
    public Page<Person> findAll(int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        return personRepository.findAll(pageable);
    }

    /**
     * 删除
     */
    public void deleteById(Long id) {
        personRepository.deleteById(id);
    }

    /**
     * 搜索
     */
    public List<Person> search(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return findAll();
        }
        return personRepository.findByNameContainingIgnoreCase(keyword);
    }

    /**
     * 根据支系查找
     */
    public List<Person> findByBranch(String branch) {
        return personRepository.findByBranchContainingIgnoreCase(branch);
    }

    /**
     * 根据父亲找子女
     */
    public List<Person> findChildrenByFatherId(Long fatherId) {
        return personRepository.findByFatherId(fatherId);
    }

    /**
     * 统计总数
     */
    public long count() {
        return personRepository.count();
    }

    /**
     * 获取最大世代数
     */
    public Integer getMaxGeneration(Long genealogyId) {
        return personRepository.findMaxGeneration(genealogyId);
    }

    /**
     * 按性别统计
     */
    public long countByGender(String gender) {
        return personRepository.countByGender(gender);
    }

    /**
     * 根据家谱ID和用户ID查找
     */
    public List<Person> findByGenealogyIdAndUserId(Long genealogyId, Long userId) {
        return personRepository.findByGenealogyIdAndUserId(genealogyId, userId);
    }

    /**
     * 统计家谱人数
     */
    public long countByGenealogyId(Long genealogyId) {
        return personRepository.countByGenealogyId(genealogyId);
    }
}
