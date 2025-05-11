package org.example.aiproject;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class TieController {

    @FXML
    private TextField columnsCount;

    @FXML
    private Button create;

    @FXML
    private TextField rowsCount;

    Perceptron perceptron;

    public TieController() {
        trainPerceptronUsingInputData();
    }
    
    

    @FXML
    public void createGrid(ActionEvent actionEvent) {

        try {
            int rows = Integer.parseInt(rowsCount.getText());
            int columns = Integer.parseInt(columnsCount.getText());

            GridPane grid = new GridPane();
            grid.setPrefSize(500, 500);
            grid.setGridLinesVisible(true);

            for (int col = 0; col < columns; col++) {
                ColumnConstraints colConst = new ColumnConstraints();
                colConst.setPercentWidth(100.0 / columns);
                grid.getColumnConstraints().add(colConst);
            }

            for (int row = 0; row < rows; row++) {
                RowConstraints rowConst = new RowConstraints();
                rowConst.setPercentHeight(100.0 / rows);
                grid.getRowConstraints().add(rowConst);
            }

            for (int row = 0; row < rows; row++) {
                for (int col = 0; col < columns; col++) {
                    Button button = new Button();
                    button.setMinSize(0, 0);
                    button.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

                    grid.add(button, col, row);

                    GridPane.setHgrow(button, Priority.ALWAYS);
                    GridPane.setVgrow(button, Priority.ALWAYS);

                    button.setOnAction(e -> showDiallogBox(button));
                }
            }

            Scene gridScene = new Scene(grid, 500, 500);
            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            stage.setScene(gridScene);

        } catch (NumberFormatException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Invalid Input");
            alert.setHeaderText("Please enter valid numbers for rows and columns.");
            alert.showAndWait();
        }
    }

    @FXML
    public void showDiallogBox(Button clickedButton) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("combo.fxml"));
            Parent root = loader.load();

            ComboBoxController comboController = loader.getController();
            comboController.setTargetButton(clickedButton);

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Manage");
            dialogStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            dialogStage.setScene(new Scene(root, 358, 254));
            dialogStage.centerOnScreen();
            dialogStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void trainPerceptronUsingInputData() {
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

        perceptron = new Perceptron(3);
        perceptron.train(inputArray, labelArray, 20);
    }

}
