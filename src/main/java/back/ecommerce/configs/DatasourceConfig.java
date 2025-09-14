package back.ecommerce.configs;

import javax.sql.DataSource;

import org.springframework.jdbc.datasource.DriverManagerDataSource;


//@Configuration
public class DatasourceConfig {

    //@Bean
    public DataSource dataource() {
        final var datasource = new DriverManagerDataSource();
        datasource.setDriverClassName("org.h2.Driver");
        datasource.setUrl("jdbc:h2:mem:testdb");
        datasource.setUsername("sa");
        datasource.setPassword("password");

        return datasource;

    }
}
