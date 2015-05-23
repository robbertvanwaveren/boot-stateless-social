package com.jdriven.stateless.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.ObjectPostProcessor;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.social.UserIdSource;
import org.springframework.social.security.SocialAuthenticationFilter;
import org.springframework.social.security.SpringSocialConfigurer;

@EnableWebSecurity
@Configuration
@Order(1)
public class StatelessAuthenticationSecurityConfig extends WebSecurityConfigurerAdapter {

	@Autowired
	private SocialAuthenticationSuccessHandler socialAuthenticationSuccessHandler;

	@Autowired
	private StatelessAuthenticationFilter statelessAuthenticationFilter;

	@Autowired
	private UserIdSource userIdSource;

	@Autowired
	private SocialUserService userService;

	public StatelessAuthenticationSecurityConfig() {
		super(true);
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		// Set a custom successHandler on the SocialAuthenticationFilter
		final SpringSocialConfigurer socialConfigurer = new SpringSocialConfigurer();
		socialConfigurer.addObjectPostProcessor(new ObjectPostProcessor<SocialAuthenticationFilter>() {
			@Override
			public <O extends SocialAuthenticationFilter> O postProcess(O socialAuthenticationFilter) {
				socialAuthenticationFilter.setAuthenticationSuccessHandler(socialAuthenticationSuccessHandler);
				return socialAuthenticationFilter;
			}
		});

		/* Bellow we first apply default configuration from WebSecurityConfigurerAdapter
         * but comment out some lines that are not useful for stateless security server
         * with some explanations.
         */
		http
    		/* Because the token is not an actual Cookie,
             * no browser can be instructed add it to requests automatically.
             * This is essential as it completely prevents any form of CSRF attacks. 
             */
            //.csrf().and()
    		/*
             * Enabling ExceptionTranslationFilter to handle security exceptions and
             * provide necessary responses such as launching AuthenticationEntryPoint
             * and redirecting to access denied pages
             */
		    .exceptionHandling().and()
		    /*
             * Enables various http headers security settings:
             *  - contentTypeOptions():
             *      This disables content sniffing.
             *      A content sniffing - browser tries to guess the MIME type based on content.
             *      A malicious user might create a document of some valid format
             *      that is also a valid JavaScript file and execute a XSS attack with it.
             *  - xssProtection()
             *      extra cross site scripting protection for IE8
             *      http://blogs.msdn.com/b/ieinternals/archive/2011/01/31/controlling-the-internet-explorer-xss-filter-with-the-x-xss-protection-http-header.aspx
             *  - cacheControl()
             *      A user may view an authenticated page, log out,
             *      and then a malicious user can use the browser history to view the cached page.
             *      This option disables browser caching. Later you may want to enable it for some
             *      resources such as CSS, JavaScript, and images.
             *  - httpStrictTransportSecurity()
             *      When user enters http address of a secured website, the website redirects user to https.
             *      A malicious user could intercept the initial HTTP request and manipulate the response later
             *      giving the illusion that you talk with a secured website on https.
             *      This option tells the browser that this server accepts only https.
             *  - frameOptions()
             *      Your website can be embedded into invisible X-Frame and other site can trick the user
             *      into clicking on your website (Clickjacking).
             *      This disable the ability to embed your server into X-Frames.
             * Notice that in the original code on Stateless auth. only cacheControl was enabled
             * .headers().cacheControl().and()
             */
		    .headers().and()
		    /*
             * This is a stateless application therefore session management is disabled
             */
            //.sessionManagement().and()
		    /*
             * This persists and restore of the SecurityContext between requests.
             * This is a stateless application therefore its disabled.
             */
            //.securityContext().and()
		    /*
             * Allows configuring the Request Cache. For example, a protected page (/protected) 
             * may be requested prior to authentication.
             * The application will redirect the user to a login page.
             * After authentication, Spring Security will redirect the user to the originally
             * requested protected page (/protected).
             * Since we login with separate step and state is held by client, this is not needed
             * and maybe confusing redirecting unauthorized calls to login page
             */
            //.requestCache().and()
		    /*
             * Allows configuring how an anonymous user is represented.
             * By default anonymous users will be represented with an AnonymousAuthenticationToken
             * and contain the role "ROLE_ANONYMOUS".
             * Original code gives access to non-authenticated users for GET requests.
             */
            .anonymous().and()
            /*
             * There some methods of HttpServletRequest such a HttpServletRequest.getRemoteUser()
             * used in jsp that will be implemented using SecurityContext data.
             * This is also needed to pass Principal to web controllers.
             */
            .servletApi().and()
            /*
             * Adds a Filter that will generate a login page - not needed here
             */
            //.apply(new DefaultLoginPageConfigurer<HttpSecurity>()).and()
            /*
             * The default is that accessing the URL "/logout" will log the user out by
             * invalidating the HTTP Session, cleaning up any rememberMe() authentication
             * that was configured, clearing the SecurityContextHolder.
             * Not needed since state is implemented on the client
             */
            //.logout().and()
			.authorizeRequests()
    			//allow anonymous font and template requests
    			.antMatchers("/").permitAll()
    			.antMatchers("/favicon.ico").permitAll()
    			.antMatchers("/resources/**").permitAll()
    
    			//allow anonymous calls to social login
    			.antMatchers("/auth/**").permitAll()
    
    			//allow anonymous GETs to API
    			.antMatchers(HttpMethod.GET, "/api/**").permitAll()
    
    			//all other request need to be authenticated
    			.antMatchers(HttpMethod.GET, "/api/users/current/details").hasRole("USER")
    			.anyRequest().hasRole("USER").and()

			// add custom authentication filter for complete stateless JWT based authentication
			.addFilterBefore(statelessAuthenticationFilter, AbstractPreAuthenticatedProcessingFilter.class)

			// apply the configuration from the socialConfigurer (adds the SocialAuthenticationFilter)
			.apply(socialConfigurer.userIdSource(userIdSource));
	}

	/*
	 * The reason of this function is to put @Bean on the default authenticationManager
	 * created by the parent.
	 * In order to put userService into AuthenticationManager, you need to call
	 * configure of this class. "configure" is called by authenticationManagerBean
	 * the parent class. However this authentication manager will not be autowired because it is not
	 * exposed as a bean. You can only Autowire a spring managed bean.
	 * If its not exposed as a bean, you cannot Autowire it. 
	 */
	@Bean
	@Override
	public AuthenticationManager authenticationManagerBean() throws Exception {
		return super.authenticationManagerBean();
	}

	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(userService);
	}

	@Override
	protected SocialUserService userDetailsService() {
		return userService;
	}
}
