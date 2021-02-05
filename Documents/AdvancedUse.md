# Advanced use: derived values

Advanced use is introduced when the file **AddrSens.txt** contains
formulas. An example is in the same directory as this text.

Purpose: to compute for each sample some secondary data
derived from the raw sensor data.  
The list that is displayed shows first the raw data and under this,
the secondary data.  
The format for this last is a bit different: on the left there is the
heading and the minimum value, on the right there is
the identity of the function and the maximum value.  
There is no indication for a missing value (computation is always
done on the last received sample).

### Principle

A "tool box" of functions is available for this. Each function
is identified by a name starting with an equal "**=**", it takes
some parameter(s) and acts on some variable(s).  
The first field of a line in AddrSens.txt starts with the name
of the function followed by each parameter and variable
separated by commas "**,**".  
The variables are identified by the character they have received
(3thd field) preceded by a dollar "**$**" character.  
A variable could be defined before or after its use in the file.  
If a variable is not defined, any function that use it is discarded
and also recursively any function using a variable defined
by this function.  
If the name is not recognized, the line is ignored.

The computation is performed in the order or definition in the file.  
The raw data and the computed data are kept in a registry between
samples.  
If a variable is used before it is declared, the value
that it has is thus the one for the previous sample: it is a
memory function.

The file AddrSens.txt is compatible with the application
[Msb2Kml](https://github.com/msb2kml/Msb2Kml)
and most of the functions are shared.

### Special variables

There is one variable name that does not need to be defined and is
computed by the program if it is used: "**\#**".  
It is the accumulated traveled distance (km) between points of
the GPS track, if the track is reconstructed.

The variable named "**%**" could be defined and it has a special
signification. It should be in the range 0.0 to 100.0.  
There is a relation between the value of this variable
and a color: from BLUE (0%) through GREEN to RED (100%) in 12 levels.  
The color is used for the segments of the track on the map.


### List of functions

Each function is presented as it could be used on a line of the file.

#### Colorization of track on the map

    =COL,-1.0,1.0,$b;-;     %

+ $b is the vario.

The range of values (-1.0 - 1.0) of the variable is converted
to the range (0.0 - 100.0). This is attributed the name "%"
to be used for colorization.  
Instead of vario, it could be height, speed, ...

#### Battery internal resistance

    =BIR,1,$i,$V,$v;mOhm  

+ $i is the current. 
+ $v is the current voltage.
+ $V is the last voltage when the current was less than the threshold
 parameter (1 ampere in this case). See =HVL for $V.  
Hint: if there is a too high spike when the current is cut off,
specify a higher threshold.   

See the screenshot [Screenshot\_Prop](Screenshots/Screenshot_Prop.jpg).


#### Summation of all sinking of the flight

    =CUMN,$d,$u;Deniv-;     u

+ $d is the difference of height
from the previous sample (see =DIFF).
+ $u is the summation itself.  
It keeps its value if $d is positive otherwise $d is subtracted
from $u.

#### Summation of all rising of the flight

    =CUMP,$d,$U;Deniv+;     U

+ $d is the difference of height
from the previous sample (see =DIFF).
+ $U is the summation itself.  
It keeps its value if $d is negative otherwise $d is added to $U.

#### Difference of values between successive samples

    =DIF,$t,$T;-;           s

Here the values are the times.

+ $t is the current value.
+ $T is the memorized value (see =MEM).

#### Glide ratio

    =GLR,0.05,$#,$a;G.Ratio

+ $# is the traveled distance computed from the GPS data.
+ $a is the altitude measured by the GPS.

The glide ratio is not computed (remains null) if the altitude
is not decreasing while the distance traveled has augmented
by the parameter: here 0.05 km (50 m).  
The parameter should not be less than 0.03 km.  
Glide ratio is defined as the ratio of the distance traveled
horizontally to the distance traveled vertically at constant
airspeed in still air.  
These conditions could rarely be maintained for long in a typical
flight of our models. You have to apply some judgment about this parameter.  
See the screenshot [Screenshot\_Slope](Screenshots/Screenshot_Slope.jpg).


#### Voltage with no load

    =HVL,1,$i,$v,$V;-;      V

+ $i is the current. 
+ $v is the current voltage.
+ $V is the memorized no load voltage.

Keeps its value if the current is lower than the threshold: here 1 A.
Otherwise follows the current voltage.  

#### Memorize the current value for the next sample

    =MEM,$t;-;              T

+ $t is the value
to memorize (here the time).

#### Time scale covering only the working of the motor

    =MOT,1,$i,$s,$M;Motor s;M

+ $i is the current.
+ $s is the difference of time from the previous sample (see =DIFF).
+ $M is the new time scale itself.

If the current is above the threshold (here 1 A) the difference
of time is added to the time scale; otherwise it is not modified.  
See the screenshot [Screenshot\_Prop](Screenshots/Screenshot_Prop.jpg).



#### Energy (as limited for F5B and F5D)

    =NRJ,$w,$s,$j;W.min;   j
+ $w is the power (see =PROD).
+ $s is the difference of time from the previous sample (see =DIFF).
+ $j is the energy itself.

The product of the power by the delta time is added to the energy.
The appropriate factor is applied for a reading in Watt\*minute.  
See the screenshot [Screenshot\_Prop](Screenshots/Screenshot_Prop.jpg).


#### Product (here power) of 2 values

    =PROD,$i,$v;Watt;       w
+ $i is here the current.
+ $v is here the voltage.  

See the screenshot [Screenshot\_Prop](Screenshots/Screenshot_Prop.jpg).

#### Smoothing of a value (here the vario)

    =SMTH,0.1,$b,$B;fVario; B

+ $b is the value to smooth
(here the vario).
+ $B is the smoothed value.

The current value is added with some weight (here 0.1) to the
memorized value multiplied by the complementary weight (here 0.9). 


#### Time scale covering only the flight without motor

    =SOA,1,$i,$s,$m;Soar s; m
+ $i is the current.
+ $s is the difference of time from the previous sample (see =DIFF).
+ $m is the new time scale itself.

If the current is below the threshold (here 1 A) the difference
of time is added to the time scale; otherwise it is not modified.  
See the screenshot [Screenshot\_Prop](Screenshots/Screenshot_Prop.jpg).

#### Cumulative traveled distance

    =TRV,$K,$#;Km

+ $# is internally computed as this
distance from the successive positions on the GPS track.
+ $K is the speed from the GPS sensor: it is not used in the
computation but its inclusion assures than this function
is not invoked if the GPS is not present.

Instead of $K you could use $G (see below) to be assured than
the track is reconstructed.  
See the screenshot [Screenshot\_Slope](Screenshots/Screenshot_Slope.jpg).


#### Remote GPS reconstruction

    =GPS,$<,$/,$|;GPS;      G
    
+ $< is the azimuth from the pilot to the plane computed by
 the GPS module.
+ $/ is the distance 2D from the pilot to the plane (module GPS).
+ $| is the altitude measured by the GPS.

This computes the real location (lat, long, alt) from the telemetry
if the starting location is provided.  
See the file "RemoteGPS".  
The value that could be printed is only 0.0 or 1.0: 1 if there has
been no conversion error; otherwise 0.

### Altitude

   =ALT,$G;Altitude

+ $G is the result of the reconstruction of the GPS location.
 It is not used otherwise than to be assured that the reconstruction
 has been provided.

This extracts the altitude (height above sea level) from the current
GPS location.  
See the screenshot [Screenshot\_Slope](Screenshots/Screenshot_Slope.jpg).

### Latitude

   =LAT,$G;Latitude

+ $G is the result of the reconstruction of the GPS location.
 It is not used otherwise than to be assured that the reconstruction
 has been provided.

This extracts the latitude from the current GPS location.  
See the screenshot [Screenshot\_Slope](Screenshots/Screenshot_Slope.jpg).

### Longitude

   =LON,$G;Longitude

+ $G is the result of the reconstruction of the GPS location.
 It is not used otherwise than to be assured that the reconstruction
 has been provided.

This extracts the longitude from the current GPS location;  
See the screenshot [Screenshot\_Slope](Screenshots/Screenshot_Slope.jpg).

### Distance to a turn point

   =DIST,Turnpoint1,$G;Turnpoint 1

+ "Turnpoint1" stands for a location named in the file StartGPS.gpx
+ $G is the result of the reconstruction of the GPS location.
 It is not used otherwise than to be assured that the reconstruction
 has been provided.

This computes the distance in meters (rounded) from the plane
to the location of "Turnpoint1".  
Of course, this function could be invoked for several locations.  
The location of turn points could be entered in the file
StartGPS.gpx with the help of the application
[Msb2Kml](https://github.com/msb2kml/Msb2Kml) or the application
[Vtrk](https://github.com/msb2kml/Vtrk/).   
This function is ignored if the distance from "Turnpoint1" to the flight start
location is greater than 10 Km.

### Normalization of the measures from a sensor

   =NRM,-1,-0.8,$Z;Acceleration

+ The first parameter (-1) is a multiplication factor.
+ The second parameter (-0.8) is added to the result.
+ $Z is the reading to process.

This computation is performed: ( $Z * (param 1) ) + (param 2)  
This could be used to revert the sign for a G-Sensor and introduce
a compensation for the gravity.  
It could also be used to convert from Centigrade to Fahrenheit.

### Vectorization

   =VCT,$X,$Y,$Z;Vect. Acc.  
   =VCT,$X,$Z;Vect. Acc.

+ $X is a measure along an axis.
+ $Y is a measure along an axis perpendicular to the previous.
+ $Z is a measure along an axis perpendicular to the 2 previous.

The result is the square root of the sum of the squares of the measures.  
This function could be used with 2 or 3 parameters.


### Note

The functions like =DIFF that make use of current and previous
values should be at the head of the file.
The functions like =MEM of =HVL that store values for the next sample
should be at the bottom of the file.

### Note about the functions relative to GPS

A function that is activated despite missing data has for effect
a line in the listing filled with "0.0". 
 
The functions =ALT, =LAT, =LON, =DIST and =TRV depends
on the existence of some data that is not used in the computation.
This is to prevent them to be activated if the GPS track is not
available.  

The GPS speed ($K in the example) is an indication that the GPS
module is present, whatever the method to obtain the track.  
The success of the reconstruction ($G in the example) exists
only when the track is reconstructed.  
Functions =ALT, =LAT an =LON are used mainly for testing.

