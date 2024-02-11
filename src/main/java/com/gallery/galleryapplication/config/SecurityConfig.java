package com.gallery.galleryapplication.config;

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

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    private final PersonDetailService personDetailService;
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
        hierarchy.setHierarchy("ROLE_ADMIN > ROLE_EDITOR\n" +
                "ROLE_EDITOR > ROLE_USER\n");
        return hierarchy;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        //.requiresChannel(x->x.anyRequest().requiresSecure()) добавить что бы редирект
        http.portMapper(x -> x.http(httpPort).mapsTo(SSLport)).formLogin(form -> form.loginPage("/auth/login").loginProcessingUrl("/auth/process_login")
                        .defaultSuccessUrl("/fan-images").failureUrl("/auth/login?error"))
                .logout(logout -> logout
                        .logoutUrl("/auth/logout")
                        .logoutSuccessUrl("/auth/login?logout"))
                .authorizeHttpRequests((authz) ->
                        authz.requestMatchers("/favicons/**").permitAll().requestMatchers("/admin/**", "auth/registration/**").hasRole("ADMIN")
                                .requestMatchers("api/*/*/edit").hasRole("EDITOR")
                                .requestMatchers("/auth/**", "/styles/**").permitAll().anyRequest().authenticated())
                .rememberMe(x -> x.userDetailsService(personDetailService)).userDetailsService(personDetailService)
                .httpBasic(withDefaults());
        return http.build();
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
