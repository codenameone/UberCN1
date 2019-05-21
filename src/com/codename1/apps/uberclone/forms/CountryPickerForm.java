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

import static com.codename1.ui.CN.*;
import com.codename1.sms.activation.ActivationForm;
import com.codename1.ui.Button;
import com.codename1.ui.Container;
import com.codename1.ui.FontImage;
import com.codename1.ui.Form;
import com.codename1.ui.Image;
import com.codename1.ui.Label;
import com.codename1.ui.Toolbar;
import com.codename1.ui.layouts.BorderLayout;
import com.codename1.ui.layouts.BoxLayout;
import com.codename1.ui.layouts.LayeredLayout;
import com.codename1.ui.plaf.Style;
import com.codename1.ui.util.Resources;

/**
 * Shows the form for picking the country dial code
 *
 * @author Shai Almog
 */
public class CountryPickerForm extends Form {
    public CountryPickerForm(Button sourceButton, Resources flags) {
        super(BoxLayout.y());
        CommonCode.initBlackTitleForm(this, "Select a Country", val -> search(val));
        
        Image blankIcon = Image.createImage(100, 70, 0);
        char lastChar = (char)-1;
        for(int iter = 0 ; iter < ActivationForm.COUNTRY_CODES.length ; iter++) {
            Button b = new Button(ActivationForm.COUNTRY_NAMES[iter], "FlagButton");
            b.setGap(convertToPixels(2));
            char current = b.getText().charAt(0);
            if(current != lastChar) {
                lastChar = current;
                Label l = new Label("" + lastChar, "FlagsLetter");
                add(l);
            }
            b.setIcon(flags.getImage(ActivationForm.COUNTRY_FLAGS[iter]));
            if(b.getIcon() == null) {
                b.setIcon(blankIcon);
            }
            String currentCountryCode = ActivationForm.COUNTRY_CODES[iter];
            b.addActionListener(ee -> {
                // picked country...
                sourceButton.setIcon(b.getIcon());
                sourceButton.setText("+" + currentCountryCode);
                sourceButton.getComponentForm().showBack();
            });
            add(b);
        }
    }

    @Override
    protected void initGlobalToolbar() {
        super.initGlobalToolbar();
        getToolbar().setUIID("BlackToolbar");
    }
    
    void search(String s) {
    }
}
