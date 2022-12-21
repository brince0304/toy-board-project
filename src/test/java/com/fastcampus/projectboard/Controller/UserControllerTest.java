package com.fastcampus.projectboard.Controller;


import com.fastcampus.projectboard.Annotation.WithPrincipal;
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
import static org.mockito.BDDMockito.given;
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
    private final TokenProvider tokenProvider;





    public UserControllerTest(@Autowired ObjectMapper mapper, @Autowired MockMvc mvc, @Autowired UserService userService, @Autowired TokenProvider tokenProvider) {
        this.mapper = mapper;
        this.mvc = mvc;
        this.userService = userService;
        this.tokenProvider = tokenProvider;
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
                signupDto.toEntity(),"haha","haha"
        );

        ArticleComment articleComment = ArticleComment.
                of(article,signupDto.toEntity(),"haha");



        userService.saveUserAccountWithoutProfile(signupDto, SaveFile.SaveFileDto.builder().build());



    }

    @DisplayName("로그인 페이지 - 정상호출")
    @Test
    public void givenNothing_whenTryingToLogIn_thenReturnsLogInView() throws Exception {
        //given


        //when & then
        mvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML));
    }

    @Test
    @WithPrincipal
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
    }

    @DisplayName("계정 마이페이지")
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
    }



    @Test
    @WithPrincipal
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
    }
    @Test
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
    }

    @Test
    void givenInfo_whenInsertingUserAccountWithProfileImg_thenSavesUserAccountWithImg() throws Exception {
//given
        given(userService.saveUserAccount(createSignupDto(),createFileDto())).willReturn(createSignupDto().getUserId());

        MockMultipartFile signupDto = new MockMultipartFile("signupDto", "signupDto", "application/json", mapper.writeValueAsString(createSignupDto()).getBytes());
        MockMultipartFile imgFile = new MockMultipartFile("imgFile", "default.jpg", "image/png", "default.jpg".getBytes());
        //when & then
        mvc.perform(multipart("/signup").file(signupDto).file(imgFile))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN));
    }

    private SaveFile.SaveFileDto createFileDto() {
        return SaveFile.builder()
                .fileName("default.jpg")
                .filePath(uploadPath+"/default.jpg")
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

    private Cookie createAccessToken() {
        UserAccount.BoardPrincipal authUser = UserAccount.BoardPrincipal.builder()
                .username("test")
                .build();
        List<String> tokens = new ArrayList<>();
        String accessToken = tokenProvider.doGenerateToken("test",TokenProvider.TOKEN_VALIDATION_SECOND);
        String refreshToken = tokenProvider.generateRefreshToken(authUser);
        tokens.add(accessToken);
        tokens.add(refreshToken);
        return CookieUtil.createCookie("accessToken",accessToken);
    }
    private Cookie createRefreshToken() {
        UserAccount.BoardPrincipal authUser = UserAccount.BoardPrincipal.builder()
                .username("test")
                .build();
        List<String> tokens = new ArrayList<>();
        String accessToken = tokenProvider.doGenerateToken("test",TokenProvider.TOKEN_VALIDATION_SECOND);
        String refreshToken = tokenProvider.generateRefreshToken(authUser);
        tokens.add(accessToken);
        tokens.add(refreshToken);
        return CookieUtil.createCookie("refreshToken",refreshToken);
    }
}
