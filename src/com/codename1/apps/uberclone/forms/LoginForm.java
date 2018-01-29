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
import com.codename1.ui.Image;
import com.codename1.ui.Label;
import com.codename1.ui.Painter;
import com.codename1.ui.Stroke;
import com.codename1.ui.animations.Animation;
import com.codename1.ui.animations.CommonTransitions;
import com.codename1.ui.animations.MorphTransition;
import com.codename1.ui.geom.Dimension;
import com.codename1.ui.geom.GeneralPath;
import com.codename1.ui.geom.Rectangle;
import com.codename1.ui.layouts.BorderLayout;
import com.codename1.ui.layouts.BoxLayout;
import com.codename1.ui.layouts.LayeredLayout;
import com.codename1.ui.util.Effects;
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
                
        Label placeholder = new Label();
        
        // it should be placed in the center
        Container logo = LayeredLayout.encloseIn(
                placeholder,
                BorderLayout.centerAbsolute(squareLogo)
        );
        
        startThread(() -> {
            final Image shadow = Effects.squareShadow(squareLogo.getPreferredW(), squareLogo.getPreferredH(), convertToPixels(14), 0.35f);
            callSerially(() -> {
                logo.replace(placeholder, BorderLayout.centerAbsolute(new Label(shadow, "Container")), null);
                revalidate();
            });
        }, "Shadow Maker").start();
        
        logo.setUIID("LogoBackground");
        logo.getUnselectedStyle().setBgPainter(new LoginFormPainter(logo));
        
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
        phoneNumber.setName("EnterMobileNumber");
        phoneNumber.getTextComponent().setColumns(80);
        phoneNumber.getTextComponent().setRows(2);
        phoneNumber.getTextComponent().setGrowByContent(false);
        phoneNumber.setUIID("Container");
        
        phoneNumber.addActionListener(e -> new EnterMobileNumberForm().show());
        
        countryCodeButton.setName("CountryCodeButton");
        Container phonePicking = BorderLayout.centerCenterEastWest(
                            phoneNumber, 
                            null, countryCodeButton);
        phonePicking.setUIID("Separator");
        
        Button social = new Button("Or connect with social", "ConnectWithSocialButton");

        MorphTransition morph = MorphTransition.create(400).
                morph(phoneNumber.getName()).
                morph(countryCodeButton.getName());
        setTransitionOutAnimator(morph);
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

    class LoginFormPainter implements Painter, Animation {
        private double angle;
        private double lastAngle = -1;
        private GeneralPath gp;
        private final Component parentCmp;
        private int counter;
        private Image buffer;
        
        public LoginFormPainter(Component parentCmp) {
            this.parentCmp = parentCmp;
            registerAnimated(this);
        }

        private void drawShape(GeneralPath gp, float x, float y, float w, float h) {
            float e = w/6;
            float ex1 = x + (w-e)/2;
            float ex2 = x + (w+e)/2;
            float ey1 = y + (h-e)/2;
            float ey2 = y + (h+e)/2;
            gp.moveTo(ex1, y);
            gp.lineTo(ex2, y);
            gp.quadTo(x+w, y, x+w, ey1);
            gp.lineTo(x+w, ey2);
            gp.quadTo(x+w, y+h, ex2, y+h);
            gp.lineTo(ex1, y+h);
            gp.quadTo(x, y+h, x, ey2);
            gp.lineTo(x, ey1);
            gp.quadTo(x, y, ex1, y);            
        }        
        
        @Override
        public void paint(Graphics g, Rectangle rect) {
            if(buffer == null || buffer.getWidth() != rect.getWidth()) {
                buffer = Image.createImage(rect.getWidth(), rect.getHeight());                
            }
            if(lastAngle != angle) {
                gp = new GeneralPath();
                int x;
                int y;
                int w = Display.getInstance().convertToPixels(10);
                int h = w;
                int x0 = getX() - getWidth();
                int xn = getX() + 2 * getWidth();
                int y0 = getY() - getHeight();
                int yn = getY() + 2 * getHeight();
                for (int offset : new int[]{0, w/2}) {
                    x = x0 +offset;
                    y = y0 + offset;
                    while (x < xn) {
                        while (y < yn) {
                            drawShape(gp, x, y, w, h);
                            y += h;
                        }
                        x += w;
                        y = y0 + offset;
                    }
                }            
                lastAngle = angle;
                Graphics ig = buffer.getGraphics();
                ig.setAlpha(255);
                ig.setColor(0x128f96);
                ig.fillRect(0, 0, rect.getWidth(), rect.getHeight());
                ig.setColor(0xffffff);
                ig.setAlpha(72);
                ig.setAntiAliased(true);
                ig.rotate((float)(Math.PI/4f + Math.toRadians(angle % 360)), getX() + getWidth()/2, getY() + getHeight()/2);
                ig.drawShape(gp, new Stroke(1.5f, Stroke.CAP_SQUARE, Stroke.JOIN_BEVEL, 1f));
                ig.resetAffine();
            }
            g.drawImage(buffer, rect.getX(), rect.getY());
        }

        @Override
        public boolean animate() {
            if(!Display.getInstance().isInTransition()) {
                counter++;
                if(counter % 2 == 0) {
                    angle += 0.3;
                    parentCmp.repaint();
                }
            }
            return false;
        }

        @Override
        public void paint(Graphics g) {
        }
        
    }
}
