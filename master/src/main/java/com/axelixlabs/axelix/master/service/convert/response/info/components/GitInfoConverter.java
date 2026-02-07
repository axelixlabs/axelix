/*
 * Copyright (C) 2025-2026 Axelix Labs
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.axelixlabs.axelix.master.service.convert.response.info.components;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import org.springframework.stereotype.Service;

import com.axelixlabs.axelix.common.api.info.components.GitInfo;
import com.axelixlabs.axelix.master.api.external.response.info.components.GitProfile;
import com.axelixlabs.axelix.master.service.convert.response.Converter;

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
