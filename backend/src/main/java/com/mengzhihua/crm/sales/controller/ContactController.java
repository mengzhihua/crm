package com.mengzhihua.crm.sales.controller;

import com.mengzhihua.crm.common.PageResult;
import com.mengzhihua.crm.common.Result;
import com.mengzhihua.crm.sales.entity.Contact;
import com.mengzhihua.crm.sales.service.ContactService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;

@Tag(name = "联系人")
@RestController
@RequestMapping("/api/contacts")
public class ContactController {
    private final ContactService contactService;

    public ContactController(ContactService contactService) {
        this.contactService = contactService;
    }

    @Operation(summary = "分页查询联系人")
    @PreAuthorize("hasAnyRole('ADMIN','SALES_MANAGER','SALES_REP','SERVICE_AGENT')")
    @GetMapping
    public Result<PageResult<Contact>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long accountId
    ) {
        return Result.ok(contactService.list(page, size, keyword, accountId));
    }

    @Operation(summary = "查询联系人")
    @PreAuthorize("hasAnyRole('ADMIN','SALES_MANAGER','SALES_REP','SERVICE_AGENT')")
    @GetMapping("/{id}")
    public Result<Contact> get(@PathVariable Long id) {
        return Result.ok(contactService.get(id));
    }

    @Operation(summary = "新增联系人")
    @PreAuthorize("hasAnyRole('ADMIN','SALES_MANAGER','SALES_REP')")
    @PostMapping
    public Result<Contact> add(@RequestBody Contact contact) {
        return Result.ok(contactService.save(contact));
    }

    @Operation(summary = "编辑联系人")
    @PreAuthorize("hasAnyRole('ADMIN','SALES_MANAGER','SALES_REP')")
    @PutMapping("/{id}")
    public Result<Contact> edit(
            @PathVariable Long id,
            @RequestBody Contact contact
    ) {
        contact.setId(id);
        return Result.ok(contactService.save(contact));
    }

    @Operation(summary = "删除联系人")
    @PreAuthorize("hasAnyRole('ADMIN','SALES_MANAGER','SALES_REP')")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        contactService.delete(id);
        return Result.ok();
    }
}
