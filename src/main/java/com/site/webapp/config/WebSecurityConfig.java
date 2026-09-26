package com.site.webapp.config;

import com.site.webapp.service.RegistrationService;
import com.site.webapp.service.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Конфигурация безопасности Spring Security.
 * Настраивает:
 * - Формы логина/регистрации
 * - OAuth2 через Google
 * - Права доступа к /admin/**
 * - CSRF отключен
 * - Методную безопасность (@PreAuthorize)
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class WebSecurityConfig{

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final RegistrationService registrationService;

    public WebSecurityConfig(UserService userService,
                             PasswordEncoder passwordEncoder,
                             RegistrationService registrationService) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.registrationService = registrationService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
        http
            .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth->auth
                .requestMatchers("/registration").permitAll()
                .requestMatchers("/authorization").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
        )
         .formLogin(form->form
                 .loginPage("/authorization")
                 .loginProcessingUrl("/authorization")
                 .defaultSuccessUrl("/", true)
                 .failureUrl("/authorization?error=true")
                 .usernameParameter("email")
                 .passwordParameter("password")
                 .permitAll()
         )
        .oauth2Login(oauth2 -> oauth2
                .loginPage("/authorization")
                .defaultSuccessUrl("/all-tasks", true)
                .userInfoEndpoint(userInfo -> userInfo
                    .userService(this::processOAuth2User)
                )
            )
         .logout(logout -> logout
             .logoutUrl("/logout")
             .logoutSuccessUrl("/authorization?logout=true")
             .permitAll()
         ).authenticationProvider(authenticationProvider());

        return http.build();
    }
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }

    /**
     * Обрабатывает OAuth2 пользователя от провайдера (Google).
     * Регистрирует или находит пользователя в системе по данным OAuth2.
     */
    private OAuth2User processOAuth2User(OAuth2UserRequest userRequest) {
        DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        String provider = userRequest.getClientRegistration().getRegistrationId();

        registrationService.registerOAuth2User(oAuth2User, provider);

        return oAuth2User;
    }

}
