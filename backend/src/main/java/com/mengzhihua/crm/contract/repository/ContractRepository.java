package com.mengzhihua.crm.contract.repository;

import com.mengzhihua.crm.contract.entity.Contract;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.List;
import com.mengzhihua.crm.common.enums.ContractStatus;

public interface ContractRepository extends JpaRepository<Contract, Long>,
        JpaSpecificationExecutor<Contract> {
    Optional<Contract> findTopByContractNoStartingWithOrderByContractNoDesc(
            String prefix
    );

    List<Contract> findByStatus(ContractStatus status);
}
