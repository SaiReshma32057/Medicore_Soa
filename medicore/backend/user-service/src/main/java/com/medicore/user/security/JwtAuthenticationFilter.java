package com.medicore.user.security;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
 private final JwtService jwtService;
 public JwtAuthenticationFilter(JwtService jwtService){this.jwtService=jwtService;}
 @Override protected void doFilterInternal(HttpServletRequest request,HttpServletResponse response,FilterChain chain)throws ServletException,IOException{
  String header=request.getHeader("Authorization");
  if(header!=null&&header.startsWith("Bearer ")){String token=header.substring(7);if(jwtService.valid(token)){String user=jwtService.username(token);String role=jwtService.role(token);SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(user,null,Collections.singleton(() -> "ROLE_"+role)));}}
  chain.doFilter(request,response);
 }
}
