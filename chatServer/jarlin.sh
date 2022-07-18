#!/bin/sh
gcc -fPIC -shared -o libsocket.so Socket.c

javac -cp jna-5.12.1.jar:jackson-annotations-2.13.3.jar:jackson-core-2.13.3.jar:jackson-databind-2.13.3.jar chatServer.java PlatformUtils.java

jar cvfm chatServer.jar META-INF/MANIFEST.MF *.class libsocket.so libsocket.dylib socket.dll jna-5.12.1.jar mysql-connector-java-8.0.29.jar jackson-*.jar
