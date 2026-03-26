package com.genealogy.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.genealogy.entity.Person;
import com.genealogy.entity.PersonVersion;
import com.genealogy.repository.PersonVersionRepository;
import com.genealogy.repository.PersonRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 人物版本服务 - 版本回滚功能
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PersonVersionService {

    private final PersonVersionRepository versionRepository;
    private final PersonRepository personRepository;
    private final ObjectMapper objectMapper;

    /**
     * 保存版本快照 - 在修改人物前调用
     */
    @Transactional
    public void createSnapshot(Long personId, Long modifierUserId, String changeDescription) {
        Optional<Person> personOpt = personRepository.findById(personId);
        if (personOpt.isEmpty()) {
            return;
        }
        Person person = personOpt.get();

        PersonVersion version = new PersonVersion();
        version.setPersonId(personId);
        version.setGenealogyId(person.getGenealogyId());
        version.setModifierUserId(modifierUserId);

        try {
            String json = objectMapper.writeValueAsString(person);
            version.setBeforeSnapshot(json);
        } catch (JsonProcessingException e) {
            log.error("序列化person快照失败", e);
        }

        version.setChangeDescription(changeDescription);
        versionRepository.save(version);
        log.info("创建人物版本快照: personId={}, modifier={}", personId, modifierUserId);
    }

    /**
     * 获取版本列表
     */
    public Page<PersonVersion> listVersions(Long personId, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        return versionRepository.findByPersonIdOrderByCreatedAtDesc(personId, pageable);
    }

    /**
     * 回滚到指定版本
     */
    @Transactional
    public Result rollback(Long versionId, Long currentUserId) {
        Optional<PersonVersion> versionOpt = versionRepository.findById(versionId);
        if (versionOpt.isEmpty()) {
            return Result.error("版本不存在");
        }
        PersonVersion version = versionOpt.get();

        // 检查人物是否存在
        Optional<Person> personOpt = personRepository.findById(version.getPersonId());
        if (personOpt.isEmpty()) {
            return Result.error("人物不存在");
        }
        Person currentPerson = personOpt.get();

        // 先保存当前版本作为快照
        try {
            PersonVersion currentVersion = new PersonVersion();
            currentVersion.setPersonId(currentPerson.getId());
            currentVersion.setGenealogyId(currentPerson.getGenealogyId());
            currentVersion.setModifierUserId(currentUserId);
            currentVersion.setChangeDescription("回滚到版本 " + versionId);
            String currentJson = objectMapper.writeValueAsString(currentPerson);
            currentVersion.setBeforeSnapshot(currentJson);
            versionRepository.save(currentVersion);
        } catch (JsonProcessingException e) {
            log.error("序列化当前person快照失败", e);
        }

        // 反序列化历史版本并保存
        try {
            Person historicalPerson = objectMapper.readValue(version.getBeforeSnapshot(), Person.class);
            historicalPerson.setId(currentPerson.getId());
            // 保持ID不变，覆盖内容
            personRepository.save(historicalPerson);
            log.info("人物回滚成功: personId={}, versionId={}", currentPerson.getId(), versionId);
            return Result.success(historicalPerson);
        } catch (JsonProcessingException e) {
            log.error("反序列化历史person快照失败", e);
            return Result.error("回滚失败，JSON解析错误");
        }
    }

    // 返回结果
    @lombok.Data
    public static class Result {
        private boolean success;
        private String message;
        private Person data;

        public static Result success(Person data) {
            Result r = new Result();
            r.setSuccess(true);
            r.setMessage("回滚成功");
            r.setData(data);
            return r;
        }

        public static Result error(String message) {
            Result r = new Result();
            r.setSuccess(false);
            r.setMessage(message);
            return r;
        }
    }
}
