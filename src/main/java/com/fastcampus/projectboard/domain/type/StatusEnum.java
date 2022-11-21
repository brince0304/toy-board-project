package com.fastcampus.projectboard.domain.type;

public enum StatusEnum {
    OK(200, "OK"),
    BAD_REQUEST(400, "BAD REQUEST"),
    NOT_FOUND(404, "NOT FOUND"),
    INTERNAL_SERVER_ERROR(500, "INTERNAL SERVER ERROR");

    final int code;
    final String message;

    StatusEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
