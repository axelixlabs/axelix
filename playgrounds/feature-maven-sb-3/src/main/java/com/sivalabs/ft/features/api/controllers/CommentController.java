package com.sivalabs.ft.features.api.controllers;

import com.sivalabs.ft.features.api.models.AddCommentPayload;
import com.sivalabs.ft.features.api.utils.SecurityUtils;
import com.sivalabs.ft.features.domain.Commands.CreateCommentCommand;
import com.sivalabs.ft.features.domain.CommentService;
import com.sivalabs.ft.features.domain.dtos.CommentDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/comments")
@Tag(name = "Comments API")
class CommentController {
    private static final Logger log = LoggerFactory.getLogger(CommentController.class);
    private final CommentService commentService;

    CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping
    @Operation(
            summary = "Add a comment",
            description = "Add a comment to a feature",
            responses = {
                @ApiResponse(
                        responseCode = "201",
                        description = "Comment added successfully",
                        headers =
                                @Header(
                                        name = "Location",
                                        required = true,
                                        description = "URI of the created comment")),
                @ApiResponse(responseCode = "400", description = "Invalid request"),
            })
    ResponseEntity<String> addComment(@RequestBody @Valid AddCommentPayload addCommentPayload) {
        String username = SecurityUtils.getCurrentUsername();
        var command = new CreateCommentCommand(addCommentPayload.featureCode(), addCommentPayload.content(), username);
        var commentId = commentService.createComment(command);

        log.info("Comment added with id: {}", commentId);
        return ResponseEntity.created(ServletUriComponentsBuilder.fromCurrentRequest()
                        .path("/{commentId}")
                        .buildAndExpand(commentId)
                        .toUri())
                .build();
    }

    @DeleteMapping("/{commentId}")
    @Operation(
            summary = "Remove a comment",
            description = "Remove a comment by its id",
            responses = {
                @ApiResponse(responseCode = "204", description = "Comment removed successfully"),
                @ApiResponse(responseCode = "400", description = "Comment not found")
            })
    ResponseEntity<Void> removeComment(@PathVariable Long commentId) {
        String username = SecurityUtils.getCurrentUsername();
        commentService.removeComment(commentId, username);
        log.info("Comment with id: {} is removed", commentId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @Operation(
            summary = "Get comments by feature code",
            description = "Retrieve comments for a specific feature",
            responses = {
                @ApiResponse(responseCode = "200", description = "Comments retrieved successfully"),
                @ApiResponse(responseCode = "404", description = "Feature not found")
            })
    ResponseEntity<List<CommentDto>> getCommentsByFeatureCode(
            @RequestParam String featureCode,
            @RequestParam(defaultValue = "0", required = false) int page,
            @RequestParam(defaultValue = "10", required = false) int size) {
        List<CommentDto> comments = commentService.findCommentsByFeatureCode(featureCode, page, size);
        log.info("Retrieved {} comments for feature code: {}", comments.size(), featureCode);
        return ResponseEntity.ok(comments);
    }
}
