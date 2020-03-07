package frc.robot.subsystems;

import java.util.ArrayList;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

import edu.wpi.cscore.CvSink;
import edu.wpi.first.cameraserver.CameraServer;

public class Vision {
    CvSink cvcam;
    private double angle = 500; // SHOULD NEVER BE WRITTEN TO OUTSIDE OF THIS CLASS
    private boolean runningVision = false;

    public int width;

    Scalar hsvMin = new Scalar(0, 120, 144);
    Scalar hsvMax = new Scalar(84, 255, 255);

    public Vision() {
        CameraServer.getInstance().startAutomaticCapture();

        cvcam = CameraServer.getInstance().getVideo();
    }

    public double getAngle() {
        if (!runningVision) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    findAngle();
                }
            }).start();
        }

        return angle;
    }

    private void findAngle() {
        // returns distance from middle as percentage from x axis, 500 if no target
        // found

        runningVision = true;

        Mat img = new Mat();
        cvcam.grabFrame(img);
        width = img.width();

        Imgproc.cvtColor(img, img, Imgproc.COLOR_BGR2HSV);

        Core.inRange(img, hsvMin, hsvMax, img);

        Imgproc.erode(img, img, null, new Point(-1, -1), 4);

        Imgproc.dilate(img, img, null, new Point(-1, -1), 4);

        ArrayList<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Mat hierarchy = new Mat();

        Imgproc.findContours(img, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        MatOfPoint max = null;
        if (contours.size() > 0) {
            double maxArea = 0;

            for (MatOfPoint i : contours) { // find biggest contour
                double elementArea = Imgproc.contourArea(i);

                if (elementArea > maxArea) {
                    max = i;
                    maxArea = elementArea;
                    continue;
                }
            }

            Moments moments = Imgproc.moments(max);

            Point centre = null;
            float[] radius = { 0f, 0f };

            if (radius[0] < 20) {// too small, quit
                angle = 500;
                runningVision = false;
                return;
            }

            Imgproc.minEnclosingCircle(new MatOfPoint2f(max.toArray()), centre, radius);

            // double centreX = moments.m10 / moments.m00;
            double centreY = moments.m01 / moments.m00;

            angle = centreY / width;
        } else {
            angle = 500;
        }

        runningVision = false;
    }
}