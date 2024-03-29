# Media Timelene Sorter

## Purpose
This is a command line tool to join several sequences (bunches, timelines)
of image and video files into one sequence of files with names, sorted chronologically.
These are usually files, created at different devices 
at the same time period, e.g. during two weeks...
 
I use it for the last 14 years to organize photos and videos that we and our friends
shot during our vacation trips. We ride and walk together, take similar pictures
of the same wonders and of each other. On our return home we have several bunches of files,
often thousands of files in each bunch.

It's very inconvenient to watch each bunch of files separately, especially when 
you are looking for the best shot of some place or an event.

This tool allows creating one bunch out of many, as if all shots were made by one person
using one device. In the end we can see all these files in one go chronologically. 

## Files preparation

Each bunch of files should be prepared separately.

1. Assign unique alphabetic *key* (usually two or three letters are enough) 
to each bunch of files. It should remind you of an origin of each file.  
Prepend each file name (of both image and video files) with "keyNNNN-", where "NNNN" is 
a chronological sequence number of a file in this bunch. I usually do this using 
[Total Commander](https://www.ghisler.com/) 's "Multi-Rename Tool" feature. 
This is easy, because files shot by one device
usually have names that are already sorted chronologically OR have consistent 
file modification dates.
   
So you will have bunches e.g. like this:

yv001-DSC-9031.jpg  
yv002-DSC-9035.jpg  
yv003-MOV-5588.mts  
yv004-DSC-9036.jpg  
...

2. Set file date of each image file to its EXIF's Date Taken. 
I usually use [GeoSetter](https://geosetter.de) tool for this. 
Quite often I have to use "Time shift" feature of GeoSetter when photos, 
created on some device, have incorrect Date/Time.

Actually, this is a case for all devices that are not connected to the Internet. 
This also often occurs when travelling to the other Time Zone.

3. Copy files from all bunches to the same folder. 

## Usage
1. Launch  media-timeline-sorter tool in a folder with all file copied. 
So it prepends each file name with a sequence number, 
creating a joined sequence, e.g.:

0001-es001-IMG_20200731_171525.jpg  
0002-yv001-DSC-9031.jpg  
0003-es002-IMG_20200731_171604.jpg  
0004-yv002-DSC-9035.jpg  
0005-yv003-MOV-5588.mts  
0006-yv004-DSC-9036.jpg  
...

In a case files weren't renamed, see its output with errors (these are usually 
inconsistencies between file times and file numbers in prefixes...)
 
Command line example:

java -cp c:\java\app\MediaTimelineSorter\target\media-timeline-sorter-1.0.3.jar com.yurivolkov.mediatimelinesorter.Main 1>c:\Users\yuri\Downloads\temp\mediatimeline-005.txt 2>&1

\- here I redirect output to a file, so it's easy to inspect results and figure out problems.

Relaunch the tool after any correction, no manual renaming is needed. 