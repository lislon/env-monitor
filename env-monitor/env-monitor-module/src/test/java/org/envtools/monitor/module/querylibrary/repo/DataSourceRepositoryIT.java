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
 * Created by anastasiya on 07.03.16.
 */

//See http://g00glen00b.be/testing-spring-data-repository/
//for the detailed description

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = PersistenceTestApplication.class)
@TestPropertySource(locations = "classpath:/persistence/application-persistence-test.properties")

//@TestExecutionListeners({DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class,
//        TransactionalTestExecutionListener.class, DbUnitTestExecutionListener.class})
//@DatabaseSetup(LibQueryRepositoryIT.DATASET)
//@DatabaseTearDown(type = DatabaseOperation.DELETE_ALL, value = { LibQueryRepositoryIT.DATASET })
//@DirtiesContext
@IntegrationTest
public class DataSourceRepositoryIT {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataSourceRepositoryIT.class);

    public static final String DATASET = "classpath:/persistence/dbunit/data-source-repo-test.xml";

//    @Autowired
//    LibQueryRepository libQueryRepository;

}
