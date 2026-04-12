package com.sivalabs.ft.features.domain;

import com.sivalabs.ft.features.domain.dtos.CommentDto;
import com.sivalabs.ft.features.domain.entities.Comment;
import com.sivalabs.ft.features.domain.exceptions.BadRequestException;
import com.sivalabs.ft.features.domain.exceptions.ResourceNotFoundException;
import com.sivalabs.ft.features.domain.mappers.CommentMapper;
import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CommentService {
    private final CommentRepository commentRepository;
    private final FeatureRepository featureRepository;
    private final CommentMapper commentMapper;

    CommentService(
            CommentRepository commentRepository, FeatureRepository featureRepository, CommentMapper commentMapper) {
        this.commentRepository = commentRepository;
        this.featureRepository = featureRepository;
        this.commentMapper = commentMapper;
    }

    @Transactional
    public Long createComment(Commands.CreateCommentCommand command) {
        var feature = featureRepository
                .findByCode(command.featureCode())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Feature with code %s not found.".formatted(command.featureCode())));

        Comment comment = new Comment();
        comment.setContent(command.content());
        comment.setFeature(feature);
        comment.setCreatedBy(command.createdBy());
        comment.setCreatedAt(Instant.now());
        commentRepository.save(comment);
        return comment.getId();
    }

    @Transactional
    public void removeComment(Long commentId, String userId) {
        int count = commentRepository.deleteComment(commentId, userId);
        if (count != 1) {
            throw new BadRequestException("comment not found");
        }
    }

    @Transactional(readOnly = true)
    public List<CommentDto> findCommentsByFeatureCode(String featureCode, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        List<Comment> comments = commentRepository.findCommentsByFeatureCode(featureCode, pageRequest);
        return comments.stream().map(commentMapper::toDto).toList();
    }
}
