package com.mengzhihua.crm.common;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class DtoUtil {
    private DtoUtil() {
    }

    public static <T> PageResult<T> page(Page<?> page, Function<Object, T> mapper) {
        List<T> records = page.getContent().stream()
                .map(mapper::apply)
                .collect(Collectors.toList());
        return new PageResult<>(
                records,
                page.getTotalElements(),
                page.getNumber() + 1,
                page.getSize()
        );
    }

    public static Pageable pageable(int page, int size) {
        int currentPage = Math.max(0, page - 1);
        int pageSize = Math.max(1, Math.min(size, 100));
        return PageRequest.of(
                currentPage,
                pageSize,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );
    }
}
