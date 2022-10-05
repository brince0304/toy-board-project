package com.fastcampus.projectboard.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

@Getter //setter는 따로 생성
@ToString
@Table(indexes = {
        @Index(columnList= "title"),
        @Index(columnList= "hashtag"),
        @Index(columnList= "createdAt"),
        @Index(columnList= "createdBy")
})  //테이블 컬럼 설정
@EntityListeners(AuditingEntityListener.class) //시간에 대해서 자동으로 값을 넣어준다
@Entity
public class Article {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 프라이머리 키
    private Long id;
    //@setter 가 붙은 값이 입력값, 없으면 자동
    @Setter @Column(nullable = false) String title; //null 이 아닌 값을 컬럼에 저장 함
    @Setter @Column(nullable = false,length = 10000) private String content;

    @Setter private String hashtag;
    @OrderBy("id")  //id 순서
    @OneToMany(mappedBy = "article", cascade = CascadeType.ALL) //양방향 관계 (article이 주체)
    @ToString.Exclude //과부하 발생 예방
    private final Set<ArticleComment> articleComments = new LinkedHashSet<>();

    @CreatedDate @Column(nullable = false) LocalDateTime createdAt; //생성 시간 자동저장
    @CreatedBy @Column(nullable = false, length= 100) private String createdBy; //생성자 자동저장
    @LastModifiedDate @Column(nullable = false) LocalDateTime modifiedAt; //수정 시간 자동저장
    @LastModifiedBy @Column(nullable = false) String modifiedBy;  //수정자 자동 저장


    protected Article() { //기본 빈 생성자
    }

    private Article(String title, String content, String hashtag) {
        this.title = title;
        this.content = content;
        this.hashtag = hashtag;
    }
    public static Article of(String title, String content, String hashtag) { //게시글 제목 내용 해시태그
        return new Article(title,content,hashtag);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Article article)) return false;
        return id != null && id.equals(article.id); //id가 존재할때
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
