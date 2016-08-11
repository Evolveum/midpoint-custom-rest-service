# midpoint-custom-service-overlay
Example of a midPoint overlay project that implements a custom web service.

The web service is implemented in the midpoint-custom-service-server. It is
implemented by using JAX-WS contract-first development. WSDL file is part of
the project and the server code is generated from that.

The midpoint-custom-service-overlay is an overlay project that is using
the web service client and itegrates it with midPoint.

There is also testing web service client in midpoint-custom-service-client.

Building and running:

Just use the simple "mvn clean install" in the top-level project. It will
build the service, create the overlay and build the client. 
The final midpoint.war will be built in midpoint-custom-service-overlay/target/midpoint.war
Deploy that WAR to the Tomcat. The web service will be listening at
http://localhost:8080/midpoint/ws/example-1

The testing client can be excuted by running the following in the midpoint-custom-service-client
directory:

mvn exec:java -Dexec.mainClass="com.example.midpoint.service.client.Main"

The client will search for a user with e-mail address jack@caribbean.com.

See https://wiki.evolveum.com/display/midPoint/Customization+With+Overlay+Project
for more details about midPoint overlay projects.
