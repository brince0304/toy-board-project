package com.fastcampus.projectboard.Controller;


import com.fastcampus.projectboard.Annotation.WithPrincipal;
import com.fastcampus.projectboard.Service.SaveFileService;
import com.fastcampus.projectboard.Service.UserService;
import com.fastcampus.projectboard.Util.CookieUtil;
import com.fastcampus.projectboard.Util.TokenProvider;
import com.fastcampus.projectboard.config.SecurityConfig;
import com.fastcampus.projectboard.domain.Article;
import com.fastcampus.projectboard.domain.ArticleComment;
import com.fastcampus.projectboard.domain.SaveFile;
import com.fastcampus.projectboard.domain.UserAccount;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import javax.servlet.http.Cookie;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.fastcampus.projectboard.Util.FileUtil.uploadPath;
import static com.mysema.commons.lang.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("View 컨트롤러 - 인증")
@AutoConfigureMockMvc
@SpringBootTest
@Import(SecurityConfig.class)
public class UserControllerTest {
    private final ObjectMapper mapper;

    private final MockMvc mvc;
    @MockBean
    private final UserService userService;
    @MockBean
    private final SaveFileService saveFileService;



    public UserControllerTest(@Autowired ObjectMapper mapper, @Autowired MockMvc mvc, @Autowired UserService userService, @Autowired SaveFileService saveFileService) {
        this.mapper = mapper;
        this.mvc = mvc;
        this.userService = userService;
        this.saveFileService = saveFileService;
    }


    @BeforeEach
    void setUp() throws IOException {
        UserAccount.SignupDto signupDto = UserAccount.SignupDto.builder()
                .userId("testtest")
                .password1("Tjrgus97!@")
                .password2("Tjrgus97!@")
                .nickname("brincebrince")
                .email("brince@email.com")
                .build();

        Article article = Article.of(
                signupDto.toEntity(), "haha", "haha"
        );

        ArticleComment articleComment = ArticleComment.
                of(article, signupDto.toEntity(), "haha");


        userService.saveUserAccountWithoutProfile(signupDto, SaveFile.SaveFileDto.builder().build());


    }

    @DisplayName("[view][GET] 로그인 페이지 - 정상호출")
    @Test
    public void givenNothing_whenTryingToLogIn_thenReturnsLogInView() throws Exception {
        //when & then
        mvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML));
    }

    @Test
    @WithPrincipal
    @DisplayName("[view][POST] 로그인 시도")
    void loginTest() throws Exception {
        UserAccount.LoginDto loginDto = UserAccount.LoginDto.builder()
                .username("test")
                .password("Tjrgus97!@")
                .build();
        //when & then
        mvc.perform(post("/login")
                        .content(mapper.writeValueAsString(loginDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
        then(userService).should().loginByUserNameAndPassword(any(),any());
    }

    @DisplayName("[view][GET] 계정 마이페이지")
    @Test
    @WithPrincipal
    void givenNothing_whenViewingAccountDetails_thenReturnsAccountDetails() throws Exception {
        //given
        given(userService.getUserAccount("test"))
                .willReturn(UserAccount.UserAccountDto.builder()
                        .userId("test")
                        .nickname("brincebrince")
                        .email("brince@email.com")
                        .memo("haha")
                        .build());
        //when & then
        mvc.perform(get("/accounts"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML));
        then(userService).should().getUserAccount("test");
    }


    @Test
    @WithPrincipal
    @DisplayName("[view][PUT] 계정 정보 수정 ")
    void givenUserId_whenUpdatingUserDetails_thenUpdatesUserDetail() throws Exception {
        //given
        UserAccount.UserAccountUpdateRequestDto userAccountUpdateRequestDto = UserAccount.UserAccountUpdateRequestDto.builder()
                .nickname("brince")
                .password1("Tjrgus97!@")
                .password2("Tjrgus97!@")
                .email("brince@email.com")
                .build();
        String body = mapper.writeValueAsString(userAccountUpdateRequestDto);
        //when & then
        mvc.perform(put("/accounts").content(body).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN));
        then(userService).should().updateUserAccount("test", userAccountUpdateRequestDto);
    }

    @Test
    @DisplayName("[view][PUT] 로그인이 되지 않은 상태에서 정보를 수정하려 하면 401 에러")
    void givenNothing_whenUpdatingUserDetails_thenGetsUnauthorizedError() throws Exception {
        //given
        UserAccount.UserAccountUpdateRequestDto userAccountUpdateRequestDto = UserAccount.UserAccountUpdateRequestDto.builder()
                .password1("Tjrgus97!@")
                .password2("Tjrgus97!@")
                .email("email@email.com")
                .build();
        String body = mapper.writeValueAsString(userAccountUpdateRequestDto);
        //when & then
        mvc.perform(put("/accounts").content(body).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized()).andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN));
    }

    @Test
    @DisplayName("[view][POST] 회원가입")
    void givenInfo_whenInsertingUserAccount_thenSavesUserAccount() throws Exception {
        //given
        UserAccount.SignupDto userAccountDto = UserAccount.SignupDto.builder()
                .userId("brince0304")
                .password1("Tjrgus97!@")
                .password2("Tjrgus97!@")
                .nickname("브린스")
                .email("brince@email.com")
                .memo("안녕하세요.")
                .build();
        given(userService.saveUserAccountWithoutProfile(userAccountDto, SaveFile.SaveFileDto.builder().build())).willReturn(userAccountDto.toEntity().getUserId());
        MockMultipartFile signupDto = new MockMultipartFile("signupDto", "signupDto", "application/json", mapper.writeValueAsString(userAccountDto).getBytes());
        //when & then
        mvc.perform(multipart("/signup").file(signupDto))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN));
        then(userService.saveUserAccountWithoutProfile(any(), any()));
    }

    @Test
    @WithPrincipal
    @DisplayName("[view][DELETE] 계정 삭제")
    void givenUserAccount_whenDeletingAccount_thenDeletesAccount() throws Exception {
        //given
        //when&then
        mvc.perform(delete("/accounts/test"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN));
        then(userService).should().deleteUserAccount("test");
    }

    @Test
    @DisplayName("[view][DELETE] 내 계정이 아닌 계정 삭제 시도시에 401에러")
    void givenUserAccount_whenDeletingAccountButNotMine_thenGetsError() throws Exception {
        //given
        //when&then
        mvc.perform(delete("/accounts/test"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN));
    }
    @Test
    @DisplayName("[view][POST] 회원가입시에 프로필 이미지를 업로드하면 기본 이미지가 아닌 업로드한 이미지를 등록한다.")
    void givenInfo_whenInsertingUserAccountWithProfileImg_thenSavesUserAccountWithImg() throws Exception {
//given
        given(userService.saveUserAccount(createSignupDto(), createFileDto())).willReturn(createSignupDto().getUserId());

        MockMultipartFile signupDto = new MockMultipartFile("signupDto", "signupDto", "application/json", mapper.writeValueAsString(createSignupDto()).getBytes());
        MockMultipartFile imgFile = new MockMultipartFile("imgFile", "default.jpg", "image/png", "default.jpg".getBytes());
        //when & then
        mvc.perform(multipart("/signup").file(signupDto).file(imgFile))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN));
        then(userService.saveUserAccount(any(), any()));
    }

    @Test
    @WithPrincipal
    @DisplayName("[view][POST] 계정 프로필 이미지 업로드")
    void givenAccount_whenUpdatingProfileImg_thenUpdatesProfileImg() throws Exception {
        //given
        given(saveFileService.saveFile(any())).willReturn(createFileDto());
        given(userService.changeAccountProfileImg(any(), any())).willReturn(SaveFile.SaveFileDto.builder().build());
        MockMultipartFile imgFile = new MockMultipartFile("imgFile", "default.jpg", "image/png", "default.jpg".getBytes());
        //when&then
        mvc.perform(multipart("/accounts/test").file(imgFile))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN));
        then(userService).should().changeAccountProfileImg("test", createFileDto());
        then(saveFileService).should().saveFile(any());
    }

    @Test
    @DisplayName("[view][POST] 이메일 중복 확인")
    void givenExampleEmail_whenCheckingIsExist_thenReturnBoolean() throws Exception {
        //given
        given(userService.isExistEmail(any())).willReturn(true);
        //when & then
        mvc.perform(post("/accounts/emailCheck").param("email", "test@test.com"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
                .andExpect(content().string("true"));
        then(userService).should().isExistEmail(any());
    }
    @Test
    @DisplayName("[view][POST] 닉네임 중복 확인")
    void givenExampleNickname_whenCheckingIsExist_thenReturnBoolean() throws Exception {
        //given
        given(userService.isExistNickname(any())).willReturn(true);
        //when & then
        mvc.perform(post("/accounts/nicknameCheck").param("nickname", "test"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
                .andExpect(content().string("true"));
        then(userService).should().isExistNickname(any());
    }
    @Test
    @DisplayName("[view][POST] 아이디 중복 확인")
    void givenExampleId_whenCheckingIsExist_thenReturnBoolean() throws Exception {
        //given
        given(userService.isUserExists(any())).willReturn(true);
        //when & then
        mvc.perform(post("/accounts/idCheck").param("userId", "test"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
                .andExpect(content().string("true"));
        then(userService).should().isUserExists(any());
    }

    private SaveFile.SaveFileDto createFileDto() {
        return SaveFile.builder()
                .fileName("default.jpg")
                .filePath(uploadPath + "/default.jpg")
                .fileSize(0L)
                .fileType("jpg")
                .uploadUser("test")
                .build().toDto();
    }

    private UserAccount.SignupDto createSignupDto() {
        return UserAccount.SignupDto.builder()
                .userId("brince0304")
                .password1("Tjrgus97!@")
                .password2("Tjrgus97!@")
                .nickname("브린스")
                .email("brince@email.com")
                .memo("안녕하세요.")
                .build();
    }


}
