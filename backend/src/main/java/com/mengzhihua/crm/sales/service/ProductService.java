package com.mengzhihua.crm.sales.service;

import com.mengzhihua.crm.common.BizException;
import com.mengzhihua.crm.common.DtoUtil;
import com.mengzhihua.crm.common.PageResult;
import com.mengzhihua.crm.sales.entity.Product;
import com.mengzhihua.crm.sales.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

@Service
public class ProductService {
    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public PageResult<Product> list(int page, int size, String keyword, Boolean active) {
        Specification<Product> specification = (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (keyword != null && !keyword.trim().isEmpty()) {
                String value = "%" + keyword.trim().toLowerCase() + "%";
                predicates.add(builder.or(
                        builder.like(builder.lower(root.get("name")), value),
                        builder.like(builder.lower(root.get("code")), value)
                ));
            }
            if (active != null) {
                predicates.add(builder.equal(root.get("active"), active));
            }
            return builder.and(predicates.toArray(new Predicate[0]));
        };
        Page<Product> result = productRepository.findAll(
                specification,
                DtoUtil.pageable(page, size)
        );
        return DtoUtil.page(result, item -> (Product) item);
    }

    public Product get(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new BizException("产品不存在"));
    }

    public Product save(Product product) {
        if (product.getCode() == null || product.getCode().trim().isEmpty()) {
            throw new BizException("产品编码不能为空");
        }
        if (product.getId() == null
                && productRepository.existsByCodeIgnoreCase(product.getCode())) {
            throw new BizException("产品编码已存在");
        }
        return productRepository.save(product);
    }

    public void delete(Long id) {
        productRepository.deleteById(id);
    }
}
