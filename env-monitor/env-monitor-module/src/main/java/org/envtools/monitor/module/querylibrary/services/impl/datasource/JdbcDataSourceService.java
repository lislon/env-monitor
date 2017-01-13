package org.envtools.monitor.module.querylibrary.services.impl.datasource;

import com.google.common.collect.ImmutableSet;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.envtools.monitor.common.util.PropertyUtil.getRequiredValue;

/**
 * Created: 13.03.16 1:38
 *
 * @author Yury Yakovlev
 */
@Service
public class JdbcDataSourceService extends AbstractDataSourceService<BasicDataSource> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractDataSourceService.class);

    public static final String DRIVER_CLASS_NAME_PROPERTY_NAME = "driverClassName";
    public static final String URL_PROPERTY_NAME = "url";
    public static final String USER_PROPERTY_NAME = "user";
    public static final String PASSWORD_PROPERTY_NAME = "password";

    private static final Set<String> EXCLUDED_PROPERTY_NAMES = ImmutableSet.of(
            PASSWORD_PROPERTY_NAME
    );

    @Override
    protected Map<String, String> getKeyParams(Map<String, String> allParams) {
        //No params are excluded
        Map<String, String> keyParams =
                allParams.entrySet()
                        .stream()
                        .filter(p -> !EXCLUDED_PROPERTY_NAMES.contains(p.getKey()))
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        return keyParams;
    }

    @Override
    protected BasicDataSource createDataSource(Map<String, String> params) {
        BasicDataSource ds = new BasicDataSource();

        ds.setDriverClassName(getRequiredValue(params, DRIVER_CLASS_NAME_PROPERTY_NAME));
        ds.setUrl(getRequiredValue(params, URL_PROPERTY_NAME));
        ds.setUsername(getRequiredValue(params, USER_PROPERTY_NAME));
        ds.setPassword(getRequiredValue(params, PASSWORD_PROPERTY_NAME));

        return ds;
    }

    @Override
    protected void close(BasicDataSource dataSource) {
        try {
            dataSource.close();
        } catch (SQLException e) {
            LOGGER.error(String.format(
                    "Could not close data source %s",
                    ToStringBuilder.reflectionToString(dataSource)), e);
        }
    }
}
