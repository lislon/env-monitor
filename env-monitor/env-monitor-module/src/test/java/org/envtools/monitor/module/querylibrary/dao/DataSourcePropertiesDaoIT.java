package org.envtools.monitor.module.querylibrary.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.envtools.monitor.model.querylibrary.db.DataSourceProperty;
import org.envtools.monitor.module.querylibrary.PersistenceTestApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;
import org.testng.Assert;

import java.util.List;

/**
 * Created: 07.03.16 21:34
 *
 * @author Anastasiya Plotnikova
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = PersistenceTestApplication.class)
@TestPropertySource(locations = "classpath:/persistence/application-persistence-test.properties")
@Transactional
public class DataSourcePropertiesDaoIT {
    private static final Logger LOGGER = LoggerFactory.getLogger(DataSourceDaoIT.class);

    @Autowired
    DataSourcePropertiesDao dataSourcePropertiesDao;

    private static final String PARAM = "gjhghjg ZERO";
    private static final String VALUE = "ZERO";
    // private static final String QUERY_SEARCH_ABSENT = "WHAT";

    @Test
    public void testDataSourcePropertiesContains() {

        Assert.assertTrue(PARAM.contains(VALUE));
        createWithText(VALUE);
        List<DataSourceProperty> foundQueries = dataSourcePropertiesDao.getValueByText(VALUE);
        Assert.assertEquals(1, foundQueries.size());

        createWithText(VALUE);
        List<DataSourceProperty> foundQueries1 = dataSourcePropertiesDao.getValueByText(VALUE);
        Assert.assertEquals(2, foundQueries1.size());


        LOGGER.info("Found queries: " + foundQueries);

    }

    @Test
    public void testDataSourcePropertiesNotContains() {

        Assert.assertFalse(VALUE.contains(PARAM));

        createWithText(VALUE);

        List<DataSourceProperty> foundQueries = dataSourcePropertiesDao.getValueByText(PARAM);
        Assert.assertEquals(0, foundQueries.size());

        LOGGER.info("Found queries: " + foundQueries);

    }

    private DataSourceProperty createWithText(String text) {
        DataSourceProperty dataSourceProperty = new DataSourceProperty();
        //Don't set Id - it will be auto generated
        dataSourceProperty.setProperty("dfgdfg");
        dataSourceProperty.setValue(text);

        return dataSourcePropertiesDao.saveAndFlush(dataSourceProperty);
    }
}
