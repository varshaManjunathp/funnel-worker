package com.pharmeasy.funnel.config;

import org.springframework.web.filter.CommonsRequestLoggingFilter;

import javax.servlet.http.HttpServletRequest;

public class RequestLoggingFilter extends CommonsRequestLoggingFilter {

        private final String healthUriPath;

        public RequestLoggingFilter(String healthUriPath) {
            this.healthUriPath = healthUriPath;
            super.setIncludeQueryString(true);
            super.setIncludePayload(true);
            super.setMaxPayloadLength(10_000);
            super.setIncludeHeaders(false);
        }

        @Override
        protected boolean shouldLog(HttpServletRequest request) {
            return logger.isInfoEnabled()
                    && !request.getRequestURI().contains(healthUriPath)
                    && !"GET".equals(request.getMethod());
        }

        @Override
        protected void beforeRequest(HttpServletRequest request, String message) {
            logger.info(message);
        }

        @Override
        protected void afterRequest(HttpServletRequest request, String message) {
            logger.info(message);
        }
    }

