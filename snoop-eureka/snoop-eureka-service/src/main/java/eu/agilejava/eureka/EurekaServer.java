package eu.agilejava.eureka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 *
 * @author Ivar Grimstad <ivar.grimstad@gmail.com>
 */
@SpringBootApplication
@EnableEurekaServer
public class EurekaServer {

   public static void main(String[] args) {
      SpringApplication.run(EurekaServer.class, args);
   }

}
