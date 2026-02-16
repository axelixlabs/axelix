---
sidebar_position: 4
---

# Properties{#main}
The Properties page provides complete visibility into all configuration properties and their sources within 
Spring Boot application. It shows the current values, where each value came from and the priority hierarchy 
of property sources.

![properties main page](../../static/img/feature/properties/properties-main-page.png)
***Properties as presented in Axelix UI***

A scrollable list of all active properties in the application, grouped by their source. It uses expandable 
sections (expanded by default), provides search functionality for easier navigation, and displays a counter 
of active properties.

- **Name**:            The sourceName of the property source.
- **Description**:     Hover your cursor over the icon ![info icon](../../static/img/feature/icons/info-icon.svg)
                       to see the custom description of this property source.
- **Properties**:      The list of property entries.

---

## Properties Details{#details}
![properties details page](../../static/img/feature/properties/properties-details-page.png)
***Properties as presented in Axelix UI***

The dropdown displays the following information:
- **Name**:                  The property name.
- **Deprecation**            If a property is deprecated, its background color is displayed in red. 
- **Value**:                 The property `value` currently used by the Spring Boot application.
- **isPrimary**:             Whether this property value is primary (i.e. this value takes precedence over the other values 
                             from other property sources). ![crown icon](../../static/img/feature/icons/crown-icon.svg)
- **ConfigPropsBeanName**:   The propertyName of the configProps (if any) bean onto which this property maps. 
                             Find a dependency marked with ![redirect image](../../static/img/feature/icons/redirect-icon.png) icon.
- **Description**            The description from spring-configuration-metadata.json.
- **InjectionPoints**        The injection points where this property is used. 
                             Find a dependency marked with ![redirect image](../../static/img/feature/icons/redirect-icon.png) icon.

:::info
By default, the property value is hidden as `*****`. To enable value visibility,
you must add the property `management.endpoint.env.show-values` with the value `always`
to the Application properties files.
:::

---

## Interactive Features{#interactive}

We provide the ability to change the property value:
1) To do this, click <img src="/img/feature/icons/overwrite-icon.png" alt="overwrite-icon" width="20" height="20"/> next to the selected property’s value.
2) After making changes, click the <img src="/img/feature/icons/cancel-icon.png" alt="cancel-icon" width="20" height="20"/> to cancel the change,
   or the <img src="/img/feature/icons/save-icon.png" alt="save-icon" width="20" height="20"/> to confirm the action.
3) After that, a pop-up message `Unexpected server error. Please, re-try request later.` will appear,
   and further interaction with the service will be unavailable until the Spring Boot application context is fully restarted.

:::warning
It is important to note that changing the value of any property triggers a restart of the Spring Boot application context,
which may lead to unpredictable consequences, **such as the application failing to start, degraded performance, or impacts on data integrity or security**.
:::