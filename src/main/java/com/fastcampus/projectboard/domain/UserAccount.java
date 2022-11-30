package com.fastcampus.projectboard.domain;

import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

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
@Builder
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








    public UserAccount() {}

    private UserAccount(String userId, String userPassword, String email, String nickname, String memo,Set<UserAccountRole> roles) {
        this.userId = userId;
        this.userPassword = userPassword;
        this.email = email;
        this.nickname = nickname;
        this.memo = memo;
        this.roles = roles;
    }

    public static UserAccount of(String userId, String userPassword, String email, String nickname, String memo,Set<UserAccountRole> roles) {
        return new UserAccount(userId, userPassword, email, nickname, memo,roles);
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

    }

    @Builder
    public record BoardPrincipal(
            String username,
            String password,
            Collection<? extends GrantedAuthority> authorities,
            String email,
            String nickname,
            String memo
    )  implements UserDetails {
        public static BoardPrincipal of(String username, String password, Collection<? extends GrantedAuthority> authorities,String email, String nickname, String memo) {
            // 지금은 인증만 하고 권한을 다루고 있지 않아서 임의로 세팅한다.

            return new BoardPrincipal(
                    username,
                    password,
                    authorities,
                    email,
                    nickname,
                    memo
            );
        }
        public static BoardPrincipal from(UserAccountDto dto) {
            return BoardPrincipal.of(
                    dto.userId(),
                    dto.userPassword(),
                    dto.roles().stream().map(role -> new SimpleGrantedAuthority(role.name())).collect(Collectors.toSet()),
                    dto.email(),
                    dto.nickname(),
                    dto.memo()
            );
        }

        public UserAccountDto toDto() {
            return UserAccountDto.of(username,password, email, nickname, memo, authorities.stream().map(role -> UserAccountRole.valueOf(role.getAuthority())).collect(Collectors.toSet()));
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
}

