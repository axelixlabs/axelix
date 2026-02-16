---
sidebar_position: 11
---

# Thread Dump {#thread-dump}
The **Thread Dump** page provides a convenient interface for analyzing all threads present in the Spring Boot application.

![thread dump main page](../../static/img/feature/thread-dump/thread-dump-main-page.png)
***Thread Dump page as presented in Axelix UI***

A scrollable list displaying all threads in the application, a convenient view of thread states at a 
specific point in time, search functionality with a thread counter for easier navigation, 
and setting <img src="/img/feature/icons/settings-icon.png" alt="settings-icon" width="20" height="20"/> to enable or disable thread contention monitoring.

---

## Thread Dump Details {#thred-dump-details}
![thread dump details page](../../static/img/feature/thread-dump/thread-dump-details-page.png)
***Thread Dump details page as presented in Axelix UI***

You can examine the state of a thread in more detail for a specific time range. To do so, 
select the desired time range, after which detailed thread information will be available.

- **ThreadId**:                  ID of the thread.
- **ThreadName**:                Name of the thread.
- **ThreadState**:               State of the thread.
- **Priority**:                  Priority of the thread.
- **BlockedCount**:              Total number of times that the thread has been blocked.This metric can be enabled or disabled.
- **BlockedTime**:               Time in milliseconds that the thread has spent blocked.
- **WaitedCount**:               Total number of times that the thread has waited for notification. This metric can be enabled or disabled.
- **WaitedTime**:                Time in milliseconds that the thread has spent waiting.
- **Daemon**:                    Whether the thread is a daemon thread.
- **Locked by**
  - **Class**:                   Fully qualified class name of the lock object.
  - **IdentityHashCode**:        Identity hash code of the lock object.
- **StackTrace**:                Stack trace of the thread.

### Color coding indicating the thread state {#color-coding}

|                                         Indication                                          | Description          |
|:-------------------------------------------------------------------------------------------:|----------------------|
|    <img src="/img/feature/thread-dump/running-green-state.png" width="25" height="25"/>     | RUNNABLE             |
|     <img src="/img/feature/thread-dump/running-blue-state.png" width="25" height="25"/>     | RUNNABLE is native   |
|    <img src="/img/feature/thread-dump/waiting-yellow-state.png" width="25" height="25"/>    | WAITING              |
|    <img src="/img/feature/thread-dump/waiting-orange-state.png" width="25" height="25"/>    | WAITING is suspended |
| <img src="/img/feature/thread-dump/timed-waiting-orange-state.png" width="25" height="25"/> | TIMED_WAITING        |
|    <img src="/img/feature/thread-dump/blocked-red-state.png" height="25"/>                  | BLOCKED              |
|   <img src="/img/feature/thread-dump/terminated-grey-state.png" width="25" height="25"/>    | TERMINATED           |
|     <img src="/img/feature/thread-dump/other-purple-state.png" width="25" height="25"/>     | other                |