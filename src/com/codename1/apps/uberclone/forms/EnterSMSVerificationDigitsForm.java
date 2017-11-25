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
import com.codename1.components.SpanLabel;
import static com.codename1.ui.CN.*;
import com.codename1.ui.Container;
import com.codename1.ui.FontImage;
import com.codename1.ui.Form;
import com.codename1.ui.Label;
import com.codename1.ui.TextField;
import com.codename1.ui.Toolbar;
import com.codename1.ui.layouts.BorderLayout;
import com.codename1.ui.layouts.BoxLayout;
import com.codename1.ui.plaf.Border;

/**
 * Implements the SMS verification code logic
 *
 * @author Shai Almog
 */
public class EnterSMSVerificationDigitsForm extends Form {
    public EnterSMSVerificationDigitsForm(String phone) {
        super(new BorderLayout());
        Form previous = getCurrentForm();
        getToolbar().setBackCommand("", Toolbar.BackCommandPolicy.AS_ARROW, e -> previous.showBack());
        
        Container box = new Container(BoxLayout.y());
        box.setScrollableY(true);
        
        box.add(new SpanLabel("Enter the 4-digit code sent to you at " + phone, "FlagButton"));
        TextField digit1 = new TextField("", "0", 1, TextField.NUMERIC);
        TextField digit2 = new TextField("", "0", 1, TextField.NUMERIC);
        TextField digit3 = new TextField("", "0", 1, TextField.NUMERIC);
        TextField digit4 = new TextField("", "0", 1, TextField.NUMERIC);
        digit1.setUIID("Digit");
        digit2.setUIID("Digit");
        digit3.setUIID("Digit");
        digit4.setUIID("Digit");
        digit1.getHintLabel().getAllStyles().setAlignment(CENTER);
        digit2.getHintLabel().getAllStyles().setAlignment(CENTER);
        digit3.getHintLabel().getAllStyles().setAlignment(CENTER);
        digit4.getHintLabel().getAllStyles().setAlignment(CENTER);
        setEditOnShow(digit1);
        box.add(BoxLayout.encloseX(digit1, digit2, digit3, digit4));
        onTypeNext(digit1, digit2);
        onTypeNext(digit2, digit3);
        onTypeNext(digit3, digit4);

        SpanLabel error = new SpanLabel("The SMS passcode you've entered is incorrect", "ErrorLabel");
        error.setVisible(false);
        box.add(error);
        
        add(CENTER, box);
        
        Label resend = new Label("Resend code in 00:12", "ResendCode");
        add(SOUTH, resend);

        FloatingActionButton fab = FloatingActionButton.createFAB(FontImage.MATERIAL_ARROW_FORWARD);
        fab.bindFabToContainer(this);
        
        fab.addActionListener(e -> {
            if(!isValid(digit1.getAsInt(0), digit2.getAsInt(0), digit3.getAsInt(0), digit4.getAsInt(0))) {
                error.setVisible(true);
                digit1.getAllStyles().setBorder(Border.createUnderlineBorder(3, 0xff0000));
                digit2.getAllStyles().setBorder(Border.createUnderlineBorder(3, 0xff0000));
                digit3.getAllStyles().setBorder(Border.createUnderlineBorder(3, 0xff0000));
                digit4.getAllStyles().setBorder(Border.createUnderlineBorder(3, 0xff0000));
                repaint();
                return;
            }
            new EnterPasswordForm().show();
        });
    }
    
    public boolean isValid(int digit1, int digit2, int digit3, int digit4) {
        // just for now
        return digit1 == 0;
    }
    
    private void onTypeNext(TextField current, TextField next) {
        current.addDataChangedListener((i, ii) -> {
            if(current.getText().length() == 1) {
                current.stopEditing();
                next.startEditingAsync();
            }
        });
    }
}
