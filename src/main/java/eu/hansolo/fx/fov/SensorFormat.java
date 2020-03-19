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

public enum SensorFormat {
    MEDIUM_FORMAT("Medium Format", 53.7, 40.2, 0.64),
    FULL_FORMAT("Full Format", 36, 23.9, 1.0),
    APS_H("APS-H", 27.9, 18.6, 1.29),
    APS_C("APS-C", 23.6, 15.8, 1.52),
    APS_C_CANON("APS-C Canon", 22.2, 14.8, 1.6),
    MICRO_FOUR_THIRDS("Micro 4/3", 17.3, 13.0, 2.0);

    private final String name;
    private final double width;
    private final double height;
    private final double cropFactor;


    SensorFormat(final String name, final double width, final double height, final double cropFactor) {
        this.name       = name;
        this.width      =  width;
        this.height     = height;
        this.cropFactor = cropFactor;
    }


    public final String getName() { return name; }

    public final double getWidth() { return width; }

    public final double getHeight() { return height; }

    public final double getCropFactor() { return cropFactor; }
}
