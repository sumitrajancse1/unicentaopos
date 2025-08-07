package com.unicenta.orderpop;

import com.unicenta.pos.forms.AppConfig;
import com.unicenta.pos.util.AltEncrypter;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

@Slf4j
public class OrderPop extends Application {

    private ScheduledExecutorService scheduler;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void init() {
        scheduler = Executors.newScheduledThreadPool(1, new OrderPopThreadFactory());
    }

    @Override
    public void start(Stage stage) {
        final ListView<String> orderView = new ListView<>();
        final ProgressIndicator databaseActivityIndicator = new ProgressIndicator();

        Image closeImage = new Image(getClass().getResourceAsStream("/com/unicenta/images/fileclose.png"));
        final Button closeOrderList = new Button("Close", new ImageView(closeImage));
        closeOrderList.setOnAction(e -> {
            try {
                e.consume();
                stop();
                Platform.exit();
            } catch (Exception ex) {
                log.error(ex.getMessage());
            }
        });

        Button completeOrder = new Button("Complete Order");
        completeOrder.setOnAction(event -> markOrderAsComplete(orderView.getSelectionModel().getSelectedItem()));

        Button itemComplete = new Button("Item Completed");
        itemComplete.setOnAction(event -> markItemAsComplete(orderView.getSelectionModel().getSelectedItem()));

        stage.setOnCloseRequest((WindowEvent e) -> {
            try {
                Platform.exit();
                stop();
            } catch (Exception ex) {
                log.error(ex.getMessage());
            }
        });

        VBox layout = new VBox(10);
        layout.setStyle("-fx-background-color: aliceblue; -fx-padding: 5;");
        HBox buttonLayout = new HBox(10, completeOrder, itemComplete, closeOrderList, databaseActivityIndicator);
        layout.getChildren().addAll(buttonLayout, orderView);
        layout.setPrefHeight(400);
        layout.setPrefWidth(500);

        stage.getIcons().add(new Image("/com/unicenta/images/unicentaopos.png"));
        stage.setTitle("Orders Waiting...");
        stage.setScene(new Scene(layout));
        stage.show();

        // Start the scheduled task to fetch orders periodically
        scheduler.scheduleAtFixedRate(() -> fetchDBOrdersListView(orderView), 0, 5, TimeUnit.SECONDS);
    }

    @Override
    public void stop() throws Exception {
        scheduler.shutdown();
        if (!scheduler.awaitTermination(3, TimeUnit.SECONDS)) {
            log.info("Scheduler did not shut down cleanly within 3 seconds");
        }
        Platform.exit();
    }

    private void fetchDBOrdersListView(ListView<String> listView) {
        FetchOrdersTask fetchOrdersTask = new FetchOrdersTask();
        fetchOrdersTask.setOnSucceeded(event -> {
            listView.getItems().clear();
            listView.getItems().addAll(fetchOrdersTask.getValue());
        });
        scheduler.submit(fetchOrdersTask);
    }

    private static class FetchOrdersTask extends Task<ObservableList<String>> {
        @Override
        protected ObservableList<String> call() {
            ObservableList<String> orders = FXCollections.observableArrayList();
            try (Connection con = getConnection()) {
                Statement st = con.createStatement();
                ResultSet rs = st.executeQuery("SELECT ticketid, orderid, ordertime, qty, details, displayid FROM orders WHERE displayid != 0 ORDER BY ordertime, orderid");
                while (rs.next()) {
                    orders.add(String.format("%s - %s - %s - %s * %s - %s",
                        rs.getString("ticketid"),
                        rs.getString("orderid"),
                        rs.getString("ordertime"),
                        rs.getString("qty"),
                        rs.getString("details"),
                        rs.getInt("displayid")));
                }
            } catch (SQLException ex) {
                log.error("Failed to fetch orders: " + ex.getMessage());
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(OrderPop.class.getName()).log(Level.SEVERE, null, ex);
            }
            return orders;
        }
    }

    private void markOrderAsComplete(String orderDetails) {
        if (orderDetails == null || orderDetails.isEmpty()) {
            return; // No order selected
        }

        // Extract relevant data from the selected order details
        String[] details = orderDetails.split(" - ");
        String ticketId = details[0];  // Assuming ticketId is in the first part
        String orderId = details[1];   // Assuming orderId is in the second part

        try (Connection con = getConnection()) {
            PreparedStatement ps = con.prepareStatement("UPDATE orders SET displayid = ?, completetime = ? WHERE ticketid = ? AND orderid = ?");
            ps.setInt(1, 0); // Set displayid to 0 to mark the order as completed
            ps.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now())); // Set the completion time
            ps.setString(3, ticketId); // Use ticketid for the WHERE clause
            ps.setString(4, orderId); // Use orderid for the WHERE clause
            ps.executeUpdate();
            log.info("Order {} marked as completed.", orderId);
        } catch (SQLException | ClassNotFoundException ex) {
            log.error("Failed to mark order as complete: " + ex.getMessage());
        }
    }

    private void markItemAsComplete(String itemDetails) {
        if (itemDetails == null || itemDetails.isEmpty()) {
            return; // No item selected
        }

        // Extract relevant data from the selected item details
        String[] details = itemDetails.split(" - ");
        String ticketId = details[0];  // Assuming ticketId is in the first part
        String orderId = details[1];   // Assuming orderId is in the second part
        String itemDetailsStr = details[4];  // Assuming details of the item are in the fifth part

        try (Connection con = getConnection()) {
            PreparedStatement ps = con.prepareStatement("UPDATE orders SET displayid = ? WHERE ticketid = ? AND orderid = ? AND details = ?");
            ps.setInt(1, 0); // Set displayid to 0 to mark the item as completed
            ps.setString(2, ticketId); // Use ticketid for the WHERE clause
            ps.setString(3, orderId); // Use orderid for the WHERE clause
            ps.setString(4, itemDetailsStr); // Use details for the WHERE clause
            ps.executeUpdate();
            log.info("Item {} from order {} marked as completed.", itemDetailsStr, orderId);
        } catch (SQLException | ClassNotFoundException ex) {
            log.error("Failed to mark item as complete: " + ex.getMessage());
        }
    }

    private static Connection getConnection() throws ClassNotFoundException, SQLException {
        log.info("Get DB connection");
        String url = AppConfig.getInstance().getProperty("db.URL")
            + AppConfig.getInstance().getProperty("db.schema")
            + AppConfig.getInstance().getProperty("db.options");
        String user = AppConfig.getInstance().getProperty("db.user");
        String password = AppConfig.getInstance().getProperty("db.password");
        if (user != null && password != null && password.startsWith("crypt:")) {
            AltEncrypter cypher = new AltEncrypter("cypherkey" + user);
            password = cypher.decrypt(password.substring(6));
        }
        return DriverManager.getConnection(url, user, password);
    }

    private static class OrderPopThreadFactory implements ThreadFactory {
        static final AtomicInteger poolNumber = new AtomicInteger(1);
        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r, "OrderPop-Scheduler-" + poolNumber.getAndIncrement() + "-thread");
            thread.setDaemon(true);
            return thread;
        }
    }
}
