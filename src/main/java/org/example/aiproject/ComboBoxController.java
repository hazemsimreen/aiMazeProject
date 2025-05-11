package org.example.aiproject;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

public class ComboBoxController {

    @FXML
    private ComboBox<String> coboBox;

    private Button targetButton;

    @FXML
    private TextField elevation;



    public void setTargetButton(Button button) {
        this.targetButton = button;
    }

    @FXML
    public void initialize() {
        coboBox.setItems(FXCollections.observableArrayList("Grass", "Water", "Obstacle"));
    }

    @FXML
    void confirm(ActionEvent event) {
        String elevationValue = elevation.getText();

        try {
            int value = Integer.parseInt(elevationValue);
            if (value < 0 || value > 10) {
                System.out.println("Elevation must be between 0 and 10.");
                return;
            }

            System.out.println("Elevation value: " + value);


            String selectedItem = coboBox.getValue();

            if (selectedItem != null && targetButton != null) {
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


            ((Button) event.getSource()).getScene().getWindow().hide();

        } catch (NumberFormatException e) {
            System.out.println("Please enter a valid number between 0 and 10.");
        }
    }

}
