package com.fastcampus.projectboard.domain.forms;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

@Getter
@Setter
public class ArticleForm {
    @NotEmpty(message = "* 제목을 입력해주세요.")
    private String title;

    @Size(min = 5, message = "* 내용은 5자 이상 입력해주세요.")
    @Size(max = 10000, message = "* 내용은 10000자 이하로 입력해주세요.")
    private String content;


    private String hashtag;


}
