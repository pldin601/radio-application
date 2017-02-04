package com.radioteria.config

import com.radioteria.domain.repository.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import javax.servlet.http.HttpServletResponse

@Configuration
@EnableGlobalMethodSecurity(securedEnabled = true)
class WebSecurityConfig : WebSecurityConfigurerAdapter() {
    @Autowired private lateinit var userRepository: UserRepository

    override fun configure(http: HttpSecurity) {
        http.httpBasic()
                .authenticationEntryPoint { request, response, exception ->
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized")
                }

        http.formLogin()
                .usernameParameter("email")
                .loginProcessingUrl("/api/auth/login")
                .successHandler { request, response, authentication ->
                    response.status = HttpServletResponse.SC_OK
                }
                .failureHandler { request, response, exception ->
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized")
                }
    }

    override fun configure(auth: AuthenticationManagerBuilder) {
        auth.userDetailsService(userDetailsService())
    }

    override fun userDetailsService(): UserDetailsService {
        return UserDetailsService { username ->
            val user = userRepository.findByEmail(username) ?:
                    throw UsernameNotFoundException("User with email \"$username\" does not exist.")

            val roles = setOf(SimpleGrantedAuthority(user.role.toString()))

            User(user.email, user.password, roles)
        }
    }
}