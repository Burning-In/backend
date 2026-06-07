package com.momentum.interfaces.api.auth;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
class AuthV1ControllerTest {

  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private ObjectMapper objectMapper;

  @Test
  @DisplayName("GET /csrf 는 csrfToken 쿠키를 발급한다")
  void csrfIssuesCookie() throws Exception {
    mockMvc.perform(get("/api/v1/auth/csrf"))
        .andExpect(status().isOk())
        .andExpect(cookie().exists("csrfToken"));
  }

  @Test
  @DisplayName("회원가입은 CSRF 없이 가능하며 accessToken 과 HttpOnly refreshToken 쿠키를 반환한다")
  void registerReturnsAccessTokenAndRefreshCookie() throws Exception {
    mockMvc.perform(post("/api/v1/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(registerBody("reg@momentum.com")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
        .andExpect(cookie().exists("refreshToken"))
        .andExpect(cookie().httpOnly("refreshToken", true));
  }

  @Test
  @DisplayName("로그인은 CSRF 토큰이 없으면 403")
  void loginWithoutCsrfIsForbidden() throws Exception {
    mockMvc.perform(post("/api/v1/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(loginBody("nocsrf@momentum.com")))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("CSRF 토큰을 포함하면 로그인되어 accessToken 과 refreshToken 쿠키를 반환한다")
  void loginWithCsrfSucceeds() throws Exception {
    mockMvc.perform(post("/api/v1/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(registerBody("loginflow@momentum.com")))
        .andExpect(status().isOk());

    mockMvc.perform(post("/api/v1/auth/login").with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(loginBody("loginflow@momentum.com")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
        .andExpect(cookie().exists("refreshToken"));
  }

  @Test
  @DisplayName("토큰 없이 계정 조회 시 isLoggedIn=false")
  void accountWithoutTokenIsLoggedOut() throws Exception {
    mockMvc.perform(get("/api/v1/auth/account"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.isLoggedIn").value(false));
  }

  @Test
  @DisplayName("Bearer accessToken 으로 계정 조회 시 로그인 정보를 반환한다")
  void accountWithBearerTokenReturnsInfo() throws Exception {
    MvcResult registered = mockMvc.perform(post("/api/v1/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(registerBody("me@momentum.com")))
        .andExpect(status().isOk())
        .andReturn();

    String accessToken = objectMapper.readTree(registered.getResponse().getContentAsString())
        .path("data").path("accessToken").asText();

    mockMvc.perform(get("/api/v1/auth/account").header("Authorization", "Bearer " + accessToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.isLoggedIn").value(true))
        .andExpect(jsonPath("$.data.userId").isNumber());
  }

  private String registerBody(String email) {
    return """
        {"email":"%s","password":"pw1234","name":"홍길동","phoneNumber":"01012345678"}
        """.formatted(email);
  }

  private String loginBody(String email) {
    return """
        {"email":"%s","password":"pw1234"}
        """.formatted(email);
  }
}
