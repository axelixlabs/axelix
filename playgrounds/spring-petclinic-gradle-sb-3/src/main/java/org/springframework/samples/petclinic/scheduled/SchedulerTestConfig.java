package org.springframework.samples.petclinic.scheduled;

import java.time.Instant;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.context.annotation.Configuration;
import org.springframework.lang.Nullable;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.TriggerContext;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

@Configuration
@EnableScheduling
public class SchedulerTestConfig implements SchedulingConfigurer {

	private static final Logger log = LoggerFactory.getLogger(SchedulerTestConfig.class);

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
		@Nullable
		public Instant nextExecution(TriggerContext triggerContext) {
			Instant lastCompletion = triggerContext.lastCompletion();
			if (lastCompletion == null) {
				return Instant.now().plusSeconds(1);
			}
			return lastCompletion.plusSeconds(2);
		}

	}

}
