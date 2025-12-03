package com.samson.restbackend.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jetbrains.annotations.NotNull;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Component
public class RequestIdFilter implements Filter {
    private static final String HDR = "X-Request-ID";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, @NotNull FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        String rid = Optional.ofNullable(req.getHeader(HDR)).orElse(UUID.randomUUID().toString());
        MDC.put("requestId", rid);
        res.setHeader(HDR, rid);

        try { chain.doFilter(request, response); }
        finally { MDC.remove("requestId"); }
    }
}
