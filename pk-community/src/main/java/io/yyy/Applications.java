
package io.yyy;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.core.env.Environment;

@Slf4j
@EnableTransactionManagement
@EnableScheduling
@SpringBootApplication
public class Applications {

    public static void main(String[] args) {
        ConfigurableApplicationContext application = SpringApplication.run(Applications.class, args);
        Environment env = application.getEnvironment();
        log.info("启动成功:{}", env.getProperty("server.port"));
    }
}
