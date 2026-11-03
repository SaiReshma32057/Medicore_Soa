package com.medicore.user.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {
 private final SecretKey key; private final long expiration;
 public JwtService(@Value("${app.jwt.secret}") String secret,@Value("${app.jwt.expiration-ms}") long expiration){this.key=Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));this.expiration=expiration;}
 public String generate(String email,String role){Date now=new Date();return Jwts.builder().subject(email).claim("role",role).issuedAt(now).expiration(new Date(now.getTime()+expiration)).signWith(key).compact();}
 public String username(String token){return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload().getSubject();}
 public String role(String token){Object role=Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload().get("role");return role instanceof String ? (String) role : "USER";}
 public boolean valid(String token){try{username(token);return true;}catch(JwtException|IllegalArgumentException e){return false;}}
}
