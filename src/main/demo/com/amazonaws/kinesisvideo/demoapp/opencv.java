package com.amazonaws.kinesisvideo.demoapp;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

import com.amazonaws.kinesisvideo.demoapp.auth.AuthHelper;
import com.amazonaws.services.kinesisvideo.AmazonKinesisVideo;
import com.amazonaws.services.kinesisvideo.AmazonKinesisVideoAsyncClient;
import com.amazonaws.services.kinesisvideo.AmazonKinesisVideoPutMedia;
import com.amazonaws.services.kinesisvideo.AmazonKinesisVideoPutMediaClient;
import com.amazonaws.services.kinesisvideo.PutMediaAckResponseHandler;
import com.amazonaws.services.kinesisvideo.model.AckEvent;
import com.amazonaws.services.kinesisvideo.model.FragmentTimecodeType;
import com.amazonaws.services.kinesisvideo.model.GetDataEndpointRequest;
import com.amazonaws.services.kinesisvideo.model.PutMediaRequest;
import org.opencv.videoio.VideoWriter;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URI;
import java.time.Instant;
import java.util.Date;
import java.util.concurrent.CountDownLatch;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.highgui.HighGui;
import org.opencv.videoio.VideoWriter;
import org.opencv.videoio.Videoio;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public final class opencv {
    // Other constants...
    private static final String DEFAULT_REGION = "us-west-2";
    private static final String PUT_MEDIA_API = "/putMedia";

    /* the name of the stream */
    private static final String STREAM_NAME = "test1-stream";

    /* sample MKV file */
    private static final String MKV_FILE_PATH = "src/main/resources/data/mkv/clusters.mkv";
    /* max upload bandwidth */
    private static final long MAX_BANDWIDTH_KBPS = 15 * 1024L;

    /* response read timeout */
    private static final int READ_TIMEOUT_IN_MILLIS = 1_000_000;

    /* connect timeout */
    private static final int CONNECTION_TIMEOUT_IN_MILLIS = 10_000;

    private opencv() { }

    public static void main(final String[] args) throws Exception {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        // Open the default camera (usually the first camera)
        VideoCapture camera = new VideoCapture(0);

        // Check if camera opened successfully
        if (!camera.isOpened()) {
            System.out.println("Error: Unable to open camera");
            return;
        }

        final AmazonKinesisVideo frontendClient = AmazonKinesisVideoAsyncClient.builder()
                .withCredentials(AuthHelper.getSystemPropertiesCredentialsProvider())
                .withRegion(DEFAULT_REGION)
                .build();

        final String dataEndpoint = frontendClient.getDataEndpoint(
                new GetDataEndpointRequest()
                        .withStreamName(STREAM_NAME)
                        .withAPIName("PUT_MEDIA")).getDataEndpoint();

        while (true) {
            final URI uri = URI.create(dataEndpoint + PUT_MEDIA_API);

            final CountDownLatch latch = new CountDownLatch(1);

            final AmazonKinesisVideoPutMedia dataClient = AmazonKinesisVideoPutMediaClient.builder()
                    .withRegion(DEFAULT_REGION)
                    .withEndpoint(URI.create(dataEndpoint))
                    .withCredentials(AuthHelper.getSystemPropertiesCredentialsProvider())
                    .withConnectionTimeoutInMillis(CONNECTION_TIMEOUT_IN_MILLIS)
                    .build();

            final PutMediaAckResponseHandler responseHandler = new PutMediaAckResponseHandler() {
                @Override
                public void onAckEvent(AckEvent event) {
                    System.out.println("onAckEvent " + event);
                }

                @Override
                public void onFailure(Throwable t) {
                    latch.countDown();
                    System.out.println("onFailure: " + t.getMessage());
                    // TODO: Add your failure handling logic here
                }

                @Override
                public void onComplete() {
                    System.out.println("onComplete");
                    latch.countDown();
                }
            };
//            VideoWriter videoWriter = new VideoWriter("output.mkv",
//                    VideoWriter.fourcc('H', '2', '6', '4'), // Example: H.264
//                    30, // Frames per second
//                    frameSize);
            // Loop to continuously capture frames and send them to Kinesis Video Streams
            while (true) {
                Mat frame = new Mat();
                camera.read(frame); // Read frame from the camera
                displayFrame(frame);
//                String outputFile = "vid/output.mkv";
//
//                // Frame width and height
//                int width = frame.cols();
//                int height = frame.rows();
//
//                // Frame rate (fps)
//                double fps = 30.0;
//                String path = "C:\\Users\\Hemant.Surariya\\output.MKV";
//                // Create a VideoWriter object to write the frame to an MKV file
//                VideoWriter videoWriter = new VideoWriter(path, VideoWriter.fourcc('H','2','6','4'), 30, new org.opencv.core.Size(width, height), true);
//                // Check if the VideoWriter object is opened successfully
////                videoWriter.open(path, VideoWriter.fourcc('H', '2', '6', '4'), 30, new org.opencv.core.Size(width, height) );
//                if (!videoWriter.isOpened()) {
//                    System.out.print(videoWriter);
//                    System.out.println("Error: Failed to open the video file for writing.");
//                }
//
//                // Write the frame to the video file
//                videoWriter.write(frame);
//
//                // Release the VideoWriter object
//                videoWriter.release();

                // Create an InputStream from the MKV file
//                File file = new File(outputFile);
//                InputStream avs_7 = new FileInputStream(file);
                InputStream inputStream = new ByteArrayInputStream(matToByteArray(frame));
                // Send frame to Kinesis Video Streams


                dataClient.putMedia(new PutMediaRequest()
                                .withStreamName(STREAM_NAME)
                                .withFragmentTimecodeType(FragmentTimecodeType.RELATIVE)
                                .withPayload(inputStream)
                                .withProducerStartTimestamp(Date.from(Instant.now())),
                        responseHandler);
                // Release the frame
                frame.release();

                // Sleep for a short duration to control the frame rate
                Thread.sleep(1000); // Assuming 30 frames per second (1000 ms / 30)
            }

            // Close camera and data client (this won't be reached in the current implementation)
//            camera.release();
//            dataClient.close();
        }
    }

    // Helper method to convert Mat object to byte array
    private static byte[] matToByteArray(Mat image) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            ImageIO.write(matToBufferedImage(image), "jpg", outputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return outputStream.toByteArray();
    }

//    public static byte[] matToByteArray(Mat frame) {
//        // Get the total number of elements (pixels) in the Mat
//        int totalBytes = (int) (frame.total() * frame.elemSize());
//
//        // Create a byte array to hold the data
//        byte[] byteArray = new byte[totalBytes];
//
//        // Copy the data from the Mat to the byte array
//        frame.get(0, 0, byteArray);
//
//        return byteArray;
//    }

    // Helper method to convert Mat object to BufferedImage
    public static void displayFrame(Mat frame) {
        // Convert Mat to BufferedImage
        BufferedImage bufferedImage = matToBufferedImage(frame);

        // Create an ImageIcon from the BufferedImage
        ImageIcon imageIcon = new ImageIcon(bufferedImage);

        // Create a JLabel to display the image
        JLabel imageLabel = new JLabel(imageIcon);

        // Create a JFrame to hold the JLabel
        JFrame frame_1 = new JFrame("Camera Stream");
        frame_1.getContentPane().add(imageLabel);
        frame_1.pack();
        frame_1.setVisible(true);
    }
    private static BufferedImage matToBufferedImage(Mat matrix) {
        int cols = matrix.cols();
        int rows = matrix.rows();
        int elemSize = (int) matrix.elemSize();
        byte[] data = new byte[cols * rows * elemSize];
        int type;
        matrix.get(0, 0, data);

        switch (matrix.channels()) {
            case 1:
                type = BufferedImage.TYPE_BYTE_GRAY;
                break;
            case 3:
                type = BufferedImage.TYPE_3BYTE_BGR;
                // bgr to rgb
                byte b;
                for (int i = 0; i < data.length; i = i + 3) {
                    b = data[i];
                    data[i] = data[i + 2];
                    data[i + 2] = b;
                }
                break;
            default:
                throw new IllegalStateException("Unsupported number of channels");
        }

        BufferedImage image = new BufferedImage(cols, rows, type);
        image.getRaster().setDataElements(0, 0, cols, rows, data);
        return image;
    }
    public static byte[] encodeFrameToH264Stream(Mat frame) throws IOException, InterruptedException {
        // Convert Mat frame to byte array based on format (e.g., YUV420p)
        byte[] frameData = convertMatToByteArray(frame);

        // FFmpeg command to encode (adjust as needed)
        String command = "ffmpeg -f rawvideo -pix_fmt yuv420p -s " + frame.width() + "x" + frame.height() +
                " -i - -c:v libx264 -preset slow -crf 23 -f mpegts -";

        // Use Runtime.exec to execute the FFmpeg command
        Process process = Runtime.getRuntime().exec(command.split(" "));

        // Write frame data to FFmpeg process stdin
        OutputStream outputStream = process.getOutputStream();
        outputStream.write(frameData);
        outputStream.flush();
        outputStream.close();

        // Capture encoded H.264 data from FFmpeg process stdout
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        InputStream inputStream = process.getInputStream();
        byte[] buffer = new byte[8192];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            byteArrayOutputStream.write(buffer, 0, bytesRead);
        }
        inputStream.close();

        // Wait for FFmpeg to finish encoding
        process.waitFor();

        // Return InputStream from the captured H.264 data
        return byteArrayOutputStream.toByteArray();
    }
    private static byte[] encodeToYUV(Mat frame) {
        // Convert BGR to YUV format
        Mat yuvFrame = new Mat(frame.rows(), frame.cols(), CvType.CV_8UC1); // Allocate space for YUV frame
        Imgproc.cvtColor(frame, yuvFrame, Imgproc.COLOR_BGR2YUV_I420);

        // Convert Mat to byte array
        byte[] yuvData = new byte[(int) (yuvFrame.total() * yuvFrame.elemSize())];
        yuvFrame.get(0, 0, yuvData);

        // Release yuvFrame
        yuvFrame.release();

        return yuvData;
    }
    private static byte[] convertMatToByteArray(Mat frame) throws IOException {
        // Get frame width, height, and number of channels
        int width = frame.width();
        int height = frame.height();
        int channels = frame.channels();

        // Check if the Mat has 3 channels (expected for YUV420p)
        if (channels != 3) {
            throw new IllegalArgumentException("Mat must have 3 channels for YUV420p conversion");
        }

        // Get the underlying data buffer from the Mat
        byte[] data = new byte[width * height * 3];  // Allocate enough space for 3 channels
        frame.get(0, 0, data);

        // Convert BGR to YUV420p format (assuming OpenCV channel order)
        // This is a simplified conversion, more advanced techniques might be needed
        int yuvIndex = 0;
        for (int i = 0; i < data.length; i += 3) {
            data[yuvIndex++] = data[i + 2];  // B -> Y
            data[yuvIndex++] = data[i];      // G -> U
            data[yuvIndex++] = data[i + 1];  // R -> V
        }

        return data;
    }

}
