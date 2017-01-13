package org.envtools.monitor.module.querylibrary.dao.yaml;

import org.codehaus.jackson.annotate.JsonProperty;

import java.util.List;

/**
 * Created by IAvdeev on 12.01.2017.
 *
 * Meta data about query in yaml format at the beginning of sql file
 */
public class QueryMetaDao {

    @JsonProperty
    private String title;

    @JsonProperty
    private String description;

    @JsonProperty
    private List<String> params;
}
