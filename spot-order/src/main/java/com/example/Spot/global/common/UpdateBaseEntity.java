package com.example.Spot.global.common;

import java.time.LocalDateTime;

import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public abstract class UpdateBaseEntity extends BaseEntity {

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by")
    private Integer updatedBy;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "deleted_by")
    private Integer deletedBy;

    protected UpdateBaseEntity(Integer createdBy) {
        super(createdBy);
        this.isDeleted = false;
    }

    public boolean getIsDeleted() {
        return isDeleted;
    }

    public void softDelete(Integer deletedBy) {
        this.isDeleted = true;
        this.deletedBy = deletedBy;
    }

    public void restore() {
        this.isDeleted = false;
        this.deletedBy = null;
    }

    public void updateBy(Integer updatedBy) {
        this.updatedBy = updatedBy;
    }
}
