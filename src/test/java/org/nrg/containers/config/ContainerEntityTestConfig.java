package org.nrg.containers.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hibernate.SessionFactory;
import org.mockito.Mockito;
import org.nrg.containers.api.ContainerControlApi;
import org.nrg.containers.daos.ContainerEntityRepository;
import org.nrg.containers.model.ContainerEntity;
import org.nrg.containers.model.ContainerEntityHistory;
import org.nrg.containers.model.ContainerEntityInput;
import org.nrg.containers.model.ContainerEntityMount;
import org.nrg.containers.model.ContainerEntityOutput;
import org.nrg.containers.model.ContainerMountFiles;
import org.nrg.containers.services.ContainerEntityService;
import org.nrg.containers.services.impl.HibernateContainerEntityService;
import org.nrg.framework.services.NrgEventService;
import org.nrg.prefs.services.NrgPreferenceService;
import org.nrg.transporter.TransportService;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xdat.security.services.PermissionsServiceI;
import org.nrg.xnat.services.archive.CatalogService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.orm.hibernate4.HibernateTransactionManager;
import org.springframework.orm.hibernate4.LocalSessionFactoryBean;
import org.springframework.transaction.support.ResourceTransactionManager;
import reactor.bus.EventBus;

import javax.sql.DataSource;
import java.util.Properties;

@Configuration
//@EnableTransactionManagement
@Import(HibernateConfig.class)
public class ContainerEntityTestConfig {
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public EventBus eventBus() {
        return Mockito.mock(EventBus.class);
    }

    @Bean
    public ContainerControlApi containerControlApi() {
        return Mockito.mock(ContainerControlApi.class);
    }

    @Bean
    public SiteConfigPreferences siteConfigPreferences() {
        return Mockito.mock(SiteConfigPreferences.class);
    }

    @Bean
    public NrgEventService nrgEventService() {
        return Mockito.mock(NrgEventService.class);
    }

    @Bean
    public NrgPreferenceService nrgPreferenceService() {
        return Mockito.mock(NrgPreferenceService.class);
    }

    @Bean
    public TransportService transportService() {
        return Mockito.mock(TransportService.class);
    }

    @Bean
    public PermissionsServiceI permissionsService() {
        return Mockito.mock(PermissionsServiceI.class);
    }

    @Bean
    public CatalogService catalogService() {
        return Mockito.mock(CatalogService.class);
    }

    @Bean
    public ContainerEntityService containerEntityService() {
        return new HibernateContainerEntityService();
    }

    @Bean
    public ContainerEntityRepository containerEntityRepository() {
        return new ContainerEntityRepository();
    }

    @Bean
    public LocalSessionFactoryBean sessionFactory(final DataSource dataSource, @Qualifier("hibernateProperties") final Properties properties) {
        final LocalSessionFactoryBean bean = new LocalSessionFactoryBean();
        bean.setDataSource(dataSource);
        bean.setHibernateProperties(properties);
        bean.setAnnotatedClasses(
                ContainerEntity.class,
                ContainerEntityHistory.class,
                ContainerEntityInput.class,
                ContainerEntityOutput.class,
                ContainerEntityMount.class,
                ContainerMountFiles.class);
        return bean;
    }

    @Bean
    public ResourceTransactionManager transactionManager(final SessionFactory sessionFactory) throws Exception {
        return new HibernateTransactionManager(sessionFactory);
    }
}
