package com.mengzhihua.crm.sales.service;

import com.mengzhihua.crm.common.BizException;
import com.mengzhihua.crm.common.DtoUtil;
import com.mengzhihua.crm.common.PageResult;
import com.mengzhihua.crm.sales.entity.PriceBook;
import com.mengzhihua.crm.sales.entity.PriceBookEntry;
import com.mengzhihua.crm.sales.repository.PriceBookEntryRepository;
import com.mengzhihua.crm.sales.repository.PriceBookRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

@Service
public class PriceBookService {
    private final PriceBookRepository priceBookRepository;
    private final PriceBookEntryRepository entryRepository;

    public PriceBookService(
            PriceBookRepository priceBookRepository,
            PriceBookEntryRepository entryRepository
    ) {
        this.priceBookRepository = priceBookRepository;
        this.entryRepository = entryRepository;
    }

    public PageResult<PriceBook> list(int page, int size, String keyword) {
        Specification<PriceBook> specification = (root, query, builder) -> {
            if (keyword == null || keyword.trim().isEmpty()) {
                return builder.conjunction();
            }
            return builder.like(
                    builder.lower(root.get("name")),
                    "%" + keyword.trim().toLowerCase() + "%"
            );
        };
        Page<PriceBook> result = priceBookRepository.findAll(
                specification,
                DtoUtil.pageable(page, size)
        );
        return DtoUtil.page(result, item -> (PriceBook) item);
    }

    public PriceBook get(Long id) {
        return priceBookRepository.findById(id)
                .orElseThrow(() -> new BizException("价格手册不存在"));
    }

    public PriceBook save(PriceBook priceBook) {
        if (priceBook.isStandard()
                && !priceBookRepository.existsByStandardTrue()) {
            return priceBookRepository.save(priceBook);
        }
        if (priceBook.isStandard()) {
            PriceBook current = priceBook.getId() == null
                    ? null
                    : get(priceBook.getId());
            if (current == null || !current.isStandard()) {
                throw new BizException("标准价格手册只能有一个");
            }
        }
        return priceBookRepository.save(priceBook);
    }

    public void delete(Long id) {
        priceBookRepository.deleteById(id);
    }

    public List<PriceBookEntry> entries(Long priceBookId) {
        get(priceBookId);
        return entryRepository.findByPriceBookId(priceBookId);
    }

    public PriceBookEntry saveEntry(Long priceBookId, PriceBookEntry entry) {
        get(priceBookId);
        entry.setPriceBookId(priceBookId);
        if (entry.getId() == null
                && entryRepository.findByPriceBookIdAndProductId(
                priceBookId,
                entry.getProductId()
        ).isPresent()) {
            throw new BizException("该产品已存在于价格手册");
        }
        return entryRepository.save(entry);
    }

    public void deleteEntry(Long id) {
        entryRepository.deleteById(id);
    }
}
