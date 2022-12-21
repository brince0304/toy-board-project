package com.fastcampus.projectboard.Controller;

import com.fastcampus.projectboard.Messages.ErrorMessages;
import com.fastcampus.projectboard.Service.SaveFileService;
import com.fastcampus.projectboard.Service.UserService;
import com.fastcampus.projectboard.Util.*;
import com.fastcampus.projectboard.domain.SaveFile;
import com.fastcampus.projectboard.domain.UserAccount;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

import javax.persistence.EntityNotFoundException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.*;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@RestController
@RequestMapping
@Slf4j
public class UserController {
    private final UserService userService;
    private final TokenProvider tokenProvider;
    private final RedisUtil redisUtil;
    private final SaveFileService saveFileService;

    @GetMapping("/signup")
    public ModelAndView signup(UserAccount.SignupDto userCreateForm) {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("user/signup_form");
        mav.addObject("userCreateForm", userCreateForm);
        return mav;
    }

    @GetMapping("/accounts")
    public ModelAndView myPage(@AuthenticationPrincipal UserAccount.BoardPrincipal principal) {
        try {
            UserAccount.UserAccountDto accountDto = userService.getUserAccount(principal.username());
            ModelAndView mav = new ModelAndView();
            mav.setViewName("user/my_page");
            mav.addObject("accountDto", UserAccount.UserAccountResponse.from(accountDto));
            return mav;
        }
        catch (Exception e){
            ModelAndView mav = new ModelAndView();
            mav.setViewName("redirect:/");
            return mav;
        }
    }
    @GetMapping("/accounts/{username}")
    public ResponseEntity<?> getProfileImg (@PathVariable String username) throws IOException {
        try {
            File profileImg = FileUtil.getFileFromSaveFile(userService.getUserAccount(username).profileImg());
            byte[] imageByteArray = IOUtils.toByteArray(new FileInputStream(profileImg));
            return new ResponseEntity<>(imageByteArray, HttpStatus.OK);
        }
        catch (EntityNotFoundException e){
            return new ResponseEntity<>(ErrorMessages.ENTITY_NOT_FOUND,HttpStatus.NOT_FOUND);
        }
    }



    @PutMapping("/accounts")
    public ResponseEntity<?> updateMyAccount(@AuthenticationPrincipal UserAccount.BoardPrincipal principal
            ,@Valid @RequestBody UserAccount.UserAccountUpdateRequestDto dto, BindingResult bindingResult) {
        try {
            if (dto.password1() != null && !dto.password1().equals(dto.password2())) {
                bindingResult.addError(new FieldError("dto", "password2", "비밀번호가 일치하지 않습니다."));
                return new ResponseEntity<>(ControllerUtil.getErrors(bindingResult), HttpStatus.BAD_REQUEST);
            } else if (principal == null) {
                return new ResponseEntity<>(ErrorMessages.NOT_ACCEPTABLE, HttpStatus.UNAUTHORIZED);
            } else if (bindingResult.hasErrors()) {
                return new ResponseEntity<>(ControllerUtil.getErrors(bindingResult), HttpStatus.BAD_REQUEST);
            }
            userService.updateUserAccount(principal.username(), dto);
            return new ResponseEntity<>("수정되었습니다.", HttpStatus.OK);
        }
        catch (Exception e){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/accounts/idCheck")
    public ResponseEntity<String> isUserExists(@RequestParam("userId") String userId) {
        if (userService.isUserExists(userId)) {
            return new ResponseEntity<>("true", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("false",HttpStatus.BAD_REQUEST);
        }
    }
    @PostMapping("/accounts/emailCheck")
    public ResponseEntity<String> isEmailExists(@RequestParam("email") String email) {
        if (userService.isExistEmail(email)) {
            return new ResponseEntity<>("true", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("false",HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/accounts/nicknameCheck")
    public ResponseEntity<String> isNicknameExists(@RequestParam("nickname") String nickname) {
        if (userService.isExistNickname(nickname)) {
            return new ResponseEntity<>("true", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("false",HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestPart("signupDto") @Valid UserAccount.SignupDto signupDto, BindingResult bindingResult
    ,@RequestPart(value = "imgFile",required = false) MultipartFile imgFile) {
        try {
            if (bindingResult.hasErrors()) {
                return new ResponseEntity<>(ControllerUtil.getErrors(bindingResult), HttpStatus.BAD_REQUEST);
            } else if (!signupDto.getPassword1().equals(signupDto.getPassword2())) {
                bindingResult.addError(new FieldError("userCreateForm", "password2", "비밀번호가 일치하지 않습니다."));
                return new ResponseEntity<>(ControllerUtil.getErrors(bindingResult), HttpStatus.BAD_REQUEST);
            }
            if (imgFile == null) {
                userService.saveUserAccountWithoutProfile(signupDto,saveFileService.getFileByFileName("default.jpg"));
            } else {
                userService.saveUserAccount(signupDto, saveFileService.saveFile(FileUtil.getFileDtoFromMultiPartFile(imgFile, signupDto.getUserId())));
            }
        }
        catch (IOException e){
            return new ResponseEntity<>(ErrorMessages.ENTITY_NOT_VALID, HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>("success", HttpStatus.OK);
    }
    @PutMapping("/accounts/{id}")
    public ResponseEntity<?> changeProfileImg(@PathVariable String id, @RequestPart(value="imgFile",required = true)  MultipartFile imgFile) {
        if (imgFile == null) {
            return new ResponseEntity<>(ErrorMessages.NOT_FOUND, HttpStatus.BAD_REQUEST);
        }
        try {
            SaveFile.SaveFileDto saveFileDto = saveFileService.saveFile(FileUtil.getFileDtoFromMultiPartFile(imgFile,id));
            saveFileService.deleteFile(userService.changeAccountProfileImg(id, saveFileDto).id());
        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity<>(ErrorMessages.ENTITY_NOT_VALID, HttpStatus.BAD_REQUEST);
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
                                HttpServletResponse res, ModelMap map)  {

        try {
            if (!bindingResult.hasErrors()) {
                createTokenCookies(userService.loginByUserNameAndPassword(user.getUsername(), user.getPassword()), res);
                return new ResponseEntity<>(map.getAttribute("prevPage"), HttpStatus.OK);
            }
            else{
                return new ResponseEntity<>(ControllerUtil.getErrors(bindingResult), HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            bindingResult.addError(new FieldError("dto", "username", "아이디 또는 비밀번호가 일치하지 않습니다."));
            return new ResponseEntity<>(ControllerUtil.getErrors(bindingResult), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/logout")
    public ResponseEntity<String> logout(
             HttpServletRequest req,
            HttpServletResponse res) {
        try {
            deleteTokenCookies(res, req);
        }
        catch (RuntimeException e){
            return new ResponseEntity<>(ErrorMessages.BAD_REQUEST, HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>("로그아웃 되었습니다.", HttpStatus.OK);
    }

    public void createTokenCookies(UserAccount.BoardPrincipal principal, HttpServletResponse res) {
        Cookie accessToken = CookieUtil.createCookie(TokenProvider.ACCESS_TOKEN_NAME, tokenProvider.generateToken(principal));
        accessToken.setMaxAge((int) TimeUnit.MILLISECONDS.toSeconds(TokenProvider.REFRESH_TOKEN_VALIDATION_SECOND));
        Cookie refreshToken = CookieUtil.createCookie(TokenProvider.REFRESH_TOKEN_NAME, tokenProvider.generateRefreshToken(principal));
        refreshToken.setMaxAge((int) TimeUnit.MILLISECONDS.toSeconds(TokenProvider.REFRESH_TOKEN_VALIDATION_SECOND));
        redisUtil.setDataExpire(refreshToken.getValue(), principal.getUsername(), TokenProvider.REFRESH_TOKEN_VALIDATION_SECOND);
        res.addCookie(accessToken);
        res.addCookie(refreshToken);
    }
    public void deleteTokenCookies(HttpServletResponse res, HttpServletRequest req) {
        SecurityContextHolder.clearContext();
        Cookie accessToken = CookieUtil.getCookie(req, TokenProvider.ACCESS_TOKEN_NAME);
        Cookie refreshToken = CookieUtil.getCookie(req, TokenProvider.REFRESH_TOKEN_NAME);
        if (accessToken != null) {
            Long expiration = tokenProvider.getExpireTime(accessToken.getValue());
            redisUtil.setBlackList(accessToken.getValue(), "accessToken", expiration-System.currentTimeMillis());
            accessToken.setMaxAge(0);
            res.addCookie(accessToken);
        }
        if (refreshToken != null) {
            refreshToken.setMaxAge(0);
            res.addCookie(refreshToken);
            redisUtil.deleteData(refreshToken.getValue());
        }
        else{
            throw new RuntimeException();
        }
    }
}
