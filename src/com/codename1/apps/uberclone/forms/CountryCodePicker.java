/*
 * Copyright (c) 2012, Codename One and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Codename One designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *  
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 * 
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 * 
 * Please contact Codename One through http://www.codenameone.com/ if you 
 * need additional information or have any questions.
 */

package com.codename1.apps.uberclone.forms;

import com.codename1.io.Log;
import com.codename1.l10n.L10NManager;
import static com.codename1.ui.CN.*;
import static com.codename1.sms.activation.ActivationForm.COUNTRY_CODES;
import static com.codename1.sms.activation.ActivationForm.COUNTRY_FLAGS;
import static com.codename1.sms.activation.ActivationForm.COUNTRY_ISO2;
import static com.codename1.sms.activation.ActivationForm.COUNTRY_ISO3;
import com.codename1.ui.Button;
import com.codename1.ui.Image;
import com.codename1.ui.util.Resources;
import java.io.IOException;

/**
 * Generic button that shows a flag and international dial prefix. Defaults to the OS locale value and when 
 * clicked allows us to pick a different locale
 *
 * @author Shai Almog
 */
public class CountryCodePicker extends Button {
    private Resources flagResource;
    public CountryCodePicker() {
        setUIID("CountryCodePicker");
        addActionListener(e -> showPickerForm());
        setGap(convertToPixels(2));
        String code = L10NManager.getInstance().getLocale();
        //String code = "IL";
        if(code != null) {
            String[] countryCodes; 
            if(code.length() == 2) {
                countryCodes = COUNTRY_ISO2;
            } else {
                if(code.length() == 3) {
                    countryCodes = COUNTRY_ISO3;
                } else {
                    return;
                }
            }
            code = code.toUpperCase();
            try {
                flagResource = Resources.open("/flags.res");
            } catch(IOException err) {
                Log.e(err);
            }
            Image blankIcon = Image.createImage(100, 70, 0);
            for(int iter = 0 ; iter < countryCodes.length ; iter++) {
                if(code.equals(countryCodes[iter])) {
                    setText("+" + COUNTRY_CODES[iter]);
                    setIcon(flagResource.getImage(COUNTRY_FLAGS[iter]));
                    if(getIcon() == null) {
                        setIcon(blankIcon);
                    }
                    return;
                }
            }
        }
    }
    
    protected void showPickerForm() {
        new CountryPickerForm(this, flagResource).show();
    }
}
