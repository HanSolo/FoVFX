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

import javafx.scene.Node;


public class Helper {
    private static final double EARTH_RADIUS = 6_378_137; // in m

    public static final Data calc(final Location camera, final Location motif,
                                  final int focalLengthInMM, final double aperture,
                                  final SensorFormat sensorFormat, final Orientation orientation) {
        final double distance = camera.getDistanceInMeterTo(motif);

        if (focalLengthInMM < 8 || focalLengthInMM > 2400) { throw new IllegalArgumentException("Error, focal length must be between 8mm and 2400mm"); }
        if (aperture < 0.7 || aperture > 99) { throw new IllegalArgumentException("Error, aperture must be between f/0.7 and f/99"); }
        if (distance < 0.01 || distance > 9999) { throw new IllegalArgumentException("Error, distance must be between 0.01m and 9999m"); }

        final double cropFactor = sensorFormat.getCropFactor();

        // Do all calculations in metres (because that's sensible).
        final double focalLength = focalLengthInMM / 1000.0;

        // Let the circle of confusion be 0.0290mm for 35mm film.
        final double circleOfConfusion = (0.0290 / 1000.0) / cropFactor;

        final double hyperFocal = (focalLength * focalLength) / (aperture * circleOfConfusion) + focalLength;
        final double nearLimit  = ((hyperFocal - focalLength) * distance) / (hyperFocal + distance - 2 * focalLength);

        final boolean infinite = (hyperFocal - distance) < 0.00000001;

        final double farLimit      = ((hyperFocal - focalLength) * distance) / (hyperFocal - distance);
        final double frontPercent  = (distance - nearLimit) / (farLimit - nearLimit) * 100;
        final double behindPercent = (farLimit - distance) / (farLimit - nearLimit) * 100;
        final double total         = farLimit - nearLimit;

        final double d = Math.sqrt((sensorFormat.getWidth() * sensorFormat.getWidth()) + (sensorFormat.getHeight() * sensorFormat.getHeight()));
        final double diagonalAngle  = 2.0 * Math.atan(d / (2.0 * focalLengthInMM));
        final double diagonalLength = ((distance * Math.sin(diagonalAngle / 2.0)) / Math.cos(diagonalAngle / 2.0)) * 2.0;
        final double phi            = Math.asin(2.0 / 3.605551);
        double fovWidth;
        double fovHeight;
        if (Orientation.LANDSCAPE == orientation) {
            fovWidth  = Math.cos(phi) * diagonalLength;
            fovHeight = Math.sin(phi) * diagonalLength;
        } else {
            fovWidth  = Math.sin(phi) * diagonalLength;
            fovHeight = Math.cos(phi) * diagonalLength;
        }

        final double halfFovWidth  = fovWidth * 0.5;
        final double halfFovHeight = fovHeight * 0.5;

        final double fovWidthAngle  = 2 * Math.asin(halfFovWidth / Math.sqrt((distance * distance) + (halfFovWidth * halfFovWidth)));
        final double fovHeightAngle = 2 * Math.asin(halfFovHeight / Math.sqrt((distance * distance) + (halfFovHeight * halfFovHeight)));
        final double radius         = Math.sqrt((halfFovWidth * halfFovWidth) + (distance * distance));

        /*
        System.out.println("-----------------------------------");
        System.out.println(focalLengthInMM + "mm f/" + aperture + " at a distance of " + String.format(Locale.US, "%.2f",distance) + "m");
        if (infinite) {
            System.out.println("Far limit        : " + "Infinity");
            System.out.println("Total DoF        : " + String.format(Locale.US, "%.2f",nearLimit) + "m to Infinity");
            System.out.println("DoF in Front     : " + String.format(Locale.US, "%.2f",(distance - nearLimit)) + "m");
            System.out.println("DoF behind       : " + "Infinte");
        } else {
            System.out.println("Far limit        : " + String.format(Locale.US, "%.2f", farLimit) + "m");
            System.out.println("Total DoF        : " + String.format(Locale.US, "%.2f", total) + "m");
            System.out.println("DoF in Front     : " + String.format(Locale.US, "%.2f", (distance - nearLimit)) + "m (" + frontPercent + "%)");
            System.out.println("DoF behind       : " + String.format(Locale.US, "%.2f",(farLimit - distance)) + "m (" + behindPercent + "%)");
        }
        System.out.println("Hyper focal dist : " + String.format(Locale.US, "%.2f",hyperFocal) + "m");
        System.out.println("Near limit       : " + String.format(Locale.US, "%.2f",nearLimit) + "m");
        System.out.println("Distance to Motif: " + String.format(Locale.US, "%.2f",distance) + "m");
        System.out.println("Diagonal Angle   : " + String.format(Locale.US, "%.2f", Math.toDegrees(diagonalAngle)));
        System.out.println("Diagonal Length  : " + String.format(Locale.US, "%.2f",diagonalLength) + "m");
        System.out.println("FoV Width        : " + String.format(Locale.US, "%.2f", fovWidth) + "m");
        System.out.println("FoV Width  Angle : " + Math.toDegrees(fovWidthAngle));
        System.out.println("FoV Height       : " + String.format(Locale.US, "%.2f", fovHeight) + "m");
        System.out.println("FoV Height Angle : " + String.format(Locale.US, "%.2f", Math.toDegrees(fovHeightAngle)));
        System.out.println("Radius           : " + radius + "m");
        System.out.println("-----------------------------------");
        */

        return new Data(camera, motif, focalLengthInMM, aperture, sensorFormat, orientation, infinite, hyperFocal, nearLimit, farLimit, frontPercent, behindPercent, total, diagonalAngle, diagonalLength, fovWidth, fovWidthAngle, fovHeight, fovHeightAngle, radius);
    }

    public static final double clamp(final double min, final double max, final double value) {
        if (value < min) { return min; }
        if (value > max) { return max; }
        return value;
    }

    public static final double distance(final double x1, final double y1, final double x2, final double y2) {
        final double ac = Math.abs(y2 - y1);
        final double cb = Math.abs(x2 - x1);
        return Math.hypot(ac, cb);
    }

    public static final double[] rotatePointAroundRotationCenter(final double x, final double y, final double rX, final double rY, final double rad) {
        final double sin = Math.sin(rad);
        final double cos = Math.cos(rad);
        final double dX  = x - rX;
        final double dY  = y - rY;
        final double nX  = rX + (dX * cos) - (dY * sin);
        final double nY  = rY + (dX * sin) + (dY * cos);
        return new double[] { nX, nY };
    }

    public static final void updateTriangle(final Location camera, final Location motif, final int focalLengthInMM, final double aperture, final SensorFormat sensorFormat, final Orientation orientation, final Triangle triangle) {
        Data           data           = calc(camera, motif, focalLengthInMM, aperture, sensorFormat, orientation);
        final double[] trianglePoints = calcTrianglePoints(data);
        triangle.setX1(trianglePoints[0]);
        triangle.setY1(trianglePoints[1]);
        triangle.setX2(trianglePoints[2]);
        triangle.setY2(trianglePoints[3]);
        triangle.setX3(trianglePoints[4]);
        triangle.setY3(trianglePoints[5]);
    }

    public static final double[] calcTrianglePoints(final Data data) {
        final double halfFovWidthAngle = data.fovWidthAngle / 2.0;
        double[] p2 = calcCoord(data.camera, data.radius, -halfFovWidthAngle);
        double[] p3 = calcCoord(data.camera, data.radius, halfFovWidthAngle);
        return new double[] { data.camera.getLatitude(), data.camera.getLongitude(), p2[0], p2[1], p3[0], p3[1] };
    }

    public static final void updateTrapezoid(final Location camera, final Location motif, final int focalLengthInMM, final double aperture, final SensorFormat sensorFormat, final Orientation orientation, final Trapezoid trapezoid) {
        Data           data            = calc(camera, motif, focalLengthInMM, aperture, sensorFormat, orientation);
        final double[] trapezoidPoints = calcTrapezoidPoints(data);
        trapezoid.setX1(trapezoidPoints[0]);
        trapezoid.setY1(trapezoidPoints[1]);
        trapezoid.setX2(trapezoidPoints[2]);
        trapezoid.setY2(trapezoidPoints[3]);
        trapezoid.setX3(trapezoidPoints[4]);
        trapezoid.setY3(trapezoidPoints[5]);
        trapezoid.setX4(trapezoidPoints[6]);
        trapezoid.setY4(trapezoidPoints[7]);
    }

    public static final double[] calcTrapezoidPoints(final Data data) {
        final double halfFovWidthAngle = data.fovWidthAngle / 2.0;
        final double radius1           = data.nearLimit / Math.cos(halfFovWidthAngle);
        final double radius2           = data.farLimit / Math.cos(halfFovWidthAngle);

        final double p1[] = calcCoord(data.camera, radius1, -halfFovWidthAngle);
        final double p2[] = calcCoord(data.camera, radius2, -halfFovWidthAngle);
        final double p3[] = calcCoord(data.camera, radius2, halfFovWidthAngle);
        final double p4[] = calcCoord(data.camera, radius1, halfFovWidthAngle);

        return new double[] { p1[0], p1[1], p2[0], p2[1], p3[0], p3[1], p4[0], p4[1] };
    }

    public static final double calculateBearing(final Location location1, final Location location2) {
        var lat1    = Math.toRadians(location1.getLatitude());
        var lon1    = location1.getLongitude();
        var lat2    = Math.toRadians(location2.getLatitude());
        var lon2    = location2.getLongitude();
        var dLon    = Math.toRadians(lon2 - lon1);
        var y       = Math.sin(dLon) * Math.cos(lat2);
        var x       = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1) * Math.cos(lat2) * Math.cos(dLon);
        var bearing = (Math.toDegrees(Math.atan2(y, x)) + 360) % 360;
        return bearing;
    }

    public static final double[] calcCoord(final Location start, final double distance, final double bearing) {
        double lat1   = Math.toRadians(start.getLatitude());
        double lon1   = Math.toRadians(start.getLongitude());
        double radius = distance / EARTH_RADIUS;

        double lat2 = Math.asin(Math.sin(lat1) * Math.cos(radius) + Math.cos(lat1) * Math.sin(radius) * Math.cos(bearing));
        double lon2 =lon1 + Math.atan2(Math.sin(bearing) * Math.sin(radius) * Math.cos(lat1), Math.cos(radius) - Math.sin(lat1) * Math.sin(lat2));
        lon2 = (lon2 + 3 * Math.PI) % (2 * Math.PI) - Math.PI;

        return new double[] { Math.toDegrees(lat2), Math.toDegrees(lon2) };
    }

    public static final void enableNode(final Node node, final boolean enable) {
        node.setVisible(enable);
        node.setManaged(enable);
    }
}
