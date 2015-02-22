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

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javax.annotation.Resource;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerService;
import javax.enterprise.context.Dependent;
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
@Dependent
public class SnoopClient {

   private static final String BASE_URI = "ws://localhost:8080/snoop-service/";
   private Map<String, String> messages = new HashMap<>();
   private CountDownLatch messageLatch;

   /**
    * Sends message to the WebSocket server.
    *
    * @param endpoint The server endpoint
    * @param msg The message
    * @return a return message
    */
   public String sendMessage(String endpoint, String msg) {

      System.out.println("Sending message!");

      String returnValue = "-1";
      try {
         messageLatch = new CountDownLatch(1);
         WebSocketContainer container = ContainerProvider.getWebSocketContainer();
         String uri = BASE_URI + endpoint;
         Session session = container.connectToServer(this, URI.create(uri));
         session.getBasicRemote().sendText(msg != null ? msg : "");
         returnValue = session.getId();
         System.out.println(returnValue);
         messageLatch.await(100, TimeUnit.SECONDS);

      } catch (DeploymentException | IOException | InterruptedException ex) {
         ex.printStackTrace();
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
      System.out.println(message);
      messages.put(session.getId(), message);
      messageLatch.countDown();

      sendMessage("snoopstatus/snoopy", "UP");
   }

   /**
    * Gets the message for this session.
    *
    * @param sessionId The session id
    * @return The message
    */
   public String getMessage(String sessionId) {
      return messages.get(sessionId);
   }
}
