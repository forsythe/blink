## Blink
### Description
Simple program that listens to the Stereo Mix output and sends it over UDP to a listening client. Works on your local wifi network only.

### Why?
If your desktop doesn't support bluetooth, but you have laptop which does, you can use Blink to turn your laptop into an "audio relay" and connect your bluetooth headphones to your laptop instead.

### Pre-requisites
* Windows
* Enable "Stereo Mix" in Sound > Recording Devices

### Instructions
1. Start the server with `gradlew runServer`. Server will now listen and accept one client connection.
2. Observe the printed server hostname, and start your client with `gradlew runClient --args=<hostname>`

Once a server is attached to a client, it will forever send to that client until the server is restarted.


