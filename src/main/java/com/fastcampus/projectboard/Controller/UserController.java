package com.fastcampus.projectboard.Controller;

import com.fastcampus.projectboard.Service.UserService;
import com.fastcampus.projectboard.domain.UserAccount;
import com.fastcampus.projectboard.domain.forms.UserCreateForm;
import com.fastcampus.projectboard.dto.UserAccountDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.validation.Valid;

@RequiredArgsConstructor
@Controller
@RequestMapping
public class UserController {
    private final UserService userService;

    @GetMapping("/signup")
    public String signup(UserCreateForm userCreateForm) {
        return "user/signup_form";
    }

    @PostMapping("/signup")
    public String signup (@Valid UserCreateForm userCreateForm, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "user/signup_form";
        }
        if(userService.isExistUser(userCreateForm.getUserId())){
            bindingResult.rejectValue("userId","error.userCreateForm","* 이미 존재하는 아이디입니다.");
            return "user/signup_form";
        }
        if(userService.isExistNickname(userCreateForm.getNickname())){
            bindingResult.rejectValue("nickname","error.userCreateForm","* 이미 존재하는 닉네임입니다.");
            return "user/signup_form";
        }
        if(userService.isExistEmail(userCreateForm.getEmail())){
            bindingResult.rejectValue("email","error.userCreateForm","* 중복 이메일입니다.");
            return "user/signup_form";
        }

        if (!userCreateForm.getPassword1().equals(userCreateForm.getPassword2())) {
            bindingResult.rejectValue("password2", "passwordInCorrect",
                    "패스워드가 일치하지 않습니다.");
            return "user/signup_form";
        }
        UserAccount account = UserAccount.of(userCreateForm.getUserId(), userCreateForm.getPassword1(),
                userCreateForm.getEmail(),userCreateForm.getNickname(),userCreateForm.getMemo());
        userService.saveUserAccount(UserAccountDto.from(account));

        return "redirect:/";
    }
    @GetMapping("/login")
    public String login() {
        return "user/login_form";
    }



}
