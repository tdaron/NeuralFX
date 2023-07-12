module org.kaporos.neuralfx {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.controlsfx.controls;
    opens org.kaporos.neuralfx to javafx.fxml;
    exports org.kaporos.neuralfx;
}