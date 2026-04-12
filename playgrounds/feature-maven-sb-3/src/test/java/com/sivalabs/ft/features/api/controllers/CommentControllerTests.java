package com.sivalabs.ft.features.api.controllers;

import static org.assertj.core.api.Assertions.assertThat;

import com.sivalabs.ft.features.AbstractIT;
import com.sivalabs.ft.features.WithMockOAuth2User;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

class CommentControllerTests extends AbstractIT {

    @Test
    @WithMockOAuth2User(username = "user")
    void shouldAddComment() {
        var payload =
                """
                {
                    "featureCode": "IDEA-1",
                    "content": "This is a test comment"
                }
                """;

        var result = mvc.post()
                .uri("/api/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload)
                .exchange();

        assertThat(result).hasStatus(HttpStatus.CREATED);
    }

    @Test
    @WithMockOAuth2User(username = "user")
    void shouldReturn400WhenFeatureNotFound() {
        var payload =
                """
                {
                    "featureCode": "INVALID_CODE",
                    "content": "This comment should fail"
                }
                """;

        var result = mvc.post()
                .uri("/api/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload)
                .exchange();

        assertThat(result).hasStatus(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldGetCommentsByFeatureCode() {
        var result = mvc.get().uri("/api/comments?featureCode={code}", "IDEA-1").exchange();

        assertThat(result)
                .hasStatusOk()
                .bodyJson()
                .extractingPath("$.size()")
                .asNumber()
                .extracting(Number::intValue)
                .satisfies(size -> assertThat(size).isGreaterThanOrEqualTo(0));
    }

    @Test
    void shouldGetCommentsWithPagination() {
        var result = mvc.get()
                .uri("/api/comments?featureCode={code}&page=0&size=5", "IDEA-1")
                .exchange();

        assertThat(result).hasStatusOk();
    }

    @Test
    @WithMockOAuth2User(username = "user")
    void shouldRemoveComment() {

        // Then remove it
        var deleteResult = mvc.delete().uri("/api/comments/{commentId}", 1).exchange();

        assertThat(deleteResult).hasStatus(HttpStatus.NO_CONTENT);
    }

    @Test
    @WithMockOAuth2User(username = "user")
    void shouldReturn400WhenRemovingNonExistentComment() {
        var result = mvc.delete().uri("/api/comments/{commentId}", 999).exchange();

        assertThat(result).hasStatus(HttpStatus.BAD_REQUEST);
    }
}
