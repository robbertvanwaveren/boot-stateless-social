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


/*
 * Authenticates every request based on the extra header token:
 *  - get the user from the extra header token
 *  - check that its hash is valid
 *  - check that it is not expired
 *  - create authorization object from the user
 *  - set authorization object into the security context for this request
 *  - let the rest of the system handle the request based on the authentication
 */
@Component
public class StatelessAuthenticationFilter extends GenericFilterBean {

	@Autowired
	private TokenAuthenticationService tokenAuthenticationService;

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
			ServletException {
		SecurityContextHolder.getContext().setAuthentication(
                tokenAuthenticationService.getAuthentication((HttpServletRequest) request));
		chain.doFilter(request, response);
	}
}