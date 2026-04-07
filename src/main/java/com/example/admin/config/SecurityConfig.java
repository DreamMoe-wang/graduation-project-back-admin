package com.example.admin.config;

import com.example.admin.security.CustomUserDetailsService;
import com.example.admin.security.JwtAuthenticationFilter;
import com.example.admin.security.RestAccessDeniedHandler;
import com.example.admin.security.RestAuthenticationEntryPoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.annotation.Resource;

/**
 * Spring Security 配置
 */
@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Resource
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Resource
    private CustomUserDetailsService customUserDetailsService;

    @Resource
    private RestAuthenticationEntryPoint restAuthenticationEntryPoint;

    @Resource
    private RestAccessDeniedHandler restAccessDeniedHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authenticationProvider(authenticationProvider())
                .exceptionHandling()
                .authenticationEntryPoint(restAuthenticationEntryPoint)
                .accessDeniedHandler(restAccessDeniedHandler)
                .and()
                .authorizeRequests()
                .antMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .antMatchers(HttpMethod.POST, "/user").permitAll()
                .antMatchers("/user/login", "/auth/login", "/error").permitAll()
                .antMatchers("/ws/**").permitAll()
                .antMatchers(HttpMethod.GET, "/user/profile/current").authenticated()
                .antMatchers(HttpMethod.PUT, "/user/profile/current").authenticated()
                .antMatchers(HttpMethod.GET, "/notice/public/**").authenticated()
                .antMatchers("/trade/publish/*/approve", "/trade/publish/*/reject").hasAuthority("trade:review")
                .antMatchers(HttpMethod.GET, "/menu/page", "/menu/tree", "/menu/*").hasAuthority("menu:view")
                .antMatchers(HttpMethod.POST, "/menu").hasAuthority("menu:create")
                .antMatchers(HttpMethod.PUT, "/menu/*").hasAuthority("menu:edit")
                .antMatchers(HttpMethod.DELETE, "/menu/*").hasAuthority("menu:delete")
                .antMatchers("/role/**", "/notice/**", "/log/**", "/setting/**").hasRole("ADMIN")
                .antMatchers(HttpMethod.GET, "/user/page", "/user/list").hasRole("ADMIN")
                .antMatchers(HttpMethod.PUT, "/user/**").hasRole("ADMIN")
                .antMatchers(HttpMethod.DELETE, "/user/**").hasRole("ADMIN")
                .anyRequest().authenticated();

        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(customUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}
