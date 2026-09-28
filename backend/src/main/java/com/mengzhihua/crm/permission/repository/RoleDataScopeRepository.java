package com.mengzhihua.crm.permission.repository;

import com.mengzhihua.crm.common.enums.DataObjectType;
import com.mengzhihua.crm.common.enums.Role;
import com.mengzhihua.crm.permission.entity.RoleDataScope;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleDataScopeRepository extends JpaRepository<RoleDataScope, Long> {
    Optional<RoleDataScope> findByRoleAndObjectType(Role role, DataObjectType objectType);
}
