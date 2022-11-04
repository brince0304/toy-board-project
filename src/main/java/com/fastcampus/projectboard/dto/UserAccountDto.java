package com.fastcampus.projectboard.dto;

import com.fastcampus.projectboard.domain.UserAccount;
import com.fastcampus.projectboard.domain.UserAccountRole;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Set;

/**
 * A DTO for the {@link com.fastcampus.projectboard.domain.UserAccount} entity
 */
public record UserAccountDto(
        String userId,
        String userPassword,
        String email,
        String nickname,
        String memo,
        LocalDateTime createdAt,
        String createdBy,
        LocalDateTime modifiedAt,
        String modifiedBy,
        Set<UserAccountRole> roles

) {
    public static UserAccountDto of(String userId, String userPassword, String email, String nickname, String memo, Set<UserAccountRole> roles) {
        return new UserAccountDto(userId, userPassword, email, nickname, memo, null,null,null,null,roles);
    }


    public static UserAccountDto of(String userId, String userPassword, String email, String nickname, String memo, LocalDateTime createdAt, String createdBy, LocalDateTime modifiedAt, String modifiedBy,Set<UserAccountRole> roles) {
        return new UserAccountDto(userId, userPassword, email, nickname, memo, createdAt, createdBy, modifiedAt, modifiedBy,roles);
    }

    public static UserAccountDto from(UserAccount entity) {
        return new UserAccountDto(
                entity.getUserId(),
                entity.getUserPassword(),
                entity.getEmail(),
                entity.getNickname(),
                entity.getMemo(),
                entity.getCreatedAt(),
                entity.getCreatedBy(),
                entity.getModifiedAt(),
                entity.getModifiedBy(),
                entity.getRoles()
        );
    }

    public UserAccount toEntity() {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

        return UserAccount.of(
                userId,
                userPassword,
                email,
                nickname,
                memo,
                roles
        );
    }

}