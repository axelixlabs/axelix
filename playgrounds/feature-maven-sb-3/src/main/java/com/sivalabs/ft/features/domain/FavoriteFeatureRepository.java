package com.sivalabs.ft.features.domain;

import com.sivalabs.ft.features.domain.entities.FavoriteFeature;
import com.sivalabs.ft.features.domain.models.UserFavoriteFeature;
import java.util.List;
import java.util.Set;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

interface FavoriteFeatureRepository extends JpaRepository<FavoriteFeature, Long> {

    @Transactional(propagation = Propagation.REQUIRED)
    @CacheEvict(value = "favorites", allEntries = true)
    @Modifying
    @Query(
            """
            delete from FavoriteFeature ff where ff.userId = :userId and
            ff.featureId = (select f.id from Feature f where f.code = :featureCode)
            """)
    int deleteByUserIdAndFeatureCode(String userId, String featureCode);

    @Transactional(readOnly = true)
    boolean existsByUserIdAndFeatureId(String userId, long featureId);

    @Transactional(propagation = Propagation.REQUIRED)
    @CacheEvict(value = "favorites", allEntries = true)
    @Modifying
    @Query(
            """
            delete from FavoriteFeature ff
            where ff.featureId = (select f.id from Feature f where f.code = :featureCode)
            """)
    void deleteByFeatureCode(String featureCode);

    @Transactional(propagation = Propagation.REQUIRED)
    @Cacheable("favorites")
    @Query(
            nativeQuery = true,
            value =
                    """
            SELECT
                f.id,
                f.code,
                CASE WHEN ff.id IS NOT NULL THEN true ELSE false END AS isFavorited
            FROM
                features f
            LEFT JOIN
                favorite_features ff
                ON f.id = ff.feature_id AND ff.user_id = :userId
            WHERE
                f.code IN (:featureCodes);
            """)
    List<UserFavoriteFeature> findByUserIdAndFeatureCodes(
            @Param("userId") String userId, @Param("featureCodes") Set<String> featureCodes);
}
