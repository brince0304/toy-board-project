package com.fastcampus.projectboard.Service;


import com.fastcampus.projectboard.domain.Article;
import com.fastcampus.projectboard.domain.Hashtag;
import com.fastcampus.projectboard.domain.SaveFile;
import com.fastcampus.projectboard.domain.UserAccount;

import com.fastcampus.projectboard.repository.ArticleRepository;
import com.fastcampus.projectboard.repository.SaveFileRepository;
import com.fastcampus.projectboard.repository.UserAccountRepository;
import org.junit.jupiter.api.Disabled;
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
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
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
    private SaveFileRepository saveFileRepository;
    @Mock
    private UserAccountRepository userAccountRepository;

    @Test
    @DisplayName("계정 정보를 조회할 수 있다.")
    void givenUserId_whenGettingUserAccount_thenGetsUserAccount() {
        //given
        UserAccount userAccount = createUserAccount();
        given(userAccountRepository.getReferenceById(userAccount.getUserId())).willReturn(userAccount);
        //when
        UserAccount.UserAccountDto dto = sut.getUserAccount(userAccount.getUserId());

        //then
        then(userAccountRepository).should().getReferenceById(userAccount.getUserId());
        assertThat(dto.userId()).isEqualTo(userAccount.getUserId());
    }

    @Test
    @DisplayName("없는 계정 정보를 조회하면 예외를 던진다.")
    void givenNothing_whenGettingNonExistUserAccount_thenThrowsException() {
        //give
        //when
        Throwable throwable = catchThrowable(() -> sut.getUserAccount("nonExistUserId"));
        //then
        then(userAccountRepository).should().getReferenceById("nonExistUserId");
        assertThat(throwable).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("없는 계정정보를 수정하려면 예외를 던진다.")
    void givenNothing_whenUpdatingNonExistUserAccount_thenThrowsException() {
        //give
        //when
        Throwable throwable = catchThrowable(() -> sut.updateUserAccount("nonExistUserId",UserAccount.UserAccountUpdateRequestDto.builder().build()));
        //then
        then(userAccountRepository).should().findById("nonExistUserId");
        assertThat(throwable).isInstanceOf(EntityNotFoundException.class);
    }
    @Test
    @DisplayName("없는 계정정보를 삭제하려면 예외를 던진다.")
    void givenNothing_whenDeletingNonExistUserAccount_thenThrowsException() {
        //give
        //when
        Throwable throwable = catchThrowable(() -> sut.deleteUserAccount("nonExistUserId"));
        //then
        then(userAccountRepository).should().existsById("nonExistUserId");
        assertThat(throwable).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @Disabled("파일 엔티티를 given 으로 리턴 해줄 수 없다.")
    @DisplayName("계정 프로필 이미지를 수정할 수 있다.")
    void givenUserAccount_whenUpdatingProfileImg_thenUpdatesProfileImg() throws IOException {
        //given
        UserAccount userAccount = createUserAccount();
        SaveFile saveFile = SaveFile.builder()
                .id(1L)
                .build();
        given(userAccountRepository.findById(userAccount.getUserId())).willReturn(Optional.of(userAccount));

        //when
        sut.changeAccountProfileImg(userAccount.getUserId(), SaveFile.SaveFileDto.from(saveFile));

        //then
        then(userAccountRepository).should().findById(userAccount.getUserId());
    }

    @DisplayName("회원 정보를 입력하면 아이디를 생성한다.")
    @Test
    void givenInfo_whenRegistering_thenSavesAccount() throws IOException {
        //Given
        UserAccount userAccount = createUserAccount();
        SaveFile saveFile = SaveFile.builder()
                .id(1L)
                .fileName("default.jpg")
                .build();
        given(userAccountRepository.save(userAccount)).willReturn(userAccount);


        //when
        sut.saveUserAccountWithoutProfile(UserAccount.SignupDto.from(userAccount), saveFile.toDto());

        //then
        then(userAccountRepository).should().save(userAccount);

    }
    @DisplayName("업데이트된 정보를 입력하면 계정 정보를 수정한다.")
    @Test
    void givenUpdatedInfo_whenUpdatingUserAccount_thenSavingUpdatedInfo() {
        //given
        UserAccount.UserAccountDto userDto = createUserAccountDto();
        UserAccount account = userDto.toEntity();
        UserAccount updatedAccount = UserAccount.builder()
                .userId(account.getUserId())
                .userPassword("1234")
                .nickname("updatedName")
                .email("updatedEmail")
                .build();
        given(userAccountRepository.findById(account.getUserId())).willReturn(Optional.of(account));
        //when
        sut.updateUserAccount(updatedAccount.getUserId(), UserAccount.UserAccountUpdateRequestDto.from(updatedAccount));

        //then
        then(userAccountRepository).should().findById(account.getUserId());
        assertThat(account.getNickname()).isEqualTo(updatedAccount.getNickname());
    }

    @DisplayName("계정 ID를 입력하면 계정을 삭제한다.")
    @Test
    void givenUserId_whenDeletingUserAccount_thenDeletesUserAccount() {
        //given
        UserAccount.UserAccountDto userAccountDto = createUserAccountDto();
        UserAccount account = userAccountDto.toEntity();
        given(userAccountRepository.existsById(account.getUserId())).willReturn(true);
        //when
        sut.deleteUserAccount(account.getUserId());
        //then
        then(userAccountRepository).should().deleteById(account.getUserId());
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
                .hashtags(Hashtag.HashtagDto.from(hashtag))
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
