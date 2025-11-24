package com.nucleonforge.axile.master.service.convert;

import org.junit.jupiter.api.Test;

import com.nucleonforge.axile.common.api.ProfileMutationResult;
import com.nucleonforge.axile.master.api.response.ProfileUpdateResponse;
import com.nucleonforge.axile.master.service.convert.response.ProfileMutationResultConverter;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link ProfileMutationResultConverter}
 *
 * @since 25.09.2025
 * @author Nikita Kirillov
 */
class ProfileMutationResultConverterTest {

    private final ProfileMutationResultConverter subject = new ProfileMutationResultConverter();

    @Test
    void testConvertHappyPath() {
        ProfileMutationResult profileMutationResult =
                new ProfileMutationResult(true, "New profiles have been activated");

        // when.
        ProfileUpdateResponse response = subject.convertInternal(profileMutationResult);

        // then.
        assertThat(response.updated()).isTrue();
        assertThat(response.reason()).isEqualTo("New profiles have been activated");
    }
}
