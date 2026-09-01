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

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.assertj.core.api.ThrowableAssert;
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
import com.axelixlabs.axelix.master.domain.RoleOrigin;
import com.axelixlabs.axelix.master.repository.RoleRepository;
import com.axelixlabs.axelix.master.repository.UserRepository;
import com.axelixlabs.axelix.master.service.state.auth.DefaultRoleService;
import com.axelixlabs.axelix.master.service.state.auth.RoleService;
import com.axelixlabs.axelix.master.service.state.auth.UserService;
import com.axelixlabs.axelix.master.utils.database.DatabaseMatrixTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
    private RoleRepository roleRepository;

    @Autowired
    private JdbcAggregateTemplate jdbcAggregateTemplate;

    @Autowired
    private JdbcClient jdbcClient;

    @Nested
    class FindByName {

        @Test
        void findByName_shouldReturnBuiltInRolesMatchingTheOnesTestsAreBuiltUpon() {
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
        void findByName_shouldNotResolveInternalRoles() {
            // when.
            Optional<Role> superAdmin = roleService.findByName("SUPER_ADMIN");
            Optional<Role> managedService = roleService.findByName("MANAGED_SERVICE");

            // then.
            assertThat(superAdmin).isEmpty();
            assertThat(managedService).isEmpty();
        }

        @Test
        void findByName_shouldReturnEmptyForAnUnknownRole() {
            // when.
            Optional<Role> role = roleService.findByName("THERE_IS_NO_SUCH_ROLE");

            // then.
            assertThat(role).isEmpty();
        }
    }

    @Nested
    class FindRolesOfUser {

        @BeforeEach
        @AfterEach
        void cleanUsers() {
            userRepository.findAll().forEach(user -> userService.deleteById(user.id()));
        }

        @Test
        void findRolesOfUser_shouldReturnTheSingleRoleGrantedToTheUser() {
            // given.
            String userId =
                    createUserWithRoles(roleRepository.findIdByName("VIEWER").orElseThrow());

            // when.
            Set<Role> roles = roleService.findRolesOfUser(userId);

            // then.
            assertThat(roles).singleElement().satisfies(role -> assertGrantsSameAs(role, TestRoles.VIEWER));
        }

        @Test
        void findRolesOfUser_shouldReturnEveryRoleWithItsAuthorities() {
            // given.
            String userId = createUserWithRoles(
                    roleRepository.findIdByName("ADMIN").orElseThrow(),
                    roleRepository.findIdByName("EDITOR").orElseThrow());

            // when.
            Set<Role> roles = roleService.findRolesOfUser(userId);

            // then.
            assertThat(roles).hasSize(2);
            assertThat(roleNamed(roles, "ADMIN")).satisfies(role -> assertGrantsSameAs(role, TestRoles.ADMIN));
            assertThat(roleNamed(roles, "EDITOR")).satisfies(role -> assertGrantsSameAs(role, TestRoles.EDITOR));
        }

        @Test
        void findRolesOfUser_shouldReturnEmptyForAUserWithoutAnyRoles() {
            // when.
            Set<Role> roles = roleService.findRolesOfUser("there-is-no-such-user");

            // then.
            assertThat(roles).isEmpty();
        }

        private String createUserWithRoles(String... roleIds) {
            userService.createLocal("alice", null, null, "alice@example.com", null, null, "p", Set.of(roleIds));
            return userRepository.findByUsername("alice").orElseThrow().id();
        }

        private static Role roleNamed(Set<Role> roles, String name) {
            return roles.stream()
                    .filter(role -> role.getName().equals(name))
                    .findFirst()
                    .orElseThrow();
        }
    }

    /**
     * The bonds of {@code roles_parents} are resolved into the components of the role, which is what makes an
     * inherited authority reach the checks. The built-in roles carry no bonds, so nothing else in this class covers
     * it.
     */
    @Nested
    class Inheritance {

        @BeforeEach
        @AfterEach
        void cleanCustomRoles() {
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

        @Test
        void shouldExposeTheRoleItDerivesFromAsAComponent() {
            // given.
            String parentId = customRole("PARENT", OssAuthority.CACHES_CLEAR);
            String childId = customRole("CHILD", OssAuthority.GARBAGE_COLLECTOR);
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
            String grandParent = customRole("ROLE A", OssAuthority.ENV_VALUES_READ);
            String parent = customRole("ROLE B", OssAuthority.CACHES_CLEAR);
            String child = customRole("ROLE C", OssAuthority.GARBAGE_COLLECTOR);
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
            String top = customRole("TOP", OssAuthority.ENV_VALUES_READ);
            String left = customRole("LEFT", OssAuthority.CACHES_CLEAR);
            String right = customRole("RIGHT", OssAuthority.CACHES_TOGGLE);
            String bottom = customRole("BOTTOM", OssAuthority.GARBAGE_COLLECTOR);
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
            String parentId = customRole("PARENT", OssAuthority.CACHES_CLEAR);
            String childId = customRole("CHILD", OssAuthority.GARBAGE_COLLECTOR);
            createBond(childId, parentId);

            userService.createLocal("alice", null, null, "alice@example.com", null, null, "p", Set.of(childId));
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

    /**
     * Dedicated nested class that covers the case when roles DAG for any reason creates a cycle. If the role is created by
     * any standard means that cannot/should not happen, but in any case, we may have a bug, or somebody may alter the state
     * of the database directly so we have to cover that as well.
     */
    @Nested
    class Cycles {

        @BeforeEach
        @AfterEach
        void cleanCustomRoles() {
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

        @Test
        void shouldTerminateOnABondCycleWrittenAroundTheApplication() {
            // given. Neither lane can write this, but a hand-written row could - and the resolution runs on the
            // login path, so looping here would leave nobody able to log in
            String roleA = customRole("ROLE A", OssAuthority.ENV_VALUES_READ);
            String roleB = customRole("ROLE B", OssAuthority.CACHES_CLEAR);
            createBond(roleA, roleB);
            createBond(roleB, roleA);

            // when.
            ThrowableAssert.ThrowingCallable callable = () -> roleService.findByName("ROLE A");

            // then.
            assertThatThrownBy(callable).isInstanceOf(IllegalStateException.class);
        }

        @Test
        void shouldTerminateOnARoleThatDerivesFromItself() {
            // given. A self-loop is the shortest cycle a hand-written row can introduce
            String role = customRole("ROLE A", OssAuthority.ENV_VALUES_READ);
            createBond(role, role);

            // when.
            ThrowableAssert.ThrowingCallable callable = () -> roleService.findByName("ROLE A");

            // then.
            assertThatThrownBy(callable).isInstanceOf(IllegalStateException.class);
        }

        @Test
        void shouldTerminateOnACycleSpanningThreeRoles() {
            // given. A --> B --> C --> A
            String roleA = customRole("ROLE A", OssAuthority.ENV_VALUES_READ);
            String roleB = customRole("ROLE B", OssAuthority.CACHES_CLEAR);
            String roleC = customRole("ROLE C", OssAuthority.CACHES_TOGGLE);
            createBond(roleA, roleB);
            createBond(roleB, roleC);
            createBond(roleC, roleA);

            // when.
            ThrowableAssert.ThrowingCallable callable = () -> roleService.findByName("ROLE A");

            // then. The message spells out the offending chain to make the hand-written row diagnosable
            assertThatThrownBy(callable)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("ROLE A --> ROLE B --> ROLE C --> ROLE A");
        }

        @Test
        void shouldTerminateWhenTheCycleSitsAboveTheQueriedRole() {
            // given. The queried role is itself acyclic, but its ancestry loops: LEAF --> A --> B --> A
            String leaf = customRole("LEAF", OssAuthority.GARBAGE_COLLECTOR);
            String roleA = customRole("ROLE A", OssAuthority.ENV_VALUES_READ);
            String roleB = customRole("ROLE B", OssAuthority.CACHES_CLEAR);
            createBond(leaf, roleA);
            createBond(roleA, roleB);
            createBond(roleB, roleA);

            // when.
            ThrowableAssert.ThrowingCallable callable = () -> roleService.findByName("LEAF");

            // then.
            assertThatThrownBy(callable).isInstanceOf(IllegalStateException.class);
        }

        @Test
        void shouldTerminateWhenResolvingTheRolesOfAUserCaughtInACycle() {
            // given. The cycle is reached through the other resolution entry point - the login path
            String roleA = customRole("ROLE A", OssAuthority.ENV_VALUES_READ);
            String roleB = customRole("ROLE B", OssAuthority.CACHES_CLEAR);
            createBond(roleA, roleB);
            createBond(roleB, roleA);

            userService.createLocal("alice", null, null, "alice@example.com", null, null, "p", Set.of(roleA));
            String userId = userRepository.findByUsername("alice").orElseThrow().id();

            // when.
            ThrowableAssert.ThrowingCallable callable = () -> roleService.findRolesOfUser(userId);

            // then.
            assertThatThrownBy(callable).isInstanceOf(IllegalStateException.class);
        }

        @Test
        void shouldNotReportACycleForADiamondSharedAncestor() {
            // given. A diamond reaches the same ancestor through two branches - that is a DAG, not a cycle, and the
            // colouring must not mistake the second visit for a back-edge
            String top = customRole("TOP", OssAuthority.ENV_VALUES_READ);
            String left = customRole("LEFT", OssAuthority.CACHES_CLEAR);
            String right = customRole("RIGHT", OssAuthority.CACHES_TOGGLE);
            String bottom = customRole("BOTTOM", OssAuthority.GARBAGE_COLLECTOR);
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

    private String customRole(String name, OssAuthority authority) {
        String id = UUID.randomUUID().toString();

        jdbcAggregateTemplate.insert(new RoleEntity(id, name, "Description", RoleOrigin.WEB_UI));
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
