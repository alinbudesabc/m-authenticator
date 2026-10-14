package com.alinbudesabc.authenticator.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import static org.mockito.Mockito.*;

class JWTTokenGeneratorFilterTest {

    private JWTTokenGeneratorFilter tokenGeneratorFilter;

   // @BeforeEach
    public void setUp() {
        tokenGeneratorFilter = new JWTTokenGeneratorFilter();
    }


    //@Test
    public void testTokenGeneration() throws Exception {
        // Mock HttpServletRequest and HttpServletResponse
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);

        // Manually set up the Authentication object in SecurityContextHolder
        UserDetails userDetails = User.withUsername("testUser")
                .password("password")
                .authorities(new SimpleGrantedAuthority("ROLE_USER"))
                .build();
        Authentication authentication =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Mock the servlet path to match the filter condition
        when(request.getServletPath()).thenReturn("/user");

        // Execute the filter
        tokenGeneratorFilter.doFilterInternal(request, response, filterChain);

        // Verify that the method was called during the filter execution and token was generated and added to the response header
        verify(response).setHeader(eq("Bearer"), anyString());

        // Verify that the filter chain was invoked
        verify(filterChain).doFilter(request, response);
    }

}