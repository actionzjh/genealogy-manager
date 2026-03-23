package com.genealogy.service;

import com.genealogy.entity.Genealogy;
import com.genealogy.repository.GenealogyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

@Service
public class GenealogyService {

    @Autowired
    private GenealogyRepository genealogyRepository;

    /**
     * 保存或更新家谱
     */
    public Genealogy save(Genealogy genealogy) {
        return genealogyRepository.save(genealogy);
    }

    /**
     * 根据ID查找
     */
    public Optional<Genealogy> findById(Long id) {
        return genealogyRepository.findById(id);
    }

    /**
     * 查询所有
     */
    public List<Genealogy> findAll() {
        return genealogyRepository.findAll();
    }

    /**
     * 分页查询
     */
    public Page<Genealogy> findAll(int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        return genealogyRepository.findAll(pageable);
    }

    /**
     * 删除
     */
    public void deleteById(Long id) {
        genealogyRepository.deleteById(id);
    }

    /**
     * 搜索
     */
    public List<Genealogy> search(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return findAll();
        }
        return genealogyRepository.findByNameContainingIgnoreCase(keyword);
    }

    /**
     * 根据姓氏查找
     */
    public List<Genealogy> findBySurname(String surname) {
        return genealogyRepository.findBySurnameContainingIgnoreCase(surname);
    }

    /**
     * 统计总数
     */
    public long count() {
        return genealogyRepository.count();
    }
}
