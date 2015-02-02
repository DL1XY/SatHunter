
ATPDEC 1.6 (c) Thierry Leconte 2004

DESCRIPTION

Atpdec is an open source program that decodes images transmitted by POES 
NOAA weather satellite series.
These satellites transmit continuously, among other things, medium 
resolution images of the earth on 137Mhz.
These transmissions could be easily received with an inexpensive antenna 
and dedicated receiver.
Output from such a receiver, is an audio signal that could be recorded 
into a soundfile with any soundcard.

Atpdec will convert these sounfiles into .png images.

For each soundfile up to 6 images could be generated :

    1. Raw image : contains the 2 transmitted channel images + telemetry 
and synchro pulses.
    2. Calibrated channel A image
    3. Calibrated channel B image
    4. Temperature compensed I.R image
    5. False color image

Input soundfiles must be mono signal sampled at 11025 Hz.
Atpdec use libsndfile to read soundfile, so any sound file format supported by libsndfile
could be read.(Only tested with .wav file).


USAGE

Atpde don't have any fancy GUI, so you must open a command shell window,
go to atpdec dir and use the following command line :

atpdec [options] soundfiles ...

OPTIONS

	-i [r|a|b|c|t]
	Toggle raw (r) , channel A (a) , channel B (b) , false color (c) ,
	temperature (t).
	Default : "ac"

	-d directory
	Optional images destination directory.
	Default : soundfile directory.

	-s n
	Satellite ident : n=0 :NOOA-15 n=1 :NOAA-17 
	Used for Temperature compensation.
	Default :  NOAA17

	-c conf_file
	Use configuration file for false color generation.
	Default : Internal parameters.

OUTPUT

Generated image are in png format, 8bits greyscale for raw and channel A|B images,
24bits RVB for false color.

Image names are soundfilename-x.png, where x is :
        -r for raw images
        -satellite instrument number (1,2,3A,3B,4,5) for channel A|B images
        -c for false colors.

EXAMPLE

atpdec -d image -i ac *.wav

Will process all .wav files in the current directory, generate only channel A and false color images and put them in the image directory.

SOURCES

Atpdec sources are available here : http://sourceforge.net/projects/atpdec

