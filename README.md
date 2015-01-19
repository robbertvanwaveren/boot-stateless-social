boot-stateless-social
===================
Example project integrating https://github.com/Robbert1/boot-stateless-auth with OAuth 2 based social login with facebook.

Facebook expects the app to run under http://socialshowcase.com(:8080).  
Therefore you need to add the following line to your hosts file for testing it locally:  
127.0.0.1  socialshowcase.com

The build files and application.properties include commented out configuration for postgresql, mostly for testing behavior across server reboots.

Needs Gradle 2 or maven 3 and JDK 7

build with `gradle build`  
run with `gradle run`

or go with `mvn spring-boot:run`


