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
package com.axelixlabs.axelix.master.service.state;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.JdbcClient;

import com.axelixlabs.axelix.master.domain.auth.Authority;
import com.axelixlabs.axelix.master.domain.auth.Role;
import com.axelixlabs.axelix.master.domain.iam.RoleOrigin;
import com.axelixlabs.axelix.master.domain.iam.UserEntity;
import com.axelixlabs.axelix.master.repository.AuthorityRepository;
import com.axelixlabs.axelix.master.repository.RoleRepository;
import com.axelixlabs.axelix.master.repository.RoleRepository.RoleProfile;
import com.axelixlabs.axelix.master.repository.UserRepository;
import com.axelixlabs.axelix.master.service.auth.DefaultAuthoritiesService;
import com.axelixlabs.axelix.master.service.auth.basic.SuperAdminUserAuthenticator;
import com.axelixlabs.axelix.master.service.state.auth.DefaultRoleService;
import com.axelixlabs.axelix.master.service.state.auth.RoleService;
import com.axelixlabs.axelix.master.service.state.auth.UserService;
import com.axelixlabs.axelix.master.testfixtures.TestRoles;
import com.axelixlabs.axelix.master.utils.database.DatabaseMatrixTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

/**
 * Integration tests of {@link DefaultRoleService}.
 *
 * @author Sergey Cherkasov
 * @author Mikhail Polivakha
 */
@DatabaseMatrixTest
class DefaultRoleServiceTest {

    @Autowired
    protected TestRoles testRoles;

    private RoleService roleService;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private AuthorityRepository authorityRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JdbcClient jdbcClient;

    @BeforeEach
    @AfterEach
    void cleanCustomRolesAndUsers() {
        roleService = new DefaultRoleService(roleRepository, new DefaultAuthoritiesService(authorityRepository));
        List<String> ids = userRepository.findAll().stream().map(UserEntity::id).toList();
        if (!ids.isEmpty()) {
            userService.deleteByIds(ids);
        }
        jdbcClient.sql("DELETE FROM roles_parents").update();
        jdbcClient
                .sql("DELETE FROM roles_authorities WHERE role_id IN (SELECT id FROM roles WHERE role_origin <> ?)")
                .param(RoleOrigin.BUILT_IN.name())
                .update();
        jdbcClient
                .sql("DELETE FROM roles WHERE role_origin <> ?")
                .param(RoleOrigin.BUILT_IN.name())
                .update();
        jdbcClient.sql("DELETE FROM authorities WHERE codename LIKE 'test:%'").update();
    }

    @Nested
    class FindByName {

        @Test
        void shouldReturnBuiltInRolesMatchingTheOnesTestsAreBuiltUpon() {
            // when.
            Optional<Role> viewer = roleService.findByName(testRoles.viewer().name());
            Optional<Role> editor = roleService.findByName(testRoles.editor().name());
            Optional<Role> admin = roleService.findByName(testRoles.admin().name());

            // then.
            assertThat(viewer).hasValueSatisfying(role -> assertGrantsSameAs(role, testRoles.viewer()));
            assertThat(editor).hasValueSatisfying(role -> assertGrantsSameAs(role, testRoles.editor()));
            assertThat(admin).hasValueSatisfying(role -> assertGrantsSameAs(role, testRoles.admin()));
        }

        @Test
        void shouldNotResolveSuperAdminRole() {
            // when.
            Optional<Role> superAdmin = roleService.findByName(SuperAdminUserAuthenticator.SUPER_ADMIN_ROLE_NAME);

            // then.
            assertThat(superAdmin).isEmpty();
        }

        @Test
        void shouldReturnEmptyForAnUnknownRole() {
            // when.
            Optional<Role> role = roleService.findByName("THERE_IS_NO_SUCH_ROLE");

            // then.
            assertThat(role).isEmpty();
        }

        @Test
        void shouldResolveAuthoritiesForWholeRoleGraph() {
            // given.
            String parentId = createCustomRole("PARENT", "test:caches:clear");
            String childId = createCustomRole("CHILD", "test:caches:toggle");
            createBond(childId, parentId);

            // when.
            Optional<Role> role = roleService.findByName("CHILD");

            // then.
            assertThat(role)
                    .hasValueSatisfying(value -> assertThat(value.getEffectiveAuthorities())
                            .extracting(Authority::getName)
                            .containsExactlyInAnyOrder("test:caches:clear", "test:caches:toggle"));
        }

        @Test
        void shouldForceNonEmptyDescriptionForEveryAuthority() {
            // when.
            int blankDescriptions = jdbcClient
                    .sql("SELECT COUNT(*) FROM authorities WHERE description IS NULL OR TRIM(description) = ''")
                    .query(Integer.class)
                    .single();

            // then.
            assertThat(blankDescriptions).isZero();
        }
    }

    @Nested
    class FindRolesOfUser {

        @Test
        void shouldReturnTheSingleRoleGrantedToTheUser() {
            // given.
            String userId = createUserWithRoles(testRoles.viewer().name());

            // when.
            Set<Role> roles = roleService.findRolesOfUser(userId);

            // then.
            assertThat(roles).singleElement().satisfies(role -> assertGrantsSameAs(role, testRoles.viewer()));
        }

        @Test
        void shouldReturnEveryRoleWithItsAuthorities() {
            // given.
            String userId = createUserWithRoles(
                    testRoles.admin().name(), testRoles.editor().name());

            // when.
            Set<Role> roles = roleService.findRolesOfUser(userId);

            // then.
            assertThat(roles).hasSize(2);
            assertThat(roleNamed(roles, testRoles.admin().name()))
                    .satisfies(role -> assertGrantsSameAs(role, testRoles.admin()));
            assertThat(roleNamed(roles, testRoles.editor().name()))
                    .satisfies(role -> assertGrantsSameAs(role, testRoles.editor()));
        }

        @Test
        void shouldReturnEmptyForAUserWithoutAnyRoles() {
            // when.
            Set<Role> roles = roleService.findRolesOfUser("there-is-no-such-user");

            // then.
            assertThat(roles).isEmpty();
        }

        private String createUserWithRoles(String... roles) {
            userService.createLocal("alice", null, null, "alice@example.com", "p", Set.of(roles));
            return userRepository.findByUsername("alice").orElseThrow().id();
        }

        private static Role roleNamed(Set<Role> roles, String name) {
            return roles.stream()
                    .filter(role -> role.name().equals(name))
                    .findFirst()
                    .orElseThrow();
        }
    }

    @Nested
    class Inheritance {

        @Test
        void shouldExposeTheRoleItDerivesFromAsAComponent() {
            // given.
            String parentId = createCustomRole("PARENT", "test:caches:clear");
            String childId = createCustomRole("CHILD", "test:gc:manage");
            createBond(childId, parentId);

            // when.
            Role child = roleService.findByName("CHILD").orElseThrow();

            // then. The parent is a component rather than its authorities being copied into the child
            assertThat(child.authorities()).extracting(Authority::getName).containsExactly("test:gc:manage");
            assertThat(child.components()).singleElement().satisfies(parent -> {
                assertThat(parent.name()).isEqualTo("PARENT");
                assertThat(parent.authorities()).extracting(Authority::getName).containsExactly("test:caches:clear");
            });
        }

        @Test
        void shouldResolveTheBondsTransitively() {
            // given. C derives from B, B derives from A
            String grandParent = createCustomRole("ROLE A", "test:environment.values:read");
            String parent = createCustomRole("ROLE B", "test:caches:clear");
            String child = createCustomRole("ROLE C", "test:gc:manage");
            createBond(parent, grandParent);
            createBond(child, parent);

            // when.
            Role c = roleService.findByName("ROLE C").orElseThrow();

            // then.
            assertThat(c.getEffectiveAuthorities())
                    .extracting(Authority::getName)
                    .containsExactlyInAnyOrder("test:gc:manage", "test:caches:clear", "test:environment.values:read");
        }

        @Test
        void shouldUnionTheAuthoritiesReachedThroughSeveralChains() {
            // given. A diamond: the top role is reached through both branches
            String top = createCustomRole("TOP", "test:environment.values:read");
            String left = createCustomRole("LEFT", "test:caches:clear");
            String right = createCustomRole("RIGHT", "test:caches:toggle");
            String bottom = createCustomRole("BOTTOM", "test:gc:manage");
            createBond(left, top);
            createBond(right, top);
            createBond(bottom, left);
            createBond(bottom, right);

            // when.
            Role role = roleService.findByName("BOTTOM").orElseThrow();

            // then. Reaching the same authority twice is not an error, it is simply unioned
            assertThat(role.getEffectiveAuthorities())
                    .extracting(Authority::getName)
                    .containsExactlyInAnyOrder(
                            "test:gc:manage",
                            "test:caches:clear",
                            "test:caches:toggle",
                            "test:environment.values:read");
        }

        @Test
        void shouldResolveTheBondsOfEveryRoleTheUserHolds() {
            // given.
            String parentId = createCustomRole("PARENT", "test:caches:clear");
            String childId = createCustomRole("CHILD", "test:gc:manage");
            createBond(childId, parentId);

            userService.createLocal("alice", null, null, "alice@example.com", "p", Set.of("CHILD"));
            String userId = userRepository.findByUsername("alice").orElseThrow().id();

            // when.
            Set<Role> roles = roleService.findRolesOfUser(userId);

            // then.
            assertThat(roles)
                    .singleElement()
                    .satisfies(role -> assertThat(role.getEffectiveAuthorities())
                            .extracting(Authority::getName)
                            .containsExactlyInAnyOrder("test:gc:manage", "test:caches:clear"));
        }
    }

    @Nested
    class GetRolesFeed {

        @Test
        void shouldSeedTheSameThreeBuiltInRolesInEveryDialect() {
            // when.
            List<RoleProfile> feed = roleService.getRolesFeed();

            // then.
            assertThat(feed)
                    .extracting(RoleProfile::id, RoleProfile::name, RoleProfile::description)
                    .containsExactlyInAnyOrder(
                            tuple(
                                    "00000000-0000-0000-0000-0000000000b1",
                                    testRoles.viewer().name(),
                                    "Read-only access to the monitored applications."),
                            tuple(
                                    "00000000-0000-0000-0000-0000000000b2",
                                    testRoles.editor().name(),
                                    "Performs runtime operations on the monitored applications, including destructive"
                                            + " ones such as clearing caches."),
                            tuple(
                                    "00000000-0000-0000-0000-0000000000b3",
                                    testRoles.admin().name(),
                                    "Everything an editor can do, plus reading sensitive configuration values."));
        }

        @Test
        void shouldCountTheUsersEachRoleIsAssignedTo() {
            // given.
            userService.createLocal(
                    "alice",
                    null,
                    null,
                    "alice@example.com",
                    "p",
                    Set.of(testRoles.editor().name()));
            userService.createLocal(
                    "bob",
                    null,
                    null,
                    "bob@example.com",
                    "p",
                    Set.of(testRoles.editor().name()));
            userService.createLocal(
                    "carol",
                    null,
                    null,
                    "carol@example.com",
                    "p",
                    Set.of(testRoles.viewer().name()));

            // when.
            List<RoleProfile> feed = roleService.getRolesFeed();

            // then.
            assertThat(entryNamed(feed, testRoles.editor().name()).membersCount())
                    .isEqualTo(2);
            assertThat(entryNamed(feed, testRoles.viewer().name()).membersCount())
                    .isEqualTo(1);
            assertThat(entryNamed(feed, testRoles.admin().name()).membersCount())
                    .isZero();
        }

        @Test
        void shouldReturnACustomRoleWithItsOwnIdAndDescription() {
            // given.
            String derivedId = createCustomRole("DERIVED", "test:environment.values:read");

            // when.
            List<RoleProfile> feed = roleService.getRolesFeed();

            // then.
            RoleProfile derived = entryNamed(feed, "DERIVED");
            assertThat(derived.id()).isEqualTo(derivedId);
            assertThat(derived.description()).isEqualTo("Description");
            assertThat(derived.membersCount()).isZero();
        }

        private static RoleProfile entryNamed(List<RoleProfile> feed, String name) {
            return feed.stream()
                    .filter(role -> role.name().equals(name))
                    .findFirst()
                    .orElseThrow();
        }
    }

    @Nested
    class Cycles {

        @Test
        void shouldTerminateOnABondCycleWrittenAroundTheApplication() {
            // given.
            String roleA = createCustomRole("ROLE A", "test:environment.values:read");
            String roleB = createCustomRole("ROLE B", "test:caches:clear");
            createBond(roleA, roleB);
            createBond(roleB, roleA);

            // when & then.
            assertThatThrownBy(() -> roleService.findByName("ROLE A")).isInstanceOf(IllegalStateException.class);
        }

        @Test
        void shouldTerminateOnARoleThatDerivesFromItself() {
            // given.
            String role = createCustomRole("ROLE A", "test:environment.values:read");
            createBond(role, role);

            // when & then.
            assertThatThrownBy(() -> roleService.findByName("ROLE A")).isInstanceOf(IllegalStateException.class);
        }

        @Test
        void shouldTerminateOnACycleSpanningThreeRoles() {
            // given. A --> B --> C --> A
            String roleA = createCustomRole("ROLE A", "test:environment.values:read");
            String roleB = createCustomRole("ROLE B", "test:caches:clear");
            String roleC = createCustomRole("ROLE C", "test:caches:toggle");
            createBond(roleA, roleB);
            createBond(roleB, roleC);
            createBond(roleC, roleA);

            // when & then.
            assertThatThrownBy(() -> roleService.findByName("ROLE A")).isInstanceOf(IllegalStateException.class);
        }

        @Test
        void shouldTerminateWhenTheCycleSitsAboveTheQueriedRole() {
            // given.
            String leaf = createCustomRole("LEAF", "test:gc:manage");
            String roleA = createCustomRole("ROLE A", "test:environment.values:read");
            String roleB = createCustomRole("ROLE B", "test:caches:clear");
            createBond(leaf, roleA);
            createBond(roleA, roleB);
            createBond(roleB, roleA);

            // when & then.
            assertThatThrownBy(() -> roleService.findByName("LEAF")).isInstanceOf(IllegalStateException.class);
        }

        @Test
        void shouldTerminateWhenResolvingTheRolesOfAUserCaughtInACycle() {
            // given.
            String roleA = createCustomRole("ROLE A", "test:environment.values:read");
            String roleB = createCustomRole("ROLE B", "test:caches:clear");
            createBond(roleA, roleB);
            createBond(roleB, roleA);

            userService.createLocal("alice", null, null, "alice@example.com", "p", Set.of("ROLE A"));
            String userId = userRepository.findByUsername("alice").orElseThrow().id();

            // when & then.
            assertThatThrownBy(() -> roleService.findRolesOfUser(userId)).isInstanceOf(IllegalStateException.class);
        }

        @Test
        void shouldNotReportACycleForADiamondSharedAncestor() {
            // given. A diamond reaches the same ancestor through two branches - that is a DAG, not a cycle, and the
            // colouring must not mistake the second visit for a back-edge
            String top = createCustomRole("TOP", "test:environment.values:read");
            String left = createCustomRole("LEFT", "test:caches:clear");
            String right = createCustomRole("RIGHT", "test:caches:toggle");
            String bottom = createCustomRole("BOTTOM", "test:gc:manage");
            createBond(left, top);
            createBond(right, top);
            createBond(bottom, left);
            createBond(bottom, right);

            // when.
            Role role = roleService.findByName("BOTTOM").orElseThrow();

            // then.
            assertThat(role.getEffectiveAuthorities())
                    .extracting(Authority::getName)
                    .containsExactlyInAnyOrder(
                            "test:gc:manage",
                            "test:caches:clear",
                            "test:caches:toggle",
                            "test:environment.values:read");
        }
    }

    private String createCustomRole(String name) {
        String id = UUID.randomUUID().toString();

        jdbcClient
                .sql("INSERT INTO roles (id, name, description, role_origin) VALUES (?, ?, ?, ?)")
                .params(id, name, "Description", RoleOrigin.WEB_UI.name())
                .update();

        return id;
    }

    private String createCustomRole(String name, String authority) {
        String authorityId = UUID.randomUUID().toString();
        jdbcClient
                .sql("INSERT INTO authorities (id, codename, description) VALUES (?, ?, ?)")
                .params(authorityId, authority, "Test authority")
                .update();
        String id = createCustomRole(name);

        jdbcClient
                .sql("INSERT INTO roles_authorities (role_id, authority_id)"
                        + " SELECT ?, id FROM authorities WHERE codename = ?")
                .params(id, authority)
                .update();

        return id;
    }

    private void createBond(String childId, String parentRoleId) {
        jdbcClient
                .sql("INSERT INTO roles_parents (role_id, parent_role_id) VALUES (?, ?)")
                .params(childId, parentRoleId)
                .update();
    }

    private static void assertGrantsSameAs(Role actual, Role expected) {
        assertThat(actual.name()).isEqualTo(expected.name());
        Set<String> expectedAuthorities =
                switch (expected.name()) {
                    case "VIEWER" -> Set.of();
                    case "EDITOR" -> Set.of("scheduled-tasks:write", "caches:clear", "caches:toggle", "gc:manage");
                    case "ADMIN" ->
                        Set.of(
                                "scheduled-tasks:write",
                                "caches:clear",
                                "caches:toggle",
                                "gc:manage",
                                "environment.values:read",
                                "config-properties.values:read");
                    default -> throw new IllegalArgumentException();
                };
        assertThat(actual.authorities())
                .extracting(Authority::getName)
                .containsExactlyInAnyOrderElementsOf(expectedAuthorities);
    }
}
