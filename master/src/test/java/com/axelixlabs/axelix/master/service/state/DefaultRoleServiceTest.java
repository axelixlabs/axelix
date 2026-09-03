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
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.jdbc.core.simple.JdbcClient;

import com.axelixlabs.axelix.common.auth.core.Authority;
import com.axelixlabs.axelix.common.auth.core.OssAuthority;
import com.axelixlabs.axelix.common.auth.core.Role;
import com.axelixlabs.axelix.common.testfixtures.TestRoles;
import com.axelixlabs.axelix.master.domain.RoleEntity;
import com.axelixlabs.axelix.master.domain.RoleFeed;
import com.axelixlabs.axelix.master.domain.RoleOrigin;
import com.axelixlabs.axelix.master.domain.UserEntity;
import com.axelixlabs.axelix.master.repository.UserRepository;
import com.axelixlabs.axelix.master.service.state.auth.DefaultRoleService;
import com.axelixlabs.axelix.master.service.state.auth.RoleService;
import com.axelixlabs.axelix.master.service.state.auth.UserService;
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
    private RoleService roleService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JdbcAggregateTemplate jdbcAggregateTemplate;

    @Autowired
    private JdbcClient jdbcClient;

    @BeforeEach
    @AfterEach
    void cleanCustomRolesAndUsers() {
        userRepository.findAll().forEach(user -> userService.deleteById(user.id()));
        jdbcClient.sql("DELETE FROM roles_parents").update();
        jdbcClient
                .sql("DELETE FROM roles_authorities WHERE role_id IN (SELECT id FROM roles WHERE role_origin <> ?)")
                .param(RoleOrigin.BUILT_IN.name())
                .update();
        jdbcClient
                .sql("DELETE FROM roles WHERE role_origin <> ?")
                .param(RoleOrigin.BUILT_IN.name())
                .update();
    }

    @Nested
    class FindByName {

        @Test
        void shouldReturnBuiltInRolesMatchingTheOnesTestsAreBuiltUpon() {
            // when.
            Optional<Role> viewer = roleService.findByName(TestRoles.VIEWER.getName());
            Optional<Role> editor = roleService.findByName(TestRoles.EDITOR.getName());
            Optional<Role> admin = roleService.findByName(TestRoles.ADMIN.getName());

            // then.
            assertThat(viewer).hasValueSatisfying(role -> assertGrantsSameAs(role, TestRoles.VIEWER));
            assertThat(editor).hasValueSatisfying(role -> assertGrantsSameAs(role, TestRoles.EDITOR));
            assertThat(admin).hasValueSatisfying(role -> assertGrantsSameAs(role, TestRoles.ADMIN));
        }

        @Test
        void shouldNotResolveInternalRoles() {
            // when.
            Optional<Role> superAdmin = roleService.findByName("SUPER_ADMIN");
            Optional<Role> managedService = roleService.findByName("MANAGED_SERVICE");

            // then.
            assertThat(superAdmin).isEmpty();
            assertThat(managedService).isEmpty();
        }

        @Test
        void shouldReturnEmptyForAnUnknownRole() {
            // when.
            Optional<Role> role = roleService.findByName("THERE_IS_NO_SUCH_ROLE");

            // then.
            assertThat(role).isEmpty();
        }
    }

    @Nested
    class FindRolesOfUser {

        @Test
        void shouldReturnTheSingleRoleGrantedToTheUser() {
            // given.
            String userId = createUserWithRoles("VIEWER");

            // when.
            Set<Role> roles = roleService.findRolesOfUser(userId);

            // then.
            assertThat(roles).singleElement().satisfies(role -> assertGrantsSameAs(role, TestRoles.VIEWER));
        }

        @Test
        void shouldReturnEveryRoleWithItsAuthorities() {
            // given.
            String userId = createUserWithRoles("ADMIN", "EDITOR");

            // when.
            Set<Role> roles = roleService.findRolesOfUser(userId);

            // then.
            assertThat(roles).hasSize(2);
            assertThat(roleNamed(roles, "ADMIN")).satisfies(role -> assertGrantsSameAs(role, TestRoles.ADMIN));
            assertThat(roleNamed(roles, "EDITOR")).satisfies(role -> assertGrantsSameAs(role, TestRoles.EDITOR));
        }

        @Test
        void shouldReturnEmptyForAUserWithoutAnyRoles() {
            // when.
            Set<Role> roles = roleService.findRolesOfUser("there-is-no-such-user");

            // then.
            assertThat(roles).isEmpty();
        }

        private String createUserWithRoles(String... roles) {
            userService.createLocal("alice", null, null, "alice@example.com", null, null, "p", roles[0]);
            UserEntity user = userRepository.findByUsername("alice").orElseThrow();
            if (roles.length > 1) {
                userService.updateUserPatch(
                        user.id(), "alice", null, null, "alice@example.com", null, null, null, Set.of(roles), null);
            }
            return user.id();
        }

        private static Role roleNamed(Set<Role> roles, String name) {
            return roles.stream()
                    .filter(role -> role.getName().equals(name))
                    .findFirst()
                    .orElseThrow();
        }
    }

    @Nested
    class Inheritance {

        @Test
        void shouldExposeTheRoleItDerivesFromAsAComponent() {
            // given.
            String parentId = createCustomRole("PARENT", OssAuthority.CACHES_CLEAR);
            String childId = createCustomRole("CHILD", OssAuthority.GARBAGE_COLLECTOR);
            createBond(childId, parentId);

            // when.
            Role child = roleService.findByName("CHILD").orElseThrow();

            // then. The parent is a component rather than its authorities being copied into the child
            assertThat(child.getAuthorities())
                    .extracting(Authority::getName)
                    .containsExactly(OssAuthority.GARBAGE_COLLECTOR.getName());
            assertThat(child.getComponents()).singleElement().satisfies(parent -> {
                assertThat(parent.getName()).isEqualTo("PARENT");
                assertThat(parent.getAuthorities())
                        .extracting(Authority::getName)
                        .containsExactly(OssAuthority.CACHES_CLEAR.getName());
            });
        }

        @Test
        void shouldResolveTheBondsTransitively() {
            // given. C derives from B, B derives from A
            String grandParent = createCustomRole("ROLE A", OssAuthority.ENV_VALUES_READ);
            String parent = createCustomRole("ROLE B", OssAuthority.CACHES_CLEAR);
            String child = createCustomRole("ROLE C", OssAuthority.GARBAGE_COLLECTOR);
            createBond(parent, grandParent);
            createBond(child, parent);

            // when.
            Role c = roleService.findByName("ROLE C").orElseThrow();

            // then.
            assertThat(c.getEffectiveAuthorities())
                    .extracting(Authority::getName)
                    .containsExactlyInAnyOrder(
                            OssAuthority.GARBAGE_COLLECTOR.getName(),
                            OssAuthority.CACHES_CLEAR.getName(),
                            OssAuthority.ENV_VALUES_READ.getName());
        }

        @Test
        void shouldUnionTheAuthoritiesReachedThroughSeveralChains() {
            // given. A diamond: the top role is reached through both branches
            String top = createCustomRole("TOP", OssAuthority.ENV_VALUES_READ);
            String left = createCustomRole("LEFT", OssAuthority.CACHES_CLEAR);
            String right = createCustomRole("RIGHT", OssAuthority.CACHES_TOGGLE);
            String bottom = createCustomRole("BOTTOM", OssAuthority.GARBAGE_COLLECTOR);
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
                            OssAuthority.GARBAGE_COLLECTOR.getName(),
                            OssAuthority.CACHES_CLEAR.getName(),
                            OssAuthority.CACHES_TOGGLE.getName(),
                            OssAuthority.ENV_VALUES_READ.getName());
        }

        @Test
        void shouldResolveTheBondsOfEveryRoleTheUserHolds() {
            // given.
            String parentId = createCustomRole("PARENT", OssAuthority.CACHES_CLEAR);
            String childId = createCustomRole("CHILD", OssAuthority.GARBAGE_COLLECTOR);
            createBond(childId, parentId);

            userService.createLocal("alice", null, null, "alice@example.com", null, null, "p", "CHILD");
            String userId = userRepository.findByUsername("alice").orElseThrow().id();

            // when.
            Set<Role> roles = roleService.findRolesOfUser(userId);

            // then.
            assertThat(roles)
                    .singleElement()
                    .satisfies(role -> assertThat(role.getEffectiveAuthorities())
                            .extracting(Authority::getName)
                            .containsExactlyInAnyOrder(
                                    OssAuthority.GARBAGE_COLLECTOR.getName(), OssAuthority.CACHES_CLEAR.getName()));
        }
    }

    @Nested
    class GetRolesFeed {

        @Test
        void shouldSeedTheSameThreeBuiltInRolesInEveryDialect() {
            // when.
            List<RoleFeed> feed = roleService.getRolesFeed();

            // then.
            assertThat(feed)
                    .extracting(RoleFeed::id, RoleFeed::name, RoleFeed::description)
                    .containsExactlyInAnyOrder(
                            tuple(
                                    "00000000-0000-0000-0000-0000000000b1",
                                    "VIEWER",
                                    "Read-only access to the monitored applications."),
                            tuple(
                                    "00000000-0000-0000-0000-0000000000b2",
                                    "EDITOR",
                                    "Performs runtime operations on the monitored applications, including destructive"
                                            + " ones such as clearing caches."),
                            tuple(
                                    "00000000-0000-0000-0000-0000000000b3",
                                    "ADMIN",
                                    "Everything an editor can do, plus reading sensitive configuration values."));
        }

        @Test
        void shouldCountTheUsersEachRoleIsAssignedTo() {
            // given.
            userService.createLocal("alice", null, null, "alice@example.com", null, null, "p", "EDITOR");
            userService.createLocal("bob", null, null, "bob@example.com", null, null, "p", "EDITOR");
            userService.createLocal("carol", null, null, "carol@example.com", null, null, "p", "VIEWER");

            // when.
            List<RoleFeed> feed = roleService.getRolesFeed();

            // then.
            assertThat(entryNamed(feed, "EDITOR").membersCount()).isEqualTo(2);
            assertThat(entryNamed(feed, "VIEWER").membersCount()).isEqualTo(1);
            assertThat(entryNamed(feed, "ADMIN").membersCount()).isZero();
        }

        @Test
        void shouldReturnACustomRoleWithItsOwnIdAndDescription() {
            // given.
            String derivedId = createCustomRole("DERIVED", OssAuthority.ENV_VALUES_READ);

            // when.
            List<RoleFeed> feed = roleService.getRolesFeed();

            // then.
            RoleFeed derived = entryNamed(feed, "DERIVED");
            assertThat(derived.id()).isEqualTo(derivedId);
            assertThat(derived.description()).isEqualTo("Description");
            assertThat(derived.membersCount()).isZero();
        }

        private static RoleFeed entryNamed(List<RoleFeed> feed, String name) {
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
            String roleA = createCustomRole("ROLE A", OssAuthority.ENV_VALUES_READ);
            String roleB = createCustomRole("ROLE B", OssAuthority.CACHES_CLEAR);
            createBond(roleA, roleB);
            createBond(roleB, roleA);

            // when & then.
            assertThatThrownBy(() -> roleService.findByName("ROLE A")).isInstanceOf(IllegalStateException.class);
        }

        @Test
        void shouldTerminateOnARoleThatDerivesFromItself() {
            // given.
            String role = createCustomRole("ROLE A", OssAuthority.ENV_VALUES_READ);
            createBond(role, role);

            // when & then.
            assertThatThrownBy(() -> roleService.findByName("ROLE A")).isInstanceOf(IllegalStateException.class);
        }

        @Test
        void shouldTerminateOnACycleSpanningThreeRoles() {
            // given. A --> B --> C --> A
            String roleA = createCustomRole("ROLE A", OssAuthority.ENV_VALUES_READ);
            String roleB = createCustomRole("ROLE B", OssAuthority.CACHES_CLEAR);
            String roleC = createCustomRole("ROLE C", OssAuthority.CACHES_TOGGLE);
            createBond(roleA, roleB);
            createBond(roleB, roleC);
            createBond(roleC, roleA);

            // when & then.
            assertThatThrownBy(() -> roleService.findByName("ROLE A")).isInstanceOf(IllegalStateException.class);
        }

        @Test
        void shouldTerminateWhenTheCycleSitsAboveTheQueriedRole() {
            // given.
            String leaf = createCustomRole("LEAF", OssAuthority.GARBAGE_COLLECTOR);
            String roleA = createCustomRole("ROLE A", OssAuthority.ENV_VALUES_READ);
            String roleB = createCustomRole("ROLE B", OssAuthority.CACHES_CLEAR);
            createBond(leaf, roleA);
            createBond(roleA, roleB);
            createBond(roleB, roleA);

            // when & then.
            assertThatThrownBy(() -> roleService.findByName("LEAF")).isInstanceOf(IllegalStateException.class);
        }

        @Test
        void shouldTerminateWhenResolvingTheRolesOfAUserCaughtInACycle() {
            // given.
            String roleA = createCustomRole("ROLE A", OssAuthority.ENV_VALUES_READ);
            String roleB = createCustomRole("ROLE B", OssAuthority.CACHES_CLEAR);
            createBond(roleA, roleB);
            createBond(roleB, roleA);

            userService.createLocal("alice", null, null, "alice@example.com", null, null, "p", "ROLE A");
            String userId = userRepository.findByUsername("alice").orElseThrow().id();

            // when & then.
            assertThatThrownBy(() -> roleService.findRolesOfUser(userId)).isInstanceOf(IllegalStateException.class);
        }

        @Test
        void shouldNotReportACycleForADiamondSharedAncestor() {
            // given. A diamond reaches the same ancestor through two branches - that is a DAG, not a cycle, and the
            // colouring must not mistake the second visit for a back-edge
            String top = createCustomRole("TOP", OssAuthority.ENV_VALUES_READ);
            String left = createCustomRole("LEFT", OssAuthority.CACHES_CLEAR);
            String right = createCustomRole("RIGHT", OssAuthority.CACHES_TOGGLE);
            String bottom = createCustomRole("BOTTOM", OssAuthority.GARBAGE_COLLECTOR);
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
                            OssAuthority.GARBAGE_COLLECTOR.getName(),
                            OssAuthority.CACHES_CLEAR.getName(),
                            OssAuthority.CACHES_TOGGLE.getName(),
                            OssAuthority.ENV_VALUES_READ.getName());
        }
    }

    private String createCustomRole(String name) {
        String id = UUID.randomUUID().toString();

        jdbcAggregateTemplate.insert(new RoleEntity(id, name, "Description", RoleOrigin.WEB_UI));

        return id;
    }

    private String createCustomRole(String name, OssAuthority authority) {
        String id = createCustomRole(name);

        jdbcClient
                .sql("INSERT INTO roles_authorities (role_id, authority_id)"
                        + " SELECT ?, id FROM authorities WHERE name = ?")
                .params(id, authority.name())
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
        assertThat(actual.getName()).isEqualTo(expected.getName());
        assertThat(actual.getAuthorities())
                .extracting(Authority::getName)
                .containsExactlyInAnyOrderElementsOf(expected.getAuthorities().stream()
                        .map(Authority::getName)
                        .toList());
    }
}
