package com.scentory.admin.config;

import com.scentory.admin.repository.UserAccountRepository;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(Customizer.withDefaults())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**").permitAll()
                .requestMatchers("/public/**").permitAll()
                .requestMatchers("/login", "/password/**", "/terms", "/privacy", "/contact").permitAll()
                .requestMatchers("/h2-console/**").permitAll()
                .requestMatchers("/staff/**", "/settings/**", "/contents/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/reports/**").hasAnyRole("ADMIN", "STAFF", "VIEWER")
                .requestMatchers("/reports/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/reservations/**", "/slots/**", "/customers/**", "/menus/**")
                    .hasAnyRole("ADMIN", "STAFF", "VIEWER")
                .requestMatchers("/reservations/**", "/slots/**", "/customers/**", "/menus/**")
                    .hasAnyRole("ADMIN", "STAFF")
                .anyRequest().authenticated()
            )
            .formLogin(login -> login
                .loginPage("/login")
                .defaultSuccessUrl("/dashboard", true)
                .failureUrl("/login?error")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutRequestMatcher(new OrRequestMatcher(
                    new AntPathRequestMatcher("/logout", "GET"),
                    new AntPathRequestMatcher("/logout", "POST")
                ))
                .logoutSuccessUrl("/login?logout")
            )
            .csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**", "/public/**"))
            .headers(headers -> headers.frameOptions(frame -> frame.disable()));
        return http.build();
    }

    @Bean
    UserDetailsService userDetailsService(UserAccountRepository repository) {
        return username -> repository.findByUsername(username)
            .map(user -> User.withUsername(user.getUsername())
                .password(user.getPassword())
                .disabled(!user.isEnabled())
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_" + user.getRoleType().name())))
                .build())
            .orElseThrow();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
