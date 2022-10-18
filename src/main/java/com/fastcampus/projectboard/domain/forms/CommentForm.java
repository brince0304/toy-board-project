package com.fastcampus.projectboard.domain.forms;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

@Getter
@Setter
public class CommentForm {
    @Size(min= 1, max = 100, message = "* 댓글은 1자 이상 100자 이하로 작성해주세요.")
    private String content;

    private Long articleId;

    private String userId;
    private String nickname;



}
