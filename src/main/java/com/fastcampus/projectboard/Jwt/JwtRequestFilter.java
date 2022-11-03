package com.fastcampus.projectboard.Jwt;

import com.fastcampus.projectboard.Service.UserSecurityService;
import com.fastcampus.projectboard.Util.CookieUtil;
import com.fastcampus.projectboard.Util.RedisUtil;
import com.fastcampus.projectboard.Util.TokenProvider;
import com.fastcampus.projectboard.dto.security.BoardPrincipal;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.GenericFilterBean;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Service
public class JwtRequestFilter extends OncePerRequestFilter {

    @Autowired
    private UserSecurityService userDetailsService;

    @Autowired
    private TokenProvider tokenProvider;

    @Autowired
    private CookieUtil cookieUtil;

    @Autowired
    private RedisUtil redisUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain) throws ServletException, IOException, java.io.IOException {
        final Cookie jwtToken = cookieUtil.getCookie(httpServletRequest, TokenProvider.ACCESS_TOKEN_NAME);

        String username = null;
        String jwt = null;
        String refreshJwt = null;
        String refreshUname = null;

        try {
            if (jwtToken != null) {
                jwt = jwtToken.getValue();
                username = tokenProvider.getUsername(jwt);
            } else {
                logger.warn("Cannot find access token");
            }
            if (username != null) {
                System.out.println("userDetails : " + username);
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                if (tokenProvider.validateToken(jwt)) {
                    logger.info("validateToken : " + jwt);
                    UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(username, null, null);
                    usernamePasswordAuthenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(httpServletRequest));
                    SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
                }
            } else {
                logger.warn("Cannot find username from access token");
            }
        } catch (ExpiredJwtException e) {
            String refreshToken = cookieUtil.getCookie(httpServletRequest, TokenProvider.REFRESH_TOKEN_NAME).getValue();
            System.out.println("refreshToken : " + refreshToken);
            if (refreshToken != null) {
                refreshJwt = refreshToken;
            } else {
                logger.warn("Cannot find refresh token");
            }
        } catch (Exception e) {
        }
        try {
            if (refreshJwt != null) {
                refreshUname = redisUtil.getData(refreshJwt);
                if (refreshUname.equals(tokenProvider.getUsername(refreshJwt))) {
                    UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(refreshUname, null, null);
                    usernamePasswordAuthenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(httpServletRequest));
                    SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
                    Cookie newAccessToken = cookieUtil.createCookie(TokenProvider.ACCESS_TOKEN_NAME, tokenProvider.doGenerateToken(refreshUname, TokenProvider.TOKEN_VALIDATION_SECOND));
                    httpServletResponse.addCookie(newAccessToken);
                }
            } else {
                logger.warn("Cannot find refresh token");
            }
        } catch (ExpiredJwtException e) {

        }

        filterChain.doFilter(httpServletRequest, httpServletResponse);
    }
}