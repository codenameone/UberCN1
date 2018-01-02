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

package com.codename1.apps.uberclone.tools;

import com.codename1.ui.Component;
import com.codename1.ui.Display;
import com.codename1.ui.Graphics;
import com.codename1.ui.Image;
import com.codename1.ui.Stroke;
import com.codename1.ui.geom.GeneralPath;
import com.codename1.ui.geom.Rectangle;
import com.codename1.ui.plaf.Border;
import com.codename1.ui.plaf.RoundRectBorder;

/**
 *
 * @author Shai Almog
 */
public class BlackAndWhiteBorder extends Border {
    private static final String CACHE_KEY = "cn1$$-bwcache";
    private final float shadowBlur = 10;
    private final float shadowSpread;
    private final int shadowOpacity = 110;
    private final float cornerRadius = 1f;
    
    private int blackLinePosition = -1;
    
    BlackAndWhiteBorder() {
        shadowSpread = Display.getInstance().convertToPixels(0.2f);
    }
    
    public static BlackAndWhiteBorder create() {
        return new BlackAndWhiteBorder();
    }
        
    public BlackAndWhiteBorder blackLinePosition(int blackLinePosition) {
        this.blackLinePosition = blackLinePosition;
        return this;
    }
    
    private Image createTargetImage(Component c, int w, int h, boolean fast) {
        Image target = Image.createImage(w, h, 0);
        
        int shapeX = 0;
        int shapeY = 0;
        int shapeW = w;
        int shapeH = h;
        
        Graphics tg = target.getGraphics();
        tg.setAntiAliased(true);
                
        int shadowSpreadL =  Display.getInstance().convertToPixels(shadowSpread);
        
        shapeW -= shadowSpreadL;
        shapeH -= shadowSpreadL;
        shapeX += Math.round(((float)shadowSpreadL) * 0.9);
        shapeY += Math.round(((float)shadowSpreadL) * 0.9);

        // draw a gradient of sort for the shadow
        for(int iter = shadowSpreadL - 1 ; iter >= 0 ; iter--) {            
            tg.translate(iter, iter);
            fillShape(tg, 0, shadowOpacity / shadowSpreadL, w - (iter * 2), h - (iter * 2));
            tg.translate(-iter, -iter);
        }
            
        if(Display.getInstance().isGaussianBlurSupported() && !fast) {
            Image blured = Display.getInstance().gaussianBlurImage(target, shadowBlur/2);
            target = Image.createImage(w, h, 0);
            tg = target.getGraphics();
            tg.drawImage(blured, 0, 0);
            tg.setAntiAliased(true);
        }
        tg.translate(shapeX, shapeY);
        c.getStyle().setBorder(Border.createEmpty());

        GeneralPath gp = createShape(shapeW, shapeH);
        tg.setClip(gp);
        c.getStyle().getBgPainter().paint(tg, new Rectangle(0, 0, w, h));
        c.getStyle().setBorder(this);
        return target;
    }    
    
    @Override
    public void paintBorderBackground(Graphics g, final Component c) {
        final int w = c.getWidth();
        final int h = c.getHeight();
        int x = c.getX();
        int y = c.getY();
        if(w > 0 && h > 0) {
            Image background = (Image)c.getClientProperty(CACHE_KEY);
            if(background != null && background.getWidth() == w && background.getHeight() == h) {
                g.drawImage(background, x, y);
                return;
            }
        } else {
            return;
        }
                
        Image target = createTargetImage(c, w, h, true);
        g.drawImage(target, x, y);
        c.putClientProperty(CACHE_KEY, target);

        // update the cache with a more refined version and repaint
        Display.getInstance().callSeriallyOnIdle(new Runnable() {
            public void run() {
                if(w == c.getWidth() && h == c.getHeight()) {
                    Image target = createTargetImage(c, w, h, false);
                    c.putClientProperty(CACHE_KEY, target);
                    c.repaint();
                }
            }
        });
    }
    
    private GeneralPath createShape(int shapeW, int shapeH) {
        GeneralPath gp = new GeneralPath();
        float radius = Display.getInstance().convertToPixels(cornerRadius);
        float x = 0;
        float y = 0;
        float widthF = shapeW;
        float heightF = shapeH;
                
        gp.moveTo(x + radius, y);
        
        if(blackLinePosition > -1) {
            gp.lineTo(x + widthF, y);
        } else {
            gp.lineTo(x + widthF - radius, y);
            gp.quadTo(x + widthF, y, x + widthF, y + radius);
        }
        gp.lineTo(x + widthF, y + heightF - radius);
        gp.quadTo(x + widthF, y + heightF, x + widthF - radius, y + heightF);
        if(blackLinePosition > -1) {
            gp.lineTo(x + radius, y + heightF);
            gp.quadTo(x, y + heightF, x, y + heightF - radius);
        } else {
            gp.lineTo(x, y + heightF);
        }
        gp.lineTo(x, y + radius);
        gp.quadTo(x, y, x + radius, y);
        gp.closePath();            
        return gp;
    }

    @Override
    public int getMinimumHeight() {
        return Display.getInstance().convertToPixels(shadowSpread) + Display.getInstance().convertToPixels(cornerRadius) * 2;
    }

    @Override
    public int getMinimumWidth() {
        return Display.getInstance().convertToPixels(shadowSpread) + Display.getInstance().convertToPixels(cornerRadius) * 2;
    }

    
    private void fillShape(Graphics g, int color, int opacity, int width, int height) {
        g.setColor(0xffffff);
        g.setAlpha(255);
        GeneralPath gp = createShape(width, height);
        if(blackLinePosition > -1) {
            int[] clip = g.getClip();
            g.clipRect(blackLinePosition, 0, width, height);
            g.fillShape(gp);
            g.setClip(clip);
            g.clipRect(0, 0, blackLinePosition, height);
            g.setColor(0);
            g.fillShape(gp);
            g.setClip(clip);
        } else {
            g.fillShape(gp);
        }
    }
    
    @Override
    public boolean isBackgroundPainter() {
        return true;
    }

}
