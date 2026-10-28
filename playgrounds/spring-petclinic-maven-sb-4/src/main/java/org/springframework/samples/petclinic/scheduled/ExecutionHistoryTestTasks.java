package org.springframework.samples.petclinic.scheduled;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Tasks for checking the execution history of the {@code @Scheduled} tasks.
 */
@Component
public class ExecutionHistoryTestTasks {

	private static final Logger log = LoggerFactory.getLogger(ExecutionHistoryTestTasks.class);

	/**
	 * Every annotation declares its own task: {@code repeatable#cron0} and
	 * {@code repeatable#fixedDelay0}.
	 */
	@Scheduled(cron = "*/3 * * * * *")
	@Scheduled(fixedDelay = 5000)
	public void repeatable() throws InterruptedException {
		log.info("Running REPEATABLE task");
		Thread.sleep(120);
	}

	/**
	 * Every execution fails, so the history keeps the type and the message of the error.
	 */
	@Scheduled(fixedRate = 4000)
	public void failing() {
		throw new IllegalStateException("Failing task boom");
	}

	/**
	 * Takes noticeable time, so the history keeps the duration of the execution.
	 */
	@Scheduled(fixedDelay = 6000, initialDelay = 1000)
	public void slow() throws InterruptedException {
		log.info("Running SLOW task");
		Thread.sleep(700);
	}

	/**
	 * Runs only once, right after the start: {@code once#oneTime0}.
	 */
	@Scheduled(initialDelay = 2000)
	public void once() {
		log.info("Running ONE-TIME task");
	}

}
