all:
        Simple_Java_Udp.jar

SimpleDNSSimulation.jar: Simple_Java_Udp.java
        javac Simple_Java_Udp.java
        jar cvfe SimpleDNSSimulation.jar Simple_Java_Udp *.class


clean:
        rm -f *.class
        rm -f SimpleDNSSimulation.jar