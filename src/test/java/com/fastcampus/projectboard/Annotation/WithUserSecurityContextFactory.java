package com.fastcampus.projectboard.Annotation;

import com.fastcampus.projectboard.domain.UserAccount;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import java.util.List;

public class WithUserSecurityContextFactory implements WithSecurityContextFactory<WithPrincipal> {

    @Override
    public SecurityContext createSecurityContext(WithPrincipal annotation) {
        String username = annotation.username();
        String nickname = annotation.nickname();
        String role = annotation.role();

        UserAccount.BoardPrincipal authUser = UserAccount.BoardPrincipal.builder()
                .username(username)
                .nickname(nickname)
                .email(username+"@test.com")
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_"+role)))
                .build();
        UsernamePasswordAuthenticationToken token =
                new UsernamePasswordAuthenticationToken(authUser, "password", List.of(new SimpleGrantedAuthority(role)));
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(token);
        return context;
    }
}
