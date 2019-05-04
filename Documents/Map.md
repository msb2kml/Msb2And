# Displaying the position of the plane on a map

For some activities like the GPS triangle it is useful to see the
real time position of the plane on a map.  
This is possible with three prerequisites:

+ The file AddrSens.txt is used (advanced use).
+ The GPS data is received and processed.
+ The application [Msb2Map](https://github.com/msb2kml/Msb2Map)
 is installed.

# Launching the map

While the parameters are displayed, taping on any parameter,
direct or computed, launch the **Msb2Map** application.  
**Msb2And** is still running in the background.  
It is using an Android mechanism of internal broadcasting to
send the geographic position of the plane and the value of the
parameter that has been taped.  
The color defined by the variable "%" in AddrSens.txt is
sent along the position to colorize the tail.

**Msb2Map** receives this data and display it while running.  
If it is terminated, **Msb2And** come back to the foreground and another
parameter could be used.

It is still possible to scroll the display of Msb2And by dragging
with the finger.

# Why two applications?

**Msb2And**

+ Not everybody need a map while flying.
+ Access to the location service (GPS) of the device is needed
 to enter the starting location.
+ Access to the Internet is not needed.

**Msb2Map**

+ Access to the location service is not needed.
+ Access to the Internet is needed to fetch the map.
+ There could be other uses in the future.  

