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
     * 根据ID查找（需要验证所有权）
     */
    public Optional<Genealogy> findById(Long id) {
        return genealogyRepository.findById(id);
    }

    /**
     * 验证所有权
     */
    public boolean isOwner(Long id, Long userId) {
        Optional<Genealogy> genealogy = findById(id);
        return genealogy.isPresent() && genealogy.get().getUserId().equals(userId);
    }

    /**
     * 查询用户的所有家谱
     */
    public List<Genealogy> findByUserId(Long userId) {
        return genealogyRepository.findByUserId(userId);
    }

    /**
     * 分页查询用户的家谱
     */
    public Page<Genealogy> findByUserId(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        return genealogyRepository.findByUserId(userId, pageable);
    }

    /**
     * 删除（调用前需验证所有权）
     */
    public void deleteById(Long id) {
        genealogyRepository.deleteById(id);
    }

    /**
     * 在用户范围内搜索
     */
    public List<Genealogy> search(Long userId, String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return findByUserId(userId);
        }
        return genealogyRepository.findByUserIdAndNameContainingIgnoreCase(userId, keyword);
    }

    /**
     * 根据姓氏查找（用户范围内）
     */
    public List<Genealogy> findBySurname(Long userId, String surname) {
        return genealogyRepository.findBySurnameContainingIgnoreCase(surname);
    }

    /**
     * 统计用户家谱数量
     */
    public long countByUserId(Long userId) {
        return genealogyRepository.countByUserId(userId);
    }
}
