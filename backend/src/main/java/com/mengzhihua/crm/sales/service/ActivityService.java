package com.mengzhihua.crm.sales.service;

import com.mengzhihua.crm.common.BizException;
import com.mengzhihua.crm.common.DtoUtil;
import com.mengzhihua.crm.common.PageResult;
import com.mengzhihua.crm.common.enums.ActivityStatus;
import com.mengzhihua.crm.common.enums.RelatedType;
import com.mengzhihua.crm.sales.entity.Activity;
import com.mengzhihua.crm.sales.repository.ActivityRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ActivityService {
    private final ActivityRepository activityRepository;

    public ActivityService(ActivityRepository activityRepository) {
        this.activityRepository = activityRepository;
    }

    public PageResult<Activity> list(
            int page,
            int size,
            String keyword,
            RelatedType relatedType,
            Long relatedId,
            ActivityStatus status
    ) {
        Specification<Activity> specification = (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (keyword != null && !keyword.trim().isEmpty()) {
                predicates.add(builder.like(
                        builder.lower(root.get("subject")),
                        "%" + keyword.trim().toLowerCase() + "%"
                ));
            }
            if (relatedType != null) {
                predicates.add(builder.equal(root.get("relatedType"), relatedType));
            }
            if (relatedId != null) {
                predicates.add(builder.equal(root.get("relatedId"), relatedId));
            }
            if (status != null) {
                predicates.add(builder.equal(root.get("status"), status));
            }
            return builder.and(predicates.toArray(new Predicate[0]));
        };
        Page<Activity> result = activityRepository.findAll(
                specification,
                DtoUtil.pageable(page, size)
        );
        return DtoUtil.page(result, item -> (Activity) item);
    }

    public Activity get(Long id) {
        return activityRepository.findById(id)
                .orElseThrow(() -> new BizException("活动不存在"));
    }

    public Activity save(Activity activity) {
        if (activity.getStatus() == null) {
            activity.setStatus(ActivityStatus.PLANNED);
        }
        return activityRepository.save(activity);
    }

    public void delete(Long id) {
        activityRepository.deleteById(id);
    }

    public Activity complete(Long id) {
        Activity activity = get(id);
        activity.setStatus(ActivityStatus.DONE);
        activity.setCompletedAt(LocalDateTime.now());
        return activityRepository.save(activity);
    }
}
