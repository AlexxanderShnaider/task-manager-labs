
package com.example.resolver;

import com.example.annotation.CurrentUsername;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.ServletWebRequest;

import static org.junit.jupiter.api.Assertions.*;

class CurrentUsernameResolverTest {

  private final CurrentUsernameResolver resolver = new CurrentUsernameResolver();

  private MethodParameter parameter(
      String methodName,
      Class<?> parameterType) throws Exception {

    return new MethodParameter(
        TestMethods.class.getDeclaredMethod(methodName, parameterType),
        0);
  }

  @Test
  void supportsParameter_acceptsAnnotatedString() throws Exception {
    assertTrue(resolver.supportsParameter(
        parameter("annotated", String.class)));

    assertFalse(resolver.supportsParameter(
        parameter("plain", String.class)));

    assertFalse(resolver.supportsParameter(
        parameter("wrongType", Long.class)));
  }

  @Test
  void resolveArgument_returnsPrincipalName() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setUserPrincipal(() -> "admin_user");

    Object result = resolver.resolveArgument(
        parameter("annotated", String.class),
        null,
        new ServletWebRequest(request),
        null);

    assertEquals("admin_user", result);
  }

  @Test
  void resolveArgument_withoutPrincipal_returnsAnonymous() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest();

    Object result = resolver.resolveArgument(
        parameter("annotated", String.class),
        null,
        new ServletWebRequest(request),
        null);

    assertEquals("anonymous", result);
  }

  private static class TestMethods {

    void annotated(@CurrentUsername String username) {
    }

    void plain(String username) {
    }

    void wrongType(@CurrentUsername Long id) {
    }
  }
}
