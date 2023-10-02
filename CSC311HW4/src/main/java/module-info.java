module com.mycompany.csc311hw4 {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires com.google.gson;
    requires java.sql;

    opens com.mycompany.csc311hw4 to javafx.fxml, javafx.media, com.google.gson;
    exports com.mycompany.csc311hw4;
}
