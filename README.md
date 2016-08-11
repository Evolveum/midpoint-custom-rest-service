# midpoint-custom-rest-overlay
Example of a midPoint overlay project that implements a custom REST service.

The service is implemented by ExampleRestService class. Two necessary Spring context
files are in "resources" directory.

This project is an adaptation (with some simplifications) of midpoint-custom-service.
It requires midPoint 3.4.1 or later (at least v3.5devel-153-g22e81e5).

Building and running:

Run:

   mvn package

The final midpoint.war will be built in target/midpoint.war. Deploy that WAR to the Tomcat.
The service will be listening at http://localhost:8080/midpoint/ws/rest-example.

Service can be invoked e.g. by pointing a browser to the following URL:

http://localhost:8080/midpoint/ws/rest-example/users/mail/jack@caribbean.com

It will return user(s) with e-mail address jack@caribbean.com.

See https://wiki.evolveum.com/display/midPoint/Customization+With+Overlay+Project
for more details about midPoint overlay projects.
