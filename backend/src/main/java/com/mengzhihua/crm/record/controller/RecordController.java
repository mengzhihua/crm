package com.mengzhihua.crm.record.controller;

import com.mengzhihua.crm.common.Result;
import com.mengzhihua.crm.common.enums.RelatedType;
import com.mengzhihua.crm.record.dto.NoteRequest;
import com.mengzhihua.crm.record.entity.Attachment;
import com.mengzhihua.crm.record.entity.FieldHistory;
import com.mengzhihua.crm.record.entity.Note;
import com.mengzhihua.crm.record.service.FieldHistoryService;
import com.mengzhihua.crm.record.service.RecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.List;

@Tag(name = "动态记录")
@Validated
@RestController
@RequestMapping("/api")
@PreAuthorize("isAuthenticated()")
public class RecordController {
    private final FieldHistoryService historyService;
    private final RecordService recordService;

    public RecordController(
            FieldHistoryService historyService,
            RecordService recordService
    ) {
        this.historyService = historyService;
        this.recordService = recordService;
    }

    @Operation(summary = "查询字段变更历史")
    @GetMapping("/history")
    public Result<List<FieldHistory>> history(
            @RequestParam RelatedType targetType,
            @RequestParam Long targetId
    ) {
        return Result.ok(historyService.list(targetType, targetId));
    }

    @Operation(summary = "查询备注")
    @GetMapping("/notes")
    public Result<List<Note>> notes(
            @RequestParam RelatedType targetType,
            @RequestParam Long targetId
    ) {
        return Result.ok(recordService.notes(targetType, targetId));
    }

    @Operation(summary = "新增备注")
    @PostMapping("/notes")
    public Result<Note> addNote(@Valid @RequestBody NoteRequest request) {
        Note note = new Note();
        note.setTargetType(request.getTargetType());
        note.setTargetId(request.getTargetId());
        note.setContent(request.getContent());
        return Result.ok(recordService.addNote(note));
    }

    @Operation(summary = "查询附件")
    @GetMapping("/attachments")
    public Result<List<Attachment>> attachments(
            @RequestParam RelatedType targetType,
            @RequestParam Long targetId
    ) {
        return Result.ok(recordService.attachments(targetType, targetId));
    }

    @Operation(summary = "上传附件")
    @PostMapping(
            value = "/attachments",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public Result<Attachment> upload(
            @RequestPart MultipartFile file,
            @RequestParam RelatedType targetType,
            @RequestParam Long targetId
    ) {
        return Result.ok(recordService.upload(file, targetType, targetId));
    }

    @Operation(summary = "下载附件")
    @GetMapping("/attachments/{id}/download")
    public ResponseEntity<Resource> download(@PathVariable Long id) {
        Attachment attachment = recordService.getAttachment(id);
        Resource resource = recordService.resource(attachment);
        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        if (attachment.getContentType() != null) {
            try {
                mediaType = MediaType.parseMediaType(attachment.getContentType());
            } catch (IllegalArgumentException ignored) {
                mediaType = MediaType.APPLICATION_OCTET_STREAM;
            }
        }
        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + attachment.getFileName() + "\""
                )
                .body(resource);
    }

    @Operation(summary = "删除附件")
    @DeleteMapping("/attachments/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        recordService.deleteAttachment(id);
        return Result.ok();
    }
}
