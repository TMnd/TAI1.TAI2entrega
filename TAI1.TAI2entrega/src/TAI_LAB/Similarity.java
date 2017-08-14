/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package TAI_LAB;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

public class Similarity {

    private HashMap<String, Double> AuthorSimi = new HashMap<>();
    private HashMap<String, HashMap<String, HashMap<String, Float>>> authorTable = new HashMap<>();
    private HashMap<String, HashMap<String, Float>> lineSum = new HashMap<>();
    private ArrayList<Integer> usedFiles = new ArrayList<>();
    private char[] charAlphabet;
    private int alphabetSize = 0;
    private int contHAMILTON = 0;
    private int contJAY = 0;
    private int contMADISON = 0;
    Random random = new Random();

    public void iterateReferenceFiles(String path, String response, int order, float alpha, int limite) {
        File dir = new File(path);
        File[] directoryListing = dir.listFiles();
        String text = null;

        if (directoryListing == null || directoryListing.length == 0) {
            createModel(text, path, response, order, limite);
        } else {
            if (response.equals("no")) {
                for (File child : directoryListing) {
                    if (child.isDirectory()) {
                        iterateReferenceFiles(child.getPath(), response, order, alpha, limite);
                    } else if (child.isFile()) {
                        createModel(text, child.getPath(), response, order, limite);
                    }
                }
            } else {
                while (contHAMILTON < limite || contJAY < limite || contMADISON < limite) {
                    int num = random.nextInt(directoryListing.length);
                    while (usedFiles.contains(num + 1)) {
                        num = random.nextInt(directoryListing.length);
                    }
                    if (directoryListing[num].isDirectory()) {
                        iterateReferenceFiles(directoryListing[num].getPath(), response, order, alpha, limite);
                    } else if (directoryListing[num].isFile()) {
                        if (createModel(text, directoryListing[num].getPath(), response, order, limite) == 1) {
                            usedFiles.add(num + 1);
                        }
                    }
                }
                Collections.sort(usedFiles);
                System.out.println("Files used (1-85): " + usedFiles);
            }
        }
        calculateWithAlpha(alpha);
    }

    public void iterateTargetFiles(String targetPath, int order) {
        File dir = new File(targetPath);
        File[] directoryListing = dir.listFiles();

        if (directoryListing == null || directoryListing.length == 0) {
            similarFiles(targetPath, order);
        } else {
            for (File child : directoryListing) {
                if (child.isDirectory()) {
                    iterateTargetFiles(child.getPath(), order);
                } else if (child.isFile()) {
                    similarFiles(child.getPath(), order);
                }
            }
        }
    }

    public int createModel(String text, String path, String response, int order, int limite) {
        text = readFile(path);
        String author = null;
        String t = null;

        criarAlfabeto(parseText(text).toLowerCase());

        if (response.equals("yes")) {
            author = getAuthor(text);

            /* Para saltar os casos em que é apresentado mais do que 1 autor*/
            if (author == null) {
                return -1;
            }

            if (author.equals("HAMILTON") && contHAMILTON != limite) {
                contHAMILTON++;
                t = parseText(text).substring(parseText(text).lastIndexOf(author) + author.length() + 1).toLowerCase();
            } else if (author.equals("JAY") && contJAY != limite) {
                contJAY++;
                t = parseText(text).substring(parseText(text).lastIndexOf(author) + author.length() + 1).toLowerCase();
            } else if (author.equals("MADISON") && contMADISON != limite) {
                contMADISON++;
                t = parseText(text).substring(parseText(text).lastIndexOf(author) + author.length() + 1).toLowerCase();
            } else {
                return -1;
            }
        } else if (response.equals("no")) {
            t = parseText(text).toLowerCase();
            author = path.substring(path.lastIndexOf("\\") + 1, path.lastIndexOf("."));
        }
        fillAuthorTable(author, t.trim(), order);
        return 1;
    }

    public int getMinLimit(String collectionPath) {
        File dir = new File(collectionPath);
        File[] directoryListing = dir.listFiles();
        int h = 0, j = 0, m = 0;

        for (File child : directoryListing) {
            if (child.isDirectory()) {
                getMinLimit(child.getPath());
            } else if (child.isFile()) {
                //System.out.println(getAuthor(readFile(child.getPath())));
                String author = getAuthor(readFile(child.getPath()));
                if (author != null) {
                    switch (author) {
                        case "HAMILTON":
                            h++;
                            break;
                        case "MADISON":
                            m++;
                            break;
                        case "JAY":
                            j++;
                            break;
                    }
                }
            }
        }
        /*System.out.println("h: " + h);
         System.out.println("j: " + j);
         System.out.println("m: " + m);*/
        return h < m ? (h < j ? h : j) : (m < j ? m : j);
    }

    public String getStartingPosition(String text) {
        String[] parsedWords = parseText(text).trim().split(" ");
        ArrayList<String> words = new ArrayList<>(Arrays.asList(parsedWords));

        for (String word : words) {
            if (!word.isEmpty() && (word.equals(Authors.HAMILTON.name()) || word.equals(Authors.JAY.name()) || word.equals(Authors.MADISON.name()))) {
                if (!words.get(words.indexOf(word) + 1).equals("AND")
                        && !words.get(words.indexOf(word) + 1).equals("OR")) {
                    return words.get(words.indexOf(word) + 1);
                } else {
                    return words.get(words.indexOf(word) + 3);
                }
            }
        }
        return null;
    }

    public void similarFiles(String path, int order) {
        String text = parseText(readFile(path)).trim();
        String startPos = getStartingPosition(readFile(path));
        if (startPos != null) {
            text = text.substring(text.indexOf(getStartingPosition(readFile(path)))).toLowerCase();
        } else {
            text = text.toLowerCase();
        }
        double calculo = 0;
        double val = 0;

        for (Map.Entry<String, HashMap<String, HashMap<String, Float>>> parent : authorTable.entrySet()) {

            String pKey = parent.getKey();

            for (int i = 0; i < text.length() - order; i++) {
                if (authorTable.containsKey(pKey) && authorTable.get(pKey).containsKey(text.substring(i, i + order)) && authorTable.get(pKey).get(text.substring(i, i + order)).containsKey(String.valueOf(text.charAt(i + order)))) {
                    val = authorTable.get(pKey).get(text.substring(i, i + order)).get(String.valueOf(text.charAt(i + order)));
                }

                if (val == 0) {
                    calculo -= logOfBase((float) 1 / (float) alphabetSize);
                } else {
                    calculo -= logOfBase(val);
                }
            }

            AuthorSimi.put(pKey, calculo);
            calculo = 0;
            val = 0;
        }

        String closestAuthor = getClosestAuthor();
        System.out.println("\nTarget: " + path.substring(path.lastIndexOf("\\") + 1, path.indexOf(".")));
        showResults();
        System.out.printf("Closest: %s with %.0f bits\n", closestAuthor, AuthorSimi.get(closestAuthor));
    }

    public String getClosestAuthor() {
        double value = 0;
        String author = null;

        for (Map.Entry<String, Double> entrySet : AuthorSimi.entrySet()) {
            String key = entrySet.getKey();
            Double value1 = entrySet.getValue();

            if (value == 0 || value1 < value) {
                value = value1;
                author = key;
            }
        }
        return author;
    }

    public void showResults() {
        AuthorSimi.entrySet().stream().forEach((entrySet) -> {
            String key = entrySet.getKey();
            Double value = entrySet.getValue();

            System.out.printf("%s with %.0f bits\n", key, value);
        });
    }

    public void calculateWithAlpha(float alpha) {
        if (alpha != 0) {
            lineSum();
            for (Map.Entry<String, HashMap<String, HashMap<String, Float>>> author : authorTable.entrySet()) {
                String aKey = author.getKey();
                HashMap<String, HashMap<String, Float>> aValue = author.getValue();

                for (Map.Entry<String, HashMap<String, Float>> parent : aValue.entrySet()) {
                    String pKey = parent.getKey();
                    HashMap<String, Float> pValue = parent.getValue();

                    for (Map.Entry<String, Float> child : pValue.entrySet()) {
                        String cKey = child.getKey();
                        /* Fórmula denominador: Soma da linha + (alpha * dimensão alfabeto) */
                        float numerador = (float) authorTable.get(aKey).get(pKey).get(cKey) + alpha;
                        float denominador = (float) lineSum.get(aKey).get(pKey) + (alphabetSize * alpha);
                        authorTable.get(aKey).get(pKey).replace(cKey, numerador / denominador);
                    }
                }
            }
        }
    }

    private void lineSum() {
        for (Map.Entry<String, HashMap<String, HashMap<String, Float>>> author : authorTable.entrySet()) {
            String aKey = author.getKey();
            HashMap<String, HashMap<String, Float>> aValue = author.getValue();

            for (Map.Entry<String, HashMap<String, Float>> parent : aValue.entrySet()) {
                String pKey = parent.getKey();
                HashMap<String, Float> pValue = parent.getValue();

                float sum = 0;
                for (Map.Entry<String, Float> child : pValue.entrySet()) {
                    String cKey = child.getKey();
                    Float cValue = child.getValue();
                    sum += cValue;
                }
                if (!lineSum.containsKey(aKey)) {
                    lineSum.put(aKey, new HashMap<>());
                }
                lineSum.get(aKey).put(pKey, sum);
            }
        }
    }

    public void fillAuthorTable(String author, String text, int order) {
        if (!authorTable.containsKey(author)) {
            authorTable.put(author, new HashMap<>());
            authorTable.replace(author, fillTable(authorTable.get(author), text.toLowerCase(), order));
        } else {

            for (int i = 0; i < text.length() - order; i++) {
                if (!authorTable.get(author).containsKey(text.substring(i, i + order))) {

                    authorTable.get(author).put(text.substring(i, i + order), new HashMap<>());

                    for (int j = 0; j < charAlphabet.length; j++) {
                        authorTable.get(author).get(text.substring(i, i + order)).put(String.valueOf(charAlphabet[j]), (float) 0);
                    }
                }

                if (authorTable.get(author).get(text.substring(i, i + order)).containsKey(String.valueOf(text.charAt(i + order)))) {
                    authorTable.get(author).get(text.substring(i, i + order)).replace(String.valueOf(text.charAt(i + order)),
                            authorTable.get(author).get(text.substring(i, i + order)).get(String.valueOf(text.charAt(i + order))) + 1);
                } else {
                    //System.out.println(String.valueOf(text.charAt(i + order)));
                    //System.out.println(authorTable.get(author).get(text.substring(i, i + order)).keySet());
                }
            }
        }
    }

    /**
     * Get author from introduced text
     *
     * @param texto
     * @return
     */
    public String getAuthor(String texto) {
        String[] parsedWords = parseText(texto).trim().split(" ");
        ArrayList<String> words = new ArrayList<>(Arrays.asList(parsedWords));

        for (String word : words) {
            if (!word.isEmpty() && (word.equals(Authors.HAMILTON.name()) || word.equals(Authors.JAY.name()) || word.equals(Authors.MADISON.name()))) {
                if (!words.get(words.indexOf(word) + 1).equals("AND")
                        && !words.get(words.indexOf(word) + 1).equals("OR")) {
                    return word;
                } else {
                    return null;
                }
            }
        }
        return null;
    }

    public String parseText(String text) {
        return text.replaceAll("[^\\p{L}\\p{Z}]", " ").replaceAll(" +", " ");
    }

    /**
     * Function to read the text file
     *
     * @param filePath
     * @return
     */
    public String readFile(String filePath) {
        String text = "";
        try {
            text = Files.lines(Paths.get(filePath)).collect(Collectors.joining("\n"));
        } catch (IOException ex) {
            System.err.println("Erro ao ler o ficheiro!");
        }
        return text;
    }

    /**
     * Function that generates the alphabet from the text inserted.
     *
     * @param texto
     */
    public void criarAlfabeto(String texto) {
        int StringTamanho = texto.length();
        char[] ArrayCaracteres = new char[StringTamanho];
        int tamanhoArray = 0;

        for (int i = 0; i < texto.length(); i++) {
            char letra = texto.charAt(i);
            boolean existeLetra = false;

            for (int y = 0; y < tamanhoArray; y++) {
                if (letra == ArrayCaracteres[y]) {
                    existeLetra = true;
                    break;
                }
            }

            if (!existeLetra) {
                ArrayCaracteres[tamanhoArray] = letra;
                tamanhoArray++;
            }
        }

        charAlphabet = new char[tamanhoArray];

        for (int i = 0; i < ArrayCaracteres.length; i++) {
            if (ArrayCaracteres[i] != '\u0000') {
                charAlphabet[i] = ArrayCaracteres[i];
            }
        }
        alphabetSize = charAlphabet.length;
    }

    /**
     * Function to fill the table and cumulativeSum HashMaps with all the
     * information gathered from the text file.
     *
     * @param table
     * @param text
     * @param order
     * @return
     */
    public HashMap<String, HashMap<String, Float>> fillTable(HashMap<String, HashMap<String, Float>> table, String text, int order) {
        for (int i = 0; i < text.length() - order; i++) {
            if (!table.containsKey(text.substring(i, i + order))) {

                table.put(text.substring(i, i + order), new HashMap<>());
                for (int j = 0; j < charAlphabet.length; j++) {
                    table.get(text.substring(i, i + order)).put(String.valueOf(charAlphabet[j]), (float) 0);
                }
            }
            table.get(text.substring(i, i + order)).replace(String.valueOf(text.charAt(i + order)),
                    table.get(text.substring(i, i + order)).get(String.valueOf(text.charAt(i + order))) + 1);
        }
        return table;
    }

    /**
     * Change logarithm of base "e" to a base defined by the user.
     *
     * @param num
     * @return
     */
    public double logOfBase(double num) {
        return Math.log(num) / Math.log(2);
    }
}
