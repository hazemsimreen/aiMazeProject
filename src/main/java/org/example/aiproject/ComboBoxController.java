package org.example.aiproject;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javax.swing.JOptionPane;

public class ComboBoxController {

    @FXML
    private ComboBox<String> coboBox;

    private Button targetButton;
    private GridController mainController;

    @FXML
    private TextField elevation;

    @FXML
    private RadioButton startingPointRadioButton;

    @FXML
    private RadioButton endingPointRadioButton;

    public void setTargetButton(Button button) {
        this.targetButton = button;
    }

    public void setMainController(GridController controller) {
        this.mainController = controller;
    }

    @FXML
    public void initialize() {
        coboBox.setItems(FXCollections.observableArrayList("Grass", "Water", "Obstacle"));
        coboBox.setValue("Grass");
    }

    @FXML
    void confirm(ActionEvent event) {
        String elevationValue = elevation.getText();

        try {
            int value = Integer.parseInt(elevationValue);
            if (value < 0 || value > 10) {
                JOptionPane.showMessageDialog(null,
                        "Elevation must be between 0 and 10.",
                        "Invalid Elevation",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            String selectedItem = coboBox.getValue();

            if (selectedItem != null && targetButton != null) {
                // Store the terrain type and elevation in the button's properties
                targetButton.getProperties().put("terrainType", selectedItem);
                targetButton.getProperties().put("elevation", value);

                // Handle starting/ending point selection
                if (startingPointRadioButton.isSelected()) {
                    if (selectedItem != null && !selectedItem.equals("Grass")) {
                        JOptionPane.showMessageDialog(null,
                                "Starting point can only be set on Grass terrain!",
                                "Invalid Terrain",
                                JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    if (value == 0) {
                        JOptionPane.showMessageDialog(null,
                                "Starting point can't have an elevation of zero",
                                "Invalid Elevation",
                                JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    mainController.setStartingPoint(targetButton);
                } else if (endingPointRadioButton.isSelected()) {
                    if (selectedItem != null && !selectedItem.equals("Grass")) {
                        JOptionPane.showMessageDialog(null,
                                "Ending point can only be set on Grass terrain!",
                                "Invalid Terrain",
                                JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    if (value == 0) {
                        JOptionPane.showMessageDialog(null,
                                "Ending point can't have an elevation of zero",
                                "Invalid Elevation",
                                JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    mainController.setEndingPoint(targetButton);
                } else {
                    // If neither radio button is selected and the button was the current starting or ending point
                    // => Reset it
                    if (Boolean.TRUE.equals(targetButton.getProperties().get("isStartingPoint"))) {
                        mainController.setStartingPoint(null);
                    }
                    if (Boolean.TRUE.equals(targetButton.getProperties().get("isEndingPoint"))) {
                        mainController.setEndingPoint(null);
                    }
                }

                // Set the button color based on terrain type
                switch (selectedItem) {
                    case "Grass":
                        targetButton.setStyle("-fx-background-color: mediumseagreen;");
                        break;
                    case "Water":
                        targetButton.setStyle("-fx-background-color: aqua;");
                        break;
                    case "Obstacle":
                        targetButton.setStyle("-fx-background-color: dimgray;");
                        break;
                }
            }

            targetButton.setText(String.valueOf(value));

            ((Button) event.getSource()).getScene().getWindow().hide();

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null,
                    "Please enter a valid number between 0 and 10.",
                    "Invalid Input",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
