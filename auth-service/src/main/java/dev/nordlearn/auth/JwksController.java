package dev.nordlearn.auth;
import java.util.Map;
import com.nimbusds.jose.jwk.*;
import org.springframework.web.bind.annotation.*;
@RestController
public class JwksController {
 private final RSAKey key;
 public JwksController(RSAKey key){this.key=key;}
 @GetMapping("/.well-known/jwks.json")
 public Map<String,Object> jwks(){return new JWKSet(key.toPublicJWK()).toJSONObject();}
}
