---
sidebar_position: 8
---

# Conditions{#main}

The **Conditions** page provides information about the configured conditions in the Spring Boot application.

![conditions main page](../../static/img/feature/conditions/conditions-main-page.png)
***Conditions page as presented in Axelix UI***

---

## TAB: Negative Matches{#negative}
**A scrollable list displaying all conditions whose requirements do not match.**

![conditions negative matches page](../../static/img/feature/conditions/conditions-not-matches-details-page.png)
***Conditions negative matches page as presented in Axelix UI***

- **ClassName**    The name of the class annotated with a conditional annotation, or the class that contains it.
- **MethodName**   The name of the method on which the conditional annotation was put.
- **NotMatched**   List of conditions that were not matched.
- **Matched**      List of conditions that were matched.
- **Message**      Descriptive message explaining why the condition matched or did not match.


## TAB: Positive Matches{#positive}
**A scrollable list displaying all conditions whose requirements match.**

![conditions matches page](../../static/img/feature/conditions/conditions-matches-details-page.png)
***Conditions matches page as presented in Axelix UI***

- **ClassName**   The name of the class annotated with a conditional annotation, or the class that contains it.
- **MethodName**  The name of the method on which the conditional annotation was put.
- **Matched**     List of conditions that were evaluated and matched.
- **Message**     Descriptive message explaining why the condition matched.