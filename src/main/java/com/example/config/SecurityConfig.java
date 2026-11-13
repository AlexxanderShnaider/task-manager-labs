package com.example.config;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http,
            ClientRegistrationRepository clientRegistrationRepository) throws Exception {
        http
                // Disabled for lab testing and develping reasons
                .csrf(csrf -> csrf.disable())

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/tasks/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/tasks/**").hasAuthority("ROLE_ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/tasks/**").hasAuthority("ROLE_ADMIN")
                        .anyRequest().authenticated())

                // OIDC
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo.oidcUserService(oidcUserService())))

                // Bearer token authentication
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(
                                jwtAuthenticationConverter())))

                .logout(logout -> logout
                        .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                        .logoutSuccessHandler(
                                oidcLogoutSuccessHandler(clientRegistrationRepository))
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID"));

        return http.build();
    }

    private OidcClientInitiatedLogoutSuccessHandler oidcLogoutSuccessHandler(
            ClientRegistrationRepository clientRegistrationRepository) {
        OidcClientInitiatedLogoutSuccessHandler successHandler = new OidcClientInitiatedLogoutSuccessHandler(
                clientRegistrationRepository);

        successHandler.setPostLogoutRedirectUri("{baseUrl}/");

        return successHandler;
    }

    // The default OIDC user service does not automatically map Keycloak roles
    // to Spring Security authorities. This custom service extracts those roles
    // so they can be used for role-based access control.

    @Bean
    public OAuth2UserService<OidcUserRequest, OidcUser> oidcUserService() {
        OidcUserService delegate = new OidcUserService();

        return userRequest -> {
            OidcUser oidcUser = delegate.loadUser(userRequest);

            // Merge existing authorities with roles from OIDC claims and the ID token.
            Set<GrantedAuthority> authorities = new HashSet<>(oidcUser.getAuthorities());

            authorities.addAll(extractRoles(oidcUser.getClaims()));

            if (oidcUser.getIdToken() != null) {
                authorities.addAll(extractRoles(oidcUser.getIdToken().getClaims()));
            }

            return new DefaultOidcUser(
                    authorities,
                    oidcUser.getIdToken(),
                    oidcUser.getUserInfo());
        };
    }

    // Spring Security maps JWT scopes to SCOPE_ authorities by default.
    // This converter uses Keycloak roles instead and maps them to ROLE_
    // authorities required by our access rules.

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();

        // Use Keycloak roles instead of the default JWT scope authorities.
        converter.setJwtGrantedAuthoritiesConverter(
                jwt -> extractRoles(jwt.getClaims()));

        return converter;
    }

    // Keycloak roles may appear in different claim structures.
    // This helper supports both top-level and realm_access roles
    // and normalizes their names for Spring Security.

    private Collection<GrantedAuthority> extractRoles(Map<String, Object> claims) {
        if (claims == null) {
            return Collections.emptyList();
        }

        Set<String> allRoles = new HashSet<>();

        if (claims.containsKey("roles")) {
            Object rolesObj = claims.get("roles");

            if (rolesObj instanceof Collection<?> rolesList) {
                rolesList.forEach(role -> allRoles.add(role.toString()));
            }
        }

        if (claims.containsKey("realm_access")) {
            Object realmAccessObj = claims.get("realm_access");

            if (realmAccessObj instanceof Map<?, ?> realmAccess) {
                Object nestedRolesObj = realmAccess.get("roles");

                if (nestedRolesObj instanceof Collection<?> nestedRolesList) {
                    nestedRolesList.forEach(role -> allRoles.add(role.toString()));
                }
            }
        }

        // Normalize role names to Spring Security's ROLE_ convention.
        return allRoles.stream()
                .filter(Objects::nonNull)
                .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }
}