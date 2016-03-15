#!/bin/sh


$JAVA7/bin/java \
 	-classpath ./jms-test/target/jms-test-1.0-SNAPSHOT.jar:$JEUS_HOME/lib/client/jclient.jar:$JEUS_HOME/lib/system/jms.jar \
	-Dexamples.test.duration=300000			\
	-Dexamples.test.warm-up-time=30000		\
	-Dexamples.test.message-size=2048 		\
	-Dexamples.test.send-session-count=2		\
	-Dexamples.test.receive-session-count=2	\
	-Dexamples.test.produce-interval=5 		\
	-Dexamples.test.ring-buffer-size=1024		\
	examples.test.TestExecutor

