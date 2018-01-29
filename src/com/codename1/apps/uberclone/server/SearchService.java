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

package com.codename1.apps.uberclone.server;

import static com.codename1.apps.uberclone.server.Globals.*;
import com.codename1.io.ConnectionRequest;
import com.codename1.io.Util;
import com.codename1.io.rest.Rest;
import com.codename1.location.Location; 
import com.codename1.maps.Coord;
import static com.codename1.ui.CN.*;
import com.codename1.util.SuccessCallback;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Encapsulates the Google GeoCoding & direction API's so we can provide
 * a decent navigational experience
 *
 * @author Shai Almog
 */
public class SearchService {
    private static ConnectionRequest lastSuggestionRequest;
    private static String lastSuggestionValue;
    private static ConnectionRequest lastLocationRequest;
    private static final Map<String, List<SuggestionResult>> locationCache = new HashMap<>();
    
    public static void nameMyCurrentLocation(Location l, SuccessCallback<String> name) {
        if(l == null) {
            return;
        }
        if(lastLocationRequest != null) {
            lastLocationRequest.kill();
        }
        lastLocationRequest = Rest.get("https://maps.googleapis.com/maps/api/geocode/json").
                queryParam("latlng", l.getLatitude() + "," + l.getLongitude()).
                queryParam("key", GOOGLE_GEOCODING_KEY).
                queryParam("language", "en").
                queryParam("result_type", "street_address|point_of_interest").
                getAsJsonMap(callbackMap -> {
                    Map data = callbackMap.getResponseData();
                    if(data != null) {
                        List results = (List)data.get("results");
                        if(results != null && results.size() > 0) {
                            Map firstResult = (Map)results.get(0);
                            name.onSucess((String)firstResult.get("formatted_address"));
                        }
                    }
                });
    }
    
    public static interface DirectionResults {
        public void onDirectionResult(List<Coord> path, int duration, String distance);
    }
    
    public static void directions(Location l, Location destination, DirectionResults response) {
        Rest.get("https://maps.googleapis.com/maps/api/directions/json").
                queryParam("origin", l.getLatitude() + "," + l.getLongitude()).
                queryParam("destination", destination.getLatitude() + "," + destination.getLongitude()).
                queryParam("mode", "driving").
                queryParam("key", GOOGLE_DIRECTIONS_KEY).
                getAsJsonMap(callbackMap -> {
                    Map data = callbackMap.getResponseData();
                    if(data != null) {
                        List results = (List)data.get("routes");
                        if(results != null && results.size() > 0) {
                            Map firstResult = (Map)results.get(0);
                            Map overview_polyline = (Map)firstResult.get("overview_polyline");
                            List<Coord> polyline = decodePolyline((String)overview_polyline.get("points"));
                            
                            List legs = (List)firstResult.get("legs");
                            Map firstLeg = (Map)legs.get(0);
                            Map distance = (Map)firstLeg.get("distance");
                            Map duration = (Map)firstLeg.get("duration");

                            response.onDirectionResult(polyline, Util.toIntValue(duration.get("value")), (String)distance.get("text"));
                        }
                    }
                });
    }

    // Taken from https://github.com/scoutant/polyline-decoder/blob/master/src/main/java/org/scoutant/polyline/PolylineDecoder.java
    private static List<Coord> decodePolyline(String str) {
        ArrayList<Coord> resultPath = new ArrayList<>();
        int index = 0;
        int lat = 0, lng = 0;

        while (index < str.length()) {
            int b, shift = 0, result = 0;
            do {
                b = str.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = str.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            Coord p = new Coord((double) lat / 1E5, (double) lng / 1E5);
            resultPath.add(p);
        }
        return resultPath;
    }

    public static class SuggestionResult {
        private final String mainText;
        private final String secondaryText;
        private final String fullText;
        private final String placeId;

        public SuggestionResult(String mainText, String secondaryText, String fullText, String placeId) {
            this.mainText = mainText;
            this.secondaryText = secondaryText;
            this.fullText = fullText;
            this.placeId = placeId;
        }

        public String getPlaceId() {
            return placeId;
        }
        
        public String getMainText() {
            return mainText;
        }

        public String getSecondaryText() {
            return secondaryText;
        }

        public String getFullText() {
            return fullText;
        }
        
        public void getLocation(SuccessCallback<Location> result) {
            Rest.get("https://maps.googleapis.com/maps/api/place/details/json").
                queryParam("placeid", placeId).
                queryParam("key", GOOGLE_PLACES_KEY).
                getAsJsonMap(callbackMap -> {
                    Map r = (Map)callbackMap.getResponseData().get("result");
                    Map geomMap = (Map)r.get("geometry");
                    Map locationMap = (Map)geomMap.get("location");
                    double lat = Util.toDoubleValue(locationMap.get("lat"));
                    double lon = Util.toDoubleValue(locationMap.get("lng"));
                    result.onSucess(new Location(lat, lon));
                });
        }
    }
    
    public static void suggestLocations(String input, Location l, SuccessCallback<List<SuggestionResult>> resultList) {
        if(lastSuggestionRequest != null) {
            if(lastSuggestionValue.equals(input)) {
                return;
            }
            lastSuggestionRequest.kill();
        }
        List<SuggestionResult> lr = locationCache.get(input);
        if(lr != null) {
            lastSuggestionValue = null;
            lastSuggestionRequest = null;
            callSerially(() -> resultList.onSucess(lr));
            return;
        }
        
        lastSuggestionValue = input;
        lastSuggestionRequest = Rest.get("https://maps.googleapis.com/maps/api/place/autocomplete/json").
                queryParam("input", input).
                queryParam("location", l.getLatitude() + "," + l.getLongitude()).
                queryParam("radius", "50000").
                queryParam("key", GOOGLE_PLACES_KEY).
                getAsJsonMap(callbackMap -> {
                    Map data = callbackMap.getResponseData();
                    if(data != null) {
                        List<Map> results = (List<Map>)data.get("predictions");
                        if(results != null && results.size() > 0) {
                            ArrayList<SuggestionResult> resultSet = new ArrayList<>();
                            for(Map currentResult : results) {
                                Map structured_formatting = (Map)currentResult.get("structured_formatting");
                                String mainText = (String)structured_formatting.get("main_text");
                                String secondaryText = (String)structured_formatting.get("secondary_text");
                                String description = (String)currentResult.get("description");
                                String placeId = (String)currentResult.get("place_id");
                                resultSet.add(new SuggestionResult(mainText, secondaryText, description, placeId));
                            }
                            locationCache.put(input, resultSet);
                            resultList.onSucess(resultSet);
                        }
                    }
                });
    }

    public static void findLocation(String name, SuccessCallback<Coord> location) {
        Rest.get("https://maps.googleapis.com/maps/api/geocode/json").
                queryParam("address", name).
                queryParam("key", Globals.GOOGLE_GEOCODING_KEY).
                getAsJsonMap(callbackMap -> {
                    Map data = callbackMap.getResponseData();
                    if(data != null) {
                        List results = (List)data.get("results");
                        if(results != null && results.size() > 0) {
                            Map firstResult = (Map)results.get(0);
                            Map geometryMap = (Map)firstResult.get("geometry");
                            Map locationMap = (Map)geometryMap.get("location");
                            double lat = Util.toDoubleValue(locationMap.get("lat"));
                            double lon = Util.toDoubleValue(locationMap.get("lng"));
                            location.onSucess(new Coord(lat, lon));
                        }
                    }
                });
    }    
}

