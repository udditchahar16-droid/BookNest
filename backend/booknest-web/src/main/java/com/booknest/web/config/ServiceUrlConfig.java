package com.booknest.web.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Centralises all back-end microservice base URLs.
 * Values are injected from application.properties so nothing is hard-coded
 * in controller classes.
 */
@Component
public class ServiceUrlConfig {

    @Value("${service.auth.url}")
    public String authUrl;

    @Value("${service.book.url}")
    public String bookUrl;

    @Value("${service.cart.url}")
    public String cartUrl;

    @Value("${service.order.url}")
    public String orderUrl;

    @Value("${service.wallet.url}")
    public String walletUrl;

    @Value("${service.review.url}")
    public String reviewUrl;

    @Value("${service.notification.url}")
    public String notificationUrl;

    @Value("${service.wishlist.url}")
    public String wishlistUrl;
}
