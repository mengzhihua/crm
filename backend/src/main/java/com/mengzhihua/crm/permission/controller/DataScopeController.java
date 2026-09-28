package com.mengzhihua.crm.permission.controller;

import com.mengzhihua.crm.auth.CurrentUser;
import com.mengzhihua.crm.common.Result;
import com.mengzhihua.crm.common.enums.AccessLevel;
import com.mengzhihua.crm.common.enums.DataObjectType;
import com.mengzhihua.crm.common.enums.DataScope;
import com.mengzhihua.crm.common.enums.Role;
import com.mengzhihua.crm.permission.entity.RecordShare;
import com.mengzhihua.crm.permission.entity.RoleDataScope;
import com.mengzhihua.crm.permission.repository.RecordShareRepository;
import com.mengzhihua.crm.permission.repository.RoleDataScopeRepository;
import com.mengzhihua.crm.permission.service.DataScopeService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class DataScopeController {
    private final RoleDataScopeRepository scopeRepository;
    private final RecordShareRepository shareRepository;
    private final DataScopeService dataScopeService;

    public DataScopeController(
            RoleDataScopeRepository scopeRepository,
            RecordShareRepository shareRepository,
            DataScopeService dataScopeService
    ) {
        this.scopeRepository = scopeRepository;
        this.shareRepository = shareRepository;
        this.dataScopeService = dataScopeService;
    }

    @GetMapping("/data-scopes")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<List<RoleDataScope>> scopes() {
        return Result.ok(scopeRepository.findAll());
    }

    @PutMapping("/data-scopes")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<RoleDataScope> save(@RequestBody RoleDataScope scope) {
        return Result.ok(scopeRepository.save(scope));
    }

    @GetMapping("/shares")
    @PreAuthorize("isAuthenticated()")
    public Result<List<RecordShare>> shares(
            @RequestParam DataObjectType objectType,
            @RequestParam Long recordId
    ) {
        return Result.ok(shareRepository.findByObjectTypeAndRecordId(objectType, recordId));
    }

    @PostMapping("/shares")
    @PreAuthorize("isAuthenticated()")
    public Result<RecordShare> share(@RequestBody RecordShare share) {
        dataScopeService.checkEdit(
                share.getObjectType(),
                dataScopeService.ownerOf(
                        share.getObjectType(),
                        share.getRecordId()
                ),
                share.getRecordId()
        );
        share.setSharedBy(CurrentUser.usernameOrDefault());
        return Result.ok(shareRepository.save(share));
    }

    @DeleteMapping("/shares")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> revoke(@RequestParam Long id) {
        RecordShare share = shareRepository.findById(id)
                .orElseThrow(() -> new com.mengzhihua.crm.common.BizException(
                        "分享不存在"
                ));
        dataScopeService.checkEdit(
                share.getObjectType(),
                dataScopeService.ownerOf(
                        share.getObjectType(),
                        share.getRecordId()
                ),
                share.getRecordId()
        );
        shareRepository.deleteById(id);
        return Result.ok();
    }
}
