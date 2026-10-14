package com.alinbudesabc.authenticator.config.dev;

import com.alinbudesabc.authenticator.auth.util.RequestHeaderExtractor;
import com.alinbudesabc.authenticator.brutforceattak.LoginAttemptService;
import com.alinbudesabc.authenticator.config.CustomUserDetailsService;
import com.alinbudesabc.authenticator.config.ProdAuthenticationProvider;
import com.alinbudesabc.authenticator.enums.RoleEnum;
import com.alinbudesabc.authenticator.errorhandler.CustomAccessDeniedHandler;
import com.alinbudesabc.authenticator.errorhandler.CustomBasicAuthenticationEntryPoint;
import com.alinbudesabc.authenticator.filter.JWTTokenValidatorFilter;
import lombok.RequiredArgsConstructor;
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

@Configuration
@Profile({"dev", "test"})
@RequiredArgsConstructor
public class SecurityDevConfig {


    @Bean
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http, AuthenticationManager authenticationManager, RequestHeaderExtractor requestHeaderExtractor) throws Exception {
        CsrfTokenRequestAttributeHandler csrf = new CsrfTokenRequestAttributeHandler();
        http.cors().disable().sessionManagement(sessionConfig -> sessionConfig.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(csrfConfig -> csrfConfig.csrfTokenRequestHandler(csrf)
                        .ignoringRequestMatchers("/js/requestHeaders.js", "/js/secureFetch.js", "/register", "/usr", "/serviceLogin", "/admin/register", "/login", "/resetPassword.html", "/resetPassword2.html",
                                "/verifyEmail", "/resetPassword", "/favicon.ico", "/style/style.css", "/style/styleLogin.css", "/style/dashboardStyle.css", "/otp", "/qrcode/get", "/basicAuth", "/dashboard", "/login.html", "/login2.html", "/profile.html",
                                "/tfa.html", "/forgotPassword", "/forgotPassword.html", "/2fa/validate", "dashboard.html", "/twofactor/settings", "/refresh-token")) // ignore csrf sessions(we use JWT tokens)
                .authenticationManager(authenticationManager)
                .addFilterBefore(new JWTTokenValidatorFilter(requestHeaderExtractor), BasicAuthenticationFilter.class)
                .authorizeHttpRequests((requests) -> requests
                        .requestMatchers("/test1/**","/pics/**","/js/requestHeaders.js", "/js/secureFetch.js","/error", "invalidSession.html", "/users", "/serviceLogin", "/admin/register", "/login.html", "/login2.html","/login", "/style/style.css",  "/style/styleLogin.css", "/style/dashboardStyle.css","/favicon.ico",
                                "/forgotPassword", "forgotPassword.html", "/verifyEmail", "/resetPassword", "/profile", "/qrcode/get",  "/basicAuth", "/tfa.html", "/resetPassword.html", "/resetPassword2.html", "/2fa/validate", "/profile.html", "dashboard.html", "/test/usr", "/refresh-token")
                        .permitAll()
                .requestMatchers("/test", "/test/basic", "/user", "dashboard.html").authenticated()
                .requestMatchers("/test/roleUser").hasAnyRole(RoleEnum.ROLE_USER.getLabel(),RoleEnum.ROLE_ADMIN.getLabel())
                .requestMatchers("/dashboard").hasAnyRole(RoleEnum.ROLE_USER.getLabel(),RoleEnum.ROLE_ADMIN.getLabel())
                .requestMatchers("/twofactor/settings").hasAnyRole(RoleEnum.ROLE_USER.getLabel(),RoleEnum.ROLE_ADMIN.getLabel())
                        .requestMatchers("users/enable").hasRole(RoleEnum.ROLE_ADMIN.getLabel())
                .requestMatchers("/register").hasRole(RoleEnum.ROLE_ADMIN.getLabel()) //needs logic here every user should be able to register but just admin should be able to add extra roles to users so we need a new endpoint for adding roles

);
//        http.formLogin(flc -> flc.disable());
//        http.formLogin(formLogin -> formLogin.loginPage("/login"));
//        http.formLogin(AbstractHttpConfigurer::disable);
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

//    @Bean
//    public UsernamePasswordAuthenticationFilter customAuthenticationFilter(AuthenticationManager authenticationManager) {
//        UsernamePasswordAuthenticationFilter filter = new UsernamePasswordAuthenticationFilter();
//        filter.setAuthenticationManager(authenticationManager);
//        return filter;
//    }

}
