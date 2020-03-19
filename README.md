## FoVFX
A little JavaFX tool to visualize the field of view incl. the depth of focus for different lenses and sensor formats.

## Screenshot
![Overview](https://raw.githubusercontent.com/HanSolo/FoVFX/master/FoVFX.jpg)

## Information
####Maps
- Street
- Satellite
- Hybrid
- Traffic

####The tool can be used to plan a photo shot incl. data like
- Distance to motif
- Width of field of view at motif distance
- Height of field of view at motif distance
- Hyperfocal length
- Near limit
- Far limit
- Total depth of field

####The data will be visualized as follows
- Magenta triangle shows the field of view
- Cyan triangle shows the depth of field
- If elevation data is available it can be shown in a popup


##Attention (Things that might change in the future)
- At the moment the sensor format is fixed to full frame but can be switched in code.
- The lenses used (Lenses.class) are the lenses for my Nikon camera.
- Location/Lens combinations cannot be stored at the moment. 
