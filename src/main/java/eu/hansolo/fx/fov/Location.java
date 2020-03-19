/*
 * Copyright (c) 2020 by Gerrit Grunwald
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.hansolo.fx.fov;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import org.json.simple.JSONObject;

import java.time.Instant;
import java.util.Locale;
import java.util.Random;


public class Location {
    public enum CardinalDirection {
        N("North", 348.75, 11.25),
        NNE("North North-East", 11.25, 33.75),
        NE("North-East", 33.75, 56.25),
        ENE("East North-East", 56.25, 78.75),
        E("East", 78.75, 101.25),
        ESE("East South-East", 101.25, 123.75),
        SE("South-East", 123.75, 146.25),
        SSE("South South-East", 146.25, 168.75),
        S("South", 168.75, 191.25),
        SSW("South South-West", 191.25, 213.75),
        SW("South-West", 213.75, 236.25),
        WSW("West South-West", 236.25, 258.75),
        W("West", 258.75, 281.25),
        WNW("West North-West", 281.25, 303.75),
        NW("North-West", 303.75, 326.25),
        NNW("North North-West", 326.25, 348.75);

        public String direction;
        public double from;
        public double to;

        private CardinalDirection(final String DIRECTION, final double FROM, final double TO) {
            direction = DIRECTION;
            from      = FROM;
            to        = TO;
        }
    }

    // Location related information
    private Instant timestamp;
    private double  latitude;
    private double  longitude;
    private double  elevation;
    private String  name;
    private String  info;


    // ******************** Constructors **************************************
    public Location(final Location location) {
        this(location.getLatitude(), location.getLongitude(), location.getElevation(), location.getTimestamp(), location.getName());
    }
    public Location() {
        this(0, 0, 0, Instant.now(), "Loc #" + new Random().nextInt());
    }
    public Location(final double latitude, final double longitude) { this(latitude, longitude, 0.0, Instant.now(), ""); }
    public Location(final double latitude, final double longitude, final double elevation) {
        this(latitude, longitude, elevation, Instant.now(), "");
    }
    public Location(final double latitude, final double longitude, final String name) {
        this(latitude, longitude, 0, Instant.now(), name);
    }
    public Location(final double latitude, final double longitude, final double elevation, final String name) {
        this(latitude, longitude, elevation, Instant.now(), name);
    }
    public Location(final double latitude, final double longitude, final double elevation, final Instant timestamp, final String name) {
        this.latitude  = latitude;
        this.longitude = longitude;
        this.elevation = elevation;
        this.timestamp = timestamp;
        this.name      = name;
        info           = "";
    }


    // ******************** Methods *******************************************
    public String getName() { return name; }
    public void setName(final String name) { this.name = name; }

    public double getLatitude() { return latitude; }
    public void setLatitude(final double latitude) {
        this.latitude = latitude;
        fireLocationEvent(new LocationEvent(Location.this, null, LocationEvent.LOCATION_CHANGED));
    }

    public double getLongitude() { return longitude; }
    public void setLongitude(final double longitude) {
        this.longitude = longitude;
        fireLocationEvent(new LocationEvent(Location.this, null, LocationEvent.LOCATION_CHANGED));
    }

    public double getElevation() { return elevation; }
    public void setElevation(final double elevation) {
        this.elevation = elevation;
        fireLocationEvent(new LocationEvent(Location.this, null, LocationEvent.LOCATION_CHANGED));
    }

    public Instant getTimestamp() { return timestamp; }
    public long getTimestampInSeconds() { return timestamp.getEpochSecond(); }
    public void setTimestamp(final Instant timestamp) { this.timestamp = timestamp; }

    public String getInfo() { return info; }
    public void setInfo(final String info) { this.info = info; }


    public void update(final double latitude, final double longitude) { set(latitude, longitude); }

    public void set(final double latitude, final double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
        timestamp = Instant.now();
        fireLocationEvent(new LocationEvent(Location.this, null, LocationEvent.LOCATION_CHANGED));
    }
    public void set(final double latitude, final double longitude, final double altitude, final Instant timestamp) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.elevation = altitude;
        this.timestamp = timestamp;
        fireLocationEvent(new LocationEvent(Location.this, null, LocationEvent.LOCATION_CHANGED));
    }
    public void set(final Location location) {
        latitude  = location.getLatitude();
        longitude = location.getLongitude();
        elevation = location.getElevation();
        timestamp = location.getTimestamp();
        info      = location.info;
        fireLocationEvent(new LocationEvent(Location.this, null, LocationEvent.LOCATION_CHANGED));
    }

    public double getDistanceInMeterTo(final Location location) { return calcDistanceInMeter(this, location); }

    public boolean isWithinRangeOf(final Location location, final double meters) { return getDistanceInMeterTo(location) < meters; }

    public double calcDistanceInMeter(final Location p1, final Location p2) {
        return calcDistanceInMeter(p1.getLatitude(), p1.getLongitude(), p2.getLatitude(), p2.getLongitude());
    }
    public double calcDistanceInKilometer(final Location p1, final Location p2) {
        return calcDistanceInMeter(p1, p2) / 1000.0;
    }
    public double calcDistanceInMeter(final double lat1, final double lon1, final double lat2, final double lon2) {
        final double EARTH_RADIUS      = 6_371_000; // m
        final double LAT_1_RADIANS     = Math.toRadians(lat1);
        final double LAT_2_RADIANS     = Math.toRadians(lat2);
        final double DELTA_LAT_RADIANS = Math.toRadians(lat2-lat1);
        final double DELTA_LON_RADIANS = Math.toRadians(lon2-lon1);

        final double A = Math.sin(DELTA_LAT_RADIANS * 0.5) * Math.sin(DELTA_LAT_RADIANS * 0.5) + Math.cos(LAT_1_RADIANS) * Math.cos(LAT_2_RADIANS) * Math.sin(DELTA_LON_RADIANS * 0.5) * Math.sin(DELTA_LON_RADIANS * 0.5);
        final double C = 2 * Math.atan2(Math.sqrt(A), Math.sqrt(1-A));

        final double DISTANCE = EARTH_RADIUS * C;

        return DISTANCE;
    }

    public double getAltitudeDifferenceInMeter(final Location location) { return (elevation - location.getElevation()); }

    public double getBearingTo(final Location location) {
        return calcBearingInDegree(getLatitude(), getLongitude(), location.getLatitude(), location.getLongitude());
    }
    public double getBearingTo(final double latitude, final double longitude) {
        return calcBearingInDegree(getLatitude(), getLongitude(), latitude, longitude);
    }

    public boolean isZero() { return Double.compare(latitude, 0d) == 0 && Double.compare(longitude, 0d) == 0; }

    public double calcBearingInDegree(final double lat1Deg, final double lon1Deg, final double lat2Deg, final double lon2Deg) {
        double lat1     = Math.toRadians(lat1Deg);
        double lon1     = Math.toRadians(lon1Deg);
        double lat2     = Math.toRadians(lat2Deg);
        double lon2     = Math.toRadians(lon2Deg);
        double deltaLon = lon2 - lon1;
        double deltaPhi = Math.log(Math.tan(lat2 * 0.5 + Math.PI * 0.25) / Math.tan(lat1 * 0.5 + Math.PI * 0.25));
        if (Math.abs(deltaLon) > Math.PI) {
            if (deltaLon > 0) {
                deltaLon = -(2.0 * Math.PI - deltaLon);
            } else {
                deltaLon = (2.0 * Math.PI + deltaLon);
            }
        }
        double bearing = (Math.toDegrees(Math.atan2(deltaLon, deltaPhi)) + 360.0) % 360.0;
        return bearing;
    }

    public String getCardinalDirectionFromBearing(final double bearing) {
        double bear = bearing % 360.0;
        for (CardinalDirection cardinalDirection : CardinalDirection.values()) {
            if (Double.compare(bear, cardinalDirection.from) >= 0 && Double.compare(bear, cardinalDirection.to) < 0) {
                return cardinalDirection.direction;
            }
        }
        return "";
    }


    // ******************** Event handling ************************************
    private ObjectProperty<EventHandler<LocationEvent>> onLocationChanged = new ObjectPropertyBase<EventHandler<LocationEvent>>() {
        @Override public Object getBean() { return Location.this; }
        @Override public String getName() { return "locationChanged";}
    };
    public ObjectProperty<EventHandler<LocationEvent>> onLocationChangedProperty() { return onLocationChanged; }
    public void setOnLocationChanged(EventHandler<LocationEvent> value) { onLocationChanged.set(value); }
    public EventHandler<LocationEvent> getOnLocationChanged() { return onLocationChanged.get(); }

    public void fireLocationEvent(LocationEvent EVENT) {
        Event.fireEvent(EVENT.getTarget(), EVENT);
        final EventHandler<LocationEvent> HANDLER;
        final EventType                   TYPE = EVENT.getEventType();
        if (LocationEvent.LOCATION_CHANGED == TYPE) {
            HANDLER = getOnLocationChanged();
        } else {
            HANDLER = null;
        }
        if (null == HANDLER) return;
        HANDLER.handle(EVENT);
    }


    // ******************** Misc **********************************************
    @Override public boolean equals(final Object OBJECT) {
        if (OBJECT instanceof Location) {
            final Location LOCATION = (Location) OBJECT;
            return (Double.compare(latitude, LOCATION.latitude) == 0 &&
                    Double.compare(longitude, LOCATION.longitude) == 0 &&
                    Double.compare(elevation, LOCATION.elevation) == 0);
        } else {
            return false;
        }
    }

    public String toJSONString() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("name", name);
        jsonObject.put("tst", Long.valueOf(timestamp.getEpochSecond()));
        jsonObject.put("lat", Double.valueOf(latitude));
        jsonObject.put("lon", Double.valueOf(longitude));
        jsonObject.put("alt", Double.valueOf(elevation));
        jsonObject.put("inf", info);
        return jsonObject.toJSONString();
    }

    @Override public String toString() {
        return new StringBuilder().append("Name     : ").append(name).append("\n")
                                  .append("Timestamp: ").append(timestamp).append("\n")
                                  .append("Latitude : ").append(latitude).append("\n")
                                  .append("Longitude: ").append(longitude).append("\n")
                                  .append("Altitude : ").append(String.format(Locale.US, "%.1f", elevation)).append(" m\n")
                                  .append("Info     : ").append(info).append("\n")
                                  .toString();
    }

    @Override public int hashCode() {
        int result;
        long temp;
        result = name != null ? name.hashCode() : 0;
        temp = Double.doubleToLongBits(latitude);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(longitude);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(elevation);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
}
