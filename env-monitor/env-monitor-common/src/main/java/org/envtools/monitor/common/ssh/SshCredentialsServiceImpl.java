package org.envtools.monitor.common.ssh;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.envtools.monitor.common.encrypting.EncryptionService;
import org.envtools.monitor.common.encrypting.EncryptionServiceImpl;
import org.envtools.monitor.common.jaxb.JaxbHelper;
import org.springframework.util.ResourceUtils;

import javax.xml.bind.JAXBException;
import java.io.*;
import java.util.Properties;

/**
 * Created by Michal Skuza on 2016-06-17.
 */
public class SshCredentialsServiceImpl implements SshCredentialsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SshCredentialsServiceImpl.class);

    private final File credentialsDirectory;

    public SshCredentialsServiceImpl(String credentialsDirectory) {
        try {
            this.credentialsDirectory = ResourceUtils.getFile(credentialsDirectory);
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Cannot open directory for credentials files: " + credentialsDirectory);
        }
    }

    @Override
    public Credentials getCredentials(String host) {
        try {
            Credentials credentials = JaxbHelper.unmarshallFromFile(getCredentialFile(host), Credentials.class, null);

            if (credentials.getPassword() == null) {
                LOGGER.info(String.format("SshCredentialsServiceImpl.getCredentials - decrypting password for %s@%s",
                        credentials.getUser(), credentials.getHost()));
            }

            return credentials;
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }

    private File getCredentialFile(String host) {
        return new File(credentialsDirectory, host + ".xml");
    }
}
