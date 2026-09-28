package com.mengzhihua.crm.dashboard;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DashboardLayoutRepository extends JpaRepository<DashboardLayout, Long> {
    Optional<DashboardLayout> findByUsername(String username);
}
