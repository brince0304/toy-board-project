package com.fastcampus.projectboard.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

@Getter //게터
@ToString //
@Table(indexes = {
        @Index(columnList= "content"),
        @Index(columnList= "createdAt"),
        @Index(columnList= "createdBy")
})
@Entity
public class ArticleComment extends AuditingFields{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Setter
    @JoinColumn(name = "userId")
    @ManyToOne(optional = false)
    private UserAccount userAccount; // 유저 정보 (ID)
    @Setter @ManyToOne(optional = false) private Article article;
    @Setter @Column(nullable = false,length= 500) private String content;

    @Setter
    @Column(nullable = false)
    private String deleted = "N";
    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private ArticleComment parent;

    @Setter
    @OneToMany(mappedBy = "parent")
    @ToString.Exclude
    private Set<ArticleComment> children = new LinkedHashSet<>();

    @Setter
    private String isParent;


    protected ArticleComment() {
    }

    public ArticleComment(UserAccount userAccount, Article article, String content, ArticleComment parent) {
        this.userAccount = userAccount;
        this.article = article;
        this.content = content;
        this.parent = parent;
    }

    public static  ArticleComment of(Article article, UserAccount userAccount,String content) {
        return new ArticleComment(userAccount,article,content,null);
    }

    public static  ArticleComment of(Article article, UserAccount userAccount,String content,ArticleComment parent) {
        return new ArticleComment(userAccount,article,content,parent);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ArticleComment that)) return false;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
