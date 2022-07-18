#!/bin/sh

gcc -dynamiclib -o libsocket.dylib Socket.c -shared
javac -cp jna-5.12.1.jar:jackson-annotations-2.13.3.jar:jackson-core-2.13.3.jar:jackson-databind-2.13.3.jar chatServer.java PlatformUtils.java
# java -cp .:jna-5.12.1.jar JNASocket
jar cvfm chatServer.jar META-INF/MANIFEST.MF *.class libsocket.dylib jna-5.12.1.jar mysql-connector-java-8.0.29.jar jackson-*.jar
