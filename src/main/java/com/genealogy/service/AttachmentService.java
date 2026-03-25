package com.genealogy.service;

import com.genealogy.entity.Attachment;
import com.genealogy.repository.AttachmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class AttachmentService {

    @Autowired
    private AttachmentRepository attachmentRepository;

    @Value("${app.upload.dir:./uploads}")
    private String uploadDir;

    public Optional<Attachment> findById(Long id) {
        return attachmentRepository.findById(id);
    }

    public List<Attachment> findByPersonId(Long personId) {
        return attachmentRepository.findByPersonIdOrderBySortOrderAscCreatedAtDesc(personId);
    }

    public List<Attachment> findByGenealogyId(Long genealogyId) {
        return attachmentRepository.findByGenealogyIdOrderBySortOrderAscCreatedAtDesc(genealogyId);
    }

    public Attachment store(Long genealogyId,
                            Long personId,
                            String bizType,
                            String description,
                            Integer sortOrder,
                            MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("上传文件不能为空");
        }
        if (genealogyId == null) {
            throw new IllegalArgumentException("genealogyId 不能为空");
        }

        String originalName = StringUtils.cleanPath(
                file.getOriginalFilename() == null ? "attachment" : file.getOriginalFilename()
        );
        String extension = "";
        int dotIndex = originalName.lastIndexOf('.');
        if (dotIndex >= 0) {
            extension = originalName.substring(dotIndex);
        }

        String storedName = UUID.randomUUID().toString().replace("-", "") + extension;
        String monthFolder = DateTimeFormatter.ofPattern("yyyyMM").format(LocalDateTime.now());
        String relativePath = Paths.get(
                "attachments",
                "genealogy-" + genealogyId,
                personId == null ? "shared" : "person-" + personId,
                monthFolder,
                storedName
        ).toString();

        Path target = getUploadRoot().resolve(relativePath).normalize();
        Files.createDirectories(target.getParent());
        file.transferTo(target);

        Attachment attachment = new Attachment();
        attachment.setGenealogyId(genealogyId);
        attachment.setPersonId(personId);
        attachment.setBizType(StringUtils.hasText(bizType) ? bizType : "PERSON");
        attachment.setFileName(originalName);
        attachment.setFilePath(relativePath);
        attachment.setFileType(file.getContentType());
        attachment.setFileSize(file.getSize());
        attachment.setDescription(StringUtils.hasText(description) ? description.trim() : null);
        attachment.setSortOrder(sortOrder == null ? 0 : sortOrder);
        return attachmentRepository.save(attachment);
    }

    public Resource loadAsResource(Attachment attachment) {
        try {
            Path filePath = resolveAttachmentPath(attachment);
            Resource resource = new UrlResource(filePath.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new IllegalArgumentException("附件文件不存在或不可读取");
            }
            return resource;
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("附件路径无效");
        }
    }

    public void deleteById(Long id) {
        Attachment attachment = attachmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("附件不存在"));
        deletePhysicalFile(attachment);
        attachmentRepository.deleteById(id);
    }

    @Transactional
    public void deleteByPersonId(Long personId) {
        if (personId == null) {
            return;
        }
        List<Attachment> attachments = attachmentRepository.findByPersonIdOrderBySortOrderAscCreatedAtDesc(personId);
        attachments.forEach(this::deletePhysicalFile);
        attachmentRepository.deleteAll(attachments);
    }

    @Transactional
    public void deleteByGenealogyId(Long genealogyId) {
        if (genealogyId == null) {
            return;
        }
        List<Attachment> attachments = attachmentRepository.findByGenealogyIdOrderBySortOrderAscCreatedAtDesc(genealogyId);
        attachments.forEach(this::deletePhysicalFile);
        attachmentRepository.deleteAll(attachments);
    }

    private Path getUploadRoot() {
        return Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    private Path resolveAttachmentPath(Attachment attachment) {
        Path resolved = getUploadRoot().resolve(attachment.getFilePath()).normalize();
        if (!resolved.startsWith(getUploadRoot())) {
            throw new IllegalArgumentException("附件路径不安全");
        }
        return resolved;
    }

    private void deletePhysicalFile(Attachment attachment) {
        try {
            Path filePath = resolveAttachmentPath(attachment);
            Files.deleteIfExists(filePath);
        } catch (Exception ignored) {
        }
    }
}
