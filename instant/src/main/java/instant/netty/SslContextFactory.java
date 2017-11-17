package instant.netty;

import java.net.Socket;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509KeyManager;
import javax.net.ssl.X509TrustManager;

/**
 * Created by Administrator on 2017/11/17.
 */

public class SslContextFactory {

    private static final SSLContext SERVER_CONTEXT;

    static {
        try {
            KeyManager keyManager = new X509KeyManager() {

                @Override
                public String chooseClientAlias(String[] keyType, Principal[] issuers, Socket socket) {
                    return null;
                }

                @Override
                public String chooseServerAlias(String keyType, Principal[] issuers, Socket socket) {
                    return null;
                }

                @Override
                public X509Certificate[] getCertificateChain(String alias) {
                    return new X509Certificate[0];
                }

                @Override
                public String[] getClientAliases(String keyType, Principal[] issuers) {
                    return new String[0];
                }

                @Override
                public String[] getServerAliases(String keyType, Principal[] issuers) {
                    return new String[0];
                }

                @Override
                public PrivateKey getPrivateKey(String alias) {
                    return null;
                }
            };
            KeyManager[] keyManagers = new KeyManager[]{keyManager};

            TrustManager trustManager = new X509TrustManager(){
                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {

                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {

                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            };
            TrustManager[] trustAllCerts = new TrustManager[]{trustManager};

            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(keyManagers, trustAllCerts, null);
            SERVER_CONTEXT = sc;
        } catch (Exception e) {
            throw new Error("Failed to initialize the client-side SSLContext", e);
        }
    }

    public static SSLContext getServerContext() {
        return SERVER_CONTEXT;
    }
}
