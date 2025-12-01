package com.skillfive.backend.repository;

import com.skillfive.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    User findByOpenId(String openId);
}
