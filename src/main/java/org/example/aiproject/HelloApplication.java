package org.example.aiproject;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("grid.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 264, 253);
        stage.setTitle("Hello!");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) throws IOException {

            String fileName = "data.txt";
            List<double[]> inputList = new ArrayList<>();
            List<Double> labelList = new ArrayList<>();

            try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] tokens = line.trim().split("\\s+");
                    if (tokens.length == 4) {
                        double[] inputs = new double[3];
                        for (int i = 0; i < 3; i++) {
                            inputs[i] = Double.parseDouble(tokens[i]);
                        }
                        double label = Double.parseDouble(tokens[3]);
                        inputList.add(inputs);
                        labelList.add(label);
                    }
                }
            } catch (IOException e) {
                System.err.println("Error reading file: " + e.getMessage());
                return;
            }
            double[][] inputArray = inputList.toArray(new double[0][]);
            double[] labelArray = labelList.stream().mapToDouble(Double::doubleValue).toArray();


            Perceptron perceptron = new Perceptron(3);
            perceptron.train(inputArray, labelArray, 20);
            launch(args);


        }

    }
