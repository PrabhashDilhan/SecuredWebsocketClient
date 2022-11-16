package org.wso2.websocket;

import java.io.*;
import java.net.Socket;
import java.net.URI;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import javax.net.SocketFactory;
import javax.net.ssl.*;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

class WebSocketChatClient extends WebSocketClient {

    public WebSocketChatClient(URI serverUri, Map<String,String> httpHeaders) {
        super(serverUri,httpHeaders);
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        System.out.println("Connected");

    }

    @Override
    public void onMessage(String message) {
        System.out.println("got: " + message);

    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("Disconnected");

    }

    @Override
    public void onError(Exception ex) {
        ex.printStackTrace();

    }

}

public class SSLClientExample {

    /*
     * Keystore with certificate created like so (in JKS format):
     *
     *keytool -genkey -keyalg RSA -validity 3650 -keystore "keystore.jks" -storepass "storepassword" -keypass "keypassword" -alias "default" -dname "CN=127.0.0.1, OU=MyOrgUnit, O=MyOrg, L=MyCity, S=MyRegion, C=MyCountry"
     */
    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        //System.out.print("Enter the path to config.properties file : ");
        String path = args[0];
        Properties prop = new Properties();
        File initialFile = new File(path);
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(initialFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        if (inputStream != null) {
            try {
                prop.load(inputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("property file is not found");
        }
        String url = prop.getProperty("wssurl");
        String token = prop.getProperty("token");
        String keystorepath = prop.getProperty("keystorepath");
        String storepassword = prop.getProperty("storepassword");
        String keypassword = prop.getProperty("keypassword");
        boolean issecured = Boolean.parseBoolean(prop.getProperty("issecured"));

        Map<String,String> httpHeaders = new HashMap<String,String>();;
        httpHeaders.put("Authorization","Bearer "+token);
        WebSocketChatClient chatclient = new WebSocketChatClient(new URI(url),httpHeaders);

        if(issecured) {


            // load up the key store
            String STORETYPE = "JKS";
            String KEYSTORE = keystorepath;
            String STOREPASSWORD = storepassword;
            String KEYPASSWORD = keypassword;

            KeyStore ks = KeyStore.getInstance(STORETYPE);
            File kf = new File(KEYSTORE);
            ks.load(new FileInputStream(kf), STOREPASSWORD.toCharArray());

            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
            kmf.init(ks, KEYPASSWORD.toCharArray());
            TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
            tmf.init(ks);
            TrustManager[] trustAllCerts = new TrustManager [] {new X509ExtendedTrustManager () {
                @Override
                public void checkClientTrusted (X509Certificate[] chain, String authType, Socket socket) {

                }

                @Override
                public void checkServerTrusted (X509Certificate [] chain, String authType, Socket socket) {

                }

                @Override
                public void checkClientTrusted (X509Certificate [] chain, String authType, SSLEngine engine) {

                }

                @Override
                public void checkServerTrusted (X509Certificate [] chain, String authType, SSLEngine engine) {

                }

                public java.security.cert.X509Certificate [] getAcceptedIssuers () {
                    return null;
                }

                public void checkClientTrusted (X509Certificate [] certs, String authType) {
                }

                public void checkServerTrusted (X509Certificate [] certs, String authType) {
                }

            }};

            SSLContext sslContext = null;
            sslContext = SSLContext.getInstance("TLS");
            sslContext.init(kmf.getKeyManagers(), trustAllCerts, null);
            // sslContext.init( null, null, null ); // will use java's default key and trust store which is sufficient unless you deal with self-signed certificates

            SSLSocketFactory factory = sslContext
                    .getSocketFactory();// (SSLSocketFactory) SSLSocketFactory.getDefault();

            chatclient.setSocketFactory(factory);

            chatclient.connectBlocking();
        }else {

            chatclient.connectBlocking();
        }



        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            String line = reader.readLine();
            if (line.equals("close")) {
                chatclient.closeBlocking();
            } else if (line.equals("open")) {
                chatclient.reconnect();
            } else {
                chatclient.send(line);
            }
        }

    }
}
