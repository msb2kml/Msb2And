# Remote GPS

The GPS module put on the data bus, beside the telemetry data,
the location data (latitude, longitude and altitude).
This data is not transmitted through the telemetry protocol and
thus is not usually available for the Msb2And application.

But it is able to reconstruct the location data from the telemetry data.
The following explanations are relative to this reconstruction.

# Preparation of the GPS module

The Multiplex Launcher program should have been used to attribute
a MSB address to three essentials measurements:

+ Azimuth: angle (degree) from the North under which the pilot see the plane.
+ Distance 2D: distance (m) from the pilot to the projection of the plane
 on the ground.
+ Height: height (m) relative to the pilot.

Together theses measurements gives the position of the plane in
a cylindrical system of coordinates centered on the pilot (in reality
the center is the place of the first valid fix by the GPS module).

Theses measures could be used in a formula to convert them to a common
location if the location of the pilot is known.

# Pilot location

Each known pilot locations is referenced for the application by a name.  
This name is a label for a location stored in a file
StartGPS.gpx in the MSBlog directory.  
The file StartGPS.gpx is shared with the application Msb2Kml
and could be opened by an application like Msb2Map
to check the locations on a map.

There are three methods available to record such a location:

+ use the GPS of the tablet or smartphone.
+ write the location known by another mean (GPS, map, ...).
+ copy the location from a previously processed flight with
 the logger on board.

See the screenshot [Screenshot_Locate](Screenshots/Screenshot_Locate.jpg).

# Condition

The reconstruction is performed by the function "=GPS" in
the file AddrSens.txt: see "**AdvancedUse**".  
If the file AddrSens.txt is used, a menu is presented.  
On this menu you have all the locations known in the file StartGPS.gpx
and an entry for each of the previously cited methods.  
No reconstruction is performed if this menu is canceled.

# Location with GPS of device

You could be asked to modify two settings:

+ Enable fine location: allow the GPS to work.
+ Give the application the access to the location.

It takes some time for the GPS to obtain a fix.
When it is acquired, you are shown the coordinates with the estimated
accuracy.  
You should give a meaningful name to this location before accepting it.  
The default name is a combination of the date and hour.

# Entering a location

You are presented with a form to enter the latitude and longitude in 
decimal degrees, and the altitude in meters.  
Also, a meaningful and unique name. The default name is a combination
of the date and hour.  
If the location you have is in degrees, minutes, seconds you could
convert it with some utilities like
[RapidTables](https://www.rapidtables.com/convert/number/degrees-minutes-seconds-to-degrees.html).

# Copying a location

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

