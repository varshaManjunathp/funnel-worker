package com.pharmeasy.funnel.config.auth;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.AllArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;

@Component
@AllArgsConstructor
public class HeaderAuthenticationService
        implements AuthenticationUserDetailsService<PreAuthenticatedAuthenticationToken> {

    // Role name needs to have "ROLE_" prefix.
    private static final List<SimpleGrantedAuthority> ROLE_USER =
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
    private final JWTTokenUtil jwtTokenUtil;

    @Override
    public UserDetails loadUserDetails(PreAuthenticatedAuthenticationToken token)
            throws UsernameNotFoundException {
        final String bearerToken = (String) token.getCredentials();

        try {
            DecodedJWT decoded = jwtTokenUtil.verifyToken(bearerToken);
            String sub = decoded.getSubject();
            if (StringUtils.hasText(sub)) {
                return new User(sub, "", ROLE_USER);
            } else {
                throw new UsernameNotFoundException("JWT token doesn't contain subject");
            }
        } catch (JWTVerificationException e) {
            throw new UsernameNotFoundException("JWT error", e);
        }
    }
}
