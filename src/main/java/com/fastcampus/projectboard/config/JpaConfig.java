package com.fastcampus.projectboard.config;

import com.fastcampus.projectboard.domain.UserAccount;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

@EnableJpaAuditing
@Configuration
public class JpaConfig {
    @Bean
    public AuditorAware<String> auditorAware() {
        Authentication authentication= SecurityContextHolder.getContext().getAuthentication();
        if(authentication == null) {
            return () -> Optional.of("anonymous");
        }
        else {
            UserAccount.BoardPrincipal principal = (UserAccount.BoardPrincipal) authentication.getPrincipal();
            return () -> Optional.of(principal.getUsername());
        }
    }
}
