package com.example.spotstore.review.domain.entity;

import java.util.UUID;

import com.example.Spot.global.common.UpdateBaseEntity;
import com.example.Spot.store.domain.entity.StoreEntity;
import com.example.Spot.user.domain.entity.UserEntity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "p_review")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReviewEntity extends UpdateBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private StoreEntity store;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(nullable = false)
    private Integer rating; // 1-5 별점

    @Column(columnDefinition = "TEXT")
    private String content; // 리뷰 내용

    @Builder
    public ReviewEntity(
            StoreEntity store,
            UserEntity user,
            Integer rating,
            String content,
            Integer createdBy
    ) {
        super(createdBy);
        this.store = store;
        this.user = user;
        this.rating = rating;
        this.content = content;
    }

    public void updateReview(Integer rating, String content, Integer updatedBy) {
        if (rating != null && rating >= 1 && rating <= 5) {
            this.rating = rating;
        }
        if (content != null) {
            this.content = content;
        }
        this.updateBy(updatedBy);
    }

    public void validateRating() {
        if (this.rating < 1 || this.rating > 5) {
            throw new IllegalArgumentException("별점은 1-5 사이여야 합니다.");
        }
    }
}
