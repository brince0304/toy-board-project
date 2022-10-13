package dto;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * A DTO for the {@link com.fastcampus.projectboard.domain.UserAccount} entity
 */
public record UserAccountDto(LocalDateTime createdAt, String createdBy, String userId, String userPassword,
                             String email, String nickName, String memo){
    public static UserAccountDto of(LocalDateTime createdAt, String createdBy, String userId, String userPassword, String email, String nickName, String memo) {
        return new UserAccountDto(createdAt, createdBy, userId, userPassword, email, nickName, memo);
    }
}