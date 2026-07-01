package com.sivalabs.ft.features.domain;

import com.sivalabs.ft.features.domain.entities.FavoriteFeature;
import com.sivalabs.ft.features.domain.entities.Feature;
import com.sivalabs.ft.features.domain.exceptions.BadRequestException;
import com.sivalabs.ft.features.domain.models.UserFavoriteFeature;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FavoriteFeatureService {
    private final FavoriteFeatureRepository favoriteFeatureRepository;
    private final FeatureRepository featureRepository;

    FavoriteFeatureService(FavoriteFeatureRepository favoriteFeatureRepository, FeatureRepository featureRepository) {
        this.favoriteFeatureRepository = favoriteFeatureRepository;
        this.featureRepository = featureRepository;
    }

    @Transactional(readOnly = true)
    public Map<String, Boolean> getFavoriteFeatures(String userId, Set<String> featureCodes) {
        List<UserFavoriteFeature> favoriteFeatures =
                this.favoriteFeatureRepository.findByUserIdAndFeatureCodes(userId, featureCodes);
        if (favoriteFeatures.isEmpty()) {
            return Map.of();
        }
        Map<String, Boolean> result = new HashMap<>();
        for (UserFavoriteFeature favoriteFeature : favoriteFeatures) {
            result.put(favoriteFeature.code(), favoriteFeature.isFavorite());
        }
        return result;
    }

    @Transactional
    public void addFavoriteFeature(String userId, String featureCode) {
        // Check if the feature exists
        final Feature feature = featureRepository
                .findByCode(featureCode)
                .orElseThrow(() -> new BadRequestException("Feature code is invalid: " + featureCode));

        // check if the favorite already exists
        if (favoriteFeatureRepository.existsByUserIdAndFeatureId(userId, feature.getId())) {
            throw new BadRequestException("Feature is already favorited by the user");
        }
        FavoriteFeature favoriteFeature = new FavoriteFeature(feature.getId(), userId);
        favoriteFeatureRepository.save(favoriteFeature);
    }

    @Transactional
    public void removeFavoriteFeature(String userId, String featureCode) {
        int count = favoriteFeatureRepository.deleteByUserIdAndFeatureCode(userId, featureCode);
        if (count != 1) {
            throw new BadRequestException("Feature is not favorited by the user to remove");
        }
    }
}
