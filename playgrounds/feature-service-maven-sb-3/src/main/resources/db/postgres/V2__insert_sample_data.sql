insert into products (id, code, prefix, name, description, image_url, disabled, created_by, created_at) values
(1, 'intellij', 'IDEA', 'IntelliJ IDEA', 'JetBrains IDE for Java', 'https://resources.jetbrains.com/storage/products/company/brand/logos/IntelliJ_IDEA.png', false, 'admin', '2024-03-01 00:00:00'),
(2, 'goland','GO','GoLand', 'JetBrains IDE for Go', 'https://resources.jetbrains.com/storage/products/company/brand/logos/GoLand.png',false, 'admin','2024-03-01 00:00:00'),
(3, 'webstorm','WEB','WebStorm', 'JetBrains IDE for Web Development','https://resources.jetbrains.com/storage/products/company/brand/logos/WebStorm.png', false, 'admin','2024-03-01 00:00:00'),
(4, 'pycharm','PY','PyCharm', 'JetBrains IDE for Python', 'https://resources.jetbrains.com/storage/products/company/brand/logos/PyCharm.png',false, 'admin','2024-03-01 00:00:00'),
(5, 'rider','RIDER','Rider', 'JetBrains IDE for .NET', 'https://resources.jetbrains.com/storage/products/company/brand/logos/Rider.png',false, 'admin','2024-03-01 00:00:00')
;

insert into releases (id, product_id, code, description, status, created_by, created_at) values
(1, 1, 'IDEA-2025.1', 'IntelliJ IDEA 2025.1', 'RELEASED', 'admin','2025-04-14'),
(2, 1, 'IDEA-2025.2', 'IntelliJ IDEA 2025.2', 'DRAFT', 'admin','2024-02-25'),
(3, 2, 'GO-2025.1', 'GoLand 2025.1', 'RELEASED', 'admin','2024-02-15'),
(4, 3, 'WEB-2025.1', 'WebStorm 2025.1', 'RELEASED', 'admin','2024-02-20'),
(5, 4, 'PY-2025.1', 'PyCharm 2025.1', 'RELEASED', 'admin','2024-02-20'),
(6, 5, 'RIDER-2025.1', 'Rider 2025.1', 'RELEASED', 'admin','2024-02-16')
;

insert into features (id, product_id, release_id, code, title, description, status, created_by, assigned_to, created_at) values
(1, 1, 1, 'IDEA-358562', 'Support Gradle Daemon Toolchains in UI', 'Gradle 8.8 introduced JVM toolchains support for the Gradle daemon itself: official doc.This feature works seamlessly with the current IDEA integration.', 'RELEASED', 'siva', 'marcobehler', '2024-08-30'),
(2, 1, 1, 'IDEA-360676', 'Create Spring beans live templates', 'Provide the ability to create static or package private Spring components via live templates.
The following live templates should be implemented: Repository, Component, Controller, Service, Configuration', 'RELEASED', 'daniiltsarev', 'siva', '2024-10-15'),
(3, 1, 1, 'IDEA-352694', 'Add to bean completion Jpa repositories which do not exists', 'Add Spring repositories for bean completion for entities for which there are no JPA repositories. Similar functionality is already implemented in the JPA Buddy plugin.', 'RELEASED', 'antonarhipov', 'andreybelyaev', '2024-04-29'),
(4, 1, 1, 'IDEA-364607', 'Structure View: Spring Security Config bean', 'Customize the structure for SecurityBuilder beans.', 'RELEASED', 'antonarhipov', 'andreybelyaev', '2024-04-29'),
(5, 1, 2, 'IDEA-264396', 'Implement support of mvnd (Maven Daemon)', 'Implement support of mvnd https://github.com/apache/maven-mvnd', 'RELEASED', 'antonarhipov', 'andreybelyaev', '2024-04-29'),
(6, 1, 2, 'IDEA-358714', 'Spring Modulith: mark modules in the project view', 'To reduce a cognitive load, we mark packages with green and red locks to indicate whether classes inside a particular package are allowed to use in other places.', 'RELEASED', 'antonarhipov', 'andreybelyaev', '2024-09-03'),
(7, 1, 2, 'IDEA-361226', 'Create Spring beans live templates for Kotlin', 'Provide the ability to create Spring components via live templates.
The following live templates should be implemented: Repository, Component, Controller, Service, Configuration', 'RELEASED', 'antonarhipov', 'andreybelyaev', '2024-10-23'),
(8, 1, 2, 'IDEA-366632', 'Spring Data JDBC: Structure for JDBC entity class should show attribute mappings', 'Though the datasource is assigned for the JDBC entity, the columns are resolved and navigation from @Table and @Column annotations to the Database view works, the Structure view doesn''t show the mappings for the attributes.', 'RELEASED', 'antonarhipov', 'andreybelyaev', '2025-01-29'),
(9, 1, 2, 'IDEA-370469', 'Optimize sealed class inheritor search to explicitly permitted ones', 'When we search for sealed class/interface inheritors, we currently ignore the ''sealed'' modifier. Sometimes, it makes the search much longer than necessary (e.g., if the sealed interface looks like a functional interface, so we use functional interface search). We should recognize the sealed modifier and perform a limited search:
* If there''s no permits-list, search in the current file only
* If there''s a permits-list, resolve all classes from permits list and return them.
This should make it much faster, and also will help to support sealed hierarchies in scratch files (IDEA-326216), as no index will be necessary for this.', 'RELEASED', 'antonarhipov', 'andreybelyaev', '2025-04-07'),
(10, 1, null, 'IDEA-399910', 'Support Spring Initializr Bookmarks', 'Spring Initializr supports bookmarks feature to pre-configure the starters. Support the same in Spring Boot project creation wizard.', 'NEW', 'siva', 'andreybelyaev', '2025-05-10'),
(11, 2, 3, 'GO-18211', 'Introduce golangci-lint checks on commit', 'Introduce golangci-lint checks on commit in the same way as we do for Go Fmt', 'RELEASED', 'antonarhipov', 'andreybelyaev', '2024-01-14'),
(12, 2, 3, 'GO-13698', 'Update struct tag when renaming field', 'Refactored a variable name from «ForceMove» into «ForceMovePawn». This was picked up everywhere except in this struct tag literal', 'RELEASED', 'antonarhipov', 'andreybelyaev', '2024-01-14'),
(13, 3, 4, 'WEB-70929', 'Prisma: Support multi-line comments syntax', 'Website: https://plugins.jetbrains.com/plugin/20686-prisma-orm, Source: https://github.com/JetBrains/intellij-plugins/tree/master/prisma', 'RELEASED', 'antonarhipov', null, '2024-01-14'),
(14, 4, null, 'PY-79258', 'Enable django admin by default for the new Django projects', 'For the new projects that use the Django project template, please enable the advanced option ''Enable Django admin'' by default. The majority of Django projects use this feature, and almost all Django tutorials do: creating projects using templates and default settings is more popular among new developers, so with this setting on by default, we''ll make our project template more suitable.', 'IN_PROGRESS', 'antonarhipov', null, '2024-01-14'),
(15, 5, null, 'RIDER-29517', 'Syntax visualizer should be available in Rider', 'Visual Studio has a nice feature which allows you to visualize a syntax tree of your code. It is super helpful when you write Roslyn analyzers and you try to understand how to process given code block. As Rider doesnt have this feature yet, I have to switch between Rider and VS all the time.', 'IN_PROGRESS', 'antonarhipov', null, '2024-01-14')
;
