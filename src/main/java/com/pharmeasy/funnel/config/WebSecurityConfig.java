package com.pharmeasy.funnel.config;


import com.pharmeasy.funnel.config.auth.HeaderAuthenticationFilter;
import com.pharmeasy.funnel.config.auth.HeaderAuthenticationService;
import com.pharmeasy.funnel.config.auth.JWTTokenUtil;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.web.access.channel.ChannelProcessingFilter;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationProvider;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.security.web.authentication.preauth.RequestHeaderAuthenticationFilter;

import java.util.Collections;

@EnableWebSecurity
@AllArgsConstructor
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    private final JWTTokenUtil jwtTokenUtil;

    @Override
    public void configure(WebSecurity web) throws Exception {
        // WebSecurity is run before HttpSecurity
        enableSwagger(web);
        web.ignoring().antMatchers(HttpMethod.GET, "/actuator/**");
    }

    private void enableSwagger(WebSecurity web) {
        web.ignoring()
                .antMatchers(HttpMethod.GET, "/swagger-ui/**")
                .antMatchers(HttpMethod.GET, "/swagger-resources/**")
                .antMatchers(HttpMethod.GET, "/v3/api-docs");
    }

    @Bean
    @Override
    protected AuthenticationManager authenticationManager() {
        return new ProviderManager(Collections.singletonList(preAuthAuthProvider()));
    }

    @Bean
    PreAuthenticatedAuthenticationProvider preAuthAuthProvider() {
        PreAuthenticatedAuthenticationProvider provider = new PreAuthenticatedAuthenticationProvider();
        provider.setPreAuthenticatedUserDetailsService(userDetailsServiceWrapper());
        return provider;
    }

    @Bean
    public AuthenticationUserDetailsService<PreAuthenticatedAuthenticationToken>
    userDetailsServiceWrapper() {
        return new HeaderAuthenticationService(jwtTokenUtil);
    }

    @Bean
    public HeaderAuthenticationFilter headerAuthenticationFilter() {
        HeaderAuthenticationFilter filter = new HeaderAuthenticationFilter();
        filter.setAuthenticationManager(authenticationManager());
        return filter;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // Don't create session id
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);

        // All the requests which are filtered from WebSecurity needs authentication
        http.csrf()
                .disable()
                .addFilterBefore(new CorsPreflightFilter(), ChannelProcessingFilter.class)
                .addFilterBefore(headerAuthenticationFilter(), RequestHeaderAuthenticationFilter.class)
                .authorizeRequests()
                .anyRequest()
                .authenticated();
    }
}
