package com.fastcampus.projectboard.Service;


import com.fastcampus.projectboard.domain.Article;
import com.fastcampus.projectboard.domain.UserAccount;

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

import java.io.IOException;
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
        UserAccount.UserAccountDto userAccountDto = createUserAccountDto();
        given(userAccountRepository.findById(userAccountDto.userId())).willReturn(Optional.of(userAccountDto.toEntity()));

        //when
        UserAccount dto2 = sut.getUserAccount(userAccountDto.userId()).toEntity();

        //then
        assertThat(dto2).isEqualTo(userAccountDto.toEntity());
    }

    @DisplayName("회원 정보를 입력하면 아이디를 생성한다.")
    @Test
    void givenInfo_whenRegistering_thenSavesAccount() throws IOException {
        //Given
        UserAccount.SignupDto signupDto = UserAccount.SignupDto.from(createUserAccount());
        given(userAccountRepository.save(signupDto.toEntity())).willReturn(signupDto.toEntity());

        //when
        sut.saveUserAccountWithoutProfile(signupDto);

        //then
        assertThat(signupDto.toEntity()).isNotNull().isEqualTo(signupDto.toEntity());

    }
    @DisplayName("업데이트된 정보를 입력하면 계정 정보를 수정한다.")
    @Test
    void givenUpdatedInfo_whenUpdatingUserAccount_thenSavingUpdatedInfo() {
        //given
        UserAccount.UserAccountDto userDto = createUserAccountDto();
        UserAccount account = userDto.toEntity();
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        UserAccount updatedAccount = UserAccount.builder()
                .userId(account.getUserId())
                .userPassword(passwordEncoder.encode("1234"))
                .nickname("updatedName")
                .email("updatedEmail")
                .build();
        given(userAccountRepository.findById(account.getUserId())).willReturn(Optional.of(account));
        String encodedPassword = passwordEncoder.encode(updatedAccount.getUserPassword());
        //when
        sut.updateUserAccount(updatedAccount.getUserId(), UserAccount.UserAccountUpdateRequestDto.from(updatedAccount));

        //then
        assertThat(account)
                .hasFieldOrPropertyWithValue("userPassword",userAccountRepository.findById(account.getUserId()).get().getUserPassword())
                .hasFieldOrPropertyWithValue("memo", updatedAccount.getMemo());
    }

    @DisplayName("계정 ID를 입력하면 계정을 삭제한다.")
    @Test
    void givenUserId_whenDeletingUserAccount_thenDeletesUserAccount() {
        //given
        UserAccount.UserAccountDto userAccountDto = createUserAccountDto();
        UserAccount account = userAccountDto.toEntity();
        //when
        sut.deleteUserAccount(account.getUserId());

        //then
        then(userAccountRepository).should().deleteById(account.getUserId());
    }

    @DisplayName("계정 ID를 입력하면 해당 ID로 등록된 게시글 목록을 반환한다.")
    @Test
    void givenUserId_whenGettingArticlesByUserId_thenReturnsArticles() {
        //given
        UserAccount.UserAccountDto userAccountDto = createUserAccountDto();
        UserAccount account = userAccountDto.toEntity();
        Article article = createArticle();
        //when
        sut.getMyArticles(account.getUserId());

        //then
        then(articleRepository).should().findAllByUserAccountUserId(account.getUserId());
    }






    private UserAccount createUserAccount() {
        return UserAccount.builder()
                .userId("brince0304")
                .userPassword("Tjrgus97!@")
                .email("brince@email.com")
                .nickname("brince")
                .memo("memo")
                .build();

    }

    private Article createArticle() {
        return Article.of(
                createUserAccount(),
                "title",
                "content"
        );
    }

    private Article.ArticleDto createArticleDto() {
        return createArticleDto("title", "content", "#java");
    }

    private Article.ArticleDto createArticleDto(String title, String content, String hashtag) {
        return Article.ArticleDto.builder()
                .id(1L)
                .title(title)
                .content(content)
                .hashtag(hashtag)
                .userAccountDto(createUserAccountDto())
                .build();
    }

    private UserAccount.UserAccountDto createUserAccountDto() {
        return UserAccount.UserAccountDto.from(createUserAccount());
    }


    private UserAccount.SignupDto createSignupDto() {
        return UserAccount.SignupDto.from(createUserAccount());
    }

}
