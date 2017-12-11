package lab2;
import javax.script.ScriptException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Skeleton for NERC task.
 *
 * @author Fabian M. Suchanek
 */
public class Nerc {

    /**
     * Labels that we will attach to the words
     */
    public enum Class {
        ARTIFACT, EVENT, GEO, NATURAL, ORGANIZATION, PERSON, TIME, OTHER
    }
    private static HashMap<String, Set<String>> m1 = new HashMap<String, Set<String>>();
    private static HashMap<String, Set<String>> m2 = new HashMap<String, Set<String>>();
    /**
     * Determines the class for the word at position 0 in the window
     */
    private static Class findClass(Window window) {
        Class c = Class.OTHER;
        int num = 0;
        if (m2.get(window.getWordAt(0)).toArray().length == 1)
            num = 0;
        else {
            String str = window.getWordAt(0)+window.getTagAt(0);
            str = m1.get(str).toArray()[0].toString();
            num = java.util.Arrays.asList(m2.get(window.getWordAt(0)).toArray()).indexOf(str);
        }
        switch (m2.get(window.getWordAt(0)).toArray()[num].toString()) {
            case "ARTIFACT":
                c = Class.ARTIFACT;
                break;
            case "EVENT":
                c = Class.EVENT;
                break;
            case "GEO":
                c = Class.GEO;
                break;
            case "NATURAL":
                c = Class.NATURAL;
                break;
            case "ORGANIZATION":
                c = Class.ORGANIZATION;
                break;
            case "PERSON":
                c = Class.PERSON;
                break;
            case "TIME":
                c = Class.TIME;
                break;
            case "OTHER":
                c = Class.OTHER;
                break;
        }
        return c;
    }
    private static Boolean isCapWord(String str) {
        return str.matches("^[A-Z][a-z]+");
    }

    private static Boolean isAllUpLetters(String str) {
        return str.matches("[A-Z]+");
    }

    /**
     * Takes as arguments:
     * (1) a testing file with sentences
     * (2) optionally: a training file with labeled sentences
     * <p>
     * Writes to the file result.tsv lines of the form
     * X-WORD \t CLASS
     * where X is a sentence number, WORD is a word, and CLASS is a class.
     */
//    private static HashMap<String,Double> m2 = new HashMap<String,Double>();
//    private static int sumAscii(String str){
//        int num = 0;
//        for (int i = 0; i < str.length(); i++) {
//            num+=(int)str.charAt(i);
//        }
//        return num;
//    }
//    public static void train(String trainFile,KNN<Nerc.Class> knn) throws IOException {
//        try (BufferedReader in = Files.newBufferedReader(Paths.get(trainFile))) {
//            String line;
//            Double d = 0.0;
//            while (null != (line = in.readLine())) {
//                d++;
//                m2.putIfAbsent(line.split("\t")[2],d);
//            }
//        }
//        try (BufferedReader in = Files.newBufferedReader(Paths.get(trainFile))) {
//            String line;
//            Double d = 0.0;
//            while (null != (line = in.readLine())) {
//                String[] split = line.split("\t");
//                switch (split[3]) {
//                    case "ARTIFACT":
//                        knn.addTrainingExample(Nerc.Class.ARTIFACT, sumAscii(split[1]),m2.get(split[2]));
//                        break;
//                    case "EVENT":
//                        knn.addTrainingExample(Nerc.Class.EVENT, sumAscii(split[1]),m2.get(split[2]));
//                        break;
//                    case "GEO":
//                        knn.addTrainingExample(Nerc.Class.GEO, sumAscii(split[1]),m2.get(split[2]));
//                        break;
//                    case "NATURAL":
//                        knn.addTrainingExample(Nerc.Class.NATURAL, sumAscii(split[1]),m2.get(split[2]));
//                        break;
//                    case "ORGANIZATION":
//                        knn.addTrainingExample(Nerc.Class.ORGANIZATION, sumAscii(split[1]),m2.get(split[2]));
//                        break;
//                    case "PERSON":
//                        knn.addTrainingExample(Nerc.Class.PERSON, sumAscii(split[1]),m2.get(split[2]));
//                        break;
//                    case "TIME":
//                        knn.addTrainingExample(Nerc.Class.TIME, sumAscii(split[1]),m2.get(split[2]));
//                        break;
//                    case "OTHER":
//                        knn.addTrainingExample(Nerc.Class.OTHER, sumAscii(split[1]),m2.get(split[2]));
//                        break;
//                }
//            }
//        }
//    }
    public static void main(String[] args) throws IOException, ScriptException {
        args = new String[]{"ner-test.tsv", "ner-train.tsv"};
        // EXPERIMENTAL: If you wish, you can train a KNN classifier here
        // on the file args[1].
        // KNN<Nerc.Class> knn = new KNN<>(5);
        // knn.addTrainingExample(Nerc.Class.ARTIFACT, 1, 2, 3);

        try (BufferedReader in = Files.newBufferedReader(Paths.get(args[1]))) {
            String line;
            String[] split;
            while (null != (line = in.readLine())) {
                split = line.split("\t");
                if (!m2.containsKey(split[1])) {
                    Set<String> ss = new LinkedHashSet<String>();
                    ss.add(split[3]);
                    m2.putIfAbsent(split[1], ss);
                    m1.putIfAbsent(split[1]+split[2],ss);
                } else {
                    m2.get(split[1]).add(split[3]);
                }

            }
        }

        try (BufferedWriter out = Files.newBufferedWriter(Paths.get("result.tsv"))) {
            try (BufferedReader in = Files.newBufferedReader(Paths.get(args[0]))) {
                String line;
                Window window = new Window(5);
                while (null != (line = in.readLine())) {
                    window.add(line);
                    if (window.getWordAt(-window.width) == null) continue;
                    try {
                        Class c = findClass(window);
                        if (c != null && c != Class.OTHER)
                            out.write(window.getSentenceNumberAt(0) + "-" + window.getWordAt(0) + "\t" + c + "\n");
                    } catch (NullPointerException e) {
                        String s = window.getWordAt(0);
                        String POS = window.getTagAt(0);
//                        out.write(window.getSentenceNumberAt(0) + "-" + s + "\t" + Class.GEO + "\n");
                        if(isCapWord(s)&&window.getWordAt(-1).equals("in"))
                            out.write(window.getSentenceNumberAt(0) + "-" + s + "\t" + Class.GEO + "\n");

                        if(isCapWord(s)&&(window.getWordAt(-1).equals("Dr.")||window.getWordAt(-1).equals("Ms.")||window.getWordAt(-1).equals("Mr.")||window.getWordAt(1).equals("says")))
                            out.write(window.getSentenceNumberAt(0) + "-" + s + "\t" + Class.PERSON + "\n");

                        if(isAllUpLetters(s)&&POS.equals("NNP")&&(window.getWordAt(-1).equals("the")||window.getWordAt(-1).equals("a")||window.getWordAt(-1).equals("an")))
                            out.write(window.getSentenceNumberAt(0) + "-" + s + "\t" + Class.ORGANIZATION + "\n");

                        if(isAllUpLetters(s)&&POS.equals("NNP")&&window.getWordAt(1).equals("is"))
                            out.write(window.getSentenceNumberAt(0) + "-" + s + "\t" + Class.ORGANIZATION + "\n");

                    }

                }
            }
        }
    }
}