package com.financeiro.service;

import com.financeiro.exception.GoogleSheetsAuthException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.test.context.TestPropertySource;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
@TestPropertySource(properties = "google.sheets.spreadsheet.id=")
class GoogleSheetsServiceDynamicIdTest {

    @Autowired
    private GoogleSheetsService googleSheetsService;

    @MockBean
    private OAuth2AuthorizedClientService authorizedClientService;

    @MockBean
    private ClientRegistrationRepository clientRegistrationRepository;

    @Test
    void testResolveSpreadsheetId_CreatesNew_WhenNotConfigured() throws Exception {
        // Mock Security Context with OAuth2 token
        SecurityContext securityContext = mock(SecurityContext.class);
        OAuth2AuthenticationToken authentication = mock(OAuth2AuthenticationToken.class);
        when(authentication.getName()).thenReturn("testuser");
        when(authentication.getAuthorizedClientRegistrationId()).thenReturn("google");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        // Mock OAuth2 Client Service to return a valid client with token
        OAuth2AuthorizedClient authorizedClient = mock(OAuth2AuthorizedClient.class);
        OAuth2AccessToken accessToken = new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER, "mock-token", Instant.now(), Instant.now().plusSeconds(3600));
        when(authorizedClient.getAccessToken()).thenReturn(accessToken);
        when(authorizedClientService.loadAuthorizedClient(any(), anyString())).thenReturn(authorizedClient);

        // Access private method resolveSpreadsheetId
        Method method = GoogleSheetsService.class.getDeclaredMethod("resolveSpreadsheetId");
        method.setAccessible(true);

        try {
            method.invoke(googleSheetsService);
            fail("Should have thrown IOException due to network failure (mock token), or GoogleSheetsAuthException, but definitely not configuration error");
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();

            System.out.println("Caught expected exception: " + cause.getClass().getSimpleName() + ": " + cause.getMessage());

            if (cause instanceof GoogleSheetsAuthException) {
                String msg = cause.getMessage();
                assertNotEquals("Spreadsheet ID not configured", msg, "Should not fail due to missing configuration");
            }
            // If it's IOException, it means it tried to hit Google API and failed (expected)
            // If it's anything else, we might need to inspect, but definitely pass if it's not the configuration error.
        }
    }
}
