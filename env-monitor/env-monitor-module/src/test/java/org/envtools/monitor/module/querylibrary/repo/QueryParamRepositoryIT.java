package org.envtools.monitor.module.querylibrary.repo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.envtools.monitor.module.querylibrary.PersistenceTestApplication;
import org.junit.runner.RunWith;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Created: 10.03.16 21:46
 *
 * @author Anastasiya Plotnikova
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = PersistenceTestApplication.class)
@TestPropertySource(locations = "classpath:/persistence/application-persistence-test.properties")

@IntegrationTest
public class QueryParamRepositoryIT {
    private static final Logger LOGGER = LoggerFactory.getLogger(QueryParamRepositoryIT.class);

    public static final String DATASET = "classpath:/persistence/dbunit/lib-query-repo-test.xml";
}
