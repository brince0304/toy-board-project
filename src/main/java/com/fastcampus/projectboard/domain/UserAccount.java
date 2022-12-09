package com.fastcampus.projectboard.domain;

import com.fastcampus.projectboard.domain.type.UserAccountRole;
import io.micrometer.core.lang.Nullable;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;


@Getter
@ToString
@Table(indexes = {
        @Index(columnList = "email", unique = true),
        @Index(columnList = "createdAt"),
        @Index(columnList = "createdBy")
})
@Entity
public class UserAccount extends AuditingFields {
    @Id
    @Column(length = 50)
    @Setter
    private String userId;

    @Setter @Column(nullable = false) private String userPassword;

    @Setter @Column(length = 100) private String email;
    @Setter @Column(length = 100) private String nickname;
    @Setter private String memo;

    @Setter
    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    private Set<UserAccountRole> roles;

    @Setter
    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "profile_img_id")
    @Nullable
    private SaveFile profileImg;








    public UserAccount() {}
    @Builder
    private UserAccount(String userId, String userPassword, String email, String nickname, String memo,Set<UserAccountRole> roles, SaveFile profileImg) {
        this.userId = userId;
        this.userPassword = userPassword;
        this.email = email;
        this.nickname = nickname;
        this.memo = memo;
        this.roles = roles;
        this.profileImg = profileImg;

    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserAccount that)) return false;
        return userId.equals(that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    public static class SignupDto implements Serializable {
        @Size(min = 6, max = 25, message = "* 아이디는 6자 이상 25자 이하로 입력해주세요.")
        private String userId;

        @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*[!@#$%^*+=-])(?=.*[0-9]).{8,25}$", message = "* 패스워드는 영문, 숫자, 특수문자를 포함한 8자이상 25자 이하여야 합니다.")
        private String password1;

        @NotEmpty(message = "* 입력값을 확인해주세요.")
        private String password2;

        @Size(min=2, max=10, message = "* 닉네임은 2자 이상 10자 이하로 입력해주세요.")
        private String nickname;

        @Email
        @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$", message = "* 이메일 형식을 확인해주세요.")
        private String email;

        @Size(max=50, message = "* 메모는 50자 이하로 입력해주세요.")
        private String memo;

        private MultipartFile imgFile;

        protected SignupDto() {}

        public static SignupDto from(UserAccount userAccount) {
            return SignupDto.builder()
                    .userId(userAccount.getUserId())
                    .password1(userAccount.getUserPassword())
                    .password2(userAccount.getUserPassword())
                    .nickname(userAccount.getNickname())
                    .email(userAccount.getEmail())
                    .memo(userAccount.getMemo())
                    .build();
        }

        public UserAccount toEntity() {
                return UserAccount.builder()
                        .userId(userId)
                        .userPassword(password1)
                        .nickname(nickname)
                        .email(email)
                        .memo(memo)
                        .roles(Set.of(UserAccountRole.ROLE_USER))
                        .build();
            }



    }

    @Builder
    public record BoardPrincipal(
            String username,
            String password,
            Collection<? extends GrantedAuthority> authorities,
            String email,
            String nickname,
            String memo,
            SaveFile.FileDto profileImg
    )  implements UserDetails {

        public static BoardPrincipal from(UserAccountDto dto) {
            return BoardPrincipal.builder()
                    .username(dto.userId())
                    .password(dto.userPassword())
                    .authorities(dto.roles().stream().map(role -> new SimpleGrantedAuthority(role.name())).collect(Collectors.toSet()))
                    .email(dto.email())
                    .nickname(dto.nickname())
                    .memo(dto.memo())
                    .profileImg(dto.profileImg())
                    .build();
        }

        public UserAccountDto toDto() {
            return UserAccountDto.builder()
                    .userId(username)
                    .userPassword(password)
                    .email(email)
                    .nickname(nickname)
                    .memo(memo)
                    .roles(authorities.stream().map(GrantedAuthority::getAuthority).map(UserAccountRole::valueOf).collect(Collectors.toSet()))
                    .profileImg(profileImg)
                    .build();
        }



        @Override public String getUsername() { return username; }
        @Override public String getPassword() { return password; }
        @Override public Collection<? extends GrantedAuthority> getAuthorities() { return authorities; }

        @Override public boolean isAccountNonExpired() { return true; }
        @Override public boolean isAccountNonLocked() { return true; }
        @Override public boolean isCredentialsNonExpired() { return true; }
        @Override public boolean isEnabled() { return true; }

    }
    @Builder
    public static class LoginDto {
        private String username;
        private String password;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    @Builder
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
            Set<UserAccountRole> roles,
            SaveFile.FileDto profileImg

    ) {


        public static UserAccountDto from(UserAccount entity) {
            return UserAccountDto.builder()
                    .userId(entity.getUserId())
                    .userPassword(entity.getUserPassword())
                    .email(entity.getEmail())
                    .nickname(entity.getNickname())
                    .memo(entity.getMemo())
                    .createdAt(entity.getCreatedAt())
                    .createdBy(entity.getCreatedBy())
                    .modifiedAt(entity.getModifiedAt())
                    .modifiedBy(entity.getModifiedBy())
                    .roles(entity.getRoles())
                    .profileImg(entity.getProfileImg() == null ? null : SaveFile.FileDto.from(entity.getProfileImg()))
                    .build();
        }

        public UserAccount toEntity() {
            BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

            return UserAccount.builder()
                    .userId(userId)
                    .userPassword(passwordEncoder.encode(userPassword))
                    .email(email)
                    .nickname(nickname)
                    .memo(memo)
                    .roles(roles)
                    .profileImg(profileImg == null ? null : profileImg.toEntity())
                    .build();
        }

    }
    @Builder
    public record UserAccountUpdateRequestDto(
            @org.springframework.lang.Nullable
            @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*[!@#$%^*+=-])(?=.*[0-9]).{8,25}$", message = "* 패스워드는 영문, 숫자, 특수문자를 포함한 8자이상 25자 이하여야 합니다.")
            String password1,
            @NotEmpty(message = "* 입력값을 확인해주세요.")
            String password2,
            @org.springframework.lang.Nullable
            @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$", message = "* 이메일 형식을 확인해주세요.")
            String email,
            @org.springframework.lang.Nullable
            @Size(min=2, max=10, message = "* 닉네임은 2자 이상 10자 이하로 입력해주세요.")
            String nickname,
            @org.springframework.lang.Nullable
            @Size(max=50, message = "* 메모는 50자 이하로 입력해주세요.")
            String memo
    ) {
        public static UserAccountUpdateRequestDto of( String password1, String password2, String email, String nickname, String memo) {
            return new UserAccountUpdateRequestDto( password1, password2, email, nickname, memo);
        }

        public static UserAccountUpdateRequestDto from(UserAccount updatedAccount) {
            return UserAccountUpdateRequestDto.builder()
                    .password1(updatedAccount.getUserPassword())
                    .password2(updatedAccount.getUserPassword())
                    .email(updatedAccount.getEmail())
                    .nickname(updatedAccount.getNickname())
                    .memo(updatedAccount.getMemo())
                    .build();
        }
    }
}

