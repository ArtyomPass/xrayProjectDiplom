module com.example.funproject {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.xml;


    opens com.example.funproject to javafx.fxml;
    exports com.example.funproject;
}