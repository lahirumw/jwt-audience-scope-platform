package dev.nordlearn.auth;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.*;
import org.springframework.security.crypto.password.PasswordEncoder;
@Configuration
public class DemoUsers {
 @Bean CommandLineRunner seed(UserRepository users,PasswordEncoder encoder){return args->{
  if(!users.existsByUsername("demo")) users.save(new UserAccount("demo",encoder.encode("DemoPass123!"),"USER"));
  if(!users.existsByUsername("admin")) users.save(new UserAccount("admin",encoder.encode("AdminPass123!"),"ADMIN"));
 };}
}
