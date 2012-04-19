rxtx-rebundled 2.1-7r2 is a slightly modified version of the original 
rxtx 2.1-7r2. The only difference is: 

1) It's a working mavenized library
2) There's just one JAR required. Nothing else. The native libs are bundled 
   with the jar-file. 
3) You don't have to care about "java.library.path". This version will on 
   startup extract the correct native libs to system temp folder and loads them 
   automatically. When JVM terminates, temp files will be removed.