package com.fastcampus.projectboard.domain.type;

import lombok.Getter;

@Getter
public enum UserAccountRole {
    ROLE_ADMIN("ROLE_ADMIN"),
    ROLE_USER("ROLE_USER"),

    ROLE_ANONYMOUS("ROLE_ANONYMOUS");

    UserAccountRole(String value) {
        this.value = value;
    }

    private String value;
}
