package dev.nordlearn.auth;
import java.security.*;
import java.security.interfaces.*;
import java.util.UUID;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.*;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.*;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.context.annotation.*;
import org.springframework.security.oauth2.jwt.*;
@Configuration
public class KeyConfiguration {
 @Bean
 RSAKey signingKey() throws Exception {
  KeyPairGenerator generator=KeyPairGenerator.getInstance("RSA"); generator.initialize(2048);
  KeyPair pair=generator.generateKeyPair();
  return new RSAKey.Builder((RSAPublicKey)pair.getPublic())
   .privateKey((RSAPrivateKey)pair.getPrivate()).keyUse(KeyUse.SIGNATURE)
   .algorithm(JWSAlgorithm.RS256).keyID(UUID.randomUUID().toString()).build();
 }
 @Bean JWKSource<SecurityContext> jwkSource(RSAKey key) {
  return new ImmutableJWKSet<>(new JWKSet(key));
 }
 @Bean JwtEncoder jwtEncoder(JWKSource<SecurityContext> source) {
  return new NimbusJwtEncoder(source);
 }
}
