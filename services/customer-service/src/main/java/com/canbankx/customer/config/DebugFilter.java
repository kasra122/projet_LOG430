package com.canbankx.customer.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class DebugFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        System.out.println(">>> FILTER START: " + request.getRequestURI());

        try {
            filterChain.doFilter(request, response);
            System.out.println(">>> FILTER END OK");
        } catch (Exception e) {
            System.out.println(">>> FILTER ERROR: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
}
