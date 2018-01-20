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

import com.codename1.apps.uberclone.dataobj.Ride;
import com.codename1.apps.uberclone.dataobj.User;
import com.codename1.apps.uberclone.server.DriverService;
import com.codename1.apps.uberclone.server.LocationService;
import com.codename1.apps.uberclone.server.PaymentService;
import com.codename1.apps.uberclone.server.SearchService;
import com.codename1.apps.uberclone.tools.AutoCompleteAddressInput;
import com.codename1.apps.uberclone.tools.BlackAndWhiteBorder;
import com.codename1.apps.uberclone.tools.BlinkDot;
import com.codename1.apps.uberclone.tools.CompletionContainer;
import com.codename1.apps.uberclone.tools.MapLayout;
import com.codename1.components.FloatingActionButton;
import com.codename1.components.InfiniteProgress;
import com.codename1.components.InteractionDialog;
import com.codename1.components.MultiButton;
import com.codename1.components.ScaleImageLabel;
import com.codename1.components.SpanLabel;
import com.codename1.components.ToastBar;
import com.codename1.googlemaps.MapContainer;
import com.codename1.location.Location;
import com.codename1.maps.BoundingBox;
import com.codename1.maps.Coord;
import com.codename1.maps.MapListener;
import static com.codename1.ui.CN.*;
import com.codename1.ui.Button;
import com.codename1.ui.Component;
import com.codename1.ui.Container;
import com.codename1.ui.Display;
import com.codename1.ui.FontImage;
import com.codename1.ui.Form;
import com.codename1.ui.Graphics;
import com.codename1.ui.Image;
import com.codename1.ui.Label;
import com.codename1.ui.TextArea;
import com.codename1.ui.TextField;
import com.codename1.ui.Toolbar;
import com.codename1.ui.animations.CommonTransitions;
import com.codename1.ui.events.FocusListener;
import com.codename1.ui.layouts.BorderLayout;
import com.codename1.ui.layouts.BoxLayout;
import com.codename1.ui.layouts.FlowLayout;
import com.codename1.ui.layouts.GridLayout;
import com.codename1.ui.layouts.LayeredLayout;
import com.codename1.ui.plaf.Style;
import com.codename1.ui.util.Effects;
import com.codename1.ui.util.Resources;
import com.codename1.ui.util.UITimer;
import java.util.List;

/**
 * The main form of the application containing the map code
 *
 * @author Shai Almog
 */
public class MapForm extends Form {
    private static final String MAP_JS_KEY = "AIzaSyDb6aabtmdQPw4Fn-HnWHLFOo72GUj-W_4";
    private Image square;

    private Image dropShadow;
    private static MapForm instance;
    private int shadowHeight;
    private MapContainer mc;
    private AutoCompleteAddressInput lastFocused;
    private MapListener lastMapListener;
    private UITimer lastTimer;
    private Button whereTo;
    private Container mapLayer;
    private boolean inNavigationMode;
    
    public static MapForm get() {
        if(instance == null) {
            instance = new MapForm();
        }
        return instance;
    }
    
    private MapForm() {
        super(new LayeredLayout());
        setScrollableY(false);
        
        shadowHeight = convertToPixels(4);
        Display.getInstance().callSeriallyOnIdle(() -> {
            dropShadow = Effects.squareShadow(getDisplayWidth() + shadowHeight * 2, shadowHeight * 2, shadowHeight, 0.3f);
        });
        
        setTransitionOutAnimator(CommonTransitions.createEmpty());
        
        mc = new MapContainer(MAP_JS_KEY);
        mc.setShowMyLocation(true);
        add(mc);
        
        mapLayer = new Container();
        mapLayer.setName("Map Layer");
        mapLayer.setLayout(new MapLayout(mc, mapLayer));
        
        final Image carImage = Resources.getGlobalResources().getImage("map-vehicle-icon-uberX.png");
        LocationService.bind(user -> {
            Label userCar = new Label();
            userCar.putClientProperty("angle", user.direction.get());
            userCar.setIcon(carImage.rotate((int)user.direction.getFloat()));
            userCar.getAllStyles().setOpacity(140);
            MapLayout.setHorizontalAlignment(userCar, MapLayout.HALIGN.CENTER);
            MapLayout.setVerticalAlignment(userCar, MapLayout.VALIGN.MIDDLE);
            mapLayer.add(new Coord(user.latitude.get(), user.longitude.get()), userCar);
            mapLayer.revalidate();
            user.direction.addChangeListener(p -> {
                Float angle = (Float)userCar.getClientProperty("angle");
                if(angle == null || angle.floatValue() != user.direction.getFloat()) {
                    userCar.setIcon(carImage.rotate((int)user.direction.getFloat()));
                    userCar.putClientProperty("angle", user.direction.get());
                }
            });
            user.latitude.addChangeListener(p -> {
                Coord crd = (Coord)mapLayer.getLayout().getComponentConstraint(userCar);
                if(crd.getLatitude() != user.latitude.get()) {
                    userCar.remove();
                    mapLayer.add(new Coord(user.latitude.get(), user.longitude.get()), userCar);                    
                    mapLayer.animateLayout(100);
                }
            });
            user.longitude.addChangeListener(p -> {
                Coord crd = (Coord)mapLayer.getLayout().getComponentConstraint(userCar);
                if(crd.getLongitude()!= user.longitude.get()) {
                    userCar.remove();
                    mapLayer.add(new Coord(user.latitude.get(), user.longitude.get()), userCar);                    
                    mapLayer.animateLayout(100);
                }
            });
        }, loc ->  mc.fitBounds(new BoundingBox(new Coord(loc.getLatitude(), loc.getLongitude()), 0.009044, 0.0089831)));
        
        add(mapLayer);
        
        square = Image.createImage(convertToPixels(0.7f), convertToPixels(0.7f), 0xff000000);
        whereTo = new Button("Where To?", square, "WhereTo");
        whereTo.setGap(convertToPixels(3));
        add(BoxLayout.encloseY(whereTo));
        
        whereTo.addActionListener(e -> showNavigationToolbar());
        
        FloatingActionButton history1 = FloatingActionButton.createFAB(FontImage.MATERIAL_HISTORY, "History");
        FloatingActionButton history2 = FloatingActionButton.createFAB(FontImage.MATERIAL_HISTORY, "History");
        TextArea history1Label = new TextArea("Mikve Yisrael Str...", 3, 4);
        TextArea history2Label = new TextArea("Burgeranch", 3, 4);
        history1Label.setUIID("HistoryLabel");
        history2Label.setUIID("HistoryLabel");
        history1Label.setEditable(false);
        history1Label.setGrowByContent(false);
        history2Label.setEditable(false);
        history2Label.setGrowByContent(false);
        Container h1 = BoxLayout.encloseY(history1, history1Label);
        Container h2 = BoxLayout.encloseY(history2, history2Label);
        h1.setLeadComponent(history1);
        h2.setLeadComponent(history2);
        ScaleImageLabel gradient = new ScaleImageLabel(Resources.getGlobalResources().getImage("gradient-overlay.png"));
        gradient.setBackgroundType(Style.BACKGROUND_IMAGE_SCALED_FILL);
        add(BorderLayout.south(gradient));
        add(BorderLayout.south(FlowLayout.encloseCenter(h1, h2)));
    }

    @Override
    protected void initGlobalToolbar() {
        setToolbar(new Toolbar(true));
        CommonCode.constructSideMenu(getToolbar());
    }
        
    void showNavigationToolbar() {
        final Container layer = getLayeredPane(MapForm.class, true);
        layer.setName("MapFormLayer");
        layer.setLayout(new BorderLayout());
        final Container pinLayer = getLayeredPane(AutoCompleteAddressInput.class, false);
        pinLayer.setName("PinLayer");
        pinLayer.setLayout(new BorderLayout(BorderLayout.CENTER_BEHAVIOR_CENTER_ABSOLUTE));
        Image pin = Resources.getGlobalResources().getImage("Pin.png");
        Label pinLabel = new Label(pin);
        MapLayout.setHorizontalAlignment(pinLabel, MapLayout.HALIGN.CENTER);
        MapLayout.setVerticalAlignment(pinLabel, MapLayout.VALIGN.BOTTOM);
        pinLayer.add(CENTER, pinLabel);
        Button back = new Button("", "TitleCommand");
        FontImage.setMaterialIcon(back, FontImage.MATERIAL_ARROW_BACK);

        CompletionContainer cc = new CompletionContainer();
        AutoCompleteAddressInput from = new AutoCompleteAddressInput("Current Location", "From", layer, cc);
        AutoCompleteAddressInput to = new AutoCompleteAddressInput("", "Where To?", layer, cc);
        from.setCurrentLocation(LocationService.getCurrentLocation());
                
        Image circle = Image.createImage(square.getWidth(), square.getHeight(), 0);
        Graphics g = circle.getGraphics();
        g.setColor(0xa4a4ac);
        g.setAntiAliased(true);
        g.fillArc(0, 0, circle.getWidth(), circle.getHeight(), 0, 360);
        
        final Label fromSelected = new Label(circle);
        final Label toSelected = new Label(square);
        
        SearchService.nameMyCurrentLocation(LocationService.getCurrentLocation(), name -> from.setTextNoEvent(name));
        to.requestFocus();
        lastFocused = to;
        from.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(Component cmp) {
                fromSelected.setIcon(square);
                lastFocused = from;
            }

            @Override
            public void focusLost(Component cmp) {
                fromSelected.setIcon(circle);
            }
        });
        to.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(Component cmp) {
                fromSelected.setIcon(circle);
                toSelected.setIcon(square);
                lastFocused = to;
            }

            @Override
            public void focusLost(Component cmp) {
                toSelected.setIcon(circle);
            }
        });
        
        addMapListener((source, zoom, center) -> {
            if(lastTimer != null) {
                lastTimer.cancel();
            }
            lastTimer = UITimer.timer(500, false, () -> {
                lastTimer = null;
                SearchService.nameMyCurrentLocation(new Location(center.getLatitude(), center.getLongitude()), 
                        name -> {
                            lastFocused.setTextNoEvent(name);
                            lastFocused.setCurrentLocation(new Location(center.getLatitude(), center.getLongitude()));
                        });
            });
        });
        
        Container navigationToolbar = BoxLayout.encloseY(back, 
                BorderLayout.centerCenterEastWest(from, null, fromSelected), 
                BorderLayout.centerCenterEastWest(to, null, toSelected)
        );
        navigationToolbar.setUIID("WhereToToolbar");
        navigationToolbar.getUnselectedStyle().setBgPainter((g1, rect) -> {
            g1.setAlpha(255);
            g1.setColor(0xffffff);
            if(dropShadow != null) {
                if(((BorderLayout)layer.getLayout()).getCenter() != null) {
                    g1.fillRect(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
                } 
                g1.drawImage(dropShadow, rect.getX() - shadowHeight, rect.getY() + rect.getHeight() - dropShadow.getHeight() / 4 * 3);
                g1.fillRect(rect.getX(), rect.getY(), rect.getWidth(), rect.getY() + rect.getHeight() - shadowHeight);
            } else {
                g1.fillRect(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
            }
            g1.setColor(0xa4a4ac);
            g1.setAntiAliased(true);
            
            int x = fromSelected.getAbsoluteX() + fromSelected.getWidth() / 2 - 1;
            int y = fromSelected.getAbsoluteY() + fromSelected.getHeight() / 2 + circle.getHeight() / 2;
            g1.fillRect(x, y, 2, toSelected.getAbsoluteY() - y + toSelected.getHeight() / 2 - circle.getHeight() / 2);
        });
        
        cc.addCompletionListener(e -> {
            if(to.getCurrentLocation() != null) {
                SearchService.directions(from.getCurrentLocation(), to.getCurrentLocation(), 
                        (path, duration, distance) -> {
                    enterNavigationMode(pinLayer, navigationToolbar, layer, path, from.getText(), to.getText(), duration); 
                });
            }
        });

        back.addActionListener(e -> {
            pinLayer.removeAll();
            navigationToolbar.setY(-navigationToolbar.getHeight());
            layer.getComponentAt(1).setY(getDisplayHeight());
            navigationToolbar.getParent().animateUnlayout(200, 120, () -> {
                    layer.removeAll();
                    revalidate();
            });
        });
        layer.add(NORTH, navigationToolbar);
        navigationToolbar.setWidth(getDisplayWidth());
        navigationToolbar.setHeight(getPreferredH());
        navigationToolbar.setY(-navigationToolbar.getHeight());
        getAnimationManager().addAnimation(layer.createAnimateLayout(200), 
                    () -> cc.showCompletionBar(layer));
    }
    
    private Component createNavigationTag(String location, int durationMinutes) {
        Label locationLabel = new Label(location, "NavigationLabel");
        if(durationMinutes > 0) {
            Label duration = new Label("" + durationMinutes, "NavigationMinuteLabel");
            Label min = new Label("MIN", "NavigationMinuteDescLabel");
            Container west = BoxLayout.encloseY(duration, min);
            Container result = BorderLayout.centerEastWest(locationLabel, 
                                                                    null, west);
            result.getUnselectedStyle().setBorder(BlackAndWhiteBorder.create().
                    blackLinePosition(west.getPreferredW()));
            return result;
        }
        locationLabel.getUnselectedStyle().setBorder(BlackAndWhiteBorder.create());
        return locationLabel;
    }
    
    public void showRide(long userId) {
        InteractionDialog id = new InteractionDialog(new BorderLayout());
        id.setTitle("Loading Ride Details");
        id.add(CENTER, new InfiniteProgress());
        id.show(getHeight() - id.getPreferredH(), 0, 0, 0);
        DriverService.fetchRideDetails(userId, ride -> {
            id.setTitle("Building Ride Path");
            final Coord[] locations = new Coord[2];
            
            if(ride.from.get() == null) {
                id.dispose();
                ToastBar.showErrorMessage("Ride no longer available...");
                return;
            }
            
            SearchService.findLocation(ride.from.get(), fromLocation -> {
                locations[0] = fromLocation;
                onShowRideResponse(id, ride, locations);
            });
            SearchService.findLocation(ride.destination.get(), toLocation -> {
                locations[1] = toLocation;
                onShowRideResponse(id, ride, locations);
            });
        });
    }
    
    private Location toLocation(Coord crd) {
        return new Location(crd.getLatitude(), crd.getLongitude());
    }
    
    public void pickUpPassenger(MapContainer.MapObject pathObject, 
            Ride ride,
            Component fromComponent, Component toComponent) {
        InteractionDialog id = new InteractionDialog("Pick Up", BoxLayout.y());
        id.add(new Label(ride.name.get(), "RideTitle"));
        Button acceptButton = new Button("Picked Up", "BlackButton");
        Button cancelButton = new Button("Cancel", "BlackButton");
        id.add(acceptButton);
        id.add(cancelButton);
        
        acceptButton.addActionListener(e -> {
            DriverService.startRide();
            id.dispose();
            InteractionDialog dlg = new InteractionDialog("Driving...", BoxLayout.y());
            dlg.add(new Label(ride.name.get(), "RideTitle"));
            Button finishButton = new Button("Finished Ride", "BlackButton");
            dlg.add(finishButton);
            
            finishButton.addActionListener(ee -> {
                DriverService.finishRide();
                fromComponent.remove();
                toComponent.remove();
                mc.removeMapObject(pathObject);
                dlg.dispose();
            });
            dlg.show(getHeight() - dlg.getPreferredH(), 0, 0, 0);
        });
        
        cancelButton.addActionListener(e -> {
            fromComponent.remove();
            toComponent.remove();
            mc.removeMapObject(pathObject);
            id.dispose();
        });
        id.show(getHeight() - id.getPreferredH(), 0, 0, 0);
    }
    
    void onShowRideResponse(InteractionDialog dlg, Ride ride, Coord[] locations) {
        if(locations[0] == null || locations[1] == null) {
            return;
        }
        
        SearchService.directions(toLocation(locations[0]), toLocation(locations[1]), 
                (path, duration, distance) -> {
            dlg.dispose();
            String from = ride.from.get();
            String to = ride.destination.get();
            Component fromComponent = createNavigationTag(from.substring(0, from.indexOf(',')), duration / 60);
            Component toComponent = createNavigationTag(to.substring(0, to.indexOf(',')), -1);
            MapContainer.MapObject pathObject = addPath(path, fromComponent, toComponent, duration);
            
            InteractionDialog id = new InteractionDialog("Ride", BoxLayout.y());
            id.add(new Label(ride.name.get(), "RideTitle"));
            Button acceptButton = new Button("Accept", "BlackButton");
            Button cancelButton = new Button("Cancel", "BlackButton");
            id.setAnimateShow(false);
            id.add(acceptButton);
            id.add(cancelButton);
            
            cancelButton.addActionListener(e -> {
                fromComponent.remove();
                toComponent.remove();
                mc.removeMapObject(pathObject);
                id.dispose();
            });
            
            acceptButton.addActionListener(e -> {
                boolean accept = DriverService.acceptRide(ride.userId.getLong());
                callSerially(() -> {
                    if(accept) {
                        id.dispose();
                        pickUpPassenger(pathObject, ride, fromComponent, toComponent);
                    } else {
                        id.dispose();
                        fromComponent.remove();
                        toComponent.remove();
                        mc.removeMapObject(pathObject);
                        getAnimationManager().flushAnimation(() -> ToastBar.showErrorMessage("Failed to grab ride"));
                    }
                });
            });
            
            id.show(getHeight() - id.getPreferredH(), 0, 0, 0);
        });    
    }
    
    private MapContainer.MapObject addPath(List<Coord> path, Component fromComponent, Component toComponent, int duration) {
        Coord[] pathCoords = new Coord[path.size()];
        path.toArray(pathCoords);
        MapContainer.MapObject pathObject = mc.addPath(pathCoords);
        BoundingBox bb = BoundingBox.create(pathCoords).
                extend(new BoundingBox(pathCoords[0], 0.01, 0.01)).
                extend(new BoundingBox(pathCoords[pathCoords.length - 1], 0.01, 0.01));
        mc.fitBounds(bb);

        MapLayout.setHorizontalAlignment(fromComponent, MapLayout.HALIGN.RIGHT);
        mapLayer.add(pathCoords[0], fromComponent);
        mapLayer.add(pathCoords[pathCoords.length - 1], toComponent);
        return pathObject;
    }
    
    private String trimmedString(String str) {
        int p = str.indexOf(',');
        if(p > -1) {
            str = str.substring(0, p);
        } 
        if(str.length() > 15) {
            str = str.substring(0, 15);
        }
        return str;
    }
    
    private void enterNavigationMode(final Container pinLayer, Container navigationToolbar, 
            final Container layer, List<Coord> path, String from, String to, int duration) {
        pinLayer.removeAll();
        navigationToolbar.setY(-navigationToolbar.getHeight());
        layer.getComponentAt(1).setY(getDisplayHeight());
        navigationToolbar.getParent().animateUnlayout(200, 120, () -> {
            if(inNavigationMode) {
                return;
            }
            inNavigationMode = true;
            callSerially(() -> {
                layer.removeAll();

                Component fromComponent = createNavigationTag(trimmedString(from), duration / 60);
                Component toComponent = createNavigationTag(trimmedString(to), -1);
                MapContainer.MapObject pathObject = addPath(path, fromComponent, toComponent, duration);

                whereTo.setVisible(false);
                getToolbar().setVisible(false);

                Button back = new Button("", "TitleCommand");
                FontImage.setMaterialIcon(back, FontImage.MATERIAL_ARROW_BACK);
                layer.add(NORTH, back);
                back.addActionListener(e -> 
                    exitNavigationMode(layer, fromComponent, toComponent, pathObject));

                Label ride = new Label("Ride", "RideTitle");
                Label taxi = new Label("Taxi", Resources.getGlobalResources().getImage("ride.png"), "RideTitle");
                taxi.setTextPosition(BOTTOM);

                Label separator = new Label("", "MarginSeparator");
                separator.setShowEvenIfBlank(true);
                Button blackButton = new Button("Confirm", "BlackButton");
                Container cnt = BoxLayout.encloseY(ride, taxi, separator, blackButton);
                cnt.setUIID("Form");
                layer.add(SOUTH, cnt);
                revalidate();
                blackButton.addActionListener(e -> {
                    exitNavigationMode(layer, fromComponent, toComponent, pathObject);
                    
                    Label searching = new Label("Finding your ride",
                            Resources.getGlobalResources().getImage("searching-cab-icon.png"),
                            "SearchingDialog");
                    pinLayer.add(SOUTH, searching);
                    pinLayer.getUnselectedStyle().setBgColor(0);
                    pinLayer.getUnselectedStyle().setBgTransparency(120);
                    pinLayer.add(CENTER, new BlinkDot());
                    LocationService.hailRide(from, to, car -> {
                        hailRideImpl(car, pinLayer);
                    });
                });
            });
        });
    }

    private void hailRideImpl(User car, final Container pinLayer) {
        pinLayer.getUnselectedStyle().setBgTransparency(0);
        pinLayer.removeAll();
        String driverName = car.givenName.get();
        String carBrand = car.car.get();
        
        SpanLabel driver = new SpanLabel("Driver found " + driverName + "\n" + carBrand);
        Container stars = new Container(new FlowLayout(CENTER));
        for(int iter = 0 ; iter < 5 ; iter++) {
            if(iter + 1 >= car.currentRating.getFloat()) {
                Label fullStar = new Label("", "Star");
                FontImage.setMaterialIcon(fullStar, FontImage.MATERIAL_STAR);
                stars.add(fullStar);
            } else {
                if(iter + 1 >= Math.round(car.currentRating.getFloat())) {
                    Label halfStar = new Label("", "Star");
                    FontImage.setMaterialIcon(halfStar, FontImage.MATERIAL_STAR_HALF);
                    stars.add(halfStar);
                } else {
                    break;
                }
            }
        }
        Button ok = new Button("Pay With Cash", "BlackButton");
        Button pay = new Button("Pay With Credit", "BlackButton");
        Container dialog = BoxLayout.encloseY(driver, stars, ok, pay);
        dialog.setUIID("SearchingDialog");
        pinLayer.add(SOUTH, dialog);
        revalidate();
        ok.addActionListener(ee -> {
            dialog.remove();
            revalidate();
        });
        pay.addActionListener(ee -> {
            dialog.remove();
            revalidate();
            PaymentService.sendPaymentAuthorization("" + car.currentRide.getLong());
        });
    }

    private void exitNavigationMode(final Container layer, Component fromComponent, Component toComponent, MapContainer.MapObject pathObject) {
        layer.removeAll();
        fromComponent.remove();
        toComponent.remove();
        mc.removeMapObject(pathObject);
        getToolbar().setVisible(true);
        whereTo.setVisible(true);
        revalidate();
        inNavigationMode = false;
    }
    
    private void addMapListener(MapListener ml) {
        if(lastMapListener != null) {
            mc.removeMapListener(lastMapListener);
        }
        lastMapListener = ml;
        mc.addMapListener(ml);
    }
}
