package com.nucleonforge.axile.master.auth.spi;

import java.util.Collections;
import java.util.Set;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import com.nucleonforge.axile.common.auth.core.AuthorizationRequest;
import com.nucleonforge.axile.common.auth.core.DefaultAuthority;
import com.nucleonforge.axile.common.auth.core.DefaultRole;
import com.nucleonforge.axile.common.auth.core.DefaultUser;
import com.nucleonforge.axile.common.auth.core.Role;
import com.nucleonforge.axile.common.auth.core.User;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

/**
 * Unit tests for {@link DefaultAuthorizer}.
 *
 * @since 29.08.2025
 * @author Sergey Cherkasov
 */
class DefaultAuthorizerTest {
    private final Authorizer authorizer = new DefaultAuthorizer();

    @Test
    void shouldAuthorizeUserWithoutRolesAndAuthorities() {
        User user = new DefaultUser("userWithoutRolesAndAuthorities", null);
        AuthorizationRequest request = new AuthorizationRequest(Collections.emptySet());

        ILoggingEvent test = getLogEven(() -> authorizer.authorize(user, request));

        assertThat(test).isNull();
        assertThatNoException().isThrownBy(() -> authorizer.authorize(user, request));
    }

    @Test
    void shouldAuthorizeUserHasRequiredAuthorities() {
        Role role = new DefaultRole(
                "admin", Set.of(DefaultAuthority.CACHES, DefaultAuthority.BEANS), Collections.emptySet());
        User user = new DefaultUser("userWithAuthorities", Set.of(role));

        AuthorizationRequest request =
                new AuthorizationRequest(Set.of(DefaultAuthority.CACHES, DefaultAuthority.BEANS));

        ILoggingEvent test = getLogEven(() -> authorizer.authorize(user, request));

        assertThat(test).isNull();
        assertThatNoException().isThrownBy(() -> authorizer.authorize(user, request));
    }

    @Test
    void shouldNotLogWarnWhenUserHasRequiredAuthoritiesInHierarchy() {
        Role componentsRole = new DefaultRole("componentsRole", Set.of(DefaultAuthority.ENV), Collections.emptySet());
        Role engineerRole = new DefaultRole(
                "engineerRole", Set.of(DefaultAuthority.CACHES, DefaultAuthority.BEANS), Collections.emptySet());
        Role adminRole = new DefaultRole(
                "adminRole", Set.of(DefaultAuthority.INFO, DefaultAuthority.BEANS), Set.of(componentsRole));
        User user = new DefaultUser("userWithAuthorities", Set.of(adminRole, engineerRole));

        AuthorizationRequest request = new AuthorizationRequest(
                Set.of(DefaultAuthority.ENV, DefaultAuthority.CACHES, DefaultAuthority.BEANS, DefaultAuthority.INFO));
        ILoggingEvent test = getLogEven(() -> authorizer.authorize(user, request));

        assertThat(test).isNull();
        assertThatNoException().isThrownBy(() -> authorizer.authorize(user, request));
    }

    @Test
    void shouldLogWarnWhenUserLacksRequiredAuthoritiesAndContinue1() {
        Role role = new DefaultRole("roleWithoutAuthorities", Set.of(DefaultAuthority.ENV), Collections.emptySet());
        User user = new DefaultUser("userWithoutAuthorities", Set.of(role));

        AuthorizationRequest request = new AuthorizationRequest(Set.of(DefaultAuthority.ENV, DefaultAuthority.BEANS));

        ILoggingEvent test = getLogEven(() -> authorizer.authorize(user, request));

        String warnMessage = String.format(
                "Authority '[%s, %s]' is not recognized and cannot be parsed. "
                        + "This may happen due to either manual interventions with token creation, "
                        + "or because of incompatible starter and master usage.",
                DefaultAuthority.BEANS, DefaultAuthority.ENV);

        assertThat(test.getLevel()).isEqualTo(Level.WARN);
        assertThat(test.getFormattedMessage()).contains(warnMessage);
        assertThatNoException().isThrownBy(() -> authorizer.authorize(user, request));
    }

    @Test
    void shouldLogWarnWhenUserLacksRequiredAuthoritiesAndContinue2() {
        Role componentsRole = new DefaultRole("componentsRole", Set.of(DefaultAuthority.ENV), Collections.emptySet());
        Role engineerRole = new DefaultRole(
                "engineerRole", Set.of(DefaultAuthority.CACHES, DefaultAuthority.BEANS), Collections.emptySet());
        Role adminRole = new DefaultRole(
                "adminRole", Set.of(DefaultAuthority.INFO, DefaultAuthority.BEANS), Set.of(componentsRole));
        User user = new DefaultUser("userWithoutAuthorities2", Set.of(adminRole, engineerRole));

        AuthorizationRequest request = new AuthorizationRequest(Set.of(
                DefaultAuthority.ENV,
                DefaultAuthority.CACHE_DISPATCHER,
                DefaultAuthority.BEANS,
                DefaultAuthority.INFO));

        ILoggingEvent test = getLogEven(() -> authorizer.authorize(user, request));

        String warnMessage = String.format(
                "Authority '[%s, %s, %s, %s]' is not recognized and cannot be parsed. "
                        + "This may happen due to either manual interventions with token creation, "
                        + "or because of incompatible starter and master usage.",
                DefaultAuthority.BEANS, DefaultAuthority.CACHE_DISPATCHER, DefaultAuthority.INFO, DefaultAuthority.ENV);

        assertThat(test.getLevel()).isEqualTo(Level.WARN);
        assertThat(test.getFormattedMessage()).contains(warnMessage);
        assertThatNoException().isThrownBy(() -> authorizer.authorize(user, request));
    }

    private ILoggingEvent getLogEven(Runnable action) {
        Logger logger = (Logger) LoggerFactory.getLogger(DefaultAuthorizer.class);

        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);

        try {
            action.run();
        } finally {
            logger.detachAppender(listAppender);
        }

        return listAppender.list.isEmpty() ? null : listAppender.list.get(0);
    }
}
