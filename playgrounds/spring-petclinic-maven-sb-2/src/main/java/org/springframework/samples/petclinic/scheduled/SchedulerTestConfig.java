package org.springframework.samples.petclinic.scheduled;

import java.time.Instant;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.Nullable;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.TriggerContext;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

@Configuration
@EnableScheduling
public class SchedulerTestConfig implements SchedulingConfigurer {

	private static final Logger log = LoggerFactory.getLogger(SchedulerTestConfig.class);

	private final ReentrantLock reentrantLock = new ReentrantLock(true);

	// IMPORTANT!
	// Fix for Spring Boot 2. this class `implement SchedulingConfigurer`.
	// see TaskSchedulingAutoConfiguration, method taskScheduler(TaskSchedulerBuilder
	// builder),
	// please note @ConditionalOnMissingBean(SchedulingConfigurer.class ...).
	@Bean
	@ConditionalOnMissingBean(TaskScheduler.class)
	public ThreadPoolTaskScheduler taskScheduler() {
		ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
		scheduler.setPoolSize(3);
		scheduler.setThreadNamePrefix("axelix-task-scheduler-");
		scheduler.setWaitForTasksToCompleteOnShutdown(true);
		scheduler.setAwaitTerminationSeconds(5);
		scheduler.initialize();
		return scheduler;
	}

	@Scheduled(fixedDelay = 10, timeUnit = TimeUnit.SECONDS)
	public void reEntrantLockTask() {
		try {
			reentrantLock.lock();

			log.info("Thread {} with id {} ENTERED the re-entrant lock", Thread.currentThread().getName(),
					Thread.currentThread().getId());

			Thread.sleep(120000);
		}
		catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		finally {
			reentrantLock.unlock();
			log.info("Thread {} with id {} RELEASED the re-entrant lock", Thread.currentThread().getName(),
					Thread.currentThread().getId());
		}
	}

	@Scheduled(fixedDelay = 10, timeUnit = TimeUnit.SECONDS)
	public void synchronizedBlockTask() throws InterruptedException {
		synchronized (this) {
			log.info("Thread {} with id {} ENTERED the synchronized lock", Thread.currentThread().getName(),
					Thread.currentThread().getId());

			Thread.sleep(120000);

			log.info("Thread {} with id {} RELEASED the synchronized lock", Thread.currentThread().getName(),
					Thread.currentThread().getId());
		}
	}

	/**
	 * CRON
	 */
	@Scheduled(cron = "*/2 * * * * *")
	public void alive() {
		log.info("alive task");
	}

	/**
	 * CRON
	 */
	@Scheduled(cron = "*/5 * * * * *")
	public void cronTask() {
		log.info("Running CRON task");
	}

	/**
	 * fixedDelay
	 */
	@Scheduled(fixedDelay = 2000)
	public void fixedDelayTask() throws InterruptedException {
		log.info("Running FIXED_DELAY task");
		Thread.sleep(50);
	}

	/**
	 * fixedRate
	 */
	@Scheduled(fixedRate = 2000, initialDelay = 100)
	public void fixedRateTask() {
		log.info("Running FIXED_RATE task");
	}

	/**
	 * Custom Trigger
	 */
	@Override
	public void configureTasks(ScheduledTaskRegistrar registrar) {
		registrar.addTriggerTask(this::customTriggerTask, new CustomTrigger());
	}

	private void customTriggerTask() {
		log.info("Running CUSTOM trigger task");
	}

	static class CustomTrigger implements Trigger {

		@Override
		public Date nextExecutionTime(TriggerContext triggerContext) {
			Date lastCompletion = triggerContext.lastCompletionTime();
			if (lastCompletion == null) {
				return Date.from(Instant.now().plusSeconds(1));
			}
			return Date.from(lastCompletion.toInstant().plusSeconds(2));
		}

	}

}
