package com.financeiro.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.servlet.server.CookieSameSiteSupplier;
import org.springframework.context.ApplicationContext;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(properties = {
    "google.sheets.spreadsheet.id=test-spreadsheet-id",
    "spring.security.oauth2.client.registration.google.client-id=test-client-id",
    "spring.security.oauth2.client.registration.google.client-secret=test-client-secret"
})
public class SecurityConfigTest {

    @Autowired
    private ApplicationContext context;

    @MockBean
    private ClientRegistrationRepository clientRegistrationRepository;

    @MockBean
    private OAuth2AuthorizedClientService authorizedClientService;

    @Test
    void testCookieSameSiteSupplierBeanExists() {
        boolean beanExists = context.containsBean("cookieSameSiteSupplier");
        assertTrue(beanExists, "CookieSameSiteSupplier bean should be present");

        CookieSameSiteSupplier supplier = context.getBean(CookieSameSiteSupplier.class);
        assertNotNull(supplier, "CookieSameSiteSupplier bean should not be null");
    }
}
