package vfeqs.model;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.UnitSphereRandomVectorGenerator;

import java.io.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class PerformanceMatrix { //todo split to separate classes PredefinedPerformanceMatrix and RandomPerformanceMatrix
    public enum GenerationMethod {
        VectorFromHypersphere("Hyper"),
        ElementFromUniform("Uniform"),
        ElementFromGauss("Gauss"),
        NA("Predefined"); // predefined performance matrix

        private final String stringValue;

        GenerationMethod(String stringValue) {
            this.stringValue = stringValue;
        }

        public String toString() {
            return this.stringValue;
        }
    }

    private final double[][] data;
    private final String identifier; // file name or seed
    private final GenerationMethod generationMethod;

    private PerformanceMatrix(double[][] data, String identifier, GenerationMethod generationMethod) {
        this.data = data;
        this.identifier = identifier;
        this.generationMethod = generationMethod;

        System.err.println("INFO: In '" + this.identifier + "' dominance relation: " + this.getNumberOfDominanceRelations() + ".");
    }

    public static PerformanceMatrix buildRandom(int rows, int columns, GenerationMethod method, int seed) {
        final double[][] data;

        if (method == GenerationMethod.ElementFromUniform) {
            data = new double[rows][columns];
            Random random = new Random(seed);

            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < columns; j++) {
                    data[i][j] = random.nextDouble();
                }
            }
        } else if (method == GenerationMethod.ElementFromGauss) {
            data = new double[rows][columns];
            Random random = new Random(seed);

            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < columns; j++) {
                    data[i][j] = random.nextGaussian();
                }
            }
        } else if (method == GenerationMethod.VectorFromHypersphere) {
            data = new double[rows][];
            UnitSphereRandomVectorGenerator generator = new UnitSphereRandomVectorGenerator(columns, new MersenneTwister(seed));

            for (int i = 0; i < rows; i++) {
                data[i] = generator.nextVector();

                for (int j = 0; j < columns; j++) {
                    data[i][j] = Math.abs(data[i][j]);
                }
            }
        } else {
            throw new IllegalArgumentException("method");
        }

        return new PerformanceMatrix(data, Integer.toString(seed), method);
    }


    public static PerformanceMatrix load(String path) throws FileNotFoundException {
        return PerformanceMatrix.load(path, "\t");
    }

    public static PerformanceMatrix load(String path, String separator) throws FileNotFoundException {
        double[][] data;

        List<double[]> lst = new ArrayList<double[]>();

        File f = new File(path);
        if (f.exists() && !f.isDirectory()) {
            try {
                BufferedReader br = new BufferedReader(new FileReader(f));

                String line;
                while ((line = br.readLine()) != null) {
                    if (line.length() > 0) {
                        String[] fields = line.split(separator);
                        double[] l = new double[fields.length];

                        for (int i = 0; i < fields.length; i++) {
                            l[i] = Double.parseDouble(fields[i]);
                        }

                        lst.add(l);
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            data = lst.toArray(new double[0][0]);
        } else {
            throw new FileNotFoundException(path);
        }

        return new PerformanceMatrix(data, f.getName().replaceAll("\\s+", "_"),
                GenerationMethod.NA);
    }

    private static boolean isVectorDominatedBy(double[] vector, double[] by) { //assume all criteria are to be maximized
        for (int i = 0; i < vector.length; i++) {
            if (vector[i] > by[i]) {
                return false;
            }
        }

        return true;
    }

    /**
     * @return Number of pairs, for which there is a dominance relation; number is in [0;(n(n-1)/2]
     */
    public int getNumberOfDominanceRelations() {
        int result = 0;

        for (int i = 0; i < data.length; i++) {
            for (int j = 0; j < data.length; j++) {
                if (i != j) {
                    if (PerformanceMatrix.isVectorDominatedBy(data[i], data[j])) {
                        result++;
//                        System.err.println("a_" + (j + 1) + " > " + "a_" + (i + 1));
                    }
                }
            }
        }

        return result;
    }

    public double[][] getData() {
        return data;
    }

    public String getIdentifier() {
        return identifier;
    }

    public GenerationMethod getGenerationMethod() {
        return generationMethod;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        DecimalFormat df = (DecimalFormat) NumberFormat.getNumberInstance(Locale.ENGLISH);

        for (double[] row : this.data) {
            boolean first = true;

            for (double element : row) {
                if (first) {
                    first = false;
                } else {
                    sb.append("\t");
                }

                sb.append(df.format(element));
            }

            sb.append("\n");
        }

        return sb.toString();
    }
}
