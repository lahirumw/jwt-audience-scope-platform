package dev.nordlearn.orders;

import dev.nordlearn.security.common.JwtSecurity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfiguration {

    @Bean
    JwtDecoder jwtDecoder(@Value("${app.jwt.jwk-set-uri}") String jwkSetUri,
                          @Value("${app.jwt.issuer}") String issuer,
                          @Value("${app.jwt.audience}") String audience) {
        return JwtSecurity.decoder(jwkSetUri, issuer, audience);
    }

    @Bean
    SecurityFilterChain resourceChain(HttpSecurity http, JwtDecoder decoder) throws Exception {
        return JwtSecurity.resourceChain(http, decoder, "/api/orders/**", "orders:read", "orders:write");
    }
}
