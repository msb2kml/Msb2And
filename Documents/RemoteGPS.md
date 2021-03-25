# Remote GPS

The GPS module puts on the data bus, beside the telemetry data,
the location data (latitude, longitude and altitude).
This data is not transmitted through the telemetry protocol and
thus is not usually available for the Msb2And application.

But it is possible to reconstruct the location data from the telemetry data.
The following explanations are relative to this reconstruction.

## Preparation of the GPS module

The Multiplex Launcher program should have been used to attribute
a MSB address to three essentials measurements:

+ Azimuth: angle (degree) from the North under which the pilot sees the plane.
+ Distance 2D: distance (m) from the pilot to the projection of the plane
 on the ground (use the expert mode).
+ Height: height (m) relative to the pilot.

Together theses measurements gives the position of the plane in
a cylindrical system of coordinates centered on the pilot (in reality
the center is the place of the first valid fix by the GPS module).

Theses measures could be used in a formula to convert them to a geographic
position if the location of the pilot is known.

## Condition

The reconstruction is performed by the function "=GPS" in
the file AddrSens.txt: see "**AdvancedUse**".  
Another condition is that a start location of the flight has been
provided with the appropriate button of the initial menu. 

## Pilot location

A table of known pilot locations is maintained in a file **StartGPS.gpx**
in the MSBlog directory.  
The location is selected by the name that reference it.    
A named location could have been prepared before or it could be specified
just before the flight.

There are three methods available to record such a location:

+ use the GPS of the tablet or smartphone.
+ write the location known by another mean (GPS, map, ...).
+ copy the location from a previously processed flight with
 the logger on board.

See the screenshot [Screenshot\_Locate](Screenshots/Screenshot_Locate.jpg).

The file StartGPS.gpx could be opened by an application such as
[Vtrk](https://github.com/msb2kml/Vtrk) to check the locations on a map.  
This application could also be used to add locations to this file.

## Location with GPS of device

You could be asked to modify two settings:

+ Enable fine location: allow the GPS to work.
+ Give the application the access to the location.

It takes some time for the GPS to obtain a fix.
When it is acquired, you are shown the coordinates with the estimated
accuracy.  
You should give a meaningful name to this location before accepting it.  
The default name is a combination of the date and hour.

## Entering a location

You are presented with a form to enter the latitude and longitude in 
decimal degrees, and the altitude in meters.  
Also, a meaningful and unique name. The default name is a combination
of the date and hour.  
If the location you have is in degrees, minutes, seconds you could
convert it with some utilities like
[RapidTables](https://www.rapidtables.com/convert/number/degrees-minutes-seconds-to-degrees.html).

## Copying a location

You are first presented a list of the previous flight for which there
exists a GPX file.  
You select one on the basis of the displayed comments.

The more time the GPS module has had to follow the satellites and the better the
accuracy of the fix.  
If it is assumed that the plane has returned exactly where
its flight has begun, the last fix recorded could be of a better quality.  
Thus, you have the choice to use the first or the last fix.  
You should enter a unique and meaningful name but the default 
name is a combination of the date and hour when the fix has been taken.

## End of flight

If the location has been reconstructed and the map displayed,
the last known position at the end of the application and the start
position are compared. If their distance is greater than 50 m,
there is a suspicion of a lost plane.  
It is proposed to the user to save the last position in the file
StartGPS.gpx. If this is done, Msb2Map submit to the Android system
a request to launch an application displaying this location on a map.
On the technical side it is an intent with a "geo" URI.  
The system presents to the user a list of the applications able to
perform this task. This list could contain the Vtrk application
and also any guiding application installed on the device.  
In the hope that the retrieval of the plane should be eased...


