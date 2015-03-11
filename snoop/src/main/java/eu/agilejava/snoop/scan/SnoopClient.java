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
package eu.agilejava.snoop.scan;

import com.fasterxml.jackson.dataformat.yaml.snakeyaml.Yaml;
import java.io.IOException;
import java.net.URI;
import java.util.Calendar;
import java.util.Map;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.ScheduleExpression;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;
import javax.websocket.ClientEndpoint;
import javax.websocket.ContainerProvider;
import javax.websocket.DeploymentException;
import javax.websocket.OnMessage;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;

/**
 *
 * @author Ivar Grimstad <ivar.grimstad@gmail.com>
 */
@ClientEndpoint
@Singleton
@Startup
public class SnoopClient {

   private static final Logger LOGGER = Logger.getLogger("eu.agilejava.snoop");
   private static final String BASE_URI = "ws://localhost:8080/snoop-service/";

   @Resource
   private TimerService timerService;

   public void register(final String clientId) {
      sendMessage("snoop", clientId);

      ScheduleExpression schedule = new ScheduleExpression();
      schedule.second("*/10").minute("*").hour("*").start(Calendar.getInstance().getTime());

      TimerConfig config = new TimerConfig();
      config.setPersistent(false);

      Timer timer = timerService.createCalendarTimer(schedule, config);

      LOGGER.config(() -> timer.getSchedule().toString());
   }

   @Timeout
   public void resend(Timer timer) {
      LOGGER.info(() -> "health update: " + Calendar.getInstance().getTime());
      LOGGER.info(() -> "Next: " + timer.getNextTimeout());
      sendMessage("snoopstatus/snoopy", "UP");
   }

   /**
    * Sends message to the WebSocket server.
    *
    * @param endpoint The server endpoint
    * @param msg The message
    * @return a return message
    */
   private String sendMessage(String endpoint, String msg) {

      LOGGER.info("Sending message!");

      String returnValue = "-1";
      try {
         WebSocketContainer container = ContainerProvider.getWebSocketContainer();
         String uri = BASE_URI + endpoint;
         Session session = container.connectToServer(this, URI.create(uri));
         session.getBasicRemote().sendText(msg != null ? msg : "");
         returnValue = session.getId();
         System.out.println(returnValue);

      } catch (DeploymentException | IOException ex) {
         LOGGER.warning(ex.getMessage());
      }

      return returnValue;
   }

   /**
    * Handles incoming message.
    *
    * @param session The WebSocket session
    * @param message The message
    */
   @OnMessage
   public void onMessage(Session session, String message) {
      LOGGER.info(() -> "Message: " + message);
      sendMessage("snoopstatus/snoopy", "UP");
   }

   @PostConstruct
   private void init() {

      LOGGER.config("Checking if snoop is enabled");

      if (SnoopExtensionHelper.isSnoopEnabled()) {

         Yaml yaml = new Yaml();
         Map<String, Object> props = (Map<String, Object>) yaml.load(this.getClass().getResourceAsStream("/application.yml"));

         Map<String, String> snoopConfig = (Map<String, String>) props.get("snoop");

         String applicationName = snoopConfig.get("applicationName");
         System.out.println("application name: " + applicationName);

         if (applicationName != null) {
            LOGGER.config(() -> "Registering " + applicationName);
            register(applicationName);
         } else {
            LOGGER.config(() -> "Registering with name from annotation: " + applicationName);
            register(SnoopExtensionHelper.getApplicationName());
         }

      } else {
         LOGGER.config("Snoop is not enabled. Use @EnableSnoopClient!");
      }
   }

}
