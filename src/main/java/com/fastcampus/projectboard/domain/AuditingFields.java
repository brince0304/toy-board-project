package com.fastcampus.projectboard.domain;

import lombok.Getter;
import lombok.ToString;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.Column;
import javax.persistence.EntityListeners;
import javax.persistence.MappedSuperclass;
import java.time.LocalDateTime;

@Getter
@ToString
@EntityListeners(AuditingEntityListener.class) //시간에 대해서 자동으로 값을 넣어준다
@MappedSuperclass //추출
public class AuditingFields {

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @CreatedDate
    @Column(nullable = false, updatable = false) // 최초 한번만 값을 넣으므로 업데이터블 false
    LocalDateTime createdAt; //생성 시간 자동저장

    @CreatedBy
    @Column(nullable = false, updatable = false,length= 100) //마찬가지로 수정 불가
    private String createdBy; //생성자 자동저장

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @LastModifiedDate
    @Column(nullable = false)
    LocalDateTime modifiedAt; //수정 시간 자동저장

    @LastModifiedBy
    @Column(nullable = false)
    String modifiedBy;  //수정자 자동 저장

}
