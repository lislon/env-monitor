package org.envtools.monitor.ui.controller.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * Created: 17.03.16 1:18
 *
 * @author Yury Yakovlev
 */
@Component
public class WebSocketTracer {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketTracer.class);

    @Resource(name = "clientInboundChannel")
    SubscribableChannel clientInboundChannel;

    @Resource(name = "clientOutboundChannel")
    SubscribableChannel clientOutboundChannel;

    @PostConstruct
    public void init() {
        LOGGER.info("WebSocketTracer.init - WebSocketTracer has been initialized. ");

        clientInboundChannel.subscribe(message -> LOGGER.info("Message from browser: " + message));
        clientOutboundChannel.subscribe(message -> LOGGER.info("Message to browser: " + message));
    }
}
