javac  -Xlint:deprecation -Xlint:unchecked daj\*.java
jar -c -f daj.jar -m meta-inf\MANIFEST.MF daj\*.class daj\algorithms\*.class daj\algorithms\visual\*.class
pause
