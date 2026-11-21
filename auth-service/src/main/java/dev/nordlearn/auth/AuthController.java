package dev.nordlearn.auth;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.*;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class AuthController {
 public record RegisterRequest(@NotBlank @Size(min=3,max=50) String username,
   @NotBlank @Size(min=8,max=72) String password) {}
 public record LoginRequest(@NotBlank String username,@NotBlank String password,
   @NotBlank String audience,List<String> scopes) {}
 public record TokenResponse(String accessToken,String tokenType,long expiresIn,
   String audience,List<String> scopes) {}

 private static final Map<String,Set<String>> USER_GRANTS=Map.of(
  "orders-api",Set.of("orders:read"),"customers-api",Set.of("customers:read"));
 private static final Map<String,Set<String>> ADMIN_GRANTS=Map.of(
  "orders-api",Set.of("orders:read","orders:write"),
  "customers-api",Set.of("customers:read","customers:write"));

 private final UserRepository users; private final PasswordEncoder encoder;
 private final AuthenticationManager authentication; private final JwtEncoder jwtEncoder;
 private final String issuer; private final long minutes;

 public AuthController(UserRepository users,PasswordEncoder encoder,AuthenticationManager authentication,
  JwtEncoder jwtEncoder,@Value("${app.jwt.issuer}") String issuer,
  @Value("${app.jwt.access-token-minutes}") long minutes) {
  this.users=users;this.encoder=encoder;this.authentication=authentication;
  this.jwtEncoder=jwtEncoder;this.issuer=issuer;this.minutes=minutes;
 }
 @PostMapping("/auth/register") @ResponseStatus(HttpStatus.CREATED)
 public Map<String,Object> register(@Valid @RequestBody RegisterRequest req) {
  if(users.existsByUsername(req.username())) throw new ResponseStatusException(HttpStatus.CONFLICT,"Username exists");
  UserAccount u=users.save(new UserAccount(req.username(),encoder.encode(req.password()),"USER"));
  return Map.of("id",u.getId(),"username",u.getUsername(),"role",u.getRole());
 }
 @PostMapping("/auth/login")
 public TokenResponse login(@Valid @RequestBody LoginRequest req) {
  authentication.authenticate(UsernamePasswordAuthenticationToken.unauthenticated(req.username(),req.password()));
  UserAccount user=users.findByUsername(req.username()).orElseThrow();
  Map<String,Set<String>> policy="ADMIN".equals(user.getRole())?ADMIN_GRANTS:USER_GRANTS;
  Set<String> allowed=policy.get(req.audience());
  if(allowed==null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Unsupported audience");
  List<String> scopes=req.scopes()==null||req.scopes().isEmpty()?allowed.stream().sorted().toList():req.scopes().stream().distinct().sorted().toList();
  if(scopes.isEmpty()||!allowed.containsAll(scopes)) throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Requested scope is not permitted");
  Instant now=Instant.now();
  JwtClaimsSet claims=JwtClaimsSet.builder().issuer(issuer).subject(user.getUsername())
   .audience(List.of(req.audience())).issuedAt(now).expiresAt(now.plus(minutes,ChronoUnit.MINUTES))
   .claim("role",user.getRole()).claim("scope",String.join(" ",scopes)).build();
  String token=jwtEncoder.encode(JwtEncoderParameters.from(JwsHeader.with(SignatureAlgorithm.RS256).build(),claims)).getTokenValue();
  return new TokenResponse(token,"Bearer",minutes*60,req.audience(),scopes);
 }
}
