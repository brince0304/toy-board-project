package com.fastcampus.projectboard.Controller;

import com.fastcampus.projectboard.Service.UserService;
import com.fastcampus.projectboard.Util.ControllerUtil;
import com.fastcampus.projectboard.Util.CookieUtil;
import com.fastcampus.projectboard.Util.RedisUtil;
import com.fastcampus.projectboard.Util.TokenProvider;
import com.fastcampus.projectboard.domain.UserAccount;
import com.fastcampus.projectboard.domain.UserAccountRole;

import lombok.RequiredArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@RestController
@RequestMapping
public class UserController {
    private final UserService userService;
    private final TokenProvider tokenProvider;
    private final CookieUtil cookieUtil;
    private final RedisUtil redisUtil;
    private final ControllerUtil controllerUtil;
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Value("${com.example.upload.path.profileImg}") // application.properties의 변수
    private String uploadPath;

    @GetMapping("/signup")
    public ModelAndView signup(UserAccount.SignupDto userCreateForm) {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("user/signup_form");
        mav.addObject("userCreateForm", userCreateForm);
        return mav;
    }

    @GetMapping("/accounts")
    public ModelAndView myPage(@AuthenticationPrincipal UserAccount.BoardPrincipal principal,ModelMap map) {
        UserAccount.UserAccountDto accountDto =  userService.getUserAccount(principal.username());
        ModelAndView mav = new ModelAndView();
        mav.setViewName("user/my_page");
        mav.addObject("accountDto", accountDto);
        return mav;
    }
    @GetMapping("/accounts/{username}")
    public ResponseEntity<?> getProfileImg (@PathVariable String username) throws IOException {
        UserAccount.UserAccountDto accountDto = userService.getUserAccount(username);
        InputStream inputStream = new FileInputStream(new File(accountDto.profileImgPath()));
        byte[] imageByteArray = IOUtils.toByteArray(inputStream);
        inputStream.close();
        return new ResponseEntity<>(imageByteArray, HttpStatus.OK);
    }

    @GetMapping("/accounts/articles")
    public ResponseEntity<?> getMyArticles(@AuthenticationPrincipal UserAccount.BoardPrincipal principal) {
        return new ResponseEntity<>(userService.getMyArticles(principal.username()), HttpStatus.OK);
    }

    @PutMapping("/accounts")
    public ResponseEntity<?> updateMyAccount(@AuthenticationPrincipal UserAccount.BoardPrincipal principal
            ,@Valid @RequestBody UserAccount.UserAccountUpdateRequestDto dto, BindingResult bindingResult) {
        if(!dto.password1().equals(dto.password2())){
            bindingResult.addError(new FieldError("dto","password2","비밀번호가 일치하지 않습니다."));
            return new ResponseEntity<>(controllerUtil.getErrors(bindingResult), HttpStatus.BAD_REQUEST);
        }
        else if(principal==null){
            return new ResponseEntity<>("로그인이 필요합니다.", HttpStatus.UNAUTHORIZED);
        }
        else if(bindingResult.hasErrors()){
            return new ResponseEntity<>(controllerUtil.getErrors(bindingResult), HttpStatus.BAD_REQUEST);
        }
        userService.updateUserAccount(principal.username(),dto);
        return new ResponseEntity<>("수정되었습니다.", HttpStatus.OK);
    }



    @PostMapping("/user/idCheck")
    public ResponseEntity<String> isUserExists(@RequestParam("userId") String userId) {
        if (userService.isUserExists(userId)) {
            return new ResponseEntity<>("true", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("false",HttpStatus.BAD_REQUEST);
        }
    }
    @PostMapping("/user/emailCheck")
    public ResponseEntity<String> isEmailExists(@RequestParam("email") String email) {
        if (userService.isExistEmail(email)) {
            return new ResponseEntity<>("true", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("false",HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/user/nicknameCheck")
    public ResponseEntity<String> isNicknameExists(@RequestParam("nickname") String nickname) {
        if (userService.isExistNickname(nickname)) {
            return new ResponseEntity<>("true", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("false",HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestPart("signupDto") @Valid UserAccount.SignupDto userCreateForm, BindingResult bindingResult
    ,@RequestPart(value = "imgFile",required = false) MultipartFile imgFile) throws IOException {
        if (bindingResult.hasErrors()) {
            return new ResponseEntity<>(controllerUtil.getErrors(bindingResult), HttpStatus.BAD_REQUEST);
        }
        if(imgFile==null){
            userService.saveUserAccountWithoutProfile(userCreateForm);
            return new ResponseEntity<>("success", HttpStatus.OK);
        }
        else {
            userService.saveUserAccount(userCreateForm, imgFile);
            return new ResponseEntity<>("success", HttpStatus.OK);
        }
    }
    @PostMapping("/accounts/upload/{id}")
    public ResponseEntity<?> changeProfileImg(@PathVariable String id, @RequestPart(value="file",required = false)  MultipartFile imgFile) {
        if (imgFile == null) {
            return new ResponseEntity<>("파일이 없습니다.", HttpStatus.BAD_REQUEST);
        }
        try {
            userService.changeAccountProfileImg(id,imgFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ResponseEntity<>("success", HttpStatus.OK);
    }

    @GetMapping("/login")
    public ModelAndView login(UserAccount.LoginDto dto, HttpServletRequest req, HttpServletResponse res) {
        String referer = req.getHeader("Referer");
        ModelAndView mav = new ModelAndView();
        mav.setViewName("user/login_form");
        mav.addObject("dto", dto);
        mav.getModelMap().addAttribute("prevPage",referer);
        return mav;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@ModelAttribute("dto") @RequestBody @Valid UserAccount.LoginDto user, BindingResult bindingResult,
                                HttpServletResponse res, HttpServletRequest req, ModelMap map) throws IOException {

        try {
            if (!bindingResult.hasErrors()) {
                final UserAccount.BoardPrincipal principal = userService.loginByUserNameAndPassword(user.getUsername(), user.getPassword());
                Cookie accessToken = cookieUtil.createCookie(TokenProvider.ACCESS_TOKEN_NAME, tokenProvider.generateToken(principal));
                accessToken.setMaxAge((int) TimeUnit.MILLISECONDS.toSeconds(TokenProvider.REFRESH_TOKEN_VALIDATION_SECOND));
                Cookie refreshToken = cookieUtil.createCookie(TokenProvider.REFRESH_TOKEN_NAME, tokenProvider.generateRefreshToken(principal));
                refreshToken.setMaxAge((int) TimeUnit.MILLISECONDS.toSeconds(TokenProvider.REFRESH_TOKEN_VALIDATION_SECOND));
                redisUtil.setDataExpire(refreshToken.getValue(), principal.getUsername(), TokenProvider.REFRESH_TOKEN_VALIDATION_SECOND);
                res.addCookie(accessToken);
                res.addCookie(refreshToken);
                return new ResponseEntity<>(map.getAttribute("prevPage"), HttpStatus.OK);
            }
            else{
                return new ResponseEntity<>(controllerUtil.getErrors(bindingResult), HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            bindingResult.addError(new FieldError("dto", "username", "아이디 또는 비밀번호가 일치하지 않습니다."));

            return new ResponseEntity<>(controllerUtil.getErrors(bindingResult), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/logout")
    public ResponseEntity<String> logout(
             HttpServletRequest req,
            HttpServletResponse res) {
        SecurityContextHolder.clearContext();
        Cookie accessToken = cookieUtil.getCookie(req, TokenProvider.ACCESS_TOKEN_NAME);
        Cookie refreshToken = cookieUtil.getCookie(req, TokenProvider.REFRESH_TOKEN_NAME);
        if (accessToken != null) {
            Long expiration = tokenProvider.getExpireTime(accessToken.getValue());
            redisUtil.setBlackList(accessToken.getValue(), "accessToken", expiration-System.currentTimeMillis());
            accessToken.setMaxAge(0);
            res.addCookie(accessToken);
        }
        else{
            return new ResponseEntity<>("로그아웃에 실패했습니다.", HttpStatus.BAD_REQUEST);
        }
        if (refreshToken != null) {
            refreshToken.setMaxAge(0);
            res.addCookie(refreshToken);
            redisUtil.deleteData(refreshToken.getValue());
        }
        return new ResponseEntity<>("로그아웃 되었습니다.", HttpStatus.OK);
    }
}
