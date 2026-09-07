package com.mengzhihua.crm.auth.service;

import com.mengzhihua.crm.auth.dto.PasswordRequest;
import com.mengzhihua.crm.auth.entity.User;
import com.mengzhihua.crm.auth.repository.UserRepository;
import com.mengzhihua.crm.common.BizException;
import com.mengzhihua.crm.common.DtoUtil;
import com.mengzhihua.crm.common.PageResult;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public PageResult<User> list(int page, int size, String keyword) {
        Specification<User> specification = (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (keyword != null && !keyword.trim().isEmpty()) {
                String value = "%" + keyword.trim().toLowerCase() + "%";
                predicates.add(builder.or(
                        builder.like(builder.lower(root.get("username")), value),
                        builder.like(builder.lower(root.get("displayName")), value)
                ));
            }
            return builder.and(predicates.toArray(new Predicate[0]));
        };
        Page<User> result = userRepository.findAll(
                specification,
                DtoUtil.pageable(page, size)
        );
        return DtoUtil.page(result, item -> (User) item);
    }

    public User get(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new BizException("用户不存在"));
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new BizException("用户不存在"));
    }

    public User save(User user) {
        if (user.getId() == null) {
            if (userRepository.existsByUsername(user.getUsername())) {
                throw new BizException("用户名已存在");
            }
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        } else {
            User current = get(user.getId());
            user.setUsername(current.getUsername());
            if (user.getPassword() == null || user.getPassword().isEmpty()) {
                user.setPassword(current.getPassword());
            } else {
                user.setPassword(passwordEncoder.encode(user.getPassword()));
            }
        }
        return userRepository.save(user);
    }

    public User resetPassword(Long id, PasswordRequest request) {
        User user = get(id);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        return userRepository.save(user);
    }

    public void delete(Long id) {
        userRepository.deleteById(id);
    }
}
