/*
 * The MIT License
 *
 * Copyright 2015 Ivar Grimstad <ivar.grimstad@gmail.com>.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package eu.agilejava.snoop.eureka.scan;

import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 *
 * @author Ivar Grimstad <ivar.grimstad@gmail.com>
 */
@Singleton
@Startup
public class EurekaClient {

   private static final Logger LOGGER = Logger.getLogger("eu.agilejava.snoop");

   @PostConstruct
   private void init() {

      LOGGER.config("Checking if snoop eureka is enabled");
      LOGGER.config(() -> "YES: " + SnoopEurekaExtensionHelper.isEurekaEnabled());

      Client eurekaClient = ClientBuilder.newClient();

      EurekaConfig eurekaConfig = new EurekaConfig();
      eurekaConfig.setHostName("localhost");
      eurekaConfig.setApp("SNOOP");
      eurekaConfig.setIpAddr("192.168.1.71");
      eurekaConfig.setPort(8080);
      eurekaConfig.setVipAddress("");
      eurekaConfig.setSecureVipAddress("");
      eurekaConfig.setStatus("UP");
      eurekaConfig.setSecurePort(443);
      eurekaConfig.setHomePageUrl("http://www.vg.no");
      eurekaConfig.setStatusPageUrl("http://www.vg.no");
      eurekaConfig.setHealthCheckUrl("http://www.vg.no");
      eurekaConfig.setDataCenterInfo(new DataCenterInfo());

      Entity<InstanceConfig> entity = Entity.entity(new InstanceConfig(eurekaConfig), MediaType.APPLICATION_JSON);
      LOGGER.config(() -> "Entity: " + entity.toString());
      Response response = eurekaClient.target("http://localhost:8761/eureka/apps/" + SnoopEurekaExtensionHelper.getApplicationName())
              .request()
              .post(entity);

      LOGGER.config(() -> "POST resulted in: " + response.getStatus() + ", " + response.getEntity());
   }
}
