package com.fastcampus.projectboard.dto;

import java.io.Serializable;

/**
 * A DTO for the {@link com.fastcampus.projectboard.domain.Hashtag} entity
 */
public record HashtagDto(Long id, String hashtag) {

        public static HashtagDto of(String hashtag) {
            return new HashtagDto(null, hashtag);
        }

        public static HashtagDto of(Long id, String hashtag) {
            return new HashtagDto(id, hashtag);
        }

        public static HashtagDto from(com.fastcampus.projectboard.domain.Hashtag entity) {
            return new HashtagDto(
                    entity.getId(),
                    entity.getHashtag()
            );
        }

        public com.fastcampus.projectboard.domain.Hashtag toEntity() {
            return com.fastcampus.projectboard.domain.Hashtag.of(
                    id,
                    hashtag
            );
        }
}