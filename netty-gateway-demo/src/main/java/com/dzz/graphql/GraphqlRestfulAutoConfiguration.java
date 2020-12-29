package com.dzz.graphql;

import com.oembedler.moon.graphql.boot.GraphQLWebAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author zoufeng
 * @date 2020-3-19
 */
@Configuration
@ConditionalOnWebApplication
@ConditionalOnProperty(value = "optim.platform.graphql.enabled", havingValue = "true")
@AutoConfigureAfter({GraphQLWebAutoConfiguration.class})
public class GraphqlRestfulAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public GraphqlControllerAspect graphqlControllerAspect() {
        return new GraphqlControllerAspect();
    }

}
