
package com.example.config;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.jwt.Jwt;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SecurityConfigTest {

  private final SecurityConfig securityConfig = new SecurityConfig();

  private Set<String> extractAuthorities(Jwt jwt) {
    Authentication authentication = securityConfig
        .jwtAuthenticationConverter()
        .convert(jwt);

    assertNotNull(authentication);

    return authentication.getAuthorities().stream()
        .map(GrantedAuthority::getAuthority)
        .collect(Collectors.toSet());
  }

  @Test
  void jwtWithRootRoles_addsRolePrefix() {
    Jwt jwt = Jwt.withTokenValue("test-token")
        .header("alg", "RS256")
        .claim("sub", "test-user")
        .claim("roles", List.of("USER", "ADMIN"))
        .build();

    Set<String> authorities = extractAuthorities(jwt);

    assertEquals(
        Set.of("ROLE_USER", "ROLE_ADMIN"),
        authorities);
  }

  @Test
  void jwtWithRealmAccess_extractsNestedRoles() {
    Jwt jwt = Jwt.withTokenValue("test-token")
        .header("alg", "RS256")
        .claim("sub", "test-user")
        .claim("realm_access", Map.of(
            "roles", List.of("ADMIN")))
        .build();

    Set<String> authorities = extractAuthorities(jwt);

    assertEquals(Set.of("ROLE_ADMIN"), authorities);
  }

  @Test
  void jwtWithRepeatedRoles_normalizesAuthorityNames() {
    Jwt jwt = Jwt.withTokenValue("test-token")
        .header("alg", "RS256")
        .claim("sub", "test-user")
        .claim("roles", List.of("ADMIN", "ROLE_ADMIN"))
        .claim("realm_access", Map.of(
            "roles", List.of("ADMIN")))
        .build();

    Set<String> authorities = extractAuthorities(jwt);

    assertEquals(Set.of("ROLE_ADMIN"), authorities);
  }

  @Test
  void jwtWithoutRoles_returnsEmptyAuthorities() {
    Jwt jwt = Jwt.withTokenValue("test-token")
        .header("alg", "RS256")
        .claim("sub", "test-user")
        .build();

    Set<String> authorities = extractAuthorities(jwt);

    assertTrue(authorities.isEmpty());
  }

  @Test
  void oidcUserService_validUser_extractsRoles() {
    OidcIdToken idToken = new OidcIdToken(
        "test-token",
        Instant.parse("2026-10-24T10:00:00Z"),
        Instant.parse("2026-10-24T11:00:00Z"),
        Map.of(
            "sub", "test-user",
            "roles", List.of("ADMIN")));

    OidcUserInfo userInfo = new OidcUserInfo(
        Map.of("sub", "test-user"));

    OidcUser originalUser = mock(OidcUser.class);
    OidcUserRequest request = mock(OidcUserRequest.class);

    when(originalUser.getIdToken()).thenReturn(idToken);
    when(originalUser.getUserInfo()).thenReturn(userInfo);
    when(originalUser.getClaims()).thenReturn(
        Map.of("roles", List.of("USER")));

    doReturn(Set.of(new SimpleGrantedAuthority("OIDC_USER")))
        .when(originalUser).getAuthorities();

    try (MockedConstruction<OidcUserService> mocked = mockConstruction(
        OidcUserService.class,
        (service, context) -> when(service.loadUser(request))
            .thenReturn(originalUser))) {

      OidcUser result = securityConfig
          .oidcUserService()
          .loadUser(request);

      Set<String> authorities = result.getAuthorities().stream()
          .map(GrantedAuthority::getAuthority)
          .collect(Collectors.toSet());

      assertTrue(authorities.contains("ROLE_USER"));
      assertTrue(authorities.contains("ROLE_ADMIN"));
      assertTrue(authorities.contains("OIDC_USER"));

      assertEquals(idToken, result.getIdToken());
      assertEquals(userInfo, result.getUserInfo());

      verify(mocked.constructed().get(0)).loadUser(request);
    }
  }
}
