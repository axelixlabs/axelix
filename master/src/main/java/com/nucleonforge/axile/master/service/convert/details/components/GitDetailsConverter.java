package com.nucleonforge.axile.master.service.convert.details.components;

import org.jspecify.annotations.NonNull;

import org.springframework.stereotype.Service;

import com.nucleonforge.axile.common.api.details.components.GitDetails;
import com.nucleonforge.axile.master.api.response.details.components.GitProfile;
import com.nucleonforge.axile.master.service.convert.Converter;

/**
 * The {@link Converter} from {@link GitDetails} to {@link GitProfile}.
 *
 * @author Sergey Cherkasov
 */
@Service
public class GitDetailsConverter implements Converter<GitDetails, GitProfile> {

    @Override
    public @NonNull GitProfile convertInternal(@NonNull GitDetails source) {
        return new GitProfile(
                source.commitShaShort(),
                source.branch(),
                source.commitAuthor().name(),
                source.commitAuthor().email(),
                source.commitTimestamp());
    }
}
