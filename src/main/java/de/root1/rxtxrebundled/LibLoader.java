/*--------------------------------------------------------------------------
|   rxtx-rebundled LibLoader is a helper class for rxtx library.
|   Copyright 2012 by Alexander Christian alex@root1.de
|
|   This library is free software; you can redistribute it and/or
|   modify it under the terms of the GNU Library General Public
|   License as published by the Free Software Foundation; either
|   version 2 of the License, or (at your option) any later version.
|
|   This library is distributed in the hope that it will be useful,
|   but WITHOUT ANY WARRANTY; without even the implied warranty of
|   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
|   Library General Public License for more details.
|
|   You should have received a copy of the GNU Library General Public
|   License along with this library; if not, write to the Free
|   Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
--------------------------------------------------------------------------*/
package de.root1.rxtxrebundled;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Helper Class used by rxtx implementation to load native libs without having 
 * java.library.path property specified. 
 * Native libs will be extracted and loaded automatically, if possible
 * 
 * @author achr
 */
public class LibLoader {
    
    private static final String LIBLOADER_VERSION_NAME = "rxtx-rebundled-2.1-7r2";
    
    /**
     * <code>Map&lt;Name of lib i.e. 'rxtxSerial', path to extracted lib&gt;</code><br>
     */
    private static final Map<String, String> extractedLibs = new HashMap<String, String>();
    
    static {
        
        String osName = System.getProperty("os.name");
        String osArch = System.getProperty("os.arch");
        
        logStdOut("os.name='"+osName+"'");
        logStdOut("os.arch='"+osArch+"'");
        
        // check for linux platform ..
        if (osName.toLowerCase().contains("linux")) {
            
            // check for architecture 64bit
            if (osArch.toLowerCase().contains("amd64") || osArch.toLowerCase().contains("x86_64")) {
                extractedLibs.put("rxtxSerial", extractLib("/jni/Linux/x86_64-unknown-linux-gnu/", "librxtxSerial.so"));
            } 
            // else 32bit
            else {
                extractedLibs.put("rxtxSerial", extractLib("/jni/Linux/i686-unknown-linux-gnu/", "librxtxSerial.so"));
                extractedLibs.put("rxtxParallel", extractLib("/jni/Linux/i686-unknown-linux-gnu/", "librxtxParallel.so"));
            }
            
        } 
        // check for windows platform
        else if (osName.toLowerCase().contains("windows")) {
            
            // check for architecture 64bit
            if (osArch.toLowerCase().contains("amd64") || osArch.toLowerCase().contains("x86_64")) {
                // currently no 64bit lib available ...
            } 
            // else 32bit
            else {
                extractedLibs.put("rxtxSerial", extractLib("/jni/Windows/i368-mingw32/", "rxtxSerial.dll"));
                extractedLibs.put("rxtxParallel", extractLib("/jni/Windows/i368-mingw32/", "rxtxParallel.dll"));
            }
            
        } 
        // check for os x platform
        else if (osName.toLowerCase().contains("os x")) {
            
            // no arch available or required?!
            extractedLibs.put("rxtxSerial", extractLib("/jni/Mac_OS_X/", "librxtxSerial.jnilib"));
            
        }
        // check for solaris platform
        else if (osName.toLowerCase().contains("solaris")) {
            
            // check for architecture 64bit
            if (osArch.toLowerCase().contains("amd64") || osArch.toLowerCase().contains("x86_64")) {
                extractedLibs.put("rxtxSerial", extractLib("/jni/Solaris/sparc-solaris/sparc64-sun-solaris2.8/", "librxtxSerial.so"));
            } 
            // else 32bit
            else {
                extractedLibs.put("rxtxSerial", extractLib("/jni/Solaris/sparc-solaris/sparc32-sun-solaris2.8/", "librxtxSerial.so"));
            }
            
        }
        // other platforms are currently not supported ...
        else {
            logStdOut("Sorry, platform '"+osName+"' currently not supported by LibLoader. Please use -Djava.library.path=<insert path to native libs here> as JVM parameter...");
        }
        
        logStdOut("Map: "+extractedLibs);
        
    }
    
    /**
     * Extracts the specified library from resource dir to a temp folder 
     * and returns the path to the extracted file
     *
     * @param folder resource folder of the lib to extract
     * @param libFileName name of the lib file in resource folder
     * @return complete path to the extracted lib
     */
    private static String extractLib(String folder, String libFileName) {
	int written = 0;
        
        String libSource = folder + libFileName;
        
	try {
	    InputStream resourceAsStream = LibLoader.class.getResourceAsStream(libSource);

	    if (resourceAsStream != null) {

		File tempFolder = new File(System.getProperty("java.io.tmpdir"));

		File rxtxTmp = new File(tempFolder, LIBLOADER_VERSION_NAME);
		rxtxTmp.mkdirs();
		rxtxTmp.deleteOnExit();

                File libFile = File.createTempFile("tmp_", "_"+libFileName, rxtxTmp);
                libFile.deleteOnExit();

		FileOutputStream fos = new FileOutputStream(libFile);
		int read = 0;
		byte[] data = new byte[512];
		while ((read = resourceAsStream.read(data)) != -1) {
		    fos.write(data, 0, read);
		    written += read;
		}
		fos.close();
                logStdOut("Extracting " + libSource + " to "+libFile.getAbsolutePath()+" *done*. written bytes: " + written);
                return libFile.getAbsolutePath();
	    } else {
		logStdOut("Could not find " + libSource + " in resources ...");
	    }
	} catch (Exception ex) {
	    logStdErr("Error extracting " + libSource  + " to temp... written bytes: " + written);
            logExceptionToStdErr(ex);
	}
        return null;
    }
    
    private static void logStdOut(String msg) {
        String property = System.getProperty("rxtx.rebundled.debug", "false");
        boolean log = Boolean.parseBoolean(property);
        if (log) {
            System.out.println(msg);
        }
    }
    
    private static void logStdErr(String msg) {
        String property = System.getProperty("rxtx.rebundled.suppress_error", "false");
        boolean suppress = Boolean.parseBoolean(property);
        if (!suppress) {
            System.err.println(msg);
        }
    }
    
    private static void logExceptionToStdErr(Exception ex) {
        String property = System.getProperty("rxtx.rebundled.suppress_error", "false");
        boolean suppress = Boolean.parseBoolean(property);
        if (!suppress) {
            ex.printStackTrace();
        }
    }
    
    public static void loadLibrary(String name) {
        
        logStdOut("Trying to load '"+name+"' ...");
        
        String lib = extractedLibs.get(name);
        
        // check if it's loadable via LibLoader mechanism
        if (lib!=null) {
            File f = new File(lib);
            logStdOut("...Loading via LibLoader mechanism: "+f.getAbsolutePath());
            System.load(f.getAbsolutePath());
        } 
        // otherwise load it the old way ...
        else {
            logStdOut("...Loading via System.loadLibrary() call");
            System.loadLibrary(name);
        }
        logStdOut("...*done*");
    }
    
}
