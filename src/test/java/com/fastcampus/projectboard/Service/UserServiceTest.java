package com.fastcampus.projectboard.Service;


import com.fastcampus.projectboard.domain.Article;
import com.fastcampus.projectboard.domain.UserAccount;
import com.fastcampus.projectboard.dto.ArticleDto;
import com.fastcampus.projectboard.dto.UserAccountDto;
import com.fastcampus.projectboard.repository.ArticleRepository;
import com.fastcampus.projectboard.repository.UserAccountRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import javax.persistence.EntityNotFoundException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@DisplayName("비즈니스 로직 - 계정")
@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @InjectMocks
    private UserService sut;

    @Mock
    private ArticleRepository articleRepository;
    @Mock
    private UserAccountRepository userAccountRepository;

    @Test
    void givenUserId_whenGettingUserAccount_thenGetsUserAccount() {
        //given
        UserAccountDto userAccountDto = createUserAccountDto();
        sut.saveUserAccount(userAccountDto);
        given(userAccountRepository.findById(userAccountDto.userId())).willReturn(Optional.of(userAccountDto.toEntity()));

        //when
        UserAccount dto2 = sut.getUserAccount(userAccountDto.userId()).toEntity();

        //then
        assertThat(dto2).isEqualTo(userAccountDto.toEntity());
    }

    @DisplayName("회원 정보를 입력하면 아이디를 생성한다.")
    @Test
    void givenInfo_whenRegistering_thenSavesAccount() {
        //Given
        UserAccountDto userAccountDto = createUserAccountDto();
        given(userAccountRepository.save(userAccountDto.toEntity())).willReturn(userAccountDto.toEntity());

        //when
        sut.saveUserAccount(userAccountDto);

        //then
        assertThat(userAccountDto.toEntity()).isNotNull().isEqualTo(userAccountDto.toEntity());

    }
    @DisplayName("업데이트된 정보를 입력하면 계정 정보를 수정한다.")
    @Test
    void givenUpdatedInfo_whenUpdatingUserAccount_thenSavingUpdatedInfo() {
        //given
        UserAccountDto userDto = createUserAccountDto();
        UserAccount account = userDto.toEntity();
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        UserAccount updatedAccount = UserAccount.of(userDto.userId()
                , "12345"
                , userDto.email()
                ,userDto.nickname()
                ,"memo123");
        given(userAccountRepository.findById(account.getUserId())).willReturn(Optional.of(account));
        String encodedPassword = passwordEncoder.encode(updatedAccount.getUserPassword());
        //when
        sut.updateUserAccount(UserAccountDto.from(updatedAccount));

        //then
        assertThat(account)
                .hasFieldOrPropertyWithValue("userPassword",userAccountRepository.findById(account.getUserId()).get().getUserPassword())
                .hasFieldOrPropertyWithValue("memo", updatedAccount.getMemo());
    }

    @DisplayName("계정 ID를 입력하면 계정을 삭제한다.")
    @Test
    void givenUserId_whenDeletingUserAccount_thenDeletesUserAccount() {
        //given
        UserAccountDto userAccountDto = createUserAccountDto();
        UserAccount account = userAccountDto.toEntity();
        //when
        sut.deleteUserAccount(account.getUserId());

        //then
        then(userAccountRepository).should().deleteById(account.getUserId());
    }



    private UserAccount createUserAccount() {
        return UserAccount.of(
                "uno",
                "password",
                "uno@email.com",
                "Uno",
                null
        );
    }

    private Article createArticle() {
        return Article.of(
                createUserAccount(),
                "title",
                "content"
        );
    }

    private ArticleDto createArticleDto() {
        return createArticleDto("title", "content", "#java");
    }

    private ArticleDto createArticleDto(String title, String content, String hashtag) {
        return ArticleDto.of(1L,
                createUserAccountDto(),
                title,
                content,
                null,
                LocalDateTime.now(),
                "Uno",
                LocalDateTime.now(),
                "Uno");
    }

    private UserAccountDto createUserAccountDto() {
        return UserAccountDto.of(
                "uno",
                "password",
                "uno@mail.com",
                "Uno",
                "This is memo",
                LocalDateTime.now(),
                "uno",
                LocalDateTime.now(),
                "uno"
        );
    }
}
