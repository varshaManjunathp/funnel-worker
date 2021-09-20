package com.pharmeasy.funnel.config.auth;

import org.springframework.http.HttpHeaders;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

public class HeaderAuthenticationFilter extends AbstractPreAuthenticatedProcessingFilter {

    private static final Object EMPTY = new Object();

    @Override
    protected Object getPreAuthenticatedPrincipal(HttpServletRequest request) {
        return EMPTY;
    }

    @Override
    protected Object getPreAuthenticatedCredentials(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader(HttpHeaders.AUTHORIZATION)).orElse("");
    }
}
