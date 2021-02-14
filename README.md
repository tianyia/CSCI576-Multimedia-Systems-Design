# USC_CSCI576_Multimedia-Systems-Design
Hw1:
To run the code from command line, first compile with:

>> javac ImageDisplay.java

and then, you can run it to take in 5 parameters "pathToRGBVideo", Y, U, V and Q. Note that Y, U, V, Q need to be integers.

>> java ImageDisplay pathToRGBVideo Y U V Q


Example: suppose Image1.rgb is in the same directory,

>> javac ImageDisplay.java
>> java ImageDisplay ./Image1.rgb 1 10 10 256
