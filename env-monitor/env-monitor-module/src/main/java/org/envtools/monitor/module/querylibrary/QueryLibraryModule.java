package org.envtools.monitor.module.querylibrary;

import org.apache.log4j.Logger;
import org.envtools.monitor.common.serialization.Serializer;
import org.envtools.monitor.model.messaging.RequestMessage;
import org.envtools.monitor.model.messaging.ResponseMessage;
import org.envtools.monitor.model.messaging.ResponseType;
import org.envtools.monitor.model.querylibrary.execution.*;
import org.envtools.monitor.model.querylibrary.execution.view.QueryExecutionResultView;
import org.envtools.monitor.model.querylibrary.provider.QueryLibraryAuthProvider;
import org.envtools.monitor.model.querylibrary.updates.DataOperation;
import org.envtools.monitor.module.AbstractPluggableModule;
import org.envtools.monitor.module.ModuleConstants;
import org.envtools.monitor.module.querylibrary.services.*;
import org.envtools.monitor.module.querylibrary.services.impl.updates.TreeUpdateTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.SubscribableChannel;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created: 12.03.16 19:13
 *
 * @author Yury Yakovlev
 */
public class QueryLibraryModule extends AbstractPluggableModule {


    private static final Logger LOGGER = Logger.getLogger(QueryLibraryModule.class);

    public static final Map<String, Class<?>> PAYLOAD_TYPES = new HashMap<String, Class<?>>() {
        {
            put("execute", QueryExecutionRequest.class);
            put("executeNext", QueryExecutionNextResultRequest.class);
            put("cancel", QueryExecutionCancelRequest.class);
            put("dataOperation", DataOperation.class);
        }
    };

    @Autowired
    QueryExecutionService queryExecutionService;

    @Autowired
    org.envtools.monitor.module.querylibrary.viewmapper.QueryExecutionResultViewMapper mapper;

    @Autowired
    Serializer serializer;

    private AtomicLong responseIdentifier = new AtomicLong(0);

    /**
     * This is incoming channel for QUERY_LIBRARY module
     */
    @Resource(name = "querylibrary.channel")
    SubscribableChannel queryLibraryModuleChannel;

    //TODO Auth is cross-module functionality
    @Resource(name = "querylibrary.provider")
    QueryLibraryAuthProvider queryLibraryAuthProvider;


    @Override
    protected <T> void processPayload(T payload, RequestMessage requestMessage)  {
        if (payload instanceof QueryExecutionRequest) {
            processExecutionRequest((QueryExecutionRequest) payload, requestMessage);
        } else if (payload instanceof QueryExecutionNextResultRequest) {
            processExecutionNextResultRequest((QueryExecutionNextResultRequest) payload, requestMessage);
        } else if (payload instanceof DataOperation) {
            processDataOperationRequest((DataOperation) payload, requestMessage);
        }
    }

    private void processExecutionRequest(QueryExecutionRequest queryExecutionRequest, RequestMessage requestMessage)  {
       // try {
            queryExecutionService.submitForExecution(queryExecutionRequest,
                    new QueryExecutionListener() {
                        @Override
                        public void onQueryCompleted(QueryExecutionResult queryResult) {
                            sendResultMessage(queryResult, requestMessage);
                        }

                        @Override
                        public void onQueryError(Throwable t) {
                            //What if duplicates here?
                            sendResultMessage(mapper.errorResult(t), requestMessage);
                        }
                    });
       // } catch (QueryExecutionException e) {
            //What if duplicates here?
       //     LOGGER.error("Query execution error", e);
          //  sendResultMessage(mapper.errorResult(e), requestMessage);
      //  }
    }

    private ExecutorService executorService = Executors.newCachedThreadPool();

    @Autowired
    DataOperationService<Long> dataOperationService;

    @Autowired
    TreeUpdateTriggerService treeUpdateTriggerService;

    @Autowired
    TreeUpdateService treeUpdateService;

    @Resource(name = "querylibrary.bootstrapper")
    BootstrapService treeBootstrapService;

    @Override
    public void init() throws Exception {
        super.init();

        treeBootstrapService.bootstrap();

        TreeUpdateTask treeUpdateTask = new TreeUpdateTask(treeUpdateTriggerService, treeUpdateService);
        executorService.execute(treeUpdateTask);
        treeUpdateTriggerService.triggerUpdate();
    }

    @PreDestroy
    private void close() {
        executorService.shutdownNow();
    }

    private void sendResultMessage(QueryExecutionResultView resultView, RequestMessage requestMessage) {
        String resultViewAsJson = serializer.serialize(resultView);

        sendMessageToCore(ResponseMessage
                .builder()
                .requestMetaData(requestMessage)
                .type(ResponseType.QUERY_EXECUTION_RESULT)
                .payload(resultViewAsJson)
                .build());
    }

    private void sendResultMessage(QueryExecutionResult queryResult, RequestMessage requestMessage) {
        QueryExecutionResultView resultView = mapper.map(queryResult);
        sendResultMessage(resultView, requestMessage);
    }

    private void processDataOperationRequest(DataOperation dataOperation, RequestMessage requestMessage) {
        DataOperationResult result = null;
        LOGGER.info("QueryLibraryModule.processDataOperationRequest - DataOperation: " + dataOperation);
        try {
            switch (dataOperation.getType()) {
                case CREATE:
                    LOGGER.info("QueryLibraryModule.processDataOperationRequest - create entity");
                    LOGGER.info("QueryLibraryModule.processDataOperationRequest - fields: " + dataOperation.getFields());
                    result = dataOperationService.create(dataOperation.getEntity(), dataOperation.getFields());
                    break;
                case UPDATE:
                    LOGGER.info("QueryLibraryModule.processDataOperationRequest - update entity");
                    result = dataOperationService.update(dataOperation.getEntity(), dataOperation.getId(), dataOperation.getFields());
                    break;
                case DELETE:
                    LOGGER.info("QueryLibraryModule.processDataOperationRequest - delete entity");
                    result = dataOperationService.delete(dataOperation.getEntity(), dataOperation.getId());
                    break;
            }
        } catch (Exception e) {
            LOGGER.error("QueryLibraryModule.processDataOperationRequest - unhandled exception: " + e.getMessage());
        }

        if (result != null && result.getStatus() == DataOperationResult.DataOperationStatusE.COMPLETED) {
            TreeUpdateTask treeUpdateTask = new TreeUpdateTask(treeUpdateTriggerService, treeUpdateService);
            executorService.submit(treeUpdateTask);
            try {
                treeUpdateTriggerService.triggerUpdate();
            } catch (InterruptedException e) {
                LOGGER.error("QueryLibraryModule.processDataOperationRequest - InterruptedException: " + e.getMessage());
            }
        }

        LOGGER.error("QueryLibraryModule.processDataOperationRequest - operation result: " + result);
        String jsonResult = serializer.serialize(result);

        sendMessageToCore(ResponseMessage
                .builder()
                .requestMetaData(requestMessage)
                .type(ResponseType.DATA_OPERATION_RESULT)
                .payload(jsonResult)
                .build());
    }

    private void processExecutionNextResultRequest(QueryExecutionNextResultRequest queryExecutionNextResultRequest,
                                                   RequestMessage requestMessage) {

    }


    @Override
    protected SubscribableChannel getModuleChannel() {
        return queryLibraryModuleChannel;
    }

    @Override
    protected Map<String, Class<?>> getPayloadTypes() {
        return PAYLOAD_TYPES;
    }

    @Override
    public String getIdentifier() {
        return ModuleConstants.QUERY_LIBRARY_MODULE_ID;
    }

}
