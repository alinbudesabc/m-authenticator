package com.alinbudesabc.authenticator.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class JWTTokenGeneratorFilter  {

   // private final TokenGenerator tokenGenerator;
    //private final AuthenticationManager authenticationManager;

//    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
//        String token = null;
//
//        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);
//
//        ObjectMapper objectMapper = new ObjectMapper();
//        LoginRequestDto credentialsDto = objectMapper.readValue(wrappedRequest.getInputStream(), LoginRequestDto.class);
//        String username = credentialsDto.username();
//        String password = credentialsDto.password();
//
//        UsernamePasswordAuthenticationToken unauthenticated = UsernamePasswordAuthenticationToken
//                .unauthenticated(username, password);
//
//        Authentication authenticate = authenticationManager.authenticate(unauthenticated);

       // Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        if(null == authentication){
//            throw new BadCredentialsException("Authentication failed");
//        }

//        if(authenticate.isAuthenticated()){
//             token = tokenGenerator.generateToken(authenticate);
//        }
//
//        response.setHeader(JWT_AUTH_HEADER, token);

//        filterChain.doFilter(request, response);
    }

    //if the path is the /login, /user then this filter should be executed(it means that just for the user login action we will generate a token)
//    @Override
//    protected boolean shouldNotFilter(HttpServletRequest request) {
//        if(request.getServletPath().equals("/login") && "GET".equals(request.getMethod())){
//            return true;
//        }
//
//        return !request.getServletPath().equals("/user")
//                && !request.getServletPath().equals("/basicAuth");
 //   }

//    private LoginRequestDto extractCredentials(HttpServletRequest request){
//        if ("application/json".equals(request.getContentType())) {
//            try {
//                ObjectMapper mapper = new ObjectMapper();
//                Map<String, String> credentials = mapper.readValue(request.getInputStream(), Map.class);
//                String username = credentials.get("username");
//                String password = credentials.get("password");
//
//                return new LoginRequestDto(username, password);
//            } catch (IOException e) {
//                throw new RuntimeException("Failed to parse authentication request body");
//            }
//        }
//        return null;
//    }
}

//        StringBuilder requestBody = new StringBuilder();
//        try (BufferedReader reader = request.getReader()) {
//            String line;
//            while ((line = reader.readLine()) != null) {
//                requestBody.append(line);
//            }
//        }
//
//        // Now you have the JSON body as a String
//        String json = requestBody.toString();
//
//        // Deserialize JSON into LoginRequestDto
//        ObjectMapper objectMapper = new ObjectMapper();
//        LoginRequestDto credentialsDto = objectMapper.readValue(json, LoginRequestDto.class);
//
//        // Now you have the username and password from the request body
//        String username = credentialsDto.username();
//        String password = credentialsDto.password();
//LoginRequestDto loginRequestDto = extractCredentials(request);
//
//        log.info("Username: {}, Password: {}", loginRequestDto.username(), loginRequestDto.password()); // Just for debugging
