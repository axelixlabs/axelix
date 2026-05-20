package com.sivalabs.ft.features;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@AutoConfigureMockMvc
@Sql(scripts = {"/test-data.sql"})
@EmbeddedKafka(
        partitions = 1,
        topics = {"test-topic"})
public abstract class AbstractIT {
    @Autowired
    protected MockMvcTester mvc;
}
