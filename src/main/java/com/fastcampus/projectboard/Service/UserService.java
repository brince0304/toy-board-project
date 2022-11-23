package com.fastcampus.projectboard.Service;

import com.fastcampus.projectboard.Jwt.TokenDto;
import com.fastcampus.projectboard.Util.RedisUtil;
import com.fastcampus.projectboard.Util.SaltUtil;
import com.fastcampus.projectboard.Util.TokenProvider;
import com.fastcampus.projectboard.domain.Article;
import com.fastcampus.projectboard.domain.UserAccount;
import com.fastcampus.projectboard.domain.UserAccountRole;
import com.fastcampus.projectboard.repository.ArticleCommentRepository;
import com.fastcampus.projectboard.repository.ArticleRepository;
import com.fastcampus.projectboard.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Transactional
@Service
public class UserService {

    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final UserAccountRepository userAccountRepository;

    private final ArticleRepository articleRepository;
    private final ArticleCommentRepository articleCommentRepository;


    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public void saveUserAccount(UserAccount.UserAccountDto user) {
        if(userAccountRepository.findById(user.userId()).isPresent()){
            throw new IllegalArgumentException("이미 존재하는 아이디입니다.");
        }
        if(userAccountRepository.findByEmail(user.email()).isPresent()){
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        }
        if(userAccountRepository.findByNickname(user.nickname()).isPresent()){
            throw new IllegalArgumentException("이미 존재하는 닉네임입니다.");
        }
        String password = user.userPassword();
        UserAccount account = userAccountRepository.save(user.toEntity());
        account.setUserPassword(new BCryptPasswordEncoder().encode(password));
    }

    @Transactional(readOnly = true)
    public boolean isUserExists(String userId){
        if(userAccountRepository.findById(userId).isPresent()){
            return true;
        }
        return false;
    }



    // 토큰 재발급 관련 메서드


    // 권한 가져오기
    @Transactional(readOnly = true)
    public String getAuthorities(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));
    }
    public void updateUserAccount(UserAccount.UserAccountDto userDto) {
        try {
            PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            UserAccount userAccount = userAccountRepository.findById(userDto.userId()).orElseThrow();
            if (userDto.userPassword() != null) { userAccount.setUserPassword(passwordEncoder.encode(userDto.userPassword())); }
            if (userDto.nickname() != null) { userAccount.setNickname(userDto.nickname()); }
            if (userDto.email() != null) { userAccount.setEmail(userDto.email()); }
            if (userDto.memo() != null) { userAccount.setMemo(userDto.memo()); }
        } catch (Exception e) {
            throw new IllegalArgumentException("회원정보 수정에 실패했습니다");
        }
    }

    @Transactional(readOnly = true)
    public UserAccount.UserAccountDto getUserAccount(String userId) {
        try {
            UserAccount userAccount = userAccountRepository.findById(userId).orElseThrow();
            return UserAccount.UserAccountDto.from(userAccount);
        } catch (Exception e) {
            throw new IllegalArgumentException("회원정보 조회에 실패했습니다");
        }
    }

    public void deleteUserAccount(String userId) {
        try {
            userAccountRepository.deleteById(userId);
        } catch (Exception e) {
            throw new IllegalArgumentException("회원정보 삭제에 실패했습니다");
        }
    }
    @Transactional(readOnly = true)
    public boolean isExistUser(String userId) {
        return userAccountRepository.findById(userId).isPresent();
    }

    @Transactional(readOnly = true)
    public boolean isExistNickname(String nickname) {
        return userAccountRepository.findByNickname(nickname).isPresent();
    }
    @Transactional(readOnly = true)
    public boolean isExistEmail(String email) {
        return userAccountRepository.findByEmail(email).isPresent();
    }

    public UserAccount.BoardPrincipal loginUser(String username, String password) {
          UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(username, password);
            Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        return UserAccount.BoardPrincipal.from(UserAccount.UserAccountDto.from(userAccountRepository.findById(username).orElseThrow()));
    }

    public Set<Article.ArticleDto> getMyArticles(String username) {
        Set<Article> articles = articleRepository.findAllByUserAccountUserId(username);
        Set<Article.ArticleDto> articleDtos = new HashSet<>();
        for (Article article : articles) {
            articleDtos.add(Article.ArticleDto.from(article));
        }
        return articleDtos;
    }
}
