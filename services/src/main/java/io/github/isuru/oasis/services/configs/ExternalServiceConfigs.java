package io.github.isuru.oasis.services.configs;

import io.github.isuru.oasis.model.configs.EnvKeys;
import io.github.isuru.oasis.model.utils.OasisUtils;
import io.github.isuru.oasis.services.backend.FlinkServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class ExternalServiceConfigs {

    private static final Logger LOG = LoggerFactory.getLogger(ExternalServiceConfigs.class);

    @Autowired
    private OasisConfigurations oasisConfigurations;

    @Autowired
    private RabbitConfigurations rabbitConfigurations;

    @Bean
    @Scope(scopeName = ConfigurableBeanFactory.SCOPE_SINGLETON)
    public FlinkServices createFlinkService() {
        LOG.info("Initializing Flink services...");
        FlinkServices flinkServices = new FlinkServices();
        flinkServices.init(OasisUtils.getEnvOr(EnvKeys.OASIS_FLINK_URL,
                oasisConfigurations.getFlinkURL()));

        return flinkServices;
    }

}
