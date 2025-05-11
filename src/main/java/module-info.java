module org.example.aiproject {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;


    opens org.example.aiproject to javafx.fxml;
    exports org.example.aiproject;
}