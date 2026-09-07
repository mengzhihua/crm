package com.mengzhihua.crm.record.service;

import com.mengzhihua.crm.auth.CurrentUser;
import com.mengzhihua.crm.common.BizException;
import com.mengzhihua.crm.common.enums.RelatedType;
import com.mengzhihua.crm.record.entity.Attachment;
import com.mengzhihua.crm.record.entity.Note;
import com.mengzhihua.crm.record.repository.AttachmentRepository;
import com.mengzhihua.crm.record.repository.NoteRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Service
public class RecordService {
    private final NoteRepository noteRepository;
    private final AttachmentRepository attachmentRepository;
    private final Path uploadDir;

    public RecordService(
            NoteRepository noteRepository,
            AttachmentRepository attachmentRepository,
            @Value("${crm.upload-dir:./data/uploads}") String uploadDir
    ) {
        this.noteRepository = noteRepository;
        this.attachmentRepository = attachmentRepository;
        this.uploadDir = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    public List<Note> notes(RelatedType type, Long targetId) {
        return noteRepository.findByTargetTypeAndTargetIdOrderByCreatedAtDesc(
                type,
                targetId
        );
    }

    public Note addNote(Note note) {
        if (note.getAuthor() == null || note.getAuthor().trim().isEmpty()) {
            note.setAuthor(CurrentUser.usernameOrDefault());
        }
        return noteRepository.save(note);
    }

    public List<Attachment> attachments(RelatedType type, Long targetId) {
        return attachmentRepository
                .findByTargetTypeAndTargetIdOrderByCreatedAtDesc(type, targetId);
    }

    public Attachment upload(
            MultipartFile file,
            RelatedType type,
            Long targetId
    ) {
        if (file == null || file.isEmpty()) {
            throw new BizException("请选择文件");
        }
        if (file.getSize() > 20 * 1024 * 1024) {
            throw new BizException("附件不能超过 20MB");
        }
        try {
            Files.createDirectories(uploadDir);
            String originalName = file.getOriginalFilename() == null
                    ? "附件"
                    : Paths.get(file.getOriginalFilename()).getFileName().toString();
            String storageName = UUID.randomUUID() + "-" + originalName;
            Path target = uploadDir.resolve(storageName).normalize();
            if (!target.startsWith(uploadDir.toAbsolutePath().normalize())) {
                throw new BizException("附件路径非法");
            }
            Files.copy(
                    file.getInputStream(),
                    target,
                    StandardCopyOption.REPLACE_EXISTING
            );
            Attachment attachment = new Attachment();
            attachment.setTargetType(type);
            attachment.setTargetId(targetId);
            attachment.setFileName(file.getOriginalFilename());
            attachment.setStorageName(storageName);
            attachment.setSize(file.getSize());
            attachment.setContentType(file.getContentType());
            attachment.setUploader(CurrentUser.usernameOrDefault());
            return attachmentRepository.save(attachment);
        } catch (IOException e) {
            throw new BizException("附件保存失败");
        }
    }

    public Attachment getAttachment(Long id) {
        return attachmentRepository.findById(id)
                .orElseThrow(() -> new BizException("附件不存在"));
    }

    public Resource resource(Attachment attachment) {
        return new FileSystemResource(uploadDir.resolve(attachment.getStorageName()));
    }

    public void deleteAttachment(Long id) {
        Attachment attachment = getAttachment(id);
        try {
            Files.deleteIfExists(uploadDir.resolve(attachment.getStorageName()));
        } catch (IOException e) {
            throw new BizException("附件删除失败");
        }
        attachmentRepository.delete(attachment);
    }
}
