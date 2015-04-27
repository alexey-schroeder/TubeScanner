package tubeScanner;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseEvent;
import javafx.util.Callback;
import tubeScanner.code.events.SearchCodeEvent;
import tubeScanner.code.frameSorce.FrameSource;
import tubeScanner.code.graph.*;
import tubeScanner.code.searchModeHandlers.SearchMultipleCodeModeHandler;
import tubeScanner.code.searchModeHandlers.SearchSingleCodeModeHandler;

import java.util.HashSet;

public class Controller {
    @FXML
    private Label numberOfCodes;
    @FXML
    private TableColumn<Node, String> codeTableColumn;
    @FXML
    private TableView<Node> table;
    @FXML
    private Canvas canvas;
    @FXML
    private Canvas graphPane;
    private FrameSource frameSource;
    private Graph graph;
    private CanvasGraphVisualiser canvasGraphVisualiser;
    private boolean stop;
    private SearchSingleCodeModeHandler searchSingleCodeModeHandler;
    private SearchMultipleCodeModeHandler searchMultipleCodeModeHandler;
    int oldGraphSize;
    private String searchedCode;
    private String lastFoundedCode;

    public void reset(ActionEvent actionEvent) {
        graph.clear();
        searchMode = SearchMode.MULTIPLE;
        GraphicsContext gc = graphPane.getGraphicsContext2D();
        gc.clearRect(0, 0, graphPane.getWidth(), graphPane.getHeight());
        table.getItems().clear();
        numberOfCodes.setText("0");
        oldGraphSize = 0;
    }

    public enum SearchMode {
        SINGLE, MULTIPLE
    }

    private SearchMode searchMode = SearchMode.MULTIPLE;

    public void initialize() {
        graphPane.setStyle("-fx-border-color: red;");

        graph = new Graph();
        canvasGraphVisualiser = new CanvasGraphVisualiser();
        canvasGraphVisualiser.setCanvas(graphPane);
        canvasGraphVisualiser.setController(this);

        searchSingleCodeModeHandler = new SearchSingleCodeModeHandler();
        searchSingleCodeModeHandler.setCanvas(canvas);
        searchSingleCodeModeHandler.setController(this);

        searchMultipleCodeModeHandler = new SearchMultipleCodeModeHandler();
        searchMultipleCodeModeHandler.setCanvas(canvas);
        searchMultipleCodeModeHandler.setController(this);
        EventHandler<SearchCodeEvent> searchCodeEventEventHandler = createSearchCodeEventEventHandler();
        canvasGraphVisualiser.setSearchCodeEventEventHandler(searchCodeEventEventHandler);
        codeTableColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Node, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<Node, String> param) {
                return new SimpleStringProperty(param.getValue().getCode());
            }
        });

        table.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                Node selectedNode = table.getSelectionModel().getSelectedItem();
                canvasGraphVisualiser.setMarkedNodeByCode(selectedNode.getCode());
            }
        });
    }

    private EventHandler<SearchCodeEvent> createSearchCodeEventEventHandler() {
        EventHandler<SearchCodeEvent> searchCodeEventEventHandler = new EventHandler<SearchCodeEvent>() {
            @Override
            public void handle(SearchCodeEvent event) {
                String code = event.getCode();
                if (code != null) {
                    searchedCode = event.getCode();
                    searchSingleCodeModeHandler.setCode(searchedCode);
                    searchMode = SearchMode.SINGLE;
                } else {
                    searchMode = SearchMode.MULTIPLE;
                }
            }
        };
        return searchCodeEventEventHandler;
    }

    public void start() throws Exception {
        startThread();
    }

    public void stop() {
        stop = true;
    }

    private void startThread() throws Exception {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!stop) {
                    threadCode();
                }
                frameSource.stop();
            }
        });
        thread.start();
    }

    private void threadCode() {
        if (searchMode == SearchMode.MULTIPLE) {
            searchMultipleCodeModeHandler.threadCode();
        } else {
            double radius = searchMultipleCodeModeHandler.getRadius();
            searchSingleCodeModeHandler.setRadius(radius);
            String foundedCode = searchSingleCodeModeHandler.threadCode();
            if (foundedCode != null) {
                if (lastFoundedCode == null) {
                    lastFoundedCode = searchedCode;
                }
                if (!foundedCode.equalsIgnoreCase(lastFoundedCode)) {
                    canvasGraphVisualiser.unmarkNodeByCode(lastFoundedCode);
                    lastFoundedCode = foundedCode;
                }
                if (!foundedCode.equalsIgnoreCase(searchedCode)) {
                    showFoundedCodeInGraph(foundedCode);
                }
            }
        }
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                refresh();
            }
        });
    }

    private void showFoundedCodeInGraph(String foundedCode) {
        canvasGraphVisualiser.markNodeByCode(foundedCode);
    }

    private void refreshNumberOfCodes(int size) {
        numberOfCodes.setText(String.valueOf(size));
    }

    private void refreshTable() {
        ObservableList<Node> nodes = FXCollections.observableArrayList();
        HashSet<Node> allNodes = graph.getAllNodes();
        nodes.addAll(allNodes);
        table.setItems(nodes);
    }

    public void refresh() {
        int currentGraphSize = graph.getAllNodes().size();
        if (currentGraphSize > oldGraphSize) {
            oldGraphSize = currentGraphSize;
            refreshTable();
            refreshNumberOfCodes(currentGraphSize);
        }
    }

    public FrameSource getFrameSource() {
        return frameSource;
    }

    public void setFrameSource(FrameSource frameSource) {
        this.frameSource = frameSource;
        searchMultipleCodeModeHandler.setFrameSource(frameSource);
        searchSingleCodeModeHandler.setFrameSource(frameSource);
    }

    public Graph getGraph() {
        return graph;
    }

    public void setGraph(Graph graph) {
        this.graph = graph;
    }

    public Canvas getGraphPane() {
        return graphPane;
    }

    public CanvasGraphVisualiser getCanvasGraphVisualiser() {
        return canvasGraphVisualiser;
    }
}
