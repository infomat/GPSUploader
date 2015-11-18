/*
 * Copyright (c) 2015-present, Parse, LLC.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */
package com.conestogac.msd.gpsuploader;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseUser;

//import com.parse.ParseCrashReporting;


public class ParseApplication extends Application {

  public static final String YOUR_APPLICATION_ID = "5M2u8zCYhBxJJIQOjQvEM5L6cgly6oD6KWaG4R1L";
  public static final String YOUR_CLIENT_KEY = "VWtOK9JNJDl9QJFQOugeTvnfpgIaRqFDRZV8rYSe";

  @Override
  public void onCreate() {
    super.onCreate();

    //   ParseCrashReporting.enable(this);
    Parse.enableLocalDatastore(this);

    // Add your initialization code here
    Parse.initialize(this, YOUR_APPLICATION_ID, YOUR_CLIENT_KEY);
    ParseUser.enableAutomaticUser();
    ParseACL defaultACL = new ParseACL();
    //to enable read by desktop application with anoymous user_id
    defaultACL.setPublicReadAccess(true);
    ParseACL.setDefaultACL(defaultACL, true);
  }
}