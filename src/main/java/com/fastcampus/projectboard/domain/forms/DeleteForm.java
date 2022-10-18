package com.fastcampus.projectboard.domain.forms;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;

@Setter
@Getter
public class DeleteForm {
    private Long articleId;

    @NotEmpty(message = "비밀번호를 입력해주세요.")
    private String password;
}
