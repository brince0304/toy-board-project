package com.fastcampus.projectboard.Controller;

import com.fastcampus.projectboard.Service.UserSecurityService;
import com.fastcampus.projectboard.Service.UserService;
import com.fastcampus.projectboard.Util.CookieUtil;
import com.fastcampus.projectboard.Util.RedisUtil;
import com.fastcampus.projectboard.Util.TokenProvider;
import com.fastcampus.projectboard.domain.UserAccount;
import com.fastcampus.projectboard.domain.UserAccountRole;
import com.fastcampus.projectboard.domain.forms.UserCreateForm;
import com.fastcampus.projectboard.dto.LoginDto;
import com.fastcampus.projectboard.dto.UserAccountDto;
import com.fastcampus.projectboard.dto.security.BoardPrincipal;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Controller
@RequestMapping
public class UserController {
    private final UserService userService;
    private final TokenProvider tokenProvider;
    private final CookieUtil cookieUtil;
    private final RedisUtil redisUtil;
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @GetMapping("/signup")
    public String signup(UserCreateForm userCreateForm) {
        return "user/signup_form";
    }

    @PostMapping("/signup")
    public String signup(@Valid UserCreateForm userCreateForm, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "user/signup_form";
        }
        if (userService.isExistUser(userCreateForm.getUserId())) {
            bindingResult.rejectValue("userId", "error.userCreateForm", "* 이미 존재하는 아이디입니다.");
            return "user/signup_form";
        }
        if (userService.isExistNickname(userCreateForm.getNickname())) {
            bindingResult.rejectValue("nickname", "error.userCreateForm", "* 이미 존재하는 닉네임입니다.");
            return "user/signup_form";
        }
        if (userService.isExistEmail(userCreateForm.getEmail())) {
            bindingResult.rejectValue("email", "error.userCreateForm", "* 중복 이메일입니다.");
            return "user/signup_form";
        }

        if (!userCreateForm.getPassword1().equals(userCreateForm.getPassword2())) {
            bindingResult.rejectValue("password2", "passwordInCorrect",
                    "패스워드가 일치하지 않습니다.");
            return "user/signup_form";
        }
        Set<UserAccountRole> roles= new HashSet<>();
        roles.add(UserAccountRole.ROLE_USER);
        roles.add(UserAccountRole.ROLE_ADMIN);
        UserAccountDto userAccountDto = UserAccountDto.of(userCreateForm.getUserId(), userCreateForm.getPassword1(),userCreateForm.getEmail() , userCreateForm.getNickname(), userCreateForm.getMemo(), roles);
        userService.saveUserAccount(userAccountDto);

        return "redirect:/";
    }

    @GetMapping("/login")
    public String login() {
        return "/user/login_form";
    }

    @PostMapping("/login")
    public String login(LoginDto user,
                        HttpServletRequest req,
                        HttpServletResponse res) {
        try {
            final BoardPrincipal principal = userService.loginUser(user.getUsername(), user.getPassword());
            Cookie accessToken = cookieUtil.createCookie(TokenProvider.ACCESS_TOKEN_NAME, tokenProvider.generateToken(principal));
            accessToken.setMaxAge((int) TimeUnit.MILLISECONDS.toSeconds(TokenProvider.TOKEN_VALIDATION_SECOND));
            Cookie refreshToken = cookieUtil.createCookie(TokenProvider.REFRESH_TOKEN_NAME, tokenProvider.generateRefreshToken(principal));
            refreshToken.setMaxAge((int) TimeUnit.MILLISECONDS.toSeconds(TokenProvider.REFRESH_TOKEN_VALIDATION_SECOND));
            redisUtil.setDataExpire(refreshToken.getValue(), principal.getUsername(), TokenProvider.REFRESH_TOKEN_VALIDATION_SECOND);
            res.addCookie(accessToken);
            res.addCookie(refreshToken);
            return "redirect:/";
        } catch (Exception e) {
            log.error("login error", e);
            return "redirect:/login";
        }
    }

    @GetMapping("/logout")
    public String logout(
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
        if (refreshToken != null) {
            refreshToken.setMaxAge(0);
            res.addCookie(refreshToken);
            redisUtil.deleteData(refreshToken.getValue());
        }


        return "redirect:/";
    }
}
