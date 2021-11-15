package io.github.azagniotov.stubby4j.server.ssl;

import io.github.azagniotov.stubby4j.annotations.GeneratedCodeCoverageExclusion;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Set;

import static io.github.azagniotov.stubby4j.server.ssl.SslUtils.keyStoreAsX509Certificates;

/**
 * This class is a wrapper around default X.509 trust manager initialized through {@link TrustManagerFactory} with the
 * default trust store and with added validation for self-signed certificates, including the default one of stubby4j's,
 * thus the "extended" part.
 */
@GeneratedCodeCoverageExclusion
public final class DefaultExtendedX509TrustManager implements X509TrustManager {

    private static final Set<X509Certificate> STUBBY_SELF_SIGNED_CERT;
    private static final X509TrustManager DEFAULT_TRUST_MANAGER;

    static {
        STUBBY_SELF_SIGNED_CERT = keyStoreAsX509Certificates();
        DEFAULT_TRUST_MANAGER = loadDefaultX509TrustManager();
    }

    public DefaultExtendedX509TrustManager() {

    }

    @Override
    public void checkClientTrusted(final X509Certificate[] chain, final String authType) throws CertificateException {
        DEFAULT_TRUST_MANAGER.checkClientTrusted(chain, authType);
    }

    @Override
    public void checkServerTrusted(final X509Certificate[] chain, final String authType) throws CertificateException {
        if (!isSelfSignedCertificate(chain)) {
            DEFAULT_TRUST_MANAGER.checkServerTrusted(chain, authType);
        }
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return DEFAULT_TRUST_MANAGER.getAcceptedIssuers();
    }

    private static X509TrustManager loadDefaultX509TrustManager() {
        try {
            final TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            // Using null here initialises the TrustManagerFactory with the default trust store.
            trustManagerFactory.init((KeyStore) null);

            for (TrustManager trustManager : trustManagerFactory.getTrustManagers()) {
                if (trustManager instanceof X509TrustManager) {
                    return (X509TrustManager) trustManager;
                }
            }

            throw new RuntimeException("No TrustManager instance of type X509TrustManager found");
        } catch (Exception e) {
            throw new Error("Could not init default X509TrustManager", e);
        }
    }

    private boolean isSelfSignedCertificate(final X509Certificate[] chain) {
        return chain.length == 1 &&
                (
                        STUBBY_SELF_SIGNED_CERT.contains(chain[0]) ||
                                chain[0].getIssuerX500Principal().getName("canonical").contains("localhost")
                );
    }
}
