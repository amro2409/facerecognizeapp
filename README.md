# facerecognizeapp
The Android Face Recognition Project aims to develop an application that can detect and recognize faces in real time using the cameras available on the device. The project relies on deep learning techniques to provide a smooth and efficient user experience.

## Features
-  face detection using a pre-trained model.
- Face recognition with embedding comparison.
- Ability to switch between front and back cameras.
- User-friendly interface with camera preview.
- Face registration with user-defined names.

## Getting Started

### Prerequisites
- Android SDK
- Android Studio IDE
- A physical device or emulator with a camera

### Installation

1. **Clone the repository**:
   ```bash
   git clone https://github.com/amro2409/facerecognizeapp.git
   cd facerecognizeapp
   ```


2. **Run the application**:
   Connect your device or start an emulator, and run:
   ```bash
   gradlew run
   ```

## How It Works

1. **Camera Setup**: 
   - Uses `availableCameras()` to list available cameras and initializes the selected camera using `CameraController`.
   - Displays a live camera preview with the option to switch cameras.

2. **Real-Time Face Detection**:
   - Captures frames from the camera and processes them through a pre-trained face detection model (face-detection ML-Kit google  ).
   - Displays bounding boxes around detected faces on the camera feed.

3. **Real-Time Face Recognition**:
   - Crops detected faces and processes them through a pre-trained face recognition model (FaceNet, MobileFaceNet).
   - Compares the generated embedding vectors with stored embeddings to identify faces.

4. **Face Registration**:
   - Captures a face and allows users to assign a name to it.
   - Stores the face embedding along with the name in a local database.

## Performance Optimization
- Utilizes quantized models to enhance performance on mobile devices.
- Implements efficient data structures for storing and comparing embeddings.
- Explores techniques like batch processing for heavy recognition tasks.


