package com.gallery.galleryapplication.config;

import com.gallery.galleryapplication.services.LogEntryService;
import com.gallery.galleryapplication.services.PersonDetailService;
import org.apache.catalina.connector.Connector;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.rememberme.TokenBasedRememberMeServices;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    private final PersonDetailService personDetailService;
    private final String MY_KEY = "TemporaryKey";
    @Value("${server.http.port}")
    private int httpPort;
    @Value("${server.port}")
    private int SSLport;

    public SecurityConfig(PersonDetailService personDetailService) {
        this.personDetailService = personDetailService;
    }

    @Bean
    static RoleHierarchy roleHierarchy() {
        RoleHierarchyImpl hierarchy = new RoleHierarchyImpl();
        hierarchy.setHierarchy("ROLE_ADMIN > ROLE_EDITOR\n" + "ROLE_EDITOR > ROLE_USER\n");
        return hierarchy;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, RememberMeServices services, CustomAuthenteficationSuccesHandler customAuthenteficationSuccesHandler) throws Exception {
        //.requiresChannel(x->x.anyRequest().requiresSecure()) добавить что бы редирект
        http.portMapper(x -> x.http(httpPort).mapsTo(SSLport))
                .formLogin(form -> form.loginPage("/auth/login")
                        .loginProcessingUrl("/auth/process_login")
                        .defaultSuccessUrl("/")
                        .failureUrl("/auth/login?error")
                        .successHandler(customAuthenteficationSuccesHandler))
                .logout(logout -> logout.logoutUrl("/auth/logout")
                        .logoutSuccessUrl("/auth/login?logout"))
                .authorizeHttpRequests((authz) -> authz.requestMatchers("/favicons/**")
                        .permitAll().requestMatchers("/admin/**")
                        .hasRole("ADMIN")
                        .requestMatchers("api/*/*/edit")
                        .hasRole("EDITOR")
                        .requestMatchers("/auth/**", "/styles/**")
                        .permitAll()
                        .anyRequest()
                        .authenticated())
                .rememberMe(x -> x.rememberMeServices(services)
                        .userDetailsService(personDetailService)
                        .authenticationSuccessHandler(customAuthenteficationSuccesHandler))
                .httpBasic(withDefaults());
        return http.build();
    }

    @Bean
    public CustomAuthenteficationSuccesHandler customAuthenteficationSuccesHandler(LogEntryService logEntryService) {
        return new CustomAuthenteficationSuccesHandler(logEntryService);
    }

    @Bean
    RememberMeServices rememberMeServices(PersonDetailService userDetailsService) {
        TokenBasedRememberMeServices.RememberMeTokenAlgorithm encodingAlgorithm = TokenBasedRememberMeServices.RememberMeTokenAlgorithm.SHA256;
        TokenBasedRememberMeServices rememberMe = new TokenBasedRememberMeServices(MY_KEY, userDetailsService, encodingAlgorithm);
        rememberMe.setMatchingAlgorithm(TokenBasedRememberMeServices.RememberMeTokenAlgorithm.MD5);
        rememberMe.setUseSecureCookie(true);
        return rememberMe;
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> cookieProcessorCustomizer() {
        return (TomcatServletWebServerFactory factory) -> {
            final Connector connector = new Connector();
            connector.setPort(httpPort);
            factory.addAdditionalTomcatConnectors(connector);
        };
    }
}
