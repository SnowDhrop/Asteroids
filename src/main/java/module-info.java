module org.sample.asteroids {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;

    opens org.sample.asteroids to javafx.fxml;
    exports org.sample.asteroids;
}