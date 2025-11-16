package com.repobackend.api.config;

import java.io.IOException;
import java.util.Enumeration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class RequestLoggingFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(RequestLoggingFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("Incoming request: ")
              .append(request.getMethod())
              .append(" ")
              .append(request.getRequestURI());
            if (request.getQueryString() != null) sb.append('?').append(request.getQueryString());
            sb.append(" from=").append(request.getRemoteAddr());
            sb.append(" headers=[");
            Enumeration<String> names = request.getHeaderNames();
            int count = 0;
            while (names != null && names.hasMoreElements() && count < 20) {
                String name = names.nextElement();
                String val = request.getHeader(name);
                sb.append(name).append(":").append(val).append(",");
                count++;
            }
            sb.append("]");
            logger.info(sb.toString());
        } catch (Exception ex) {
            logger.warn("Error while logging request", ex);
        }
        filterChain.doFilter(request, response);
    }
}

