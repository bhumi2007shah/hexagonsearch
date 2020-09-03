/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.litmusblox.server.utils.StringEncryptorDecryptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.Serializable;
import java.util.Date;
import java.util.function.Function;

/**
 * Utility class for jwt token
 *
 * @author : Shital Raval
 * Date : 18/7/19
 * Time : 10:35 AM
 * Class Name : JwtTokenUtil
 * Project Name : server
 */
@Component
public class JwtTokenUtil implements Serializable {

    private static final long serialVersionUID = -2550185165626007488L;

    public static final long JWT_TOKEN_VALIDITY = 24 * 60 * 60 * 1000; //24 hours as milliseconds

    @Value("${jwt.secret}")
    private String secret;

    //retrieve username from jwt token
    public String getUsernameFromToken(String token) {
        String userName = getClaimFromToken(token, Claims::getSubject);
        return StringEncryptorDecryptor.decrypt(userName);
    }

    //retrieve expiration date from jwt token
    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }
    //for retrieveing any information from token we will need the secret key
    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
    }

    //check if the token has expired
    private Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    //generate token for user
    public String generateToken(UserDetails userDetails, Long userId, Long companyId) {
        return doGenerateToken(userDetails.getAuthorities().toString().replace("[","").replace("]",""),
                userDetails.getUsername(),
                userId,
                companyId);
    }

    //while creating the token -
    //1. Define  claims of the token, like Issuer, Expiration, Subject, and the ID
    //2. Sign the JWT using the HS512 algorithm and secret key.
    //3. According to JWS Compact Serialization(https://tools.ietf.org/html/draft-ietf-jose-json-web-signature-41#section-3.1)
    //   compaction of the JWT to a URL-safe string
    private String doGenerateToken(String role, String subject, Long userId, Long companyId) {

        return Jwts.builder()
                .claim("roles", StringEncryptorDecryptor.encrypt(role))
                .claim("userId", StringEncryptorDecryptor.encrypt(userId.toString()))
                .claim("company", StringEncryptorDecryptor.encrypt(companyId.toString()))
                .setSubject(StringEncryptorDecryptor.encrypt(subject))
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + JWT_TOKEN_VALIDITY))
                .signWith(SignatureAlgorithm.HS512, secret).compact();
    }

    //validate token
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = getUsernameFromToken(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    public static String getAuthToken(){
        return  (((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())).getRequest().getHeader("Authorization");
    }
}
