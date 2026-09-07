package com.mengzhihua.crm;

import com.mengzhihua.crm.common.enums.OpportunityStage;
import com.mengzhihua.crm.forecast.entity.SalesTarget;
import com.mengzhihua.crm.forecast.repository.SalesTargetRepository;
import com.mengzhihua.crm.forecast.service.ForecastService;
import com.mengzhihua.crm.sales.entity.Opportunity;
import com.mengzhihua.crm.sales.repository.OpportunityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
class ForecastServiceTest {
    @Autowired
    private ForecastService forecastService;

    @Autowired
    private OpportunityRepository opportunityRepository;

    @Autowired
    private SalesTargetRepository salesTargetRepository;

    @BeforeEach
    void setUp() {
        opportunityRepository.deleteAll();
        salesTargetRepository.deleteAll();
    }

    @Test
    void calculatesWeightedAmountAndAchievementRate() {
        LocalDate today = LocalDate.now();
        SalesTarget target = new SalesTarget();
        target.setOwner("sales");
        target.setYear(today.getYear());
        target.setMonth(today.getMonthValue());
        target.setTargetAmount(new BigDecimal("10000"));
        salesTargetRepository.save(target);

        Opportunity won = new Opportunity();
        won.setOwner("sales");
        won.setAmount(new BigDecimal("4000"));
        won.setStage(OpportunityStage.CLOSED_WON);
        won.setProbability(100);
        won.setClosedAt(LocalDateTime.now());
        won.setExpectedCloseDate(today);
        opportunityRepository.save(won);

        Opportunity pipeline = new Opportunity();
        pipeline.setOwner("sales");
        pipeline.setAmount(new BigDecimal("6000"));
        pipeline.setStage(OpportunityStage.PROPOSAL);
        pipeline.setProbability(50);
        pipeline.setExpectedCloseDate(today);
        opportunityRepository.save(pipeline);

        List<Map<String, Object>> rows = forecastService.forecast(
                today.getYear(),
                today.getMonthValue(),
                "sales"
        );
        Map<String, Object> row = rows.get(0);
        assertEquals(
                0,
                ((BigDecimal) row.get("weightedAmount"))
                        .compareTo(new BigDecimal("3000"))
        );
        assertEquals(
                0,
                ((BigDecimal) row.get("achievementRate"))
                        .compareTo(new BigDecimal("40"))
        );
    }
}
