package com.fastcampus.projectboard.domain;

import lombok.Getter;

@Getter
public enum UserAccountRole {
    ADMIN("ROLE_ADMIN"),
    USER("ROLE_USER"),

    ANONYMOUS("ROLE_ANONYMOUS");

    UserAccountRole(String value) {
        this.value = value;
    }

    private String value;
}
