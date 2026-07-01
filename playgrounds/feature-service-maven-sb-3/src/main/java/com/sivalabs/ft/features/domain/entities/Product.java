package com.sivalabs.ft.features.domain.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "products_id_gen")
    @SequenceGenerator(name = "products_id_gen", sequenceName = "product_id_seq")
    @Column(name = "id", nullable = false)
    private Long id;

    @Size(max = 50) @NotNull @Column(name = "code", nullable = false, length = 50)
    private String code;

    @Size(max = 50) @NotNull @Column(name = "prefix", nullable = false, length = 10)
    private String prefix;

    @Size(max = 255) @NotNull @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", length = Integer.MAX_VALUE)
    private String description;

    @Column(name = "image_url")
    private String imageUrl;

    @NotNull @ColumnDefault("false")
    @Column(name = "disabled", nullable = false)
    private Boolean disabled = false;

    @Size(max = 255) @NotNull @Column(name = "created_by", nullable = false)
    private String createdBy;

    @NotNull @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Size(max = 255) @Column(name = "updated_by")
    private String updatedBy;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @OneToMany(mappedBy = "product")
    private Set<Release> releases = new LinkedHashSet<>();

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            this.createdAt = Instant.now();
        }
    }

    @PreUpdate
    public void preUpdate() {
        if (updatedAt == null) {
            this.updatedAt = Instant.now();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Boolean getDisabled() {
        return disabled;
    }

    public void setDisabled(Boolean disabled) {
        this.disabled = disabled;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Set<Release> getReleases() {
        return releases;
    }

    public void setReleases(Set<Release> releases) {
        this.releases = releases;
    }
}
