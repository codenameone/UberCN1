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

import com.codename1.components.FloatingActionButton;
import static com.codename1.ui.CN.*;
import com.codename1.ui.FontImage;
import com.codename1.ui.Form;
import com.codename1.ui.Label;
import com.codename1.ui.TextField;
import com.codename1.ui.Toolbar;
import com.codename1.ui.layouts.BorderLayout;
import com.codename1.ui.layouts.BoxLayout;
import com.codename1.ui.layouts.FlowLayout;

/**
 * Form representing mobile number entry
 *
 * @author Shai Almog
 */
public class EnterMobileNumberForm extends Form {
    public EnterMobileNumberForm() {
        super(BoxLayout.y());
        Form previous = getCurrentForm();
        getToolbar().setBackCommand("", Toolbar.BackCommandPolicy.AS_ARROW, e -> previous.showBack());

        add(new Label("Enter your mobile number", "FlagButton"));
        
        CountryCodePicker countryCodeButton = new CountryCodePicker();
        TextField phoneNumber = new TextField("", "050-123-4567", 40, TextField.PHONENUMBER);
        add(BorderLayout.centerEastWest(
                FlowLayout.encloseMiddle(phoneNumber), 
                null, 
                countryCodeButton));
        setEditOnShow(phoneNumber);
        FloatingActionButton fab = FloatingActionButton.createFAB(FontImage.MATERIAL_ARROW_FORWARD);
        fab.bindFabToContainer(this);
        
        fab.addActionListener(e -> {
            String number = phoneNumber.getText();
            if(number.startsWith("0")) {
                number = number.substring(1);
            }
            new EnterSMSVerificationDigitsForm(countryCodeButton.getText() + "-" + number).show();
        });
    }
}