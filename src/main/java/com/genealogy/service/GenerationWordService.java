package com.genealogy.service;

import com.genealogy.entity.GenerationWord;
import com.genealogy.repository.GenerationWordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 字辈查询服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GenerationWordService {

    private final GenerationWordRepository generationWordRepository;

    /**
     * 搜索字辈
     */
    public Page<GenerationWord> search(String surname, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return generationWordRepository.findBySurnameContainingIgnoreCaseAndIsPublicTrue(surname, pageable);
    }

    /**
     * 查询某姓氏所有字辈
     */
    public List<GenerationWord> listBySurname(String surname) {
        return generationWordRepository.findBySurnameIgnoreCaseAndIsPublicTrue(surname);
    }

    /**
     * 根据排行查询对应字辈
     * 输入: 姓氏 + 排行(第几个字) → 返回对应的字辈
     */
    public String getWordAtPosition(String surname, int position) {
        List<GenerationWord> list = generationWordRepository.findBySurnameIgnoreCaseAndIsPublicTrue(surname);
        if (list.isEmpty()) {
            return null;
        }
        // 简单返回第一个匹配的对应位置字辈
        GenerationWord gw = list.get(0);
        String[] words = gw.getWords().split("\\s+");
        if (position >= 1 && position <= words.length) {
            return words[position - 1];
        }
        return null;
    }
}
