/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package TAI_LAB;

import java.io.File;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        int n = 0, limite = 0;
        String option = "";
        float alpha = 0;
        Scanner sc = new Scanner(System.in);
        Similarity simi = new Similarity();

        while (!(option.equals("1"))) {
            System.out.println("\n1. Text Similarity");
            System.out.println("2. Exit");
            System.out.print("\nChoose an option: ");
            option = sc.next();

            if (option.equals("1")) {
                String collectionPath = "";
                File file = null;
                while (file == null || !file.exists() || !file.isDirectory()) {
                    System.out.print("Enter the collection path: ");
                    collectionPath = sc.next();
                    file = new File(collectionPath);
                    if (!file.exists() || !file.isDirectory()) {
                        System.err.println("Error reading the file/collection!");
                    }
                }

                while (n <= 0) {
                    System.out.print("Enter the order number: ");
                    String input = sc.next();
                    try {
                        n = Integer.parseInt(input);
                    } catch (NumberFormatException e) {
                        System.err.println("Wrong format! Please enter an integer!");
                    }
                }
                while (alpha <= 0) {
                    System.out.print("Enter the alpha number: ");
                    String input = sc.next();
                    try {
                        alpha = Integer.parseInt(input);
                    } catch (NumberFormatException e) {
                        System.err.println("Wrong format! Please enter an integer!");
                    }
                }
                System.out.print("You want to parse author ? (yes/no) ");
                String response = sc.next();

                while (!response.equals("yes") && !response.equals("no")) {
                    System.out.print("You want to parse author ? (yes/no)");
                    response = sc.next();
                }

                String targetPath = "";
                file = null;
                while (file == null || !file.exists()) {
                    System.out.print("Enter the target collection/file path: ");
                    targetPath = sc.next();
                    file = new File(targetPath);
                    if (!file.exists()) {
                        System.err.println("Error reading the file/collection!");
                    }
                }
                if (response.equals("yes")) {
                    int min = simi.getMinLimit(collectionPath);

                    while (limite <= 0 || limite > min) {
                        System.out.print("Insert the file limit per author (1-" + min + "): ");
                        String input = sc.next();
                        try {
                            limite = Integer.parseInt(input);
                        } catch (NumberFormatException e) {
                            System.err.println("Wrong format! Please enter an integer!");
                        }
                    }
                }

                simi.iterateReferenceFiles(collectionPath, response, n, alpha, limite);
                simi.iterateTargetFiles(targetPath, n);
            }

            if (option.equals("2")) {
                System.exit(0);
            }
        }
    }
}
