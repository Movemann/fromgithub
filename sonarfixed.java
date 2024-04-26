package com.santander.myapps.application1.service;

import com.santander.myapps.application1.config.ServiceProperties;
import com.santander.myapps.application1.exceptions.MapfreConflictException;
import com.santander.myapps.application1.exceptions.MapfreRestTemplateResponseErrorHandler;
import com.santander.myapps.application1.model.TokenMapfreRequest;
import com.santander.myapps.application1.util.Constants;
import com.santander.myapps.application1.util.ErrorPropertyFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyStore;
import java.security.cert.CertificateException;

@Service
public class TokenMapfreServiceImpl implements TokenMapfreService {

    @Autowired
    private ErrorPropertyFile errorProperties;

    @Autowired
    private ServiceProperties serviceProperties;

    @Value("${service.tokenMapfreUrl}")
    private String mapfreUrl;

    private KeyStore getKeyStore(String keyStorePath, String keyStorePassword) throws IOException {
        FileInputStream fileInputStream = null;
        try {
            KeyStore keyStore = KeyStore.getInstance(Constants.SecurityConfig.OPERATIONS_KEYSTORE_TYPE);
            fileInputStream = new FileInputStream(keyStorePath);
            keyStore.load(fileInputStream, keyStorePassword.toCharArray());
            return keyStore;
        } catch (FileNotFoundException e) {
            String nameException = Constants.ErrorConfig.MAPFRESEC_KEYSTORE_NOTFOUND_ERROR;
            int internalCode = Integer.parseInt(errorProperties.getPropertyError(nameException
                    + Constants.MsgOperations.MSG_OPERATIONS_INTERNALCODE));
            String detailMessage = Constants.MsgExceptions.MSG_ERROR_KEYSTORE_NOT_FOUND + e;
            throw new MapfreConflictException(detailMessage, nameException, internalCode, detailMessage);
        } finally {
            if (fileInputStream != null) {
                fileInputStream.close();
            }
        }
    }

    public RestTemplate createSSLContext(String pathFile, String keyStorePassword) {
        try {
            KeyStore keyStore = getKeyStore(pathFile, keyStorePassword);
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, null, null);
            HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
            factory.setHttpClient(HttpClients.custom().setSslcontext(sslContext).build());
            RestTemplate restTemplate = new RestTemplate(factory);
            restTemplate.setErrorHandler(new MapfreRestTemplateResponseErrorHandler());
            return restTemplate;
        } catch (Exception e) {
            String detailMessage = Constants.ErrorConfig.MAPFRESEC_KEY_SEC_ERROR + e;
            String nameException = Constants.ErrorConfig.MAPFRESEC_TECHNICAL_ERROR;
            int internalCode = Integer.parseInt(errorProperties.getPropertyError(nameException
                    + Constants.MsgOperations.MSG_OPERATIONS_INTERNALCODE));
            throw new MapfreConflictException(detailMessage, nameException, internalCode, detailMessage);
        }
    }

    @Override
    public TokenMapfreRequest generateTokenMapfre() {
        RestTemplate rest = createSSLContext(serviceProperties.getKeyStorePath(),
                serviceProperties.getKeyStorePassword());
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Basic QVBQLURDVFBOU0UtU0FOLUJTMTpLbVUwcVNkbHAx");
        HttpEntity<?> request = new HttpEntity<>(headers);
        ResponseEntity<TokenMapfreRequest> response = rest.exchange(mapfreUrl, HttpMethod.POST, request, TokenMapfreRequest.class);
        return response.getBody();
    }
}
