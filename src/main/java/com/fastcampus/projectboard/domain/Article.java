package com.fastcampus.projectboard.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

@Getter //setter는 따로 생성 ( 자동으로 설정되는 값이 있기 때문 )
@ToString(callSuper = true)
@Table(indexes = {
        @Index(columnList= "title"),
        @Index(columnList= "hashtag"),
        @Index(columnList= "createdAt"),
        @Index(columnList= "createdBy")
})  //테이블 컬럼 인덱스 설정
 // 인덱스 이름이 없으니 키 부여
@Entity
public class Article extends AuditingFields{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 프라이머리 키
    private Long id;
    //@setter 가 붙은 값이 입력값, 없으면 자동
    @Setter @Column(nullable = false) String title; //null 이 아닌 값을 컬럼에 저장 함
    @Setter
    @JoinColumn(name = "userId")
    @ManyToOne(optional = false)
    private UserAccount userAccount; // 유저 정보 (ID)
    @Setter @Column(nullable = false,length = 10000) private String content;

    @Setter private String hashtag;

    @OrderBy("createdAt DESC")  //id 순서
    @OneToMany(mappedBy = "article", cascade = CascadeType.ALL) //양방향 관계 (article이 주체)
    @ToString.Exclude //과부하 발생 예방
    private final Set<ArticleComment> articleComments = new LinkedHashSet<>();

    @ToString.Exclude
    @JoinTable(
            name = "article_hashtag",
            joinColumns = @JoinColumn(name = "article_Id"),
            inverseJoinColumns = @JoinColumn(name = "hashtag_Id")
    )
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private Set<Hashtag> hashtags = new LinkedHashSet<>();



    protected Article() { //기본 빈 생성자
    }

    private Article(UserAccount userAccount,String title, String content, Set<Hashtag> hashtags) {
        this.userAccount = userAccount;
        this.title = title;
        this.content = content;
        this.hashtags = hashtags;
    }
    public static Article of(UserAccount userAccount,String title, String content, Set<Hashtag> hashtags) { //게시글 제목 내용 해시태그
        return new Article(userAccount,title,content,hashtags);
    }

    public static Article of(UserAccount userAccount,String title, String content) { //게시글 제목 내용
        return new Article(userAccount,title,content,null);
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
