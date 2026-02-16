---
sidebar_position: 7
---

# Scheduled Tasks{#main}
The “Scheduled Tasks” page provides access to all tasks annotated with `@Scheduled` within a Spring Boot application.
You can also update the task status, modify its schedule, and trigger it manually when required.

![scheduled tasks main page](../../static/img/feature/scheduled-tasks/scheduled-tasks-main-page.png)
***Scheduled Tasks as presented in Axile UI***

A scrollable list displaying all configured `Scheduled Tasks` in the application, grouped by property types:
**fixed delay**, **fixed rate** and **cron**, with a search function for scheduled tasks to enable easy navigation.

---

## Scheduled Tasks Details{#details}

### Cron{#cron}    
![cron expression page details](../../static/img/feature/scheduled-tasks/cron-expression-page-details.png)
***Scheduled Tasks as presented in Axile UI***

*A scheduled task with an exact execution configuration.*
- **Runnable**:                 The *target* that will be executed.
- **Cron expression**:          The cron expression (e.g., "0 1 1 5 7 3" or "0 0/15 9-17 ? * MON,WED,FRI" (seconds minutes hours day_of_month month day_of_week)) (See **Interactive Features**)
- **Status**:                   Shows the target state and provides the ability to enable <img src="/img/feature/icons/switch-on-icon.png" alt="switch-on-icon" width="32" height="15"/>
                                or disable <img src="/img/feature/icons/switch-off-icon.png" alt="switch-off-icon" width="32" height="15"/>
                                the target. (See **Interactive Features**)
- **Run**:                      Forces immediate execution of the task. (See **Interactive Features**)

### Fixed delay{#fixed-delay} 
![fixed rate page details](../../static/img/feature/scheduled-tasks/fixed-rate-page-details.png)
***Scheduled Tasks as presented in Axile UI***

*Schedules Tasks with the interval between completing tasks, counted from the end of the previous task.*
- **Runnable**:                 The *target* that will be executed.
- **Initial Delay (ms)**:       The delay, in milliseconds, before first execution.
- **Interval (ms)**:            The interval, in milliseconds, between the start of each execution. (See **Interactive Features**)
- **Status**                    Shows the target state and provides the ability to enable <img src="/img/feature/icons/switch-on-icon.png" alt="switch-on-icon" width="32" height="15"/>
                                or disable <img src="/img/feature/icons/switch-off-icon.png" alt="switch-off-icon" width="32" height="15"/>
                                the target. (See **Interactive Features**)
- **Run**:                      Forces immediate execution of the task. (See **Interactive Features**)

### Fixed rate{#fixed-rate}  
![fixed delay page details](../../static/img/feature/scheduled-tasks/fixed-delay-page-details.png)
***Scheduled Tasks as presented in Axile UI***

*Schedules Tasks with the interval between completing tasks, counted from the start of the previous task.*
- **Runnable**:                 The *target* that will be executed.
- **Initial Delay (ms)**:       The delay, in milliseconds, before first execution.
- **Interval (ms)**:            The interval, in milliseconds, between the start of each execution. (See **Interactive Features**)
- **Status**                    Shows the target state and provides the ability to enable <img src="/img/feature/icons/switch-on-icon.png" alt="switch-on-icon" width="32" height="15"/> 
                                or disable <img src="/img/feature/icons/switch-off-icon.png" alt="switch-off-icon" width="32" height="15"/> 
                                the target. (See **Interactive Features**)
- **Run**:                      Forces immediate execution of the task. (See **Interactive Features**)

---

## Interactive Features{#interactive}

### Run{#run}
We provide the ability to trigger a task manually without affecting its schedule. To do so, simply click <img src="/img/feature/icons/play-icon.png" alt="play-icon" width="20" height="20"/>, 
and the task will be executed immediately.

### Interval/Cron expression{#modify}
You have a convenient way to modify the interval and cron expression of a scheduled task in real time.
1. Click <img src="/img/feature/icons/overwrite-icon.png" alt="overwrite-icon" width="20" height="20"/> next to the schedule you want to modify.
2. An interactive dialog will open, allowing you to make changes. After making changes, click the <img src="/img/feature/icons/cancel-icon.png" alt="cancel-icon" width="20" height="20"/> to cancel the change,
   or the <img src="/img/feature/icons/save-icon.png" alt="save-icon" width="20" height="20"/> to confirm the action.
3. Once confirmed, the task will follow the new schedule.

### Status{#status}
We provide the ability to manage the target state. The initial state of each target is (on) 
<img src="/img/feature/icons/switch-on-icon.png" alt="switch-on-icon" width="32" height="15"/>, 
meaning the task is executed according to the configured schedule. When the **Status** is switched to (off) 
<img src="/img/feature/icons/switch-off-icon.png" alt="switch-off-icon" width="32" height="15"/>, 
a target that is currently executing its scheduled work continues until completion and then ignores the schedule.
To return the target to its initial state, in which the task follows the scheduled execution, it is necessary to switch 
the **Status** back to (on) <img src="/img/feature/icons/switch-on-icon.png" alt="switch-on-icon" width="32" height="15"/>.