package com.sivalabs.ft.notifications

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.kafka.test.context.EmbeddedKafka

@EmbeddedKafka(partitions = 1, topics = ["test-topic"])
@SpringBootTest
class NotificationServiceApplicationTests {
    @Test
    fun contextLoads() {
    }
}
