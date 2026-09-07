package com.mengzhihua.crm.common;

import org.springframework.data.domain.*;
import java.util.stream.Collectors;

public final class DtoUtil {
    private DtoUtil() {}
    public static <T> PageResult<T> page(Page<?> p, java.util.function.Function<Object,T> mapper) {
        return new PageResult<>(p.getContent().stream().map(x -> mapper.apply(x)).collect(Collectors.toList()), p.getTotalElements(), p.getNumber()+1, p.getSize());
    }
    public static Pageable pageable(int page, int size) { return PageRequest.of(Math.max(0,page-1), Math.max(1, Math.min(size,100)), Sort.by(Sort.Direction.DESC, "id")); }
}
