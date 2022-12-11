package com.fastcampus.projectboard.domain;

import lombok.*;

import javax.persistence.*;
import java.util.*;

@Entity
@Getter
@ToString(callSuper = true)
@Builder
public class Hashtag{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @Column(nullable = true, unique = true)
    private String hashtag;

    @OneToMany(mappedBy = "hashtag")
    @ToString.Exclude
    private Set<ArticleHashtag> articles = new HashSet<>();

    protected Hashtag() {
    }

    public Hashtag(Long id,String hashtag, Set<ArticleHashtag> articles) {
        this.id = id;
        this.hashtag = hashtag;
        this.articles = articles;
    }
    public Hashtag(String hashtag) {
        this.hashtag = hashtag;
    }

    public static Hashtag of(String hashtag) {
        return new Hashtag(hashtag);
    }

    public static Hashtag of(Long id, String hashtag) {
        return new Hashtag(id, hashtag,null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Hashtag hashtag)) return false;
        return id!=null && id.equals(hashtag.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    @Builder
    public record HashtagDto(Long id, String hashtag) {

        public static HashtagDto of(String hashtag) {
            return new HashtagDto(null, hashtag);
        }

        public static HashtagDto of(Long id, String hashtag) {
            return new HashtagDto(id, hashtag);
        }

        public static HashtagDto from(Hashtag entity) {
            return new HashtagDto(
                    entity.getId(),
                    entity.getHashtag()
            );
        }

        public static List<HashtagDto> from(String hashtag){
            List<Hashtag.HashtagDto> hashtagDtos =new ArrayList<>();
            if(hashtag!=null) {
                if (hashtag.contains("#")) {
                    String newHashtag = hashtag.replaceAll(" ", "");
                    StringTokenizer st = new StringTokenizer(newHashtag, "#");
                    while (st.hasMoreTokens()) {
                        hashtagDtos.add(Hashtag.HashtagDto.of(st.nextToken()));
                    }
                }
            }
            return hashtagDtos;
        }


        public Hashtag toEntity() {
            return Hashtag.of(
                    id,
                    hashtag
            );
        }
    }
}
