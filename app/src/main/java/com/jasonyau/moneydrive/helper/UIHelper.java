/*
 *   Copyright 2015 Benoit LETONDOR
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.jasonyau.moneydrive.helper;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import com.jasonyau.moneydrive.activities.CalendarActivity;

/**
 *      # COMP 4521    #  YAU Yat Fung        20371106          yfyau@connect.ust.hk
 */
public class UIHelper
{

    /**
     * Check if the os version is compatible with activity enter animations (Android 5+)
     *
     * @return
     */
    public static boolean isCompatibleWithActivityEnterAnimation()
    {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    /**
     * Check if the os version is compatible with activity enter animations (Android 5+) && the
     * activity contains the animation key
     *
     * @return
     */
    public static boolean willAnimateActivityEnter(Activity activity)
    {
        return isCompatibleWithActivityEnterAnimation() && activity.getIntent().getBooleanExtra(CalendarActivity.ANIMATE_TRANSITION_KEY, false);
    }

    /**
     * Are animations enabled (can be disabled by user in settings)
     *
     * @param context
     * @return
     */
    public static boolean areAnimationsEnabled(@NonNull Context context)
    {
        return Parameters.getInstance(context).getBoolean(ParameterKeys.ANIMATIONS_ENABLED, true);
    }

    /**
     * This helper prevents the user to add unsupported values into an EditText for decimal numbers
     *
     * @param editText
     */
    public static void preventUnsupportedInputForDecimals(final @NonNull EditText editText)
    {
        editText.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {

            }

            @Override
            public void afterTextChanged(Editable s)
            {
                String value = editText.getText().toString();

                try
                {
                    // Remove - that is not at first char
                    int minusIndex = value.lastIndexOf("-");
                    if (minusIndex > 0)
                    {
                        s.delete(minusIndex, minusIndex + 1);

                        if (value.startsWith("-"))
                        {
                            s.delete(0, 1);
                        }
                        else
                        {
                            s.insert(0, "-");
                        }

                        return;
                    }

                    int comaIndex = value.indexOf(",");
                    int dotIndex = value.indexOf(".");
                    int lastDotIndex = value.lastIndexOf(".");

                    // Remove ,
                    if (comaIndex >= 0)
                    {
                        if (dotIndex >= 0)
                        {
                            s.delete(comaIndex, comaIndex + 1);
                        }
                        else
                        {
                            s.replace(comaIndex, comaIndex + 1, ".");
                        }

                        return;
                    }

                    // Disallow double .
                    if (dotIndex >= 0 && dotIndex != lastDotIndex)
                    {
                        s.delete(lastDotIndex, lastDotIndex + 1);
                    }
                    // No more than 2 decimals
                    else if (dotIndex > 0)
                    {
                        String decimals = value.substring(dotIndex + 1);
                        if (decimals.length() > 2)
                        {
                            s.delete(dotIndex + 3, value.length());
                        }
                    }
                }
                catch (Exception e)
                {

                }
            }
        });
    }

}
