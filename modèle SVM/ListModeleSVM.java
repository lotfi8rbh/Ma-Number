import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ListModeleSVM {
    public class ModeleSVM {

        public double[][] vecteursSupport; // Matrice de vecteurs de support pour chaque classe (vecteurs par classe)
        public double[] coefficients; // Tableau des coefficients (alpha) pour chaque classe
        public double biais; // Tableau des biais pour chaque classe
        public double gamma = 0.001; // Paramètre gamma pour le noyau RBF

        public ModeleSVM(String fichierSupportVecteurs, String fichierCoefficients, String fichierBiais)
                throws IOException {
            this.vecteursSupport = chargerVecteursSupports(fichierSupportVecteurs);
            this.coefficients = chargerCoefficients(fichierCoefficients);
            this.biais = chargerBiais(fichierBiais);

        }

        public double[] chargerCoefficients(String file) throws IOException {
            

            // Utilisation de try-with-resources pour assurer la fermeture du fichier
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line = br.readLine(); // Lire uniquement la première ligne

               

                // Vérification si la ligne est vide
                if (line == null || line.trim().isEmpty()) {
                    throw new IOException("Le fichier est vide ou ne contient pas de coefficients.");
                }

                // Diviser la ligne par les espaces, virgules ou points-virgules
                String[] values = line.trim().split("[\\s,;]+");
                double[] vector = new double[values.length];

                // Conversion des chaînes en doubles
                for (int i = 0; i < values.length; i++) {
                    try {
                        vector[i] = Double.parseDouble(values[i]);
                    } catch (NumberFormatException e) {
                        throw new IOException("Erreur de format à la position " + i + ": " + values[i], e);
                    }
                }

                return vector; // Retourner le tableau de doubles
            }
        }

        public double[][] chargerVecteursSupports(String fichierSupportVecteurs) throws IOException {
            
            BufferedReader br = new BufferedReader(new FileReader(fichierSupportVecteurs));
            List<double[]> vecteursSupport = new ArrayList<>();
            String vecteur;
            while ((vecteur = br.readLine()) != null) {
                String[] values = vecteur.split(" ");
                double[] row = new double[values.length];
                for (int i = 0; i < values.length; i++) {
                    row[i] = Double.parseDouble(values[i]);
                }
                vecteursSupport.add(row);
            }
            br.close();
            return vecteursSupport.toArray(new double[0][]);

        }

        public double chargerBiais(String fichierBiais) throws IOException {
          
            BufferedReader br = new BufferedReader(new FileReader(fichierBiais));
            String biais = br.readLine();
            br.close();
            return Double.parseDouble(biais);

        }

        public double noyau_rbf(double[] vecteur_image, double[] vecteurs_support) {
            double sum = 0;
            for (int i = 0; i < vecteur_image.length; i++) {
                sum += (vecteur_image[i] - vecteurs_support[i]) * (vecteur_image[i] - vecteurs_support[i]);
            }
            return Math.exp(-this.gamma * sum);
        }

        public double predict_score(double [] vecteur_image) {
            double score = 0.0;

            for (int i = 0; i < this.vecteursSupport.length; i++) {
                double valeur_noyau = noyau_rbf(vecteur_image, this.vecteursSupport[i]);
                score += this.coefficients[i] * valeur_noyau;
            }

            // Ajouter le biais
            score += this.biais;

            return score;
        }

    }

    ArrayList<ModeleSVM> listesDeModeleSVM = new ArrayList<>();

    public ListModeleSVM() throws IOException {
        for (int i = 0; i <= 9; i++) {
            String cheminBiais = "data/biais_" + i + ".txt";
            String cheminVecteurs = "data/vecteur_support_" + i + ".txt";
            String cheminCoef = "data/coefficients_" + i + ".txt";

            ModeleSVM modelsvm = new ModeleSVM(cheminVecteurs, cheminCoef, cheminBiais);
            listesDeModeleSVM.add(modelsvm);

        }
    }

    int predict_chiffre(double[] vecteur_image) {
        double[] scores = new double[10];
        int i = 0;
        for (ModeleSVM modele : listesDeModeleSVM) {
            System.out.println(i);
            scores[i] = modele.predict_score(vecteur_image);
            System.out.println(scores[i]);
            i++;
        }
    
        int classe_predite = 0;
        double score_max = scores[0];
        for (int c = 0; c <= 9; c++) {
            if (scores[c] > score_max) {
                score_max = scores[c];
                classe_predite = c;
            }
        }

        return classe_predite;
    }

    public static void main(String[] args) throws Exception {

        ListModeleSVM modeleSVM = new ListModeleSVM();

        double[] vecteur_image = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,1,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,1,0,0,0,0,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,0,0,0,0,0,0,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,0,0,0,0,0,0,0,0,0,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,1,0,0,0,0,0,0,0,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,0,0,0,0,0,0,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,1,1,1,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
        int predictedClass = modeleSVM.predict_chiffre(vecteur_image);

        // Afficher la classe prédite
        System.out.println("Classe prédite : " + predictedClass);
    }

}
