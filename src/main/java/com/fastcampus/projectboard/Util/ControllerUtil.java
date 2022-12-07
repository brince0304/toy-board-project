package com.fastcampus.projectboard.Util;

import com.fastcampus.projectboard.domain.Hashtag;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


public class ControllerUtil {

    private ControllerUtil() {
    }
    public static  String getIp(HttpServletRequest request){
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    public static String hashtagsToString(Set<Hashtag.HashtagDto> dto){
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < dto.size(); i++) {
            if (i != dto.size() - 1) {
                sb.append("#").append(dto.stream().toList().get(i).hashtag()).append(" ");
            } else {
                sb.append("#").append(dto.stream().toList().get(i).hashtag());
            }
        }
        return  sb.toString();
    }

    public static Set<ErrorDto> getErrors(BindingResult bindingResult){
        return bindingResult.getFieldErrors().stream().map(e->new ErrorDto(e.getField(),e.getDefaultMessage())).collect(Collectors.toSet());
    }
    @Getter
    @Setter
    @NoArgsConstructor
    public static class ErrorDto implements Serializable{
        private String field;
        private String message;

        public ErrorDto(String field, String defaultMessage) {
            this.field = field;
            this.message = defaultMessage;
        }

        public static ErrorDto of(FieldError fieldError){
            return new ErrorDto(fieldError.getField(),fieldError.getDefaultMessage());
        }


    }
}
