package com.fastcampus.projectboard.repository;

import com.fastcampus.projectboard.domain.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;

import java.util.Optional;

public interface UserAccountRepository extends JpaRepository<UserAccount,String> {

    Optional<UserAccount> findByEmail(String email);

    Optional<UserAccount> findByNickname(String nickname);
}
