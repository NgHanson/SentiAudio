# [SentiAudio](https://devpost.com/software/senti-audio)
### by Christophe Gaboury, Hanson Ng, and Shabab Ayub

Senti Audio is an android application that takes your audio files, sorts them by emotion, and plays whichever emotion that you are displaying on your face.


*Tools:*   
Android Studio  
TarsosDSP - A Real-time audio processing framework for Java  
AWS S3 and Machine Learning
Microsoft Project Oxford

#### Example screenshots:
![alt tag](https://github.com/NgHanson/SentiAudio/blob/master/Training_data_and_images/home_page.png)  

The following image displays the camera view which allows users to capture images of themselves. This sample view acts as a audio player, seen on the bottom, as well.  
![alt tag](https://github.com/NgHanson/SentiAudio/blob/master/Training_data_and_images/camera_view.png)  

The following data was used to train the AWS machine learning model. The pitch was analyzed at a 44.1kHz sampling rate for the first 20 seconds of the file.
![alt tag](https://github.com/NgHanson/SentiAudio/blob/master/Training_data_and_images/training_data.png)  

