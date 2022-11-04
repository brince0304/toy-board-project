package com.fastcampus.projectboard.dto.security;

import com.fastcampus.projectboard.domain.UserAccountRole;
import com.fastcampus.projectboard.dto.UserAccountDto;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A DTO for the {@link com.fastcampus.projectboard.domain.UserAccount} entity
 */
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