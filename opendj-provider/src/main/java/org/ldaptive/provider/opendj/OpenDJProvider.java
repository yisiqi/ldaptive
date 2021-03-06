/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.provider.opendj;

import java.security.GeneralSecurityException;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.SSLContext;
import org.forgerock.opendj.ldap.LDAPOptions;
import org.ldaptive.ConnectionConfig;
import org.ldaptive.LdapURL;
import org.ldaptive.provider.Provider;
import org.ldaptive.provider.ProviderConnectionFactory;
import org.ldaptive.ssl.CredentialConfig;
import org.ldaptive.ssl.DefaultHostnameVerifier;
import org.ldaptive.ssl.DefaultSSLContextInitializer;
import org.ldaptive.ssl.HostnameVerifyingTrustManager;
import org.ldaptive.ssl.SSLContextInitializer;

/**
 * OpenDJ provider implementation. Provides connection factories for clear, SSL, and TLS connections.
 *
 * @author  Middleware Services
 */
public class OpenDJProvider implements Provider<OpenDJProviderConfig>
{

  /** Provider configuration. */
  private OpenDJProviderConfig config = new OpenDJProviderConfig();


  @Override
  public ProviderConnectionFactory<OpenDJProviderConfig> getConnectionFactory(final ConnectionConfig cc)
  {
    LDAPOptions options = config.getOptions();
    if (options == null) {
      options = getDefaultLDAPOptions(cc);
    }
    return new OpenDJConnectionFactory(cc.getLdapUrl(), cc.getConnectionStrategy(), config, options);
  }


  /**
   * Returns an SSLContext configured with a default hostname verifier. Uses a {@link DefaultHostnameVerifier} if no
   * trust managers have been configured.
   *
   * @param  cc  connection configuration
   *
   * @return  SSL Context
   */
  protected SSLContext getHostnameVerifierSSLContext(final ConnectionConfig cc)
  {
    SSLContext sslContext;
    SSLContextInitializer contextInit;
    if (cc.getSslConfig() != null && cc.getSslConfig().getCredentialConfig() != null) {
      try {
        final CredentialConfig credConfig = cc.getSslConfig().getCredentialConfig();
        contextInit = credConfig.createSSLContextInitializer();
      } catch (GeneralSecurityException e) {
        throw new IllegalArgumentException(e);
      }
    } else {
      contextInit = new DefaultSSLContextInitializer();
    }
    if (cc.getSslConfig() != null && cc.getSslConfig().getTrustManagers() != null) {
      contextInit.setTrustManagers(cc.getSslConfig().getTrustManagers());
    } else {
      final LdapURL ldapUrl = new LdapURL(cc.getLdapUrl());
      contextInit.setTrustManagers(
        new HostnameVerifyingTrustManager(new DefaultHostnameVerifier(), ldapUrl.getHostnames()));
    }
    try {
      sslContext = contextInit.initSSLContext("TLS");
    } catch (GeneralSecurityException e) {
      throw new IllegalArgumentException(e);
    }
    return sslContext;
  }


  /**
   * Returns the default connection options for this provider.
   *
   * @param  cc  to configure options with
   *
   * @return  ldap connection options
   */
  protected LDAPOptions getDefaultLDAPOptions(final ConnectionConfig cc)
  {
    final LDAPOptions options = new LDAPOptions();
    SSLContext sslContext;
    if (cc.getUseStartTLS() || cc.getUseSSL()) {
      sslContext = getHostnameVerifierSSLContext(cc);
      options.setSSLContext(sslContext);
    }
    if (cc.getUseStartTLS()) {
      options.setUseStartTLS(true);
    } else if (cc.getUseSSL()) {
      options.setUseStartTLS(false);
    }
    if (cc.getSslConfig() != null && cc.getSslConfig().getEnabledCipherSuites() != null) {
      options.addEnabledCipherSuite(cc.getSslConfig().getEnabledCipherSuites());
    }
    if (cc.getSslConfig() != null && cc.getSslConfig().getEnabledProtocols() != null) {
      options.addEnabledProtocol(cc.getSslConfig().getEnabledProtocols());
    }
    if (cc.getResponseTimeout() != null) {
      options.setTimeout(cc.getResponseTimeout().toMillis(), TimeUnit.MILLISECONDS);
    }
    return options;
  }


  @Override
  public OpenDJProviderConfig getProviderConfig()
  {
    return config;
  }


  @Override
  public void setProviderConfig(final OpenDJProviderConfig pc)
  {
    config = pc;
  }


  @Override
  public OpenDJProvider newInstance()
  {
    return new OpenDJProvider();
  }
}
