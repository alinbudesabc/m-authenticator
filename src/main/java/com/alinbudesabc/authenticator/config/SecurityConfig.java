package com.alinbudesabc.authenticator.config;

import com.alinbudesabc.authenticator.auth.util.RequestHeaderExtractor;
import com.alinbudesabc.authenticator.brutforceattak.LoginAttemptService;
import com.alinbudesabc.authenticator.enums.RoleEnum;
import com.alinbudesabc.authenticator.errorhandler.CustomAccessDeniedHandler;
import com.alinbudesabc.authenticator.errorhandler.CustomBasicAuthenticationEntryPoint;
import com.alinbudesabc.authenticator.filter.JWTTokenValidatorFilter;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.password.CompromisedPasswordChecker;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.password.HaveIBeenPwnedRestApiPasswordChecker;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@Profile("prod")
public class SecurityConfig {


    @Bean
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http, AuthenticationManager authenticationManager, RequestHeaderExtractor requestHeaderExtractor) throws Exception {
        CsrfTokenRequestAttributeHandler csrf = new CsrfTokenRequestAttributeHandler();
        http.sessionManagement(sessionConfig -> sessionConfig.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .requiresChannel(rcc -> rcc.anyRequest().requiresSecure())//only https traffic accepted
                .csrf(csrfConfig -> csrfConfig.csrfTokenRequestHandler(csrf)
                        .ignoringRequestMatchers("/register", "/serviceLogin"))
//                .addFilterAfter(new JWTTokenGeneratorFilter(tokenGenerator, authenticationManager), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(new JWTTokenValidatorFilter(requestHeaderExtractor), BasicAuthenticationFilter.class)
                .authorizeHttpRequests((requests) -> requests
                        .requestMatchers("/register", "/error", "invalidSession.html", "/users", "/serviceLogin", "/admin/register", "/login",
                                "/forgotPassword", "/verifyEmail", "/resetPassword")
                        .permitAll()
                        .requestMatchers("/test", "/test/basic", "/user").authenticated()
                        .requestMatchers("/test/roleUser").hasRole(RoleEnum.ROLE_USER.getLabel())
                );
        http.formLogin(withDefaults());
        http.httpBasic(httpSecurityHttpBasicConfigurer -> httpSecurityHttpBasicConfigurer
                .authenticationEntryPoint(new CustomBasicAuthenticationEntryPoint()));//It is just for the login page
        http.exceptionHandling(ehc -> ehc.accessDeniedHandler(new CustomAccessDeniedHandler()));//It applies to the global configuration, regardless of where the error occurs.
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    /*  this method will check if your password is compromised
        there is a maintained api/link that is checked for the passwords:
        https://haveibeenpwned.com/API/v3#PwnedPasswords
        also check HaveIBeenPwnedRestApiPasswordChecker class (you get the API check link in the comment placed in this class)
        ex: GET https://api.pwnedpasswords.com/range/{first 5 hash chars}
     */
    @Bean
    public CompromisedPasswordChecker compromisedPasswordChecker(){
        return new HaveIBeenPwnedRestApiPasswordChecker();
    }

    @Bean
    @Primary
    public AuthenticationManager authenticationManager(CustomUserDetailsService userDetailsService, PasswordEncoder passwordEncoder, ApplicationEventPublisher eventPublisher, LoginAttemptService loginAttemptService){
        ProdAuthenticationProvider authenticationProvider = new ProdAuthenticationProvider(userDetailsService,  passwordEncoder, eventPublisher, loginAttemptService);
        ProviderManager providerManager = new ProviderManager(authenticationProvider);
        providerManager.setEraseCredentialsAfterAuthentication(false); //the default is true but we need the authentication object in our flow for some manual actions
        return providerManager;
    }
}
