package com.fastcampus.projectboard.Service;

import com.fastcampus.projectboard.domain.UserAccount;
import com.fastcampus.projectboard.dto.UserAccountDto;
import com.fastcampus.projectboard.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.parameters.P;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Transactional
@Service
public class UserService {

    private final UserAccountRepository userAccountRepository;


    public void saveUserAccount(UserAccountDto userAccountDto) {
            PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
           UserAccount account = userAccountDto.toEntity();
           if(userAccountRepository.findById(account.getUserId()).isPresent()){
               throw new IllegalArgumentException("이미 존재하는 아이디입니다.");
           }
           if(userAccountRepository.findByEmail(account.getEmail()).isPresent()){
               throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
           }
           if(userAccountRepository.findByNickname(account.getNickname()).isPresent()){
               throw new IllegalArgumentException("이미 존재하는 닉네임입니다.");
           }
           else {
               account.setUserPassword(passwordEncoder.encode(userAccountDto.userPassword()));
                userAccountRepository.save(account);
           }
                    }

    public void updateUserAccount(UserAccountDto userDto) {
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

    public UserAccountDto getUserAccount(String userId) {
        UserAccount userAccount = userAccountRepository.findById(userId).orElseThrow();
        return UserAccountDto.from(userAccount);
    }

    public void deleteUserAccount(String userId) {
        try {
            userAccountRepository.deleteById(userId);
        } catch (Exception e) {
            throw new IllegalArgumentException("회원정보 삭제에 실패했습니다");
        }
    }

    public boolean isExistUser(String userId) {
        return userAccountRepository.findById(userId).isPresent();
    }

    public boolean isExistNickname(String nickname) {
        return userAccountRepository.findByNickname(nickname).isPresent();
    }

    public boolean isExistEmail(String email) {
        return userAccountRepository.findByEmail(email).isPresent();
    }

//    private String getEncodedPassword(String password) {
//        return ("{noop}" + password);
//    }
}
