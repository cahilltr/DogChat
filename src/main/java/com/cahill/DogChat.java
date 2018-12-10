package com.cahill;

import org.openimaj.audio.*;
import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.colour.Transforms;
import org.openimaj.image.processing.face.detection.DetectedFace;
import org.openimaj.image.processing.face.detection.FaceDetector;
import org.openimaj.image.processing.face.detection.HaarCascadeDetector;
import org.openimaj.video.Video;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.VideoDisplayListener;
import org.openimaj.video.capture.VideoCapture;
import org.openimaj.video.capture.VideoCaptureException;

import java.util.List;

public class DogChat {

	public static void main(String[] args) throws VideoCaptureException {

		JavaSoundAudioGrabber javaSoundAudioGrabber = new JavaSoundAudioGrabber(new AudioFormat(16, 44.1, 2));
		Thread audioThread = new Thread(javaSoundAudioGrabber);
		audioThread.start();

		//TODO reduce feedback -> echo cancellation
		AudioPlayer audioPlayer = new AudioPlayer(javaSoundAudioGrabber);
		Thread audioPlayerThread = new Thread(audioPlayer);
		audioPlayerThread.start();
		audioPlayer.addAudioEventListener(new AudioEventListener() {
			@Override
			public void beforePlay(SampleChunk sampleChunk) {
			}

			@Override
			public void afterPlay(AudioPlayer audioPlayer, SampleChunk sampleChunk) {

			}

			@Override
			public void audioEnded() {

			}
		});

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			javaSoundAudioGrabber.stop();
			audioPlayer.stop();
		}));

		Video<MBFImage> video = new VideoCapture(1024, 768);

		VideoDisplay<MBFImage> display = VideoDisplay.createVideoDisplay(video);


		//TODO use isFacePresent to attempt to draw dog to screen
		//TODO move face detection into separate thread so to not hold up processing
//		//same as above; event based
		display.addVideoListener(
			new VideoDisplayListener<MBFImage>() {
				long lastCheckTime = System.currentTimeMillis();
				FaceDetector<DetectedFace, FImage> fd = new HaarCascadeDetector(40);
				public void beforeUpdate(MBFImage frame) {
					if ((System.currentTimeMillis() - lastCheckTime) >= 30000) {
						System.out.println("Is Face Present:" + isFacePresent(frame, fd));
						fd = new HaarCascadeDetector(40);
						lastCheckTime = System.currentTimeMillis();
					}
				}

				public void afterUpdate(VideoDisplay<MBFImage> display) {
				}
			});

	}

	private static boolean isFacePresent(MBFImage frame, FaceDetector<DetectedFace, FImage> fd) {
		List<DetectedFace> faces = fd.detectFaces(Transforms.calculateIntensity(frame));
		for( DetectedFace face : faces ) {
			frame.drawShape(face.getBounds(), RGBColour.RED);
		}
		return !faces.isEmpty();
	}
}
