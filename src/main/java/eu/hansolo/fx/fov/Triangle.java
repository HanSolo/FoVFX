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

import java.util.List;


public class Triangle {
    private double x1;
    private double y1;
    private double x2;
    private double y2;
    private double x3;
    private double y3;


    public Triangle() {
        this(0, 0, 0, 0, 0, 0);
    }
    public Triangle(final double[] points) {
        this(points[0], points[1], points[2], points[3], points[4], points[5]);
    }
    public Triangle(final double x1, final double y1, final double x2, final double y2, final double x3, final double y3) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
        this.x3 = x3;
        this.y3 = y3;
    }


    public double getX1() {
        return x1;
    }
    public void setX1(final double x1) {
        this.x1 = x1;
    }

    public double getY1() {
        return y1;
    }
    public void setY1(final double y1) {
        this.y1 = y1;
    }

    public double getX2() {
        return x2;
    }
    public void setX2(final double x2) {
        this.x2 = x2;
    }

    public double getY2() {
        return y2;
    }
    public void setY2(final double y2) {
        this.y2 = y2;
    }

    public double getX3() {
        return x3;
    }
    public void setX3(final double x3) {
        this.x3 = x3;
    }

    public double getY3() {
        return y3;
    }
    public void setY3(final double y3) {
        this.y3 = y3;
    }

    public double[] getPoints() {
        return new double[] { x1, y1, x2, y2, x3, y3 };
    }
    public List<Double> getPointsAsList() {
        return List.of(x1, y1, x2, y2, x3, y3);
    }

    @Override public String toString() {
        return new StringBuilder().append("P1 [").append(x1).append(", ").append(y1).append("]")
                                  .append("P2 [").append(x2).append(", ").append(y2).append("]")
                                  .append("P3 [").append(x3).append(", ").append(y3).append("]")
                                  .toString();
    }
}
