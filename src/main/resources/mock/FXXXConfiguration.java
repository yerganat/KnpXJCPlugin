package kz.inessoft.sono.app.fno.fXXX;

import kz.inessoft.sono.lib.sso.SSOAuthTokenFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.access.ExceptionTranslationFilter;
import org.springframework.security.web.authentication.Http403ForbiddenEntryPoint;

import javax.sql.DataSource;

@Configuration
@ComponentScan("kz.inessoft.sono")
@EnableJpaRepositories({"kz.inessoft.sono.lib.mq.recvr.repositories"})
public class FXXXConfiguration extends WebSecurityConfigurerAdapter {
    private final SSOAuthTokenFilter ssoAuthTokenFilter;

    @Value("${datasource.username}")
    private String dbUserName;

    @Value("${datasource.password}")
    private String dbPassword;

    @Value("${datasource.url}")
    private String dbUrl;

    @Autowired
    public FXXXConfiguration(SSOAuthTokenFilter ssoAuthTokenFilter) {
        this.ssoAuthTokenFilter = ssoAuthTokenFilter;
    }

    @Bean
    public DataSource dataSource() {
        return DataSourceBuilder.create()
                .driverClassName("org.postgresql.Driver")
                .url(dbUrl)
                .username(dbUserName)
                .password(dbPassword)
                .build();
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(EntityManagerFactoryBuilder builder, DataSource dataSource) {
        return builder
                .dataSource(dataSource)
                .persistenceUnit("sonoUnit")
                .packages("kz.inessoft.sono.lib.mq.recvr.entities")
                .build();
    }

    @Bean
    public String formCode() {
        return FXXXConstants.FORM_CODE;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
                .addFilter(ssoAuthTokenFilter)
                .addFilterBefore(new ExceptionTranslationFilter(new Http403ForbiddenEntryPoint()), ssoAuthTokenFilter.getClass())
                .csrf().disable();
        http.authorizeRequests().anyRequest().permitAll().anyRequest().anonymous();
        http.requestMatcher(httpServletRequest -> {
            String requestURI = httpServletRequest.getRequestURI();
            return requestURI.startsWith("/fno/x_form_path/x_fno_version/newPrefilledForm") ||
                    requestURI.startsWith("/fno/x_form_path/x_fno_version/document/") ||
                    requestURI.startsWith("/fno/x_form_path/x_fno_version/saveDraft") ||
                    requestURI.startsWith("/fno/x_form_path/x_fno_version/draft") ||
                    requestURI.startsWith("/fno/x_form_path/x_fno_version/serializeToXmlForPrint") ||
                    requestURI.startsWith("/fno/x_form_path/x_fno_version/serializeToXmlForSign");
        }).authorizeRequests().anyRequest().authenticated();
    }
}
