---
sidebar_position: 5
---

# Beans{#main}

The **Beans** page provides comprehensive visibility into all Spring beans within your managed applications.
![beans main page](../../static/img/feature/beans/beans-main-page.png)
***Beans page as presented in Axelix UI***

This page displays every active bean with its metadata in an interactive.
A scrollable list displaying all active beans in the application, organized with expandable dropdowns
and a search function for easy navigation.

## Bean Scope Color Indicator (Right side){#scope}
A prominent, framed section on the right side that displays the scope of the currently selected bean, such as:
- **Singleton**
- **Prototype**
- **Request**
- **Session**
- **Application**
- **WebSocket**
- **Refresh** (Spring Cloud)
- **Custom scopes** (User custom scopes)
---

## Bean Details Dropdown{#details}
When you expand any bean in the list, a detailed dropdown reveals extended information about that specific bean.
![beans dropdown page](../../static/img/feature/beans/beans-dropdown-page.png)
***Bean dropdown page as presented in Axelix UI***

The dropdown displays the following information:
- **Bean Name**: The unique identifier of the bean within the Spring context
- **Class Name**: Fully qualified class name (e.g., `org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor`, 
is located under the name Bean)
- **Scope**: Current scope with visual color indicator matching the right side panel (described above)
- **Dependencies** List of other beans that this bean depends on
- **Aliases**: Alternative names for this bean (if any)
- **Proxying Strategy:**
    - **JDK_PROXY** - JDK dynamic proxy
    - **CGLIB** - CGLIB proxy
    - **NO_PROXYING** - No proxy applied
- **Qualifiers**: List of `@Qualifier` annotations applied to this bean
- **Lazy Initialized**: Indicates if the bean is lazily instantiated
- **Primary**: Primary Status whether this bean is marked as primary (`@Primary`)
- **Origin:**
    - **COMPONENT_ANNOTATION**
      - Bean originated from some variant of a `@Component` annotation.
    - **BEAN_METHOD**
        - Bean created via `@Bean` method in configuration class
        - Includes:
            - **Enclosing Class**: Class containing the `@Bean` method
            - **Method Name**: Name of the factory method
    - **FACTORY_BEAN**
        - Bean created by a `FactoryBean`   
        - Shows the class name of the factory bean that produced this bean
    - **SYNTHETIC_BEAN**
        - Bean created programmatically inside Spring Framework or libraries
        - Typically internal Spring beans or beans from libraries like Spring Data
    - **Configuration Properties bean**
        - Bean created by the `@ConfigurationProperties` annotation.
        - A bean created via a `@Bean` method in a configuration class, and annotated with `@ConfigurationProperties`, 
          is primarily classified as a **BEAN_METHOD**.
    - **UNKNOWN**
        - Origin cannot be determined
        - Usually beans registered programmatically via `BeanDefinitionRegistry`
---

## Interactive Features{#interactive}

### Navigation Icons & Quick Actions{#navigation}
The Beans page includes interactive icons that provide quick access to related functionality.\

Example: redirect icon - ![redirect icon](../../static/img/feature/icons/redirect-icon.png)

#### **Redirect Icon Location**
The redirect icon ![redirect icon](../../static/img/feature/icons/redirect-icon.png) can appear in multiple locations:

**1. Next to Bean Name**
When a bean itself is a Configuration Properties bean:
![redirect bean name](../../static/img/feature/beans/redirect-bean-name.png)

*Example: Bean with @ConfigurationProperties annotation*

**2. Next to Dependency Name**
When a bean has a dependency that is a Configuration Properties bean:
![redirect dependency](../../static/img/feature/beans/redirect-dependency.png)
*Example: Bean depending on @ConfigurationProperties bean*

**Clicking this icon 
![redirect icon](../../static/img/feature/icons/redirect-icon.png):**
- Navigates to the **Configuration Properties** page
- Automatically selects the corresponding configuration properties entry

**Example workflow:**
1. Identify a bean name with the ![redirect image](../../static/img/feature/icons/redirect-icon.png) icon (Configuration Properties bean)
2. Click the redirect icon next to the bean name
3. You're taken to the Configuration Properties page
4. The specific configuration properties for that bean are automatically expanded and highlighted

![redirect bean gif](../../static/img/feature/beans/redirect-bean-name.gif)

**Same for dependencies:**
1. Expand a bean to see its dependencies
2. Find a dependency marked with ![redirect image](../../static/img/feature/icons/redirect-icon.png) icon
3. Click the icon to navigate to that dependency's Configuration Properties
4. View the configuration properties being injected