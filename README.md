# Purpose
The Msb2And Android application is designed to display on line the
data collected by the sensors on the MSB telemetry bus.

The Android device is connected remotely through a
[Multiplex Souffleur](https://www.multiplex-rc.de/produkte/45185-souffleur-deutsch) or
directly to the MSB bus of the receiver (for a test on the ground).
It could also be connected to the COM port of a Multiplex HF module.

You need a USB-OTG cable and a
[Multiplex USB interface](https://www.multiplex-rc.de/produkte/85149-usb-pc-kabel-rx-s-telemetrie-uni).

If the application is authorized to write to the storage system,
it could record files with the same format as the
[Multiplex Flight Recorder](https://www.multiplex-rc.de/produkte/85420-flightrecorder).
Theses files could be used with the
[Msb2Kml application](https://github.com/msb2kml/Msb2Kml).

There is also a possibility to use one of theses files to simulate
a flight.

This Android/Java application could be installed if you have
checked that applications from other sources than Google Play
could be installed.
It should work on Android versions from Jelly Bean (4.1) to
Oreo (8.0).

# Storage
This application request at the first use the right to write to the
internal SD card. This is usually a partition set aside from the
main storage.
It is possible to work without granting this permission: there is then
no saving of data.

For the early versions of Android the path to the SD card was
"/sdcard/" as for any well raised Linux. But now there is a new management
of the storage and the application has to ask to the system what the
path is: it will be something like "/storage/emulated/0/sdcard".

This application has for base on the SD card a directory "**MSBlog**" that
it is able to create. This directory is shared with the other
application of the family: Msb2Kml.

The telemetry data is recorded in files mimicking the ones recorded
by the Multiplex Flight Recorder. Theses files have a name of the
form "**MSB\_xxxx.csv**" where "xxxx" is a unique number from "0000" to
"9999". The files are spread in sub directories named as "**Allexx**"
where "xx" is the 2 first digits of the "xxxx" of the files it contains.
Theses sub directories are created as needed.
The Flight Recorder start the sequence of files at "**MSB\_0000.csv**"
and increments.
This application Msb2And start at "**MSB\_5000.csv**" and increments
until a free name is found.

# Starting
The USB-OTG cable should be connected to the Android device.
For some configurations it is necessary to provide a power supply
to the USB bus.
The Multiplex interface cable should be connected on a side to
the USB-OTG cable and on the other side to the MSB connector
of the Souffleur or to the sensor port of the receiver.

The application works exclusively with a portrait orientation.

Once the application is launched you have to chose between a setup
for the Souffleur of for the receiver. The configuration of the
interface is specific for each possibility.

Previously you could have had a question about allowing access
to the storage.

# Monitoring
Once the data source has been selected you are presented with
a button "**Start**". Listening for data start when this button is pushed.
It is possible to have a question to authorize the access to the USB port.

So long as data is received you have a running count of the seconds
at the top of the screen.
Under this you have a list with an entry for each address in use
on the telemetry bus.
For each entry:
- on the left, the address (0-15) and the minimum value measured.
- in the center, the value currently measured.
- on the right, the maximum value measured.
Scrolling could be used to see the higher addresses.
The screen never sleep in this phase.

If there is an alarm for an address, its number is displayed in red.
If no value is received for some sensor, the last value received
is kept but displayed between parenthesis.

An accessible file "**AddrSens.txt**" on the base directory "**MSBlog**"
could be used to give a name to the addresses
of each sensor. A line in this file of the form "** A:xx;name**" with
"xx" standing for a MSB address (00 to 15) is specifying the name "**name**"
for this sensor. This is compatible with the sister application
Msb2Kml even if other features of this file are not (yet) exploited.
The name is appended in the list to the address.
There is a check box on the start screen to disable the use
of this file.

# Stop monitoring
The application run until the "**Stop**" button is pushed.
If the right to write to storage has not been granted, it is the
end of the application.

On the other case there is the question of the disposition of the files.
The first is the CSV log file (like the ones from the Flight Logger).

The second is the "meta" file that could be used to help the
processing of the log file with the application Msb2Kml.
It contains the date and hour of the flight, the name of the plane
and a comment.

This is how it is presented:
The date and hour of the flight, the full path for each of the 2 files,
the duration of the flight and the number of data points are displayed.
There is a field for the plane name and a field for a comment.
They are filled by default with the values from the previous
use of the application.
Then there are 2 buttons: "**Forget it**" and "**Record it**".
The files are not recorded or erased if "**Forget it**" is pushed and kept
if the other button is pushed.

# Simulation
This could be useful to test the configuration of the application
without the need of a new flight.
When you chose to make a simulation you are presented with a selection
process for the file to use, similar to a File Explorer.
Only the files with a name like "MSB_xxxx.csv" are considered.

While the simulation is running, you could pause it.
When you restart, you could skip some time from the flight.

# Library
This application make use of the library
[UsbSerial](https://github.com/felHR85/UsbSerial).

