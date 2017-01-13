package org.envtools.monitor.module.querylibrary.services.bootstrap;

import org.envtools.monitor.model.querylibrary.db.Category;
import org.envtools.monitor.module.querylibrary.PersistenceTestApplication;
import org.envtools.monitor.module.querylibrary.dao.CategoryDao;
import org.envtools.monitor.module.querylibrary.services.BootstrapService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * Created by IAvdeev on 11.01.2017.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = { PersistenceTestApplication.class, QueryBootstrapTestConfiguration.class  })
@TestPropertySource(locations = {
        "classpath:/persistence/application-persistence-test.properties",
        "classpath:/querylibrary/bootstrap/bootstrap-test.properties"
})
@Transactional
public class ZipArchiveBootstrapServiceIT implements ApplicationContextAware {

    @Resource(name = "querylibrary.bootstrapper.zip")
    BootstrapService bootstrapService;

    @Autowired
    CategoryDao categoryDao;

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Test
    public void testPositive() throws Exception
    {
        bootstrapService.bootstrap();
        List<Category> rootCategories = categoryDao.getRootCategories();
        Assert.assertEquals(1, rootCategories.size());
    }
}