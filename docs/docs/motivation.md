---
sidebar_position: 2
---

# Motivation

This document explains the core problems that Axile solves for Spring Boot developers and organizations.
Through real-world examples, we'll explore the pain points that inspired Axile's creation.

## Motivation. Prologue

The typical enterprise has a lot of internally-written software. This software solves various problems and is typically
written in a number of programming languages, such as JavaScript, Java, Go etc. One of the most popular choices for
the server-side/backend applications in large enterprises 
[is Java](https://en.wikipedia.org/wiki/Programming_languages_used_in_most_popular_websites). This is especially the case
in banking domain.

So, apart from that, the most popular framework for writing applications in Java on the backend
[is, indisputable, Spring Boot](https://en.wikipedia.org/wiki/Spring_Boot). It is an additional layer on top 
of [Spring Framework](https://spring.io/) that simplifies the configuration of the latter. It is by far the most popular
choice for enterprises because of various reasons. For example, Netflix 
[have migrated its ecosystem (more than 3000 applications) to Spring Boot 3](https://www.youtube.com/watch?v=IMGcD08rdVw).
So, again, the idea is that the Spring Boot is a very popular choice on the backend when writing Java applications.

## Motivation. Pain Point

As stated earlier, Spring Boot itself started out as just an additional abstraction layer over the Spring Framework,
but now it is much more. And all Java applications that run on Spring Boot operate over the same Spring Boot abstractions.

For instance:
- Each Spring Boot Java application consists of a number of _Beans_, which are just Java objects whose management to some
- extent is managed by Spring Framework.
- Each Spring Boot application has an `ApplicationContext`, whose primary purpose is to manage the Beans of the applications
- and the overall state of the running Spring Boot application
- Each Spring Boot application has quite a number of properties that can come from various sources 
- (`application.properties` file, or from OS process environment, or from Java System properties etc).

So, the idea is that Spring is quite large, and it creates a lot of moving parts, where each part affect
the other (e.g. properties affect bean creation, created beans can bring up more properties etc.)

**And one of the core reasons why this project exists is because if the Spring Boot application is working locally
as expected, if all the test cases pass, it does not mean at all that the same application would work perfectly
when deployed on production or testing environment. We all know that assuming everything will be fine is simply not true.**

And, following that, it is often the case that when we, as Java developers that use Spring Boot, deploy our applications
on any environment, we encounter the same problems again and again and again. Let's walk through a couple of examples
that demonstrate the problem.

### Example 1. Fine-grained Logging

To deeply understand what exactly Axile is trying to solve, let's go through a couple examples of Java code. Imagine that we have the following Java class in our application. It is a Spring Bean, that we use to resolve the set of permissions that the hypothetical client possess:

```java
@Slf4j
@Service
public class PermissionsResolver {

    @Value("${permission.caching.enabled:true}")
    private boolean cacheLookupsEnabled;

    @Autowired
    private ExternalPermissionsResolver delegate;

    @Autowired
    private PermissionsCache cache;

    @PostConstruct
    void init() {
        log.debug("Permissions cache lookup enabled: {}", cacheLookupsEnabled);
    }

    public Set<Permission> resolveClientPermissions(Client client) {
        log.debug("Resolving permissions for the client {}", client.getId());

        try {
            return delegate.resolve(client);
        } catch (SomeExpectedException e) {
            // if we can, we do a cache lookup
            if (featureFlag && client.getStatus() == ClientStatus.EXISTING) {
                return cache.get(client.getId());
            } else {
                throw e;
            }
        }
    }
}
```

We have written this code. We have also written the tests for it. We tested that locally. 
It worked. Now, we deploy this code to our sandbox environment.

But, because of the Murphy's Law:\
***Anything that can go wrong will go wrong***\
Something is not working correctly on our sandbox/testing environment. But we have tested locally - it worked, but 
on testing environment - it suddenly does not. Although, we have the logs in the class, that would help us understand
what is actually going on inside the deployed app, these log statements are in `DEBUG` mode, and we only have
the `INFO` mode enabled. **It would be great to change the log level temporarily(!), on the fly, only for that specific
Java class or a specific Java package.**
Can we change the logging level in the application configuration and re-deploy the app - yes, we definitely can,
but do we really want to go through the entire process of re-build and re-deploy just to check the logs? 
And what if the logs do not help? Build and re-deploy again?

**At Axile, we deeply understand this pain very deeply.** And we want to provide a way to solve it.

### Example 2. Sources of Configuration

Okay, let's assume that we solved the first problem, and we manged to enable the logging via Axile UI for that specific
Java class. Now, we have another problem. Suppose that we understood, that in our case the issue arises because 
the incorrect property was injected into the `cacheLookupsEnabled` property. We expected the for the client _XYZ_ 
we would make take the data from the cache, but we did not. And we did not do that because of `cacheLookupsEnabled` 
being set to `false`.

Now, what shell we do then? The only thing we know is that this property is set to `false`, and we did not do that.
Well, it means, _somewhere_, in some configuration source of our application it was set to `false`.
But again, where exactly?

**And let's be honest, we as developers in the large enterprise do not always even understand how exactly our App gets
deployed on the environment.** We do not always fully understand how our application is packaged into 
the Docker container, how is then deployed via Helm chart to our K8S clusters, what additional configuration sources
are applied to our application etc. **We have _some_ understanding of how our app is deployed, but this understanding
is incomplete, that is the core problem.**

In Axile, we not only provide the ways to see from which exactly configuration source did the value for the property
come from, we also provide the ways to change the value of the property on the fly on the live Java application 
by re-loading the application context on the hot JVM, which does not require re-build/re-deploy.

### Example 3. Application's State

Now, we have changed the `cacheLookupsEnabled` value to `true`, and everything should be fine, right? **Wrong**.
It still does not work. And it does not work because the `PermissionsCache` is actually the interface:

```java

public interface PermissionsCache {

    Set<Permission> get(UUID clientId);
}

@Component
@ConditionalOnProperty(prefix = "permissions.cache", value = "source", havingValue = "in-memory", matchIfMissing = true)
@ConditionalOnMissingBean(PermissionsCache.class)
public class InMemoryPermissionsCache {

    @Override
    public Set<Permission> get(UUID clientId) {
        // implementation
    }
}

@Component
@ConditionalOnProperty(prefix = "permissions.cache", value = "source", havingValue = "redis")
@ConditionalOnRedisPresent
@ConditionalOnMissingBean(PermissionsCache.class)
public class RedisPermissionsCache {

    @Override
    public Set<Permission> get(UUID clientId) {
        // implementation
    }
}

```

I think everybody agrees, that it is very often the case having an `interface` that defines the contract, and we have
various implementations of this Java interface, each of which is a Spring bean. We expect those Spring beans to be
bootstrapped only when some conditions are met. This notorious `ConditionalOnProperty`, `ConditionalOnMissingBean` etc
are familiar to pretty much every Spring developer.

And let's assume that in our case, we expected for the permissions of given client to be taken from Redis cache,
not from in-memory data structure. But, in reality, the `InMemoryPermissionsCache` was queried. Are we even sure it was
an `InMemoryPermissionsCache`? Maybe some corporate library that we import as a dependency brings its 
own `ThreadSafeInMemoryPermissionsCache`? In other words, we want to answer the questions:

- ***What*** beans are currently active in the Spring application?
- ***Why*** those exact beans are active, and not the others?

**Axile helps you do that as well.**
