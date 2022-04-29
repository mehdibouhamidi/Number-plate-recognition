package detection.controller;

import com.jfoenix.controls.JFXButton;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Scanner;

import java.util.*;
import net.sourceforge.tess4j.ITesseract;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import static org.opencv.imgproc.Imgproc.MORPH_RECT;
import static org.opencv.imgproc.Imgproc.getStructuringElement;
import java.io.File;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;


public class ImportController implements Initializable {

    @FXML
    private AnchorPane rootPane;

    @FXML
    private Pane pnlImport;

    @FXML
    private JFXButton btnTraiter;

    @FXML
    private ImageView imgImport;

    @FXML
    private JFXButton btnQuitter;

    @FXML
    private Pane pnlResult;

    @FXML
    private ImageView imgResult;

    @FXML
    private JFXButton btnEnregistrer;

    @FXML
    private JFXButton btnQuitterResultat;
    @FXML
    private Label lblIndica1;
    @FXML
    private Label lblIndica2;
    @FXML
    private Label lblIndica3;
    @FXML
    private Label lblMatricule;


    private static String imgPath;
    private static final int
            CV_MOP_CLOSE = 3,
            CV_THRESH_OTSU = 8,
            CV_THRESH_BINARY = 0,
            CV_ADAPTIVE_THRESH_GAUSSIAN_C = 1,
            CV_ADAPTIVE_THRESH_MEAN_C = 0,
            CV_THRESH_BINARY_INV = 1;

    @FXML
    private void handleButtonAction(ActionEvent event) throws IOException, TesseractException {
        if (event.getSource() == btnTraiter) {
            //traiter
            traiterImage(imgPath);
            getWords();
            File filResult = new File("C:/Users/Hawk oum/IdeaProjects/detectionmat/src/detection/results/mf.png");
            BufferedImage bufferedImage = ImageIO.read(filResult);
            Image image = SwingFXUtils.toFXImage(bufferedImage, null);
            imgResult.setImage(image);
            pnlResult.toFront();
        } else if (event.getSource() == btnQuitter) {
            //quitter l'app
            Platform.exit();
            System.exit(0);

        } else if (event.getSource() == btnQuitterResultat) {
            //quitter le resultat
            imgResult.setImage(null);
            lblIndica3.setText(null);
            lblIndica2.setText(null);
            lblIndica1.setText(null);
            lblMatricule.setText(null);
            pnlImport.toFront();
        } else if (event.getSource() == btnEnregistrer) {
            //enregistrer le traitement en BDD
        }
    }

    @FXML
    private void importAction(MouseEvent event) throws IOException {
        //importer l'image du matricule
        FileChooser fileChooser = new FileChooser();

        //Set extension filter
        FileChooser.ExtensionFilter extFilterJPG =
                new FileChooser.ExtensionFilter("JPG files (*.JPG)", "*.JPG");
        FileChooser.ExtensionFilter extFilterjpg =
                new FileChooser.ExtensionFilter("jpg files (*.jpg)", "*.jpg");
        FileChooser.ExtensionFilter extFilterPNG =
                new FileChooser.ExtensionFilter("PNG files (*.PNG)", "*.PNG");
        FileChooser.ExtensionFilter extFilterpng =
                new FileChooser.ExtensionFilter("png files (*.png)", "*.png");
        fileChooser.getExtensionFilters()
                .addAll(extFilterJPG, extFilterjpg, extFilterPNG, extFilterpng);

        //Show open file dialog
        File file = fileChooser.showOpenDialog(null);

        BufferedImage bufferedImage = ImageIO.read(file);
        Image image = SwingFXUtils.toFXImage(bufferedImage, null);
        imgImport.setImage(image);
        imgPath = file.getPath();
        //System.out.println(file.getPath());
    }

    private static boolean checkRatio(RotatedRect candidate) {
        double error = 0.3;
        //Moroccan car plate size: 52x11 aspect 4,7272
        //double aspect = 52/11.5;
        double aspect = 5;
        int min = 15 * (int) aspect * 15;
        int max = 125 * (int) aspect * 125;
        //Get only patchs that match to a respect ratio.
        double rmin = aspect - aspect * error;
        double rmax = aspect + aspect * error;
        double area = candidate.size.height * candidate.size.width;
        float r = (float) candidate.size.width / (float) candidate.size.height;
        if (r < 1)
            r = 1 / r;
        if ((area < min || area > max) || (r < rmin || r > rmax)) {
            return false;
        } else {
            return true;
        }
    }

    private static boolean checkDensity(Mat candidate) {
        float whitePx = 0;
        float allPx = 0;
        if (candidate.cols() > candidate.rows()) {
            whitePx = Core.countNonZero(candidate);

            allPx = candidate.cols() * candidate.rows();
        }
        //System.out.println(whitePx/allPx)
        return 0.5 <= whitePx / allPx;
    }

    private static void traiterImage(String imgPath) {
        List<MatOfPoint> crt = new ArrayList<MatOfPoint>();

        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();

        Mat img = new Mat();
        Mat imgGray = new Mat();
        Mat imgGaussianBlur = new Mat();
        Mat imgSobel = new Mat();
        Mat imgThreshold = new Mat();
        Mat imgAdaptiveThreshold = new Mat();
        Mat imgAdaptiveThreshold_forCrop = new Mat();
        Mat imgMoprhological = new Mat();
        Mat imgContours = new Mat();
        Mat imgMinAreaRect = new Mat();
        Mat imgDetectedPlateCandidate = new Mat();
        Mat imgDetectedPlateTrue = new Mat();
        Mat imgThreshold2 = new Mat();
        Mat imgSobel2 = new Mat();
        Mat lines = new Mat();


        img = Imgcodecs.imread(imgPath);
        //Imgcodecs.imwrite("C:/Users/Hawk oum/Downloads/filterimage/1_True_Image.png", img);
        Imgproc.cvtColor(img, imgGray, Imgproc.COLOR_BGR2GRAY);
        //Imgcdecs.imwrite("C:/Users/Hawk oum/Downloads/filterimage/2_imgGray.png", imgGray);
        Imgproc.GaussianBlur(imgGray, imgGaussianBlur, new Size(3, 3), 0);
        // Imgcodecs.imwrite("C:/Users/Hawk oum/Downloads/filterimage/3_imgGaussianBlur.png", imgGaussianBlur);
        Imgproc.Sobel(imgGaussianBlur, imgSobel, -1, 1, 0);
        // Imgcodecs.imwrite("C:/Users/Hawk oum/Downloads/filterimage/4_imgSobel.png", imgSobel);
        Imgproc.threshold(imgGray, imgThreshold, 0, 255, CV_THRESH_OTSU + CV_THRESH_BINARY);
        Mat element = getStructuringElement(MORPH_RECT, new Size(23, 2));
        Imgproc.morphologyEx(imgThreshold, imgMoprhological, CV_MOP_CLOSE, element); //или imgThreshold
        Imgcodecs.imwrite("C:/Users/Hawk oum/Downloads/filterimage/6_imgMoprhologicald.png", imgMoprhological);
        imgContours = imgMoprhological.clone();
        Imgproc.findContours(imgContours,
                contours,
                new Mat(),
                Imgproc.RETR_LIST,
                Imgproc.CHAIN_APPROX_SIMPLE);
        Imgproc.drawContours(imgContours, contours, -1, new Scalar(255, 0, 0));
         Imgcodecs.imwrite("C:/Users/Hawk oum/Downloads/filterimage/7_imgContours.png", imgContours);
        imgMinAreaRect = img.clone();
        if (contours.size() > 0) {
            for (MatOfPoint matOfPoint : contours) {
                MatOfPoint2f points = new MatOfPoint2f(matOfPoint.toArray());

                RotatedRect box = Imgproc.minAreaRect(points);
                if (checkRatio(box)) {
                    Imgproc.rectangle(imgMinAreaRect, box.boundingRect().tl(), box.boundingRect().br(), new Scalar(0, 0, 255));
                    imgDetectedPlateCandidate = new Mat(imgThreshold, box.boundingRect());
                    if (checkDensity(imgDetectedPlateCandidate))
                        imgDetectedPlateTrue = imgDetectedPlateCandidate.clone();
                } //else
                //Imgproc.rectangle(imgMinAreaRect, box.boundingRect().tl(), box.boundingRect().br(), new Scalar(0, 255, 0));
            }
        }
        Imgcodecs.imwrite("C:/Users/Hawk oum/Downloads/filterimage/8_imgMinAreaRect.png", imgMinAreaRect);
        Imgcodecs.imwrite("C:/Users/Hawk oum/Downloads/filterimage/9_imgDetectedPlateCandidate.png", imgDetectedPlateCandidate);
        Imgcodecs.imwrite("C:/Users/Hawk oum/Downloads/filterimage/10_imgDetectedPlateTrue.png", imgDetectedPlateTrue);

        //2éme étape
        Mat mat = new Mat();
        Mat f = new Mat();
        mat = Imgcodecs.imread("C:/Users/Hawk oum/Downloads/filterimage/10_imgDetectedPlateTrue.png");
        f = Imgcodecs.imread("C:/Users/Hawk oum/Downloads/filterimage/10_imgDetectedPlateTrue.png");
        Mat canny = new Mat();
        Mat Morph = new Mat();
        Mat Kernel = new Mat(new Size(3, 3), CvType.CV_8U, new Scalar(255));
        Imgproc.cvtColor(mat, imgGray, Imgproc.COLOR_BGR2GRAY);
        Imgproc.Canny(imgGray, canny, 50, 200, 3);
        Imgproc.morphologyEx(canny, Morph, CV_MOP_CLOSE, Kernel);
        //Imgcodecs.imwrite("C:/Users/Hawk oum/Downloads/filterimage/mf.png", Morph);
        Mat fin = new Mat();
        Imgproc.adaptiveThreshold(imgGray, fin, 255, CV_ADAPTIVE_THRESH_MEAN_C, CV_THRESH_BINARY, 15, 40);

        Mat imgCtr = new Mat();

        Mat hierarchy = new Mat();
        imgCtr = Morph.clone();
        Imgproc.findContours(imgCtr,
                crt,
                hierarchy,
                Imgproc.RETR_LIST,
                Imgproc.CHAIN_APPROX_SIMPLE);

        Rect rect = null;
        double maxArea = 200;
        ArrayList<Mat> ele = new ArrayList<Mat>();
        ArrayList<Rect> arr = new ArrayList<Rect>();

        for (Mat contour : crt) {
            double contourArea = Imgproc.contourArea(contour);

            if (contourArea > maxArea) {
                rect = Imgproc.boundingRect(contour);
                if (rect.width < 7) {
                    Imgproc.rectangle(mat, rect, new Scalar(255, 255, 255), 20);
                    ele.add(new Mat(fin, rect));
                }
                /*
                if ((rect.width > 10) || (rect.height >= 30)) {
                    Imgproc.rectangle(f, rect, new Scalar(0, 0, 255), 2);
                }

                 */
            }
        }//Imgcodecs.imwrite("C:/Users/Hawk oum/Downloads/filterimage/f.png", f);
        // for(int i=0;i<ele.size();i++) {

        // Imgcodecs.imwrite("C:/Users/Hawk oum/Downloads/filterimage/caractere "+ i +".png", ele.get(i));
        // }
        Imgcodecs.imwrite("C:/Users/Hawk oum/IdeaProjects/detectionmat/src/detection/results/mf.png", mat);
        // System.out.println("width"+mat.width());1

    }

    private void getWords() throws TesseractException {

        File imageFile = new File("C:/Users/Hawk oum/IdeaProjects/detectionmat/src/detection/results/mf.png");
        ITesseract instance = new Tesseract();

        instance.setTessVariable("tessedit_char_whitelist", "acekopxyABCEHKMOPTXY0123456789");

        instance.setLanguage("ara");

        String result = instance.doOCR(imageFile);
        System.out.println(result);
        //matricule
        lblMatricule.setText(result);
        String[] res = result.split(" ");
        String chiffres = "";
        String cara = "";
        String serie = "";

        for (int i = 0; i < 3; ) {
            if (res[i].length() <= 2) {
                chiffres = res[i];
                System.out.println("l’identifiant :" + chiffres);
                i++;
            }
            try {
                Integer.parseInt(res[i]);

            } catch (NumberFormatException e) {
                cara = res[i];
                System.out.println("ceractére:" + cara);
                i++;
            }
            if (res[i].length() >= 4) {
                serie = res[i];
                System.out.println("Numéro d’enregistrement  :" + serie);
                i++;
            }
           /* if (res[i] == res[3]) {
                break;
            }
            */

        lblIndica1.setText(chiffres);
        lblIndica2.setText(cara);
        lblIndica3.setText(serie);
    }

}
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        //initialisation des composantes
    }}
