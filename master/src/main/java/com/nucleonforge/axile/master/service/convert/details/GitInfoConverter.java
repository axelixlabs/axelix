package com.nucleonforge.axile.master.service.convert.details;

import org.jspecify.annotations.NonNull;

import org.springframework.stereotype.Service;

import com.nucleonforge.axile.common.api.details.components.GitInfo;
import com.nucleonforge.axile.master.api.response.details.components.GitProfile;
import com.nucleonforge.axile.master.service.convert.Converter;

/**
 * The {@link Converter} from {@link GitInfo} to {@link GitProfile}.
 *
 * @author Sergey Cherkasov
 */
@Service
public class GitInfoConverter implements Converter<GitInfo, GitProfile> {
    @Override
    public @NonNull GitProfile convertInternal(@NonNull GitInfo source) {

        return new GitProfile(
                source.commitShaShort(),
                source.branch(),
                source.commitAuthor().name(),
                source.commitAuthor().email(),
                source.commitTimestamp());
    }
}
