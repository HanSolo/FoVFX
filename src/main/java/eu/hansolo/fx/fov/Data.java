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

import java.util.Locale;


public class Data {
    public final Location     camera;
    public final Location     motif;
    public final double       focalLength;
    public final double       aperture;
    public final double       distance;
    public final SensorFormat sensorFormat;
    public final Orientation  orientation;
    public final boolean      infinite;
    public final double       hyperFocal;
    public final double       nearLimit;
    public final double       farLimit;
    public final double       frontPercent;
    public final double       behindPercent;
    public final double       total;
    public final double       diagonalAngle;
    public final double       diagonalLength;
    public final double       fovWidth;
    public final double       fovWidthAngle;
    public final double       fovHeight;
    public final double       fovHeightAngle;
    public final double       radius;
    public final double       angleBetweenCameraAndMotif;
    public final double       dofInFront;
    public final double       dofBehind;


    public Data(final Location camera, final Location motif, final double focalLength, final double aperture, final SensorFormat sensorFormat, final Orientation orientation, final boolean infinite, final double hyperFocal, final double nearLimit, final double farLimit,
                final double frontPercent, final double behindPercent, final double total,
                final double diagonalAngle, final double diagonalLength,
                final double fovWidth, final double fovWidthAngle,
                final double fovHeight, final double fovHeightAngle,
                final double radius) {
        this.camera                     = camera;
        this.motif                      = motif;
        this.focalLength                = focalLength;
        this.aperture                   = aperture;
        this.distance                   = camera.getDistanceInMeterTo(motif);
        this.sensorFormat               = sensorFormat;
        this.orientation                = orientation;
        this.infinite                   = infinite;
        this.hyperFocal                 = hyperFocal;
        this.nearLimit                  = nearLimit;
        this.farLimit                   = infinite ? 10000 : farLimit;
        this.frontPercent               = frontPercent;
        this.behindPercent              = behindPercent;
        this.total                      = infinite ? 10000 : total;
        this.diagonalAngle              = diagonalAngle;
        this.diagonalLength             = diagonalLength;
        this.fovWidth                   = fovWidth;
        this.fovWidthAngle              = fovWidthAngle;
        this.fovHeight                  = fovHeight;
        this.fovHeightAngle             = fovHeightAngle;
        this.radius                     = radius;
        this.angleBetweenCameraAndMotif = Math.toRadians(Helper.calculateBearing(camera, motif));
        this.dofInFront                 = distance - nearLimit;
        this.dofBehind                  = infinite ? 10000 : farLimit - distance;
    }


    @Override public String toString() {
        return new StringBuilder().append("Sensor      : ").append(sensorFormat.getName()).append("\n")
                                  .append("Focal length: ").append(String.format(Locale.US, "%.0f", focalLength)).append(" mm\n")
                                  .append("Aperture    : f").append(String.format(Locale.US, "%.1f", aperture)).append("\n")
                                  .append("Orientation : ").append(orientation.getName()).append("\n")
                                  .append("-----------------------------\n")
                                  .append("Distance    : ").append(String.format(Locale.US, "%.2f", distance)).append(" m\n")
                                  .append("FoV width   : ").append(String.format(Locale.US, "%.2f", fovWidth)).append("(").append(String.format(Locale.US, "%.2f\u00b0", Math.toDegrees(fovWidthAngle))).append(")").append("\n")
                                  .append("FoV height  : ").append(String.format(Locale.US, "%.2f", fovHeight)).append("(").append(String.format(Locale.US, "%.2f\u00b0", Math.toDegrees(fovHeightAngle))).append(")").append("\n")
                                  .append("-----------------------------\n")
                                  .append("Hyperfocal  : ").append(String.format(Locale.US, "%.2f", hyperFocal)).append(" m\n")
                                  .append("Near limit  : ").append(String.format(Locale.US, "%.2f", nearLimit)).append(" m\n")
                                  .append("Far  limit  : ").append(String.format(Locale.US, "%.2f", farLimit)).append(" m\n")
                                  .append("In front    : ").append(String.format(Locale.US, "%.2f", dofInFront)).append(" m\n")
                                  .append("Behind      : ").append(String.format(Locale.US, "%.2f", dofBehind)).append(" m\n")
                                  .append("Total       : ").append(String.format(Locale.US, "%.2f", total)).append(" m\n")
                                  .append("-----------------------------\n")
                                  .toString();
    }
}
