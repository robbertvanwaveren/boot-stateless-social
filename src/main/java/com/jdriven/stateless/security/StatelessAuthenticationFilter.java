package com.jdriven.stateless.security;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

@Component
public class StatelessAuthenticationFilter extends GenericFilterBean {

	@Autowired
	private TokenAuthenticationService tokenAuthenticationService;

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
			ServletException {

		setAuthenticationFromHeader((HttpServletRequest) request);

		chain.doFilter(request, response);
	}

	private void setAuthenticationFromHeader(HttpServletRequest request) {
		final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (!(authentication instanceof UserAuthentication)) {
			final UserAuthentication userAuthentication = tokenAuthenticationService.getAuthentication(request);
			if (userAuthentication != null) {
				SecurityContextHolder.getContext().setAuthentication(userAuthentication);
			}
		}
	}
}