package org.envtools.monitor.common.ssh;

import com.jcraft.jsch.JSchException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by MSkuza on 2016-07-01.
 */
public class SshHelperServiceImpl implements SshHelperService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SshHelperServiceImpl.class);

    private Map<String, SshHelper> sshHelpersMap;

    public SshHelperServiceImpl() {
        this.sshHelpersMap = new HashMap<>();
    }

    @Override
    public SshHelper getHelper(String host) {

        if (!this.sshHelpersMap.containsKey(host)) {
            throw new RuntimeException(String.format("No SSH connection configured for host %s, configured hosts: %s",
                    host, this.sshHelpersMap.keySet()));
        }

        return this.sshHelpersMap.get(host);
    }

    @Override
    public void register(String host, SshHelper sshHelper) {
        sshHelpersMap.put(host, sshHelper);
    }

    @Override
    public void loginAllSshHelpers() {
        for (SshHelper helper : sshHelpersMap.values()) {
            try {
                helper.login();
            } catch (JSchException e) {
                throw new RuntimeException(e);
            }
        }

        LOGGER.info("SshHelpers logged in");
    }

    @Override
    public void logoutAllSshHelpers() {
        for (SshHelper helper : sshHelpersMap.values()) {
            helper.logout();
        }
        LOGGER.info("SshHelpers logged out");
    }
}
