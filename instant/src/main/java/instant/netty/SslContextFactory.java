package instant.netty;

import java.io.File;
import java.net.Socket;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509KeyManager;
import javax.net.ssl.X509TrustManager;

import io.netty.handler.ssl.ApplicationProtocolConfig;
import io.netty.handler.ssl.IdentityCipherSuiteFilter;
import io.netty.handler.ssl.JdkSslClientContext;
import io.netty.handler.ssl.OpenSslClientContext;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslProvider;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;

/**
 * Created by Administrator on 2017/11/17.
 */

public class SslContextFactory {

    private static io.netty.handler.ssl.SslContext SERVER_CONTEXT=null;

    static {
        try {
//            KeyManager keyManager = new X509KeyManager() {
//
//                @Override
//                public String chooseClientAlias(String[] keyType, Principal[] issuers, Socket socket) {
//                    return "";
//                }
//
//                @Override
//                public String chooseServerAlias(String keyType, Principal[] issuers, Socket socket) {
//                    return "";
//                }
//
//                @Override
//                public X509Certificate[] getCertificateChain(String alias) {
//                    return new X509Certificate[]{};
//                }
//
//                @Override
//                public String[] getClientAliases(String keyType, Principal[] issuers) {
//                    return new String[]{};
//                }
//
//                @Override
//                public String[] getServerAliases(String keyType, Principal[] issuers) {
//                    return new String[]{};
//                }
//
//                @Override
//                public PrivateKey getPrivateKey(String alias) {
//                    return null;
//                }
//            };
//            KeyManager[] keyManagers = new KeyManager[]{keyManager};
//
//            TrustManager trustManager = new X509TrustManager(){
//                @Override
//                public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
//
//                }
//
//                @Override
//                public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
//
//                }
//
//                @Override
//                public X509Certificate[] getAcceptedIssuers() {
//                    return new X509Certificate[]{};
//                }
//            };
//            TrustManager[] trustAllCerts = new TrustManager[]{trustManager};
//
//            SSLContext sc = SSLContext.getInstance("SSL");
//            sc.init(keyManagers, trustAllCerts, null);


            TrustManagerFactory trustManagerFactory = InsecureTrustManagerFactory.INSTANCE;
            SslProvider provider = SslContext.defaultClientProvider();
            File trustCertChainFile = null;
            File keyCertChainFile = null;
            File keyFile = null;
            String keyPassword = null;
            KeyManagerFactory keyManagerFactory = null;
            Iterable<String> ciphers = null;
            ApplicationProtocolConfig apn = null;
            io.netty.handler.ssl.SslContext sslContext=null;
            switch (provider) {
                case JDK:
                    sslContext = new JdkSslClientContext(
                            trustCertChainFile, trustManagerFactory, keyCertChainFile, keyFile, keyPassword,
                            keyManagerFactory, ciphers, IdentityCipherSuiteFilter.INSTANCE, apn, 0, 0);
                    break;
                case OPENSSL:
                default:
                    sslContext = new OpenSslClientContext(
                            trustCertChainFile, trustManagerFactory,
                            ciphers, apn, 0, 0);
                    break;
            }
            SERVER_CONTEXT=sslContext;
        } catch (Exception e) {
            throw new Error("Failed to initialize the client-side SSLContext", e);
        }
    }

    public static io.netty.handler.ssl.SslContext getServerContext() {
        return SERVER_CONTEXT;
    }
}
