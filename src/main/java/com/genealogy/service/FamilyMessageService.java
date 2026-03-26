package com.genealogy.service;

import com.genealogy.entity.FamilyMessage;
import com.genealogy.repository.FamilyMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 宗亲留言服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FamilyMessageService {

    private final FamilyMessageRepository messageRepository;

    /**
     * 发布留言
     */
    @Transactional
    public Result create(FamilyMessage message, String clientIp) {
        // 基础校验
        if (message.getGenealogyId() == null) {
            return Result.error("家谱ID不能为空");
        }
        if (message.getAuthorName() == null || message.getAuthorName().trim().isEmpty()) {
            return Result.error("请填写姓名");
        }
        if (message.getContent() == null || message.getContent().trim().isEmpty()) {
            return Result.error("请填写留言内容");
        }
        if (message.getContent().length() > 2000) {
            return Result.error("留言内容不能超过2000字");
        }

        // 如果是一级留言不需要审核权限？让家谱管理员审核，所以默认false
        message.setClientIp(clientIp);
        messageRepository.save(message);
        log.info("发布宗亲留言成功: genealogyId={}, author={}", message.getGenealogyId(), message.getAuthorName());
        return Result.success(message);
    }

    /**
     * 获取家谱一级留言分页
     */
    public Page<FamilyMessage> listApproved(Long genealogyId, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return messageRepository.findByGenealogyIdAndParentIdAndApprovedTrue(genealogyId, 0L, pageable);
    }

    /**
     * 获取某个留言下的所有回复
     */
    public List<FamilyMessage> listReplies(Long parentId) {
        return messageRepository.findByParentIdAndApprovedTrue(parentId);
    }

    /**
     * 统计留言数
     */
    public long countApproved(Long genealogyId) {
        return messageRepository.countByGenealogyIdAndApprovedTrue(genealogyId);
    }

    /**
     * 审核通过留言
     */
    @Transactional
    public Result approve(Long id, boolean approved) {
        return messageRepository.findById(id).map(message -> {
            message.setApproved(approved);
            messageRepository.save(message);
            log.info("审核留言: id={}, approved={}", id, approved);
            return Result.success(message);
        }).orElse(Result.error("留言不存在"));
    }

    /**
     * 删除留言 (家谱管理员权限)
     */
    @Transactional
    public Result delete(Long id) {
        if (!messageRepository.existsById(id)) {
            return Result.error("留言不存在");
        }
        messageRepository.deleteById(id);
        return Result.success(null);
    }

    @lombok.Data
    public static class Result {
        private boolean success;
        private String message;
        private Object data;

        public static Result success(Object data) {
            Result r = new Result();
            r.setSuccess(true);
            r.setMessage("成功");
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
