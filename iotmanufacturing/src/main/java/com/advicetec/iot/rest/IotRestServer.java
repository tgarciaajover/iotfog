package com.advicetec.iot.rest;

import org.restlet.Application;
import org.restlet.Component;
import org.restlet.Restlet;
import org.restlet.Server;
import org.restlet.data.Protocol;
import org.restlet.routing.Router;

/**
 * A simple HTTP server that provides access to a configuration objects and language syntax checker via REST.
 * 
 * This class does two things: 
 *   (1) it sets up and runs a web application (via the main() method), and 
 *   (2) it defines how URLs sent to this web application get dispatched to ServerResources that handle them.
 *   
 * @author Andres Marentes
 */
public class IotRestServer extends Application {
  
  /**
   * Starts a server running on the specified port.
   * 
   * The context root will be "iotserver".
   * We create a separate runServer method, rather than putting this code into the main() method,
   * so that we can run tests on a separate port.  
   * 
   * This illustrates one way to provide a "test" configuration that differs from the "production" configuration.
   * 
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
    
    application.setDescription("REST application");
    application.setName("IoT server application");
    
    component.getDefaultHost().attach(contextRoot, application);
    component.start();
  }

    
  /**
   * Specify the dispatching restlet that maps URIs to their associated resources for processing.
   * 
   * @return A Router restlet that implements dispatching.
   */
  @Override
  public Restlet createInboundRoot() {
      // Create a router restlet.
      Router router = new Router(getContext());
      // Attach the resources to the router.
      
      // Resource for the transformation language checker 
      router.attach("/checker/transformation", LanguageTransformationResource.class);
      
      // Resource for the behavior language checker
      router.attach("/checker/behavior", LanguageBehaviorResource.class);
      
      // Resource for Implementing Signal unit CRUD 
      router.attach("/SignalUnit/{uniqueID}", SignalUnitResource.class);
      
      // Resource for Implementing Signal type CRUD
      router.attach("/SignalType/{uniqueID}", SignalTypeResource.class);
      
      // Resource for Implementing Signal CRUD
      router.attach("/Signal/{uniqueID}", SignalResource.class);
      
      // Resource for Implementing Device type CRUD
      router.attach("/DeviceType/{uniqueID}", DeviceTypeResource.class);
      
      // Resource for Implementing Reason code CRUD
      router.attach("/ReasonCode/{uniqueID}", ReasonCodeResource.class);
      
      // Resource for Implementing Monitoring Device CRUD
      router.attach("/MonitoringDevice/{uniqueID}", MonitoringDeviceResource.class);
      
      // Resource for Implementing Measuring Entity CRUD, it assumes components are previously deleted
      router.attach("/MeasuredEntity/{uniqueID}", MeasuredEntityResource.class);
      
      // Resource for Implementing Measuring Entity Behavior CRUD
      router.attach("/MeasuredEntity/{uniqueID}/Behavior/{BehaviorID}", MeasuredEntityBehaviorResource.class);
      
      // Resource for Implementing Measuring Entity State Behavior CRUD
      router.attach("/MeasuredEntity/{uniqueID}/StateBehavior/{BehaviorID}", MeasuredEntityStateBehaviorResource.class);
      
      // Resource for Implementing Measuring Entity State Transition CRUD
      router.attach("/MeasuredEntity/{uniqueID}/StateTransition/{TransitionID}", MeasuredEntityStateTransitionResource.class);
      
      // Resource for Implementing Measuring Entity Scheduled Events CRUD
      router.attach("/MeasuredEntity/{uniqueID}/ScheduledEvent/{EventID}", MeasuredEntityScheduledEventResource.class);
      
      // Resource for verify which transformations use a behavior
      router.attach("/MeasuredEntity/{uniqueID}/TransformationsUsingBehavior/{BehaviorID}", MeasuredEntityBehaviorCheckResource.class);
      
      // Resource for Implementing the classes available to translate signals into interpreted signals. 
      router.attach("/TranslationClasses", TranslationClassesResource.class);  
      
      // Interfaces for the web application
      
      // Resource for Implementing the status interface for a measured entity (list the current values of the attribute registered )
      router.attach("/Status", StatusResource.class);
      
      // Resource for Implementing the evolution of an attribute registered in a measured entity
      router.attach("/AttributeValue/{uniqueID}", AttributeValueResource.class);
      
      // Resource for Implementing the state intervals interface for a measured entity
      router.attach("/StateInterval/{uniqueID}", IntervalResource.class);
      
      // Resource for Implementing the activity register interface for a measured entity
      router.attach("/Register/ActivityRegister", ActivityRegistrationResource.class);  
      
      // POST request to get a state array by time range 
      router.attach("/State",StateResource.class);
      
      // POST request to get the evolution of a trend variable
      router.attach("/Trend",TrendResource.class);
      
      // POST request for downtime reasons
      router.attach("/DownTimeReason",DowntimeReasonResource.class);
      
      // POST request to get the OEE for a measured entity
      router.attach("/OverallEquipmentEffectiveness", OverallEquipmentEffectivenessResource.class);
      
      // POST request to get the OEE for a production order
      router.attach("/OEEProductionOrder", OverallEquipmentEffectivenessExecutedEntityResource.class); 
      
      // POST request to get the definition of the measured entity attribute. 
      router.attach("/MeasuredEntityAtttributes", MeasuredEntityAttributesResource.class);
      
      // Return the root router
      return router;
  }
}