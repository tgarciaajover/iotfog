package com.advicetec.iot.rest;

import org.restlet.Application;
import org.restlet.Component;
import org.restlet.Restlet;
import org.restlet.Server;
import org.restlet.data.Protocol;
import org.restlet.routing.Router;

/**
 * A simple HTTP server that provides access to a "Language Syntax Checker" via a REST interface.
 * This class does two things: 
 *   (1) it sets up and runs a web application (via the main() method), and 
 *   (2) it defines how URLs sent to this web application get dispatched to ServerResources that handle them.
 * @author Philip Johnson
 */
public class IotRestServer extends Application {
  
  /**
   * Starts a server running on the specified port.
   * The context root will be "configurationserver".
   * We create a separate runServer method, rather than putting this code into the main() method,
   * so that we can run tests on a separate port.  
   * 
   * This illustrates one way to provide a "test" configuration that differs from the "production" configuration.
   * @param port The port on which this server should run.
   * @throws Exception if problems occur starting up this server. 
   */
  public static void runServer(int port) throws Exception {
    // Create a component.  
    Component component = new Component();
    Server s = new Server(Protocol.HTTP, port);
    component.getServers().add(s);
    s.getContext().getParameters().add("tracing", "true");
    // Create an application (this class).
    Application application = new IotRestServer();
    // Attach the application to the component with a defined contextRoot.
    String contextRoot = "/iotserver";
    component.getDefaultHost().attach(contextRoot, application);
    component.start();
  }

    
  /**
   * Specify the dispatching restlet that maps URIs to their associated resources for processing.
   * @return A Router restlet that implements dispatching.
   */
  @Override
  public Restlet createInboundRoot() {
      // Create a router restlet.
      Router router = new Router(getContext());
      // Attach the resources to the router.
      router.attach("/checker/transformation", LanguageTransformationResource.class);
      router.attach("/checker/behavior", LanguageBehaviorResource.class);
      router.attach("/SignalUnit/{uniqueID}", SignalUnitResource.class);
      router.attach("/SignalType/{uniqueID}", SignalTypeResource.class);
      router.attach("/Signal/{uniqueID}", SignalResource.class);
      router.attach("/DeviceType/{uniqueID}", DeviceTypeResource.class);
      router.attach("/MonitoringDevice/{uniqueID}", MonitoringDeviceResource.class);
      router.attach("/MeasuredEntity/{uniqueID}", MeasuredEntityResource.class);
      router.attach("/MeasuredEntity/{uniqueID}/Behavior/{BehaviorID}", MeasuredEntityBehaviorResource.class);
      router.attach("/MeasuredEntity/{uniqueID}/StateBehavior/{BehaviorID}", MeasuredEntityStateBehaviorResource.class);
      router.attach("/MeasuredEntity/{uniqueID}/StateTransition/{TransitionID}", MeasuredEntityStateTransitionResource.class);
      router.attach("/Status/{uniqueID}", StatusResource.class);
      router.attach("/AttributeValue/{uniqueID}", AttributeValueResource.class);
      router.attach("/StateInterval/{uniqueID}", IntervalResource.class);
      router.attach("/TranslationClasses", TranslationClassesResource.class);  
      router.attach("/Register/ActivityRegister", ActivityRegistrationResource.class);  
      // POST request for state array by time range 
      router.attach("/State",StateResource.class);
      // POST requent for trend variable
      router.attach("/Trend",TrendResource.class);
      // POST requent for downtime reasons
      router.attach("/DowntimeReason",DowntimeReasonResource.class);
      
      // Return the root router
      return router;
  }
}