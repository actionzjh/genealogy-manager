package com.genealogy.service;

import com.genealogy.entity.Branch;
import com.genealogy.repository.BranchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

@Service
public class BranchService {

    @Autowired
    private BranchRepository branchRepository;

    public Branch save(Branch branch) {
        return branchRepository.save(branch);
    }

    public Optional<Branch> findById(Long id) {
        return branchRepository.findById(id);
    }

    public List<Branch> findAll() {
        return branchRepository.findAll();
    }

    public List<Branch> findByGenealogyId(Long genealogyId) {
        return branchRepository.findByGenealogyIdOrderBySortOrderAscIdAsc(genealogyId);
    }

    public List<Branch> findChildren(Long parentId) {
        return branchRepository.findByParentIdOrderBySortOrderAscIdAsc(parentId);
    }

    public List<Branch> search(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return findAll();
        }
        return branchRepository.findByNameContainingIgnoreCase(keyword);
    }

    public void deleteById(Long id) {
        branchRepository.deleteById(id);
    }

    public long count() {
        return branchRepository.count();
    }
}
