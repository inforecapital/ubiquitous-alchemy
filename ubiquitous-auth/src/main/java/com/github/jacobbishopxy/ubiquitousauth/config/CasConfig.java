/**
 * Created by Jacob Xie on 4/12/2022.
 */

package com.github.jacobbishopxy.ubiquitousauth.config;

import org.jasig.cas.client.session.SingleSignOutFilter;
import org.jasig.cas.client.validation.Cas20ServiceTicketValidator;
import org.jasig.cas.client.validation.TicketValidator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.cas.ServiceProperties;
import org.springframework.security.cas.authentication.CasAuthenticationProvider;
import org.springframework.security.cas.web.CasAuthenticationEntryPoint;
import org.springframework.security.cas.web.CasAuthenticationFilter;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;

@Configuration
@PropertySource("classpath:cas.properties")
public class CasConfig {

  @Value("${casServerUrl}")
  private String casServerUrl;

  @Value("${baseUrl}")
  private String baseUrl;

  @Bean
  public AuthenticationEntryPoint authenticationEntryPoint() {
    CasAuthenticationEntryPoint entryPoint = new CasAuthenticationEntryPoint();
    entryPoint.setLoginUrl(casServerUrl + "/login");
    entryPoint.setServiceProperties(this.serviceProperties());
    return entryPoint;
  }

  @Bean
  protected AuthenticationManager authenticationManager() {
    return new ProviderManager(this.casAuthenticationProvider());
  }

  @Bean
  public CasAuthenticationFilter casAuthenticationFilter(
      AuthenticationManager authenticationManager,
      ServiceProperties serviceProperties) throws Exception {
    CasAuthenticationFilter filter = new CasAuthenticationFilter();
    filter.setAuthenticationManager(authenticationManager);
    filter.setServiceProperties(serviceProperties);
    return filter;
  }

  @Bean
  public ServiceProperties serviceProperties() {
    ServiceProperties serviceProperties = new ServiceProperties();
    serviceProperties.setService(baseUrl + "/login/cas");
    serviceProperties.setSendRenew(false);
    return serviceProperties;
  }

  @Bean
  public TicketValidator ticketValidator() {
    return new Cas20ServiceTicketValidator(casServerUrl);
  }

  // @Bean
  // public CustomCasService customCasService() {
  // return new CustomCasService();
  // }

  @Bean
  public CasAuthenticationProvider casAuthenticationProvider() {
    CasAuthenticationProvider provider = new CasAuthenticationProvider();
    provider.setServiceProperties(this.serviceProperties());
    provider.setTicketValidator(this.ticketValidator());
    // provider.setAuthenticationUserDetailsService(customCasService());
    provider.setKey("CAS_PROVIDER_LOCALHOST");
    return provider;
  }

  @Bean
  SecurityContextLogoutHandler securityContextLogoutHandler() {
    return new SecurityContextLogoutHandler();
  }

  @Bean
  LogoutFilter logoutFilter() {
    LogoutFilter filter = new LogoutFilter(casServerUrl + "/logout", securityContextLogoutHandler());
    filter.setFilterProcessesUrl("/logout/cas");
    return filter;
  }

  @Bean
  public SingleSignOutFilter singleSignOutFilter() {
    SingleSignOutFilter filter = new SingleSignOutFilter();
    filter.setIgnoreInitConfiguration(true);
    return filter;
  }
}
