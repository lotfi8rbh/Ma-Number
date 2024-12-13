import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class ImageProcessor {

    // Charger une image depuis le chemin spécifié
    public static BufferedImage loadImage(String imagePath) throws IOException {
        File imageFile = new File(imagePath);
        BufferedImage image = ImageIO.read(imageFile);
        if (image == null) {
            throw new IllegalArgumentException("Erreur de chargement de l'image.");
        }
        return image;
    }

    // Redimensionner une image aux dimensions spécifiées
    public static BufferedImage resizeImage(BufferedImage image, int width, int height) {
        BufferedImage resizedImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D graphics = resizedImage.createGraphics();
        graphics.drawImage(image, 0, 0, width, height, null);
        graphics.dispose();
        // System.out.println("Image redimensionnée à : " + width + "x" + height);
        System.out.println(
                "Dimensions après redimensionnement : " + resizedImage.getWidth() + "x" + resizedImage.getHeight());
        return resizedImage;
    }

    // Calculer le seuil optimal avec l'algorithme d'Otsu
    public static int otsuThreshold(BufferedImage image) {
        int[] histogram = new int[256];

        // Calculer l'histogramme des pixels
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int grayValue = new Color(image.getRGB(x, y)).getRed();
                histogram[grayValue]++;
            }
        }

        int totalPixels = image.getWidth() * image.getHeight();

        int sum = 0;
        for (int i = 0; i < 256; i++) {
            sum += i * histogram[i];
        }

        int sumB = 0;
        int wB = 0;
        int wF;
        double maximumVariance = 0.0;
        int threshold = 0;

        for (int i = 0; i < 256; i++) {
            wB += histogram[i];
            if (wB == 0)
                continue;

            wF = totalPixels - wB;
            if (wF == 0)
                break;

            sumB += i * histogram[i];
            double mB = (double) sumB / wB;
            double mF = (double) (sum - sumB) / wF;

            double betweenVariance = (double) wB * wF * (mB - mF) * (mB - mF);

            if (betweenVariance > maximumVariance) {
                maximumVariance = betweenVariance;
                threshold = i;
            }
        }

        System.out.println("Seuil optimal trouvé par Otsu : " + threshold);
        return threshold;
    }

    // Binariser l'image en utilisant le seuil donné
    public static int[][] binarizeImage(BufferedImage image, int threshold) {
        int width = image.getWidth();
        int height = image.getHeight();
        int[][] binarizedPixels = new int[height][width];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int grayValue = new Color(image.getRGB(x, y)).getRed();
                binarizedPixels[y][x] = (grayValue > threshold) ? 1 : 0;
            }
        }

        System.out.println("Image binarisée avec un seuil de " + threshold + ".");
        return binarizedPixels;
    }

    // Aplatir la matrice binarisée en un vecteur 1D
    public static int[] flattenBinarizedImage(int[][] binarizedPixels) {
        int height = binarizedPixels.length;
        int width = binarizedPixels[0].length;
        int[] flatVector = new int[height * width];
        int index = 0;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                flatVector[index++] = binarizedPixels[y][x];
            }
        }

        System.out.println("Image aplatie en vecteur de taille : " + flatVector.length);
        return flatVector;
    }

    public static void main(String[] args) {
        String imagePath = "test4.png";

        try {
            BufferedImage originalImage = loadImage(imagePath);
            BufferedImage resizedImage = resizeImage(originalImage, 28, 28);

            // Calculer le seuil optimal avec Otsu
            int otsuThresholdValue = otsuThreshold(resizedImage);

            // Binariser l'image avec le seuil trouvé par Otsu
            int[][] binarizedPixels = binarizeImage(resizedImage, otsuThresholdValue);
            int[] flattenedVector = flattenBinarizedImage(binarizedPixels);

            System.out.println("Vecteur aplati :");
            for (int value : flattenedVector) {
                System.out.print(value + " ");
            }

        } catch (Exception e) {
            System.out.println("Erreur : " + e.getMessage());
        }
    }
}
