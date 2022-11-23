package com.fastcampus.projectboard.Util;

import com.fastcampus.projectboard.domain.UserAccount;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;


@Component
public class TokenProvider  {

    private final Logger logger = LoggerFactory.getLogger(TokenProvider.class);

    private final RedisUtil redisUtil;

    public final static long TOKEN_VALIDATION_SECOND = 1000L * 600; // 10분
    public final static long REFRESH_TOKEN_VALIDATION_SECOND = 1000L * 60 * 60 * 24 * 7; // 7일

    final static public String ACCESS_TOKEN_NAME = "accessToken";
    final static public String REFRESH_TOKEN_NAME = "refreshToken";

    @Value("${spring.jwt.secret}")
    private String SECRET_KEY;

    public TokenProvider(RedisUtil redisUtil) {
        this.redisUtil = redisUtil;
    }

    private Key getSigningKey(String secretKey) {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public Claims extractAllClaims(String token) throws ExpiredJwtException {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey(SECRET_KEY))
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String getUsername(String token) {
        return extractAllClaims(token).get("username", String.class);
    }

    public Boolean isTokenExpired(String token) {
        final Date expiration = extractAllClaims(token).getExpiration();
        return expiration.before(new Date());
    }

    public String generateToken(UserAccount.BoardPrincipal member) {
        return doGenerateToken(member.getUsername(), TOKEN_VALIDATION_SECOND);
    }

    public String generateRefreshToken(UserAccount.BoardPrincipal member) {
        return doGenerateToken(member.getUsername(), REFRESH_TOKEN_VALIDATION_SECOND);
    }

    public String doGenerateToken(String username, long expireTime) {

        Claims claims = Jwts.claims();
        claims.put("username", username);

        String jwt = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expireTime))
                .signWith(getSigningKey(SECRET_KEY), SignatureAlgorithm.HS256)
                .compact();

        return jwt;
    }

    public Long getExpireTime(String token) {
        return extractAllClaims(token).getExpiration().getTime();
    }

    public Boolean validateToken(String token) {
        if (redisUtil.hasKeyBlackList("LOGOUT_"+token)) {
            return false;
        }
        return true;
    }

    public Authentication getAuthentication(String requestAccessToken) {
        UserDetails userDetails = new UserAccount.BoardPrincipal(getUsername(requestAccessToken), null, null, null,null,null);
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }
}