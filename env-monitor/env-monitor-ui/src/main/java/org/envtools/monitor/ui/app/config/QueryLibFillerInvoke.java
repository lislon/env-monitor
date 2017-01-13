package org.envtools.monitor.ui.app.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.envtools.monitor.module.querylibrary.services.filler.QueryLibDbFiller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * Created by sergey on 20.03.2016.
 */

@Component
@Transactional
public class QueryLibFillerInvoke {

    private static final Logger LOGGER = LoggerFactory.getLogger(QueryLibFillerInvoke.class);

    @PersistenceContext
    protected EntityManager entityManager;


    @Transactional
    public void createDb(boolean createSampleTestHistory) {
        QueryLibDbFiller.fillDatabase(entityManager, createSampleTestHistory);
    }
}
