package com.sivalabs.ft.features.domain.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

@Entity
@Table(name = "favorite_features")
public class FavoriteFeature {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "favorite_features_id_gen")
    @SequenceGenerator(name = "favorite_features_id_gen", sequenceName = "favorite_features_id_seq")
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "feature_id", nullable = false)
    private Long featureId;

    @Column(name = "user_id", nullable = false)
    private String userId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getFeatureId() {
        return featureId;
    }

    public void setFeatureId(Long featureId) {
        this.featureId = featureId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public FavoriteFeature() {}

    public FavoriteFeature(Long featureId, String userId) {
        this.featureId = featureId;
        this.userId = userId;
    }
}
