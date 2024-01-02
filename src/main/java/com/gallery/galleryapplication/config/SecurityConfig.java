package com.gallery.galleryapplication.config;

import com.gallery.galleryapplication.services.PersonDetailService;
import org.apache.catalina.connector.Connector;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
public class SecurityConfig {
    private final PersonDetailService personDetailService;
    @Value("${server.http.port}")
    private int httpPort;

    public SecurityConfig(PersonDetailService personDetailService) {
        this.personDetailService = personDetailService;
    }


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        //.requiresChannel(x->x.anyRequest().requiresSecure()) добавить что бы редирект
        http.portMapper(x->x.http(8080).mapsTo(8443)).formLogin(form->form.loginPage("/auth/login").loginProcessingUrl("/auth/process_login")
                        .defaultSuccessUrl("/").failureUrl("/auth/login?error"))
                .logout(logout->logout
                        .logoutUrl("/auth/logout")
                        .logoutSuccessUrl("/auth/login?logout"))
                .authorizeHttpRequests((authz) ->
                        authz.requestMatchers("/admin/**","auth/registration/**").hasRole("ADMIN")
                                .requestMatchers("/auth/**","/styles/**").permitAll()
                                .anyRequest().authenticated())
                .rememberMe(x->x.userDetailsService(personDetailService)).userDetailsService(personDetailService)
                .httpBasic(withDefaults());
        return http.build();
    }
    @Bean
    PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }
    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> cookieProcessorCustomizer() {
        return (TomcatServletWebServerFactory factory) -> {
            // also listen on http
            final Connector connector = new Connector();
            connector.setPort(httpPort);
            factory.addAdditionalTomcatConnectors(connector);
        };
    }

}
