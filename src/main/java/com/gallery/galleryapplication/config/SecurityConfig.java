package com.gallery.galleryapplication.config;

import com.gallery.galleryapplication.repositories.PersonRepository;
import com.gallery.galleryapplication.services.PersonDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final PersonDetailService personDetailService;

    public SecurityConfig(PersonDetailService personDetailService) {
        this.personDetailService = personDetailService;
    }


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.formLogin(form->form.loginPage("/login").loginProcessingUrl("/process_login")
                        .defaultSuccessUrl("/").failureUrl("/login?error"))
                .logout(logout->logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login"))
                .authorizeHttpRequests((authz) ->
                        authz.requestMatchers("/login/**","/registration/**")
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
