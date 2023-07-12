<img align="right" width="250" height="47" src="../Gematik_Logo_Flag_With_Background.png"/> <br/>

# epa-ps-sim-app

A Spring Boot app to offer the epa-ps-sim functionality as standalone application

## Getting started

* It can simply be started like `java -jar epa-ps-sim-app-<version>.jar`
* It is configured via the [application.yaml](src/main/resources/application.yaml)
  * To overwrite configuration on start please consult the [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/1.0.0.RC5/reference/html/boot-features-external-config.html).
* **But**: The app won't start out of the box, because it does not include a TLS client keystore.
  There's two ways to deal with this:
  1. Configure a TLS client keystore
     * To do so, edit the `application.yaml` in the section `konnektor.connection.tlsconfig`.
  2. Connect via HTTP to the Konnektor
     * To do so, in the `application.yaml` change the value of `konnektor.connection.address.protocol` to "http".
     * Be aware, this can only work, if the Konnektor, you want to connect to, permits the use of unsecured connections.

## Usage

* The Konnektor to connect to, can not be changed on runtime.
  So anytime another Konnektor shall be used, the app has to be stopped,
  the connection changed in the application.yaml and the app restarted.
  * We plan to change this in a future release