package com.example.Dividend.security;

import com.example.Dividend.service.MemberService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.security.Key;
import java.util.Date;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TokenProvider {

    private static final String KEY_ROLES = "roles";
    private static final long TOKEN_EXPIRE_TIME = 1000 * 60 *60; // 1000밀리세컨드에 초, 분을 곱해서 총 1시간을 의미

    private final MemberService memberService;

    // 비밀키 값을 가져옴
    @Value("${spring.jwt.secret}")
    private String secretKey;

    /**
     * 토큰 생성(발급)
     * @param username
     * @param roles
     * @return
     */
    // 토큰 생성
    public String generateToken(String username, List<String> roles) {
        // 비밀키를 생성
        Key key = Keys.hmacShaKeyFor(secretKey.getBytes());
        // 사용자의 권한 정보를 저장하기 위해 Claim 생성
        Claims claims = (Claims) Jwts.claims().setSubject(username);
        claims.put(KEY_ROLES, roles); // 클레임에 데이터를 저장하기 위해선 키-밸류 타입으로 저장되어야 함-> 상수로 키를 생성하여 사용하는 것이 좋음

        // 토큰 생성 시간
        var now = new Date();

        // 토큰 만료시간
        var expiredDate = new Date(now.getTime() + TOKEN_EXPIRE_TIME);

        // 이렇게 생성된 Claims 정보와 만료 시간을 토큰에 넣어서 생성
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now) // 토큰 생성 시간
                .setExpiration(expiredDate) // 토큰 만료 시간
                .signWith(key, SignatureAlgorithm.HS512) // 토큰 시그니처: 사용할 암호화 알고리즘 & 비밀키
                .compact();
    }

    // jwt 토큰으로부터 인증 정보를 가져오는 메소드
    public Authentication getAuthentication(String jwt) {
        UserDetails userDetails = this.memberService.loadUserByUsername(this.getUsername(jwt));
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }


    // 토큰 유효성 확인
    public String getUsername(String token) {
        return this.parseClaims(token).getSubject();
    }

    public boolean validateToken(String token) {
        if (!StringUtils.hasText(token)) return false; // token이 빈 값이라면 false

        var claims = this.parseClaims(token);
        // 클레임 만료시간에 현재시간을 비교한 값을 받아서 이전인지 아닌지를 체크하여 토큰이 유효한지 아닌지 확인
        return !claims.getExpiration().before(new Date());
    }

    // 토큰으로부터 Claim 정보를 가져오는 메소드
    private Claims parseClaims(String token) {
        // 비밀키를 생성
        Key key = Keys.hmacShaKeyFor(secretKey.getBytes());
        // 토큰 만료시간이 경과한 상태로 secretKey 값을 가져오는 경우를 발생하는 ExpiredJwtException 핸들링을 위해 try-catch 문
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)  // Key 객체로 설정
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            // TODO
            return e.getClaims();
        }
    }
}