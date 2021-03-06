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

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.telephony.PhoneNumberUtils;

import com.gft.unity.android.activity.AbstractActivityManagerListener;
import com.gft.unity.android.activity.AndroidActivityManager;
import com.gft.unity.android.activity.IActivityManager;
import com.gft.unity.core.system.log.Logger;
import com.gft.unity.core.system.log.Logger.LogCategory;
import com.gft.unity.core.telephony.AbstractTelephony;
import com.gft.unity.core.telephony.CallType;

// TODO implement other call types
public class AndroidTelephony extends AbstractTelephony {

	private static final String LOGGER_MODULE = "ITelephony";
	private static final Logger LOGGER = Logger.getInstance(
			LogCategory.PLATFORM, LOGGER_MODULE);

	private static final String PHONE_PREFIX = "tel://";
	private static final int CALL = 0;

	public AndroidTelephony() {
	}

	@Override
	public void Call(String number, CallType type) {
		LOGGER.logOperationBegin("Call", new String[] { "number", "type" },
				new Object[] { number, type });

		
		try {	

			AndroidActivityManager aam = (AndroidActivityManager) AndroidServiceLocator
					.GetInstance().GetService(AndroidServiceLocator.SERVICE_ANDROID_ACTIVITY_MANAGER);
			
			aam.requestPermision(Manifest.permission.CALL_PHONE, aam.CALL, new CallPermissionListener(number, type));
		} catch (Exception ex) {
			LOGGER.logError("Call", "Error", ex);
		} 
	}
	
	private void CallOnApproval(String number, CallType type){


		try {
			Context context = AndroidServiceLocator.getContext();

			String url = PhoneNumberUtils.formatNumber(number);
			Intent intent = new Intent(Intent.ACTION_CALL,
					Uri.parse(PHONE_PREFIX + url));
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(intent);
			
		} catch (Exception ex) {
			LOGGER.logError("Call", "Error", ex);
		} finally {
			LOGGER.logOperationEnd("Call", "Calling");
		}
		
		
	}
	
	private class CallPermissionListener extends AbstractActivityManagerListener {

		private String number;
		private CallType type;
		
		public CallPermissionListener(String number, CallType type) {
			this.type = type;
			this.number = number;
		}

		public CallPermissionListener() {
		}
		
		

		@Override
		public void onOk(int requestCode, Intent data) {

			LOGGER.logInfo("CallPermissionListener.onOk", ((data!=null)?data.getDataString(): ""));
			try {	
				CallOnApproval(number,type);
			} catch (Exception ex) {
				LOGGER.logError("CallPermissionListener.onOk", "Error", ex);
			} 

		}


		@Override
		public void onCancel(int requestCode, Intent data) {
			LOGGER.logInfo("CallPermissionListener.onCancel", ((data!=null)?data.getDataString(): ""));

			try {
				IActivityManager am = (IActivityManager) AndroidServiceLocator
						.GetInstance().GetService(
								AndroidServiceLocator.SERVICE_ANDROID_ACTIVITY_MANAGER);
				am.executeJS("Appverse.OnPhoneDenied", null);
				
			} catch (Exception ex) {
				LOGGER.logError("CallPermissionListener.onCancel", "Error", ex);
			}
		}
	}
}
