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
package com.axelixlabs.axelix.common.api.registration;

import java.util.Objects;

/**
 * The common git commit info.
 *
 * @author Mikhail Polivakha
 */
public final class GitInfo {

    private final String commitShaShort;
    private final String branch;
    // TODO: Migrate to Instant later on. The parsing of a timestamp needs to be taken carefully
    private final String commitTimestamp;
    private final CommitAuthor commitAuthor;

    public GitInfo(String commitShaShort, String branch, String commitTimestamp, CommitAuthor commitAuthor) {
        this.commitShaShort = commitShaShort;
        this.branch = branch;
        this.commitTimestamp = commitTimestamp;
        this.commitAuthor = commitAuthor;
    }

    public String commitShaShort() {
        return commitShaShort;
    }

    public String branch() {
        return branch;
    }

    public String commitTimestamp() {
        return commitTimestamp;
    }

    public CommitAuthor commitAuthor() {
        return commitAuthor;
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
        return Objects.equals(commitShaShort, gitInfo.commitShaShort)
                && Objects.equals(branch, gitInfo.branch)
                && Objects.equals(commitTimestamp, gitInfo.commitTimestamp)
                && Objects.equals(commitAuthor, gitInfo.commitAuthor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(commitShaShort, branch, commitTimestamp, commitAuthor);
    }

    @Override
    public String toString() {
        return "GitInfo{"
                + "commitShaShort='"
                + commitShaShort
                + '\''
                + ", branch='"
                + branch
                + '\''
                + ", commitTimestamp='"
                + commitTimestamp
                + '\''
                + ", commitAuthor="
                + commitAuthor
                + '}';
    }

    /**
     * Author of the commit information
     *
     * @author Mikhail Polivakha
     */
    public static final class CommitAuthor {

        private final String name;
        private final String email;

        public CommitAuthor(String name, String email) {
            this.name = name;
            this.email = email;
        }

        public String name() {
            return name;
        }

        public String email() {
            return email;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            CommitAuthor that = (CommitAuthor) o;
            return Objects.equals(name, that.name) && Objects.equals(email, that.email);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, email);
        }

        @Override
        public String toString() {
            return "CommitAuthor{" + "name='" + name + '\'' + ", email='" + email + '\'' + '}';
        }
    }
}
