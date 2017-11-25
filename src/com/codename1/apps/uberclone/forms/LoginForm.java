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

import com.codename1.components.SpanButton;
import com.codename1.ui.Button;
import static com.codename1.ui.CN.*;
import com.codename1.ui.Component;
import com.codename1.ui.Container;
import com.codename1.ui.Display;
import com.codename1.ui.Form;
import com.codename1.ui.Graphics;
import com.codename1.ui.Label;
import com.codename1.ui.TextArea;
import com.codename1.ui.TextField;
import com.codename1.ui.events.FocusListener;
import com.codename1.ui.geom.Dimension;
import com.codename1.ui.layouts.BorderLayout;
import com.codename1.ui.layouts.BoxLayout;
import com.codename1.ui.layouts.FlowLayout;
import com.codename1.ui.layouts.LayeredLayout;
import com.codename1.ui.util.Resources;

/**
 * Implements the prompt for a phone with uber logo on top portion of the form
 * 
 * @author Shai Almog
 */
public class LoginForm extends Form {
    public LoginForm() {
        super(new BorderLayout());
        
        // I want the logo to be square so height and width would be identical
        Label squareLogo = new Label("", Resources.getGlobalResources().getImage("uber-logo.png"), "SquareLogo") {
            @Override
            protected Dimension calcPreferredSize() {
                Dimension size = super.calcPreferredSize();
                size.setHeight(size.getWidth());
                return size;
            }
        };
        
        // it should be placed in the center
        Container logo = BorderLayout.centerAbsolute(squareLogo);
        logo.setUIID("LogoBackground");
        
        add(CENTER, logo);
        
        Label getMovingWithUber = new Label("Get moving with Uber", "GetMovingWithUber");
        
        CountryCodePicker countryCodeButton = new CountryCodePicker() {
            @Override
            protected void showPickerForm() {
                // number entry form
                new EnterMobileNumberForm().show();
            }  
        };
        SpanButton phoneNumber = new SpanButton("Enter your mobile number", "PhoneNumberHint");
        phoneNumber.getTextComponent().setColumns(80);
        phoneNumber.getTextComponent().setRows(2);
        phoneNumber.getTextComponent().setGrowByContent(false);
        phoneNumber.setUIID("Container");
        
        phoneNumber.addActionListener(e -> new EnterMobileNumberForm().show());
        
        Container phonePicking = BorderLayout.centerCenterEastWest(
                            phoneNumber, 
                            null, countryCodeButton);
        phonePicking.setUIID("Separator");
        
        Button social = new Button("Or connect with social", "ConnectWithSocialButton");
        social.addActionListener(e -> new FacebookOrGoogleLoginForm().show());
        
        add(SOUTH, BoxLayout.encloseY(getMovingWithUber, phonePicking, social));
    }

    @Override
    protected boolean shouldPaintStatusBar() {
        return false;
    }

    @Override
    protected void initGlobalToolbar() {
    }
}
