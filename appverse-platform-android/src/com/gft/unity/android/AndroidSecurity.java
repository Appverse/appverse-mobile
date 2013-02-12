/*
 Copyright (c) 2012 GFT Appverse, S.L., Sociedad Unipersonal.

 This Source  Code Form  is subject to the  terms of  the Appverse Public License 
 Version 2.0  ("APL v2.0").  If a copy of  the APL  was not  distributed with this 
 file, You can obtain one at http://appverse.org/legal/appverse-license/.

 Redistribution and use in  source and binary forms, with or without modification, 
 are permitted provided that the  conditions  of the  AppVerse Public License v2.0 
 are met.

 THIS SOFTWARE IS PROVIDED BY THE  COPYRIGHT HOLDERS  AND CONTRIBUTORS "AS IS" AND
 ANY EXPRESS  OR IMPLIED WARRANTIES, INCLUDING, BUT  NOT LIMITED TO,   THE IMPLIED
 WARRANTIES   OF  MERCHANTABILITY   AND   FITNESS   FOR A PARTICULAR  PURPOSE  ARE
 DISCLAIMED. EXCEPT IN CASE OF WILLFUL MISCONDUCT OR GROSS NEGLIGENCE, IN NO EVENT
 SHALL THE  COPYRIGHT OWNER  OR  CONTRIBUTORS  BE LIABLE FOR ANY DIRECT, INDIRECT,
 INCIDENTAL,  SPECIAL,   EXEMPLARY,  OR CONSEQUENTIAL DAMAGES  (INCLUDING, BUT NOT
 LIMITED TO,  PROCUREMENT OF SUBSTITUTE  GOODS OR SERVICES;  LOSS OF USE, DATA, OR
 PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT(INCLUDING NEGLIGENCE OR OTHERWISE) 
 ARISING  IN  ANY WAY OUT  OF THE USE  OF THIS  SOFTWARE,  EVEN  IF ADVISED OF THE 
 POSSIBILITY OF SUCH DAMAGE.
 */
package com.gft.unity.android;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;

import android.os.Build;

import com.gft.unity.core.security.AbstractSecurity;

public class AndroidSecurity extends AbstractSecurity {

	@Override
	public boolean IsDeviceModified() {
		if (checkRootMethod1()){return true;}
		if (checkRootMethod2()){return true;}

		// Tactical solution applied (16/01/2013) ::
		// ICS devices (levels 14 & 15) hang the application when the checkRootMethod3 is used. 
		// Application processes are blocked in that case and the address port could not be used anymore by any app till the device is fully restarted.
		if(Build.VERSION.SDK_INT < 14 || Build.VERSION.SDK_INT > 15) {
        	if (checkRootMethod3()){return true;}
		}
		
        return false;
	}
	
	 private boolean checkRootMethod1(){
	        String buildTags = android.os.Build.TAGS;

	        if (buildTags != null && buildTags.contains("test-keys")) {
	            return true;
	        }
	        return false;
	    }

	    
	 private boolean checkRootMethod2(){
		 try {
			 File file = new File("/system/app/Superuser.apk");
	         if (file.exists()) {
	        	 return true;
	         }
	     } catch (Exception e) { }
	     return false;
	 }

	 private boolean checkRootMethod3() {
		 if (new ExecShell().executeCommand(ExecShell.SHELL_CMD.check_su_binary) != null){
			 return true;
	     }else{
	    	 if (new ExecShell().executeCommand(ExecShell.SHELL_CMD.check_su_permission) != null){
	    		 return true;
	    	 }else return false;
	     }
	 }
	 
	 private static class ExecShell
	 {
		 public static enum SHELL_CMD {
			 check_su_binary(new String[] {"/system/xbin/which","su"}),
			 check_su_permission(new String[] {"su","-v"});
			 
			 String[] command;
		     SHELL_CMD(String[] command){
		    	 this.command = command;
		     }
		     
		     
		 }

		 public ArrayList<String> executeCommand(SHELL_CMD shellCmd){
			 String line = null;
			 ArrayList<String> fullResponse = new ArrayList<String>();
			 Process localProcess = null;
			 
			 try {
				 localProcess = Runtime.getRuntime().exec(shellCmd.command);
			 } catch (Exception e) {
				 return null;
				 //e.printStackTrace();
			 }

			 BufferedReader in = new BufferedReader(new InputStreamReader(localProcess.getInputStream()));
			 BufferedReader er = new BufferedReader(new InputStreamReader(localProcess.getErrorStream()));

			 try {
				 while ((line = in.readLine()) != null) {
					 fullResponse.add(line);
				 }
				 if(fullResponse.size()<=0){
					 while ((line = er.readLine()) != null) {
						 fullResponse.add(line);
					 }
				 }
				 in.close();
				 er.close();
			 } catch (Exception e) {
				 //e.printStackTrace();
			 }
			 return fullResponse;		 
		 }
	 }
}