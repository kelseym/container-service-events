package org.nrg.containers.config;

import org.mockito.Mockito;
import org.nrg.containers.services.ContainerService;
import org.nrg.prefs.services.NrgPreferenceService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@Configuration
@EnableWebMvc
@ComponentScan("org.nrg.containers.rest, org.nrg.containers.model")
@Import(ImageMetadataObjectMapper.class)
public class RestApiTestConfig {
    @Bean
    public ContainerService mockContainerService() {
        return Mockito.mock(ContainerService.class);
    }

    @Bean
    public NrgPreferenceService mockPrefsService() {
        return Mockito.mock(NrgPreferenceService.class);
    }
}
