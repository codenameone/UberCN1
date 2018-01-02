package com.codename1.apps.uberclone.tools;

import com.codename1.googlemaps.MapContainer;
import com.codename1.maps.Coord;
import com.codename1.maps.MapListener;
import com.codename1.ui.Component;
import static com.codename1.ui.CN.*;
import com.codename1.ui.Container;
import com.codename1.ui.geom.Dimension;
import com.codename1.ui.geom.Point;
import com.codename1.ui.layouts.Layout;

/**
 * A constraint based layout that positions components relatively to the position of a Map
 *
 * @author Shai Almog
 */
public class MapLayout extends Layout implements MapListener {
        private static final String COORD_KEY = "$coord";
        private static final String POINT_KEY = "$point";
        private static final String HORIZONTAL_ALIGNMENT = "$align";
        private static final String VERTICAL_ALIGNMENT = "$valign";
        private final MapContainer map;
        private final Container actual;
        private boolean inUpdate;
        private Runnable nextUpdate;        

        public static enum HALIGN {
            LEFT {

                @Override
                int convert(int x, int width) {
                    return x;
                }
            },
            CENTER {
                @Override
                int convert(int x, int width) {
                    return x - width / 2;
                }
            }, 
            RIGHT {
                @Override
                int convert(int x, int width) {
                    return x - width;
                }
            };
            
            abstract int convert(int x, int width);
        }

        public static enum VALIGN {
            TOP {
                @Override
                int convert(int y, int height) {
                    return y;
                }
            }, 
            MIDDLE {
                @Override
                int convert(int y, int height) {
                    return y + height / 2;
                }
            }, 
            BOTTOM {
                @Override
                int convert(int y, int height) {
                    return y + height;
                }
            };

            abstract int convert(int y, int height);
        }
        
        public MapLayout(MapContainer map, Container actual) {
            this.map = map;
            this.actual = actual;
            map.addMapListener(this);
        }

        @Override
        public void addLayoutComponent(Object value, Component comp, Container c) {
            comp.putClientProperty(COORD_KEY, (Coord) value);
        }

        @Override
        public boolean isConstraintTracking() {
            return true;
        }

        @Override
        public Object getComponentConstraint(Component comp) {
            return comp.getClientProperty(COORD_KEY);
        }

        @Override
        public boolean isOverlapSupported() {
            return true;
        }

        public static void setHorizontalAlignment(Component cmp, HALIGN a) {
            cmp.putClientProperty(HORIZONTAL_ALIGNMENT, a);
        }
        
        public static void setVerticalAlignment(Component cmp, VALIGN a) {
            cmp.putClientProperty(VERTICAL_ALIGNMENT, a);
        }
        
        @Override
        public void layoutContainer(Container parent) {
            int parentX = 0;
            int parentY = 0;
            for (Component current : parent) {
                Coord crd = (Coord) current.getClientProperty(COORD_KEY);
                Point p = (Point) current.getClientProperty(POINT_KEY);
                if (p == null) {
                    p = map.getScreenCoordinate(crd);
                    current.putClientProperty(POINT_KEY, p);
                }
                HALIGN h = (HALIGN)current.getClientProperty(HORIZONTAL_ALIGNMENT);
                if(h == null) {
                    h = HALIGN.LEFT;
                }
                VALIGN v = (VALIGN)current.getClientProperty(VERTICAL_ALIGNMENT);
                if(v == null) {
                    v = VALIGN.TOP;
                }
                current.setSize(current.getPreferredSize());
                current.setX(h.convert(p.getX() - parentX, current.getWidth()));
                current.setY(v.convert(p.getY() - parentY, current.getHeight()));
            }
        }

        @Override
        public Dimension getPreferredSize(Container parent) {
            return new Dimension(100, 100);
        }
        
        @Override
        public void mapPositionUpdated(Component source, int zoom, Coord center) {
            Runnable r = new Runnable() {
                public void run() {
                    inUpdate = true;
                    try {
                        for (int iter = 0 ; iter < actual.getComponentCount() ; iter++) {
                            Component current = actual.getComponentAt(iter);
                            Coord crd = (Coord) current.getClientProperty(COORD_KEY);
                            Point p = map.getScreenCoordinate(crd);
                            current.putClientProperty(POINT_KEY, p);
                        }
                        actual.setShouldCalcPreferredSize(true);
                        actual.revalidate();
                        if (nextUpdate != null) {
                            Runnable nex = nextUpdate;
                            nextUpdate = null;
                            callSerially(nex);
                        }
                    } finally {
                        inUpdate = false;
                    }
                    
                }
                    
            };
            if (inUpdate) {
                nextUpdate = r;
            } else {
                nextUpdate = null;
                callSerially(r);
            }           
        }
}