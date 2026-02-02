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
package com.axelixlabs.axelix.common.api.info.components;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jspecify.annotations.Nullable;

/**
 * DTO that encapsulates the git information of the given artifact.
 *
 * @author Sergey Cherkasov
 */
public final class GitInfo {

    private final String branch;

    @Nullable
    private final Commit commit;

    public GitInfo(@JsonProperty("branch") String branch, @JsonProperty("commit") @Nullable Commit commit) {
        this.branch = branch;
        this.commit = commit;
    }

    public String branch() {
        return branch;
    }

    @Nullable
    public Commit commit() {
        return commit;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GitInfo gitInfo = (GitInfo) o;
        return Objects.equals(branch, gitInfo.branch) && Objects.equals(commit, gitInfo.commit);
    }

    @Override
    public int hashCode() {
        return Objects.hash(branch, commit);
    }

    @Override
    public String toString() {
        return "GitInfo{" + "branch='" + branch + '\'' + ", commit=" + commit + '}';
    }

    public static final class Commit {

        private final String id;
        private final String time;

        public Commit(@JsonProperty("id") String id, @JsonProperty("time") String time) {
            this.id = id;
            this.time = time;
        }

        public String id() {
            return id;
        }

        public String time() {
            return time;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Commit commit = (Commit) o;
            return Objects.equals(id, commit.id) && Objects.equals(time, commit.time);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, time);
        }

        @Override
        public String toString() {
            return "Commit{" + "id='" + id + '\'' + ", time='" + time + '\'' + '}';
        }
    }
}
