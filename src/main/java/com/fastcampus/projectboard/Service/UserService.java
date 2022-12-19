package com.fastcampus.projectboard.Service;

import com.fastcampus.projectboard.Util.FileUtil;
import com.fastcampus.projectboard.domain.Article;
import com.fastcampus.projectboard.domain.SaveFile;
import com.fastcampus.projectboard.domain.UserAccount;
import com.fastcampus.projectboard.repository.ArticleRepository;
import com.fastcampus.projectboard.repository.SaveFileRepository;
import com.fastcampus.projectboard.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.io.IOException;
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
    private final SaveFileRepository fileRepository;

    @Value("${com.example.upload.path.profileImg}") // application.properties의 변수
    private String uploadPath;



    public String saveUserAccount(UserAccount.SignupDto user, SaveFile.SaveFileDto saveFileDto) throws IOException {
        UserAccount account = userAccountRepository.save(user.toEntity());
        account.setUserPassword(new BCryptPasswordEncoder().encode(user.getPassword1()));
        account.setProfileImg(saveFileDto.toEntity());
        return user.getUserId();
    }

    public SaveFile.SaveFileDto changeAccountProfileImg(String id, SaveFile.SaveFileDto saveFileDto) throws IOException {
        UserAccount account = userAccountRepository.findById(id).orElseThrow(EntityNotFoundException::new);
        if(account.getProfileImg()!=null && !account.getProfileImg().getFileSize().equals(0L)){
            FileUtil.deleteFile(SaveFile.SaveFileDto.from(account.getProfileImg()));
        }
        SaveFile.SaveFileDto currentImg = SaveFile.SaveFileDto.from(account.getProfileImg());
        account.setProfileImg(saveFileDto.toEntity());
        return currentImg;
    }

    @Transactional(readOnly = true)
    public boolean isUserExists(String userId){
        return userAccountRepository.existsById(userId);
    }

    public void updateUserAccount(String userId,UserAccount.UserAccountUpdateRequestDto userDto) {
        try {
            PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            UserAccount userAccount = userAccountRepository.findById(userId).orElseThrow(EntityNotFoundException::new);
            if (userDto.password1() != null) { userAccount.setUserPassword(passwordEncoder.encode(userDto.password1())); }
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
            UserAccount userAccount = userAccountRepository.getReferenceById(userId);
            return UserAccount.UserAccountDto.from(userAccount);
        } catch (Exception e) {
            throw new IllegalArgumentException("회원정보 조회에 실패했습니다");
        }
    }

    public void deleteUserAccount(String userId) {
       if(userAccountRepository.existsById(userId)){
           userAccountRepository.deleteById(userId);
       }
       else{
              throw new EntityNotFoundException("존재하지 않는 회원입니다");
       }
    }
    @Transactional(readOnly = true)
    public boolean isExistUser(String userId) {
        return userAccountRepository.existsByUserId(userId);
    }

    @Transactional(readOnly = true)
    public boolean isExistNickname(String nickname) {
        return userAccountRepository.existsByNickname(nickname);
    }
    @Transactional(readOnly = true)
    public boolean isExistEmail(String email) {
        return userAccountRepository.existsByEmail(email);
    }

    public UserAccount.BoardPrincipal loginByUserNameAndPassword(String username, String password) {
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

    public String saveUserAccountWithoutProfile(UserAccount.SignupDto user) {
        SaveFile defaultImg = fileRepository.findByFileName("default.jpg");
        UserAccount account = userAccountRepository.save(user.toEntity());
        account.setUserPassword(new BCryptPasswordEncoder().encode(user.getPassword1()));
        if(defaultImg!=null){
            account.setProfileImg(defaultImg);
        }
        else{
            throw new EntityNotFoundException("default.jpg 파일이 존재하지 않습니다");
        }
        return account.getUserId();
    }
}
