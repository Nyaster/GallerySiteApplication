package com.gallery.galleryapplication.config;

import com.gallery.galleryapplication.services.PersonDetailService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
public class SecurityConfig {
    private final PersonDetailService personDetailService;

    public SecurityConfig(PersonDetailService personDetailService) {
        this.personDetailService = personDetailService;
    }


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.formLogin(form->form.loginPage("/auth/login").loginProcessingUrl("/auth/process_login")
                        .defaultSuccessUrl("/").failureUrl("/auth/login?error"))
                .logout(logout->logout
                        .logoutUrl("/auth/logout")
                        .logoutSuccessUrl("/auth/login?logout"))
                .authorizeHttpRequests((authz) ->
                        authz.requestMatchers("/auth/**","/styles/**")
                                .permitAll()
                                .anyRequest()
                                .authenticated()).userDetailsService(personDetailService)
                .httpBasic(withDefaults());
        return http.build();
    }
    @Bean
    PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

}
