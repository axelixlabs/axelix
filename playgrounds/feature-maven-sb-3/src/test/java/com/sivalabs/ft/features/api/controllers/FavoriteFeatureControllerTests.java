package com.sivalabs.ft.features.api.controllers;

import static org.assertj.core.api.Assertions.assertThat;

import com.sivalabs.ft.features.AbstractIT;
import com.sivalabs.ft.features.WithMockOAuth2User;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class FavoriteFeatureControllerTests extends AbstractIT {

    @Test
    @WithMockOAuth2User(username = "user")
    void shouldAddFavoriteFeature() {
        var featureCode = "IDEA-1";
        var result = mvc.post()
                .uri("/api/features/{featureCode}/favorites", featureCode)
                .exchange();
        assertThat(result).hasStatus(HttpStatus.CREATED);
    }

    @Test
    @WithMockOAuth2User(username = "user")
    void shouldReturn400WhenAddingInvalidFeatureCode() {
        var invalidFeatureCode = "invalid_code";
        var result = mvc.post()
                .uri("/api/features/{featureCode}/favorites", invalidFeatureCode)
                .exchange();
        assertThat(result).hasStatus(HttpStatus.BAD_REQUEST);
    }

    @Test
    @WithMockOAuth2User(username = "user")
    void shouldRemoveFavoriteFeature() {
        var featureCode = "IDEA-1";
        // First, add to favorites
        mvc.post().uri("/api/features/{featureCode}/favorites", featureCode).exchange();

        // Then, remove from favorites
        var result = mvc.delete()
                .uri("/api/features/{featureId}/favorites", featureCode)
                .exchange();
        assertThat(result).hasStatus(HttpStatus.NO_CONTENT);
    }

    @Test
    @WithMockOAuth2User(username = "user")
    void shouldReturn400WhenRemovingInvalidFeatureCode() {
        var invalidFeatureCode = "invalid_code";
        var result = mvc.delete()
                .uri("/api/features/{featureCode}/favorites", invalidFeatureCode)
                .exchange();
        assertThat(result).hasStatus(HttpStatus.BAD_REQUEST);
    }

    @Test
    @WithMockOAuth2User(username = "user")
    void shouldReturn400WhenAddingAlreadyFavoriteFeature() {
        var featureCode = "IDEA-2"; // this is already added form script

        var result = mvc.post()
                .uri("/api/features/{featureCode}/favorites", featureCode)
                .exchange();
        assertThat(result).hasStatus(HttpStatus.BAD_REQUEST);
    }

    @Test
    @WithMockOAuth2User(username = "user")
    void shouldReturn400WhenAddingNonExistentFeature() {
        var nonExistentFeatureCode = "NON_EXISTENT_FEATURE";
        var result = mvc.post()
                .uri("/api/features/{featureCode}/favorites", nonExistentFeatureCode)
                .exchange();
        assertThat(result).hasStatus(HttpStatus.BAD_REQUEST);
    }
}
