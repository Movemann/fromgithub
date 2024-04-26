package com.santander.myapps.application1.service;

import com.santander.myapps.application1.config.ServiceProperties;
import com.santander.myapps.application1.exceptions.MapfreConflictException;
import com.santander.myapps.application1.exceptions.MapfreRestTemplateResponseErrorHandler;
import com.santander.myapps.application1.model.TokenMapfreRequest;
import com.santander.myapps.application1.util.Constants;
import com.santander.myapps.application1.util.ErrorPropertyFile;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.ssl.SSLContextBuilder;


@Service
public class TokenMapfreServiceImpl implements  TokenMapfreService{

    @Autowired
    private ErrorPropertyFile errorProperties;

    @Autowired
    private ServiceProperties serviceProperties;

    @Value("${service.tokenMapfreUrl}")
    private String mapfreUrl;

    private KeyStore getKeyStore(String keyStorePath, String keyStorePassword) throws IOException {

        FileInputStream fileInputStream = null;
        try {

            /**
             * Service will load keystore from config
             * yml file config will provide path file for retrieving
             * certificate
             */
            KeyStore keyStore = KeyStore.getInstance(Constants.SecurityConfig.OPERATIONS_KEYSTORE_TYPE);

            fileInputStream = new FileInputStream(keyStorePath);
            keyStore.load(fileInputStream, keyStorePassword.toCharArray());

            return keyStore;

        } catch (FileNotFoundException e){

            /**
             * Keystore not found
             * Error for handshake
             */
          // TODO  LOGGER.debug(String.valueOf(e));

            /** Config exception parameters */
            String nameException = Constants.ErrorConfig.MAPFRESEC_KEYSTORE_NOTFOUND_ERROR;
            int internalCode = Integer.parseInt(errorProperties.getPropertyError(nameException
                    + Constants.MsgOperations.MSG_OPERATIONS_INTERNALCODE));
            String detailMessage = Constants.MsgExceptions.MSG_ERROR_KEYSTORE_NOT_FOUND + e;

            throw new MapfreConflictException(detailMessage, nameException, internalCode, detailMessage);

        } catch (IOException | CertificateException | NoSuchAlgorithmException | KeyStoreException e) {

            /**
             * Technical error decrypting
             * keystore information
             */
            // TODO    LOGGER.debug(String.valueOf(e));

            /** Config exception parameters */
            String detailMessage = Constants.ErrorConfig.MAPFRESEC_KEY_SEC_ERROR + e;
            String nameException = Constants.ErrorConfig.MAPFRESEC_TECHNICAL_ERROR;
            int internalCode = Integer.parseInt(errorProperties.getPropertyError(nameException
                    + Constants.MsgOperations.MSG_OPERATIONS_INTERNALCODE));

            /** Launch exception */
            throw new MapfreConflictException(detailMessage , nameException, internalCode, detailMessage);
        } finally {
            if (fileInputStream!=null){
                fileInputStream.close();
            }
        }
    }

    public RestTemplate createSSLContext(String pathFile, String keyStorePassword) {

        try {

            /**
             * Service will inject certificate to inject
             * over handshake REST API Calls with Mapfre Services
             */
            SSLContext sslContext = new SSLContextBuilder()
                    .loadTrustMaterial(getKeyStore(pathFile, keyStorePassword),new TrustSelfSignedStrategy())
                    .build();

            /**
             * Configure SocketFactory to enclose HTTPClient
             * object
             */
            org.apache.http.conn.ssl.SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(sslContext);
            HttpClient httpClient = HttpClients.custom()
                    .setSSLSocketFactory(socketFactory)
                    .build();

            /**
             * Assign object to restTemplate
             */
            HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
            RestTemplate restTemplate = new RestTemplate(factory);

            /** Set error handler for RestTemplate */
            MapfreRestTemplateResponseErrorHandler mapfreRestTemplateResponseErrorHandler =
                    new MapfreRestTemplateResponseErrorHandler();
            restTemplate.setErrorHandler(mapfreRestTemplateResponseErrorHandler);

            /** RestTemplate object */
            return restTemplate;

        }  catch (IOException | KeyManagementException | NoSuchAlgorithmException | KeyStoreException e) {
            /**
             * Exceptions with security algorithms encrypt
             * and decrypt runtime process
             */
          //TODO  LOGGER.debug(String.valueOf(e));

            /** Config exception parameters */
            String detailMessage = Constants.ErrorConfig.MAPFRESEC_KEY_SEC_ERROR + e;
            String nameException = Constants.ErrorConfig.MAPFRESEC_TECHNICAL_ERROR;
            int internalCode = Integer.parseInt(errorProperties.getPropertyError(nameException
                    + Constants.MsgOperations.MSG_OPERATIONS_INTERNALCODE));

            /** Launch exception */
            throw new MapfreConflictException(detailMessage , nameException, internalCode, detailMessage);

        }
    }



    @Override
    public TokenMapfreRequest generateTokenMapfre() {

        /** Load SSL certificate */
        RestTemplate rest = createSSLContext(serviceProperties.getKeyStorePath(),
                serviceProperties.getKeyStorePassword());




      //  String recvCOS ="";
      //  String Authorization = recvCOS;
        //TODO 1º  VER COMO RECIBO LOS VALORES DEL PAYLOAD DOCUMENT Y POLIZA
        //TODO 2º  LLEVAR LOS VALORES PREDEFINIDOS A APPLICATION.PROPERTIES

        HttpHeaders headers = new HttpHeaders();

        headers.set("Authorization","Basic QVBQLURDVFBOU0UtU0FOLUJTMTpLbVUwcVNkbHAx");
        //  headers.setContentType(MediaType.APPLICATION_JSON);
        //  headers.set("Authentication",Authorization);
        //  headers.set("x-clientid","darwin");

        //Cuerpo solicitud
        //   Map<String, Object> payloadMap = new HashMap<>();
        //  payloadMap.put("documento", "0000000X");
        //  payloadMap.put("poliza", "123456789");

      //  Map<String, Object> requestBodyMap = new HashMap<>();
      //  requestBodyMap.put("keyalias","mykeystoredomain");
      //  requestBodyMap.put("payload",payloadMap);

      //  RestTemplate restTemplate = new RestTemplate();
     //   HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBodyMap, headers);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(headers);
        ResponseEntity<TokenMapfreRequest> response = rest.exchange(mapfreUrl, HttpMethod.POST, request, TokenMapfreRequest.class);




        return response.getBody();
    }
}
