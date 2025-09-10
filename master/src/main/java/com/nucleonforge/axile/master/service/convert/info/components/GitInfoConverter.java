package com.nucleonforge.axile.master.service.convert.info.components;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import org.springframework.stereotype.Service;

import com.nucleonforge.axile.common.api.info.components.GitInfo;
import com.nucleonforge.axile.master.api.response.info.components.GitProfile;
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
        GitProfile.Commit commit = convertCommit(source);

        return new GitProfile(source.branch(), commit);
    }

    private GitProfile.@Nullable Commit convertCommit(GitInfo source) {
        GitInfo.Commit commit = source.commit();
        if (commit != null) {
            return new GitProfile.Commit(commit.id(), commit.time());
        }

        return null;
    }
}
