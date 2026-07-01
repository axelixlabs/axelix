package com.sivalabs.ft.features.domain;

import com.sivalabs.ft.features.domain.entities.Comment;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

interface CommentRepository extends JpaRepository<Comment, Long> {

    @Modifying
    @Query("delete from Comment c where c.createdBy = :userId and c.id = :commentId")
    int deleteComment(Long commentId, String userId);

    @Query("""
            select c from Comment c where c.feature.code = :featureCode
            """)
    List<Comment> findCommentsByFeatureCode(String featureCode, PageRequest pageRequest);
}
