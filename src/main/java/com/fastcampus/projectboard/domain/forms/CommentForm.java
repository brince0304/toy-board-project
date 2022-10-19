package com.fastcampus.projectboard.domain.forms;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

public class CommentForm {
    @NotEmpty(message = "* 댓글을 입력해주세요.")
    @Size(max = 100, message = "* 댓글은 100자 이하로 작성해주세요.")
    private String content;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
