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

import com.codename1.apps.uberclone.server.UserService;
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
import com.codename1.ui.util.UITimer;

/**
 * Implements the SMS verification code logic
 *
 * @author Shai Almog
 */
public class EnterSMSVerificationDigitsForm extends Form {
    private int resendTime = 120;
    private UITimer timer;
    public EnterSMSVerificationDigitsForm(String phone) {
        super(new BorderLayout());
        Form previous = getCurrentForm();
        getToolbar().setBackCommand("", Toolbar.BackCommandPolicy.AS_ARROW, e -> previous.showBack());
        
        Container box = new Container(BoxLayout.y());
        box.setScrollableY(true);
        
        box.add(new SpanLabel("Enter the 4-digit code sent to you at " + phone, "FlagButton"));
        TextField[] digits = createDigits(4);
        setEditOnShow(digits[0]);
        box.add(BoxLayout.encloseX(digits));

        SpanLabel error = new SpanLabel("The SMS passcode you've entered is incorrect", "ErrorLabel");
        error.setVisible(false);
        box.add(error);
        
        add(CENTER, box);
        
        Label resend = new Label("Resend code in " + formatSeconds(resendTime), "ResendCode");
        add(SOUTH, resend);
        timer = UITimer.timer(1000, true, this, () -> {
            if(resendTime > 0) {
                resendTime--;
                resend.setText("Resend code in " + formatSeconds(resendTime));
                return;
            }
            timer.cancel();
            UserService.resendSMSActivationCode(phone);
        });

        FloatingActionButton fab = FloatingActionButton.createFAB(FontImage.MATERIAL_ARROW_FORWARD);
        fab.bindFabToContainer(this);
        
        fab.addActionListener(e -> {
            if(!isValid(toString(digits))) {
                error.setVisible(true);
                errorFields(digits);
                repaint();
                return;
            }
            new EnterPasswordForm(phone, null, null).show();
        });
    }

    private String formatSeconds(int time) {
        return twoDigits(time / 60) + ":" + twoDigits(time % 60);
    }
    
    private String twoDigits(int t) {
        if(t < 10) {
            return "0" + t;
        }
        return "" + t;
    }
    
    private TextField[] createDigits(int count) {
        TextField[] response = new TextField[count];
        for(int iter = 0 ; iter < count ; iter++) {
            TextField t = new TextField("", "0", 1, TextField.NUMERIC);
            t.setUIID("Digit");
            t.getHintLabel().getAllStyles().setAlignment(CENTER);
            response[iter] = t;
        }

        for(int iter = 0 ; iter < count - 1 ; iter++) {
            onTypeNext(response[iter], response[iter + 1]);
        }
        
        return response;
    }
    
    private void errorFields(TextField... fields) {
        for(TextField f : fields) {
            f.getAllStyles().setBorder(Border.createUnderlineBorder(2, 0xcc0000));
            f.getSelectedStyle().setBorder(Border.createUnderlineBorder(4, 0xcc0000));
        }
    }

    private String toString(TextField[] digits) {
        StringBuilder s = new StringBuilder();
        for(TextField t : digits) {
            s.append(t.getAsInt(0));
        }
        return s.toString();
    }
    
    public final boolean isValid(String s) {
        return UserService.validateSMSActivationCode(s);
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
