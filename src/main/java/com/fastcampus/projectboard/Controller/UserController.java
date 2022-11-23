package com.fastcampus.projectboard.Controller;

import com.fastcampus.projectboard.Service.UserSecurityService;
import com.fastcampus.projectboard.Service.UserService;
import com.fastcampus.projectboard.Util.ControllerUtil;
import com.fastcampus.projectboard.Util.CookieUtil;
import com.fastcampus.projectboard.Util.RedisUtil;
import com.fastcampus.projectboard.Util.TokenProvider;
import com.fastcampus.projectboard.domain.UserAccount;
import com.fastcampus.projectboard.domain.UserAccountRole;

import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
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

    @GetMapping("/accounts/articles")
    public ResponseEntity<?> getMyArticles(@AuthenticationPrincipal UserAccount.BoardPrincipal principal) {
        return new ResponseEntity<>(userService.getMyArticles(principal.username()), HttpStatus.OK);
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
    public ResponseEntity<?> signup(@RequestBody @Valid UserAccount.SignupDto userCreateForm, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return new ResponseEntity<>(controllerUtil.getErrors(bindingResult), HttpStatus.BAD_REQUEST);
        }
        Set<UserAccountRole> roles= new HashSet<>();
        roles.add(UserAccountRole.ROLE_USER);
        roles.add(UserAccountRole.ROLE_ADMIN);
        UserAccount.UserAccountDto userAccountDto = UserAccount.UserAccountDto.of(userCreateForm.getUserId(), userCreateForm.getPassword1(),userCreateForm.getEmail() , userCreateForm.getNickname(), userCreateForm.getMemo(), roles);
        userService.saveUserAccount(userAccountDto);
        return new ResponseEntity<>("success", HttpStatus.OK);
    }

    @GetMapping("/login")
    public ModelAndView login(UserAccount.LoginDto dto, HttpServletRequest req, HttpServletResponse res) {
        String referer = req.getHeader("Referer");
        req.getSession().setAttribute("prevPage", referer);
        ModelAndView mav = new ModelAndView();
        mav.setViewName("user/login_form");
        mav.addObject("dto", dto);
        return mav;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@ModelAttribute("dto") @RequestBody @Valid UserAccount.LoginDto user, BindingResult bindingResult,
                                HttpServletResponse res, HttpServletRequest req) throws IOException {

        try {
            if (!bindingResult.hasErrors()) {
                final UserAccount.BoardPrincipal principal = userService.loginUser(user.getUsername(), user.getPassword());
                Cookie accessToken = cookieUtil.createCookie(TokenProvider.ACCESS_TOKEN_NAME, tokenProvider.generateToken(principal));
                accessToken.setMaxAge((int) TimeUnit.MILLISECONDS.toSeconds(TokenProvider.TOKEN_VALIDATION_SECOND));
                Cookie refreshToken = cookieUtil.createCookie(TokenProvider.REFRESH_TOKEN_NAME, tokenProvider.generateRefreshToken(principal));
                refreshToken.setMaxAge((int) TimeUnit.MILLISECONDS.toSeconds(TokenProvider.REFRESH_TOKEN_VALIDATION_SECOND));
                redisUtil.setDataExpire(refreshToken.getValue(), principal.getUsername(), TokenProvider.REFRESH_TOKEN_VALIDATION_SECOND);
                res.addCookie(accessToken);
                res.addCookie(refreshToken);
                return new ResponseEntity<>(req.getSession().getAttribute("prevPage").toString(), HttpStatus.OK);
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
