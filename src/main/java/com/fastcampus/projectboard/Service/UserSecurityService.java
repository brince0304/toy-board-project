package com.fastcampus.projectboard.Service;

import com.fastcampus.projectboard.domain.UserAccount;
import com.fastcampus.projectboard.domain.UserAccountRole;
import com.fastcampus.projectboard.domain.forms.UserCreateForm;
import com.fastcampus.projectboard.dto.UserAccountDto;
import com.fastcampus.projectboard.dto.security.BoardPrincipal;
import com.fastcampus.projectboard.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Transactional
@Service
public class UserSecurityService implements UserDetailsService {
    private final UserAccountRepository userAccountRepository;

    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
            Optional<UserAccount> _account = userAccountRepository.findById(userId);
            if (_account.isEmpty()) {
                throw new UsernameNotFoundException("사용자를 찾을수 없습니다.");
            }
            UserAccount account = _account.get();
            List<GrantedAuthority> authorities = new ArrayList<>();
            if ("test".equals(userId)) {
                authorities.add(new SimpleGrantedAuthority(UserAccountRole.ADMIN.getValue()));
                authorities.add(new SimpleGrantedAuthority(UserAccountRole.USER.getValue()));
            } else {
                authorities.add(new SimpleGrantedAuthority(UserAccountRole.USER.getValue()));
            }
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        return new BoardPrincipal(account.getUserId(), account.getUserPassword(), authorities, account.getEmail(), account.getNickname(), account.getMemo());
        }


}
