package at.ac.tuwien.sepm.groupphase.backend.config;

import at.ac.tuwien.sepm.groupphase.backend.security.JwtAuthorizationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Collections;
import java.util.List;

@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true, jsr250Enabled = true, prePostEnabled = true)
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthorizationFilter jwtAuthorizationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.cors().and()
            .csrf().disable()
            .addFilterBefore(jwtAuthorizationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        final List<String> permitAll = Collections.singletonList("*");
        final List<String> permitMethods = List.of(HttpMethod.GET.name(), HttpMethod.POST.name(), HttpMethod.PUT.name(),
            HttpMethod.PATCH.name(), HttpMethod.DELETE.name(), HttpMethod.OPTIONS.name(), HttpMethod.HEAD.name(),
            HttpMethod.TRACE.name());
        final CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedHeaders(permitAll);
        configuration.setAllowedOrigins(permitAll);
        configuration.setAllowedMethods(permitMethods);
        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
