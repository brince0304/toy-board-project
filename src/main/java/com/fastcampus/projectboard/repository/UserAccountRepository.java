package com.fastcampus.projectboard.repository;

import com.fastcampus.projectboard.domain.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAccountRepository extends JpaRepository<UserAccount,String> {

    boolean existsByEmail(String email);

    boolean existsByNickname(String nickname);

    boolean existsByUserId(String userId);
}
