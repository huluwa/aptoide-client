/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package cm.aptoidetv.pt;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

public class MainActivity extends Activity implements RequestsTvListener {
    private static final int MINTIME_FOR_SPLASHSCREEN =3000;
    public static final String ARGS_SKIP ="SKIP";
    SplashDialogFragment d;
    long time;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        ThemePicker.setThemePicker(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        if(getIntent().getExtras()==null || getIntent().getExtras().containsKey(ARGS_SKIP)) {
            if (getResources().getBoolean(R.bool.showsplash)) {
                Log.d("pois","splash");
                d = new SplashDialogFragment();
                time = System.currentTimeMillis();
                d.show(getFragmentManager(), "SSF");
            }
        }
        new AutoUpdate(this).execute();
    }

    @Override
    public boolean onSearchRequested() {
        startActivity(new Intent(this, SearchActivity.class));
        return true;
    }

    @Override
    public void onSuccess() {
        if(d==null)
            return;
        long timepassed = System.currentTimeMillis()-time;
        Log.d("pois","timePassed for SplashScreen: "+timepassed);
        if (timepassed>= MINTIME_FOR_SPLASHSCREEN && d.isAdded()) {
            d.dismissAllowingStateLoss();
        }
        else{
            Handler handler = new Handler(Looper.getMainLooper());
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    onSuccess();
                }
            }, MINTIME_FOR_SPLASHSCREEN -timepassed);
        }
    }

    @Override
    public void onFailure() {
        if (d.isAdded()) {
            d.dismissAllowingStateLoss();
        }
        startActivity(new Intent(this, MainFail.class));
        finish();
    }
}
