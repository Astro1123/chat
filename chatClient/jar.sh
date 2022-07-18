#!/bin/sh

gcc -dynamiclib -o libsocket.dylib Socket.c -shared
javac -cp jna-5.12.1.jar:jackson-annotations-2.13.3.jar:jackson-core-2.13.3.jar:jackson-databind-2.13.3.jar chatClient.java
# java -cp .:jna-5.12.1.jar JNASocket
jar cvfm chatClient.jar META-INF/MANIFEST.MF *.class libsocket.dylib socket.dll jna-5.12.1.jar mysql-connector-java-8.0.29.jar jackson-*.jar
java -jar chatClient.jar
