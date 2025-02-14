package sicxesimulator;

import sicxesimulator.simulation.virtualMachine.Machine;
import sicxesimulator.simulation.systems.FileHandler;
import sicxesimulator.simulation.systems.Interpreter;
import sicxesimulator.simulation.systems.Assembler;
import sicxesimulator.simulation.systems.Console;

import javafx.animation.PauseTransition;
import javafx.util.Duration;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.input.KeyCode;
import javafx.scene.image.ImageView;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaPlayer.Status;
import javafx.util.Duration;
import javafx.stage.Stage;

import java.io.File;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Random;
import java.util.List;

public class MainApp extends Application {

    private Console console;
    private TextArea outputArea;
    private TextArea inputField;
    private TableView<RegisterEntry> registerTable;
    private TableView<MemoryEntry> memoryTable;
    private MediaPlayer mediaPlayer;
    private String previousMusicFile = ""; 

    @Override
    public void start(Stage primaryStage) {

        // Criando instâncias das classes necessorias
        FileHandler fileHandler = new FileHandler();
        Machine machine = new Machine(); // Ou a forma correta de instanciar Machine
        Interpreter interpreter = new Interpreter(machine);
        Assembler assembler = new Assembler();

        // Agora passamos os argumentos corretamente para Console
        console = new Console(machine, fileHandler, interpreter, assembler);

        // Título da janela
        primaryStage.setTitle("Simulador SIC/XE");

        // Obtom as dimensoes da tela
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        double screenWidth = screenBounds.getWidth();
        double screenHeight = screenBounds.getHeight();

        // Aba de saída
        outputArea = new TextArea();
        outputArea.setEditable(false);
        outputArea.setWrapText(true); // Ativa quebra automotica de linha
        outputArea.setPrefWidth(screenWidth * 0.3);
        outputArea.setPrefHeight(screenHeight * 0.4);

        // Aba de entrada
        inputField = new TextArea();
        inputField.setPromptText("Digite um programa...");
        inputField.setWrapText(true); // Permite que o texto quebre automaticamente
        inputField.setStyle("-fx-alignment: top-left; -fx-padding: 5px;");
        inputField.setPrefWidth(screenWidth * 0.3);
        inputField.setPrefHeight(screenHeight * 0.2);

        // Configura a açao para enviar apenas quando Ctrl+Enter for pressionado
        inputField.setOnKeyPressed(event -> {
            if (event.isControlDown() && event.getCode() == KeyCode.ENTER) {
                handleInputAction(null); // Envia a mensagem
            }
        });

        // Criando os botoes
        Button montarButton = new Button("Montar");
        montarButton.setOnAction(event -> handleMontageAction());
        Button executeButton = new Button("Executar");
        executeButton.setOnAction(event -> handleExecuteAction());
        Button proxButton = new Button("Proximo");
        proxButton.setOnAction(event -> handleNextAction());
        Button limparButton = new Button("Limpar");
        limparButton.setOnAction(event -> handleClearAction());
        Button sairButton = new Button("Sair");
        sairButton.setOnAction(event -> handleExitAction());

        // Definindo a largura dos botoes
        double buttonWidth = screenWidth * 0.05;
        montarButton.setPrefWidth(buttonWidth);
        executeButton.setPrefWidth(buttonWidth);
        proxButton.setPrefWidth(buttonWidth);
        limparButton.setPrefWidth(buttonWidth);
        sairButton.setPrefWidth(buttonWidth);

        // Criando a coluna para os botoes
        VBox buttonColumn = new VBox(30, montarButton, executeButton, proxButton, limparButton, sairButton);
        buttonColumn.setAlignment(Pos.CENTER_LEFT); // Alinhamento vertical ao centro

        // Cria a tabela de registradores
        registerTable = new TableView<>();
        TableColumn<RegisterEntry, String> nameColumn = new TableColumn<>("Registrador");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        TableColumn<RegisterEntry, String> valueColumn = new TableColumn<>("Valor");
        valueColumn.setCellValueFactory(new PropertyValueFactory<>("value"));
        registerTable.getColumns().addAll(nameColumn, valueColumn);
        registerTable.setPrefWidth(screenWidth * 0.12);
        registerTable.setPrefHeight(screenHeight * 0.5);
        registerTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY); // Impede o redimensionamento das colunas pelo usuorio

        // Cria a tabela de endereços de memoria
        memoryTable = new TableView<>();
        TableColumn<MemoryEntry, String> addressColumn = new TableColumn<>("Endereço");
        addressColumn.setCellValueFactory(new PropertyValueFactory<>("address"));
        TableColumn<MemoryEntry, String> memoryValueColumn = new TableColumn<>("Valor");
        memoryValueColumn.setCellValueFactory(new PropertyValueFactory<>("value"));
        memoryTable.getColumns().addAll(addressColumn, memoryValueColumn);
        memoryTable.setPrefWidth(screenWidth * 0.12);
        memoryTable.setPrefHeight(screenHeight * 0.5);
        memoryTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY); // Impede o redimensionamento das colunas pelo usuorio

        // Criando um container para a tabela de registradores
        VBox registerBox = new VBox(registerTable);
        registerBox.setPadding(new Insets(10));
        registerBox.setAlignment(Pos.TOP_RIGHT); // Alinha ao topo direito

        // Criando um container para a tabela de endereços de memoria
        VBox memoryBox = new VBox(memoryTable);
        memoryBox.setPadding(new Insets(10));
        memoryBox.setAlignment(Pos.TOP_RIGHT); // Alinha ao topo direito

        // Criando a caixa horizontal para entrada e registradores (CORREÇaO)
        HBox topBox = new HBox(10, inputField, buttonColumn, registerBox); // Coloca a tabela junto com a entrada
        topBox.setPadding(new Insets(15));
        topBox.setAlignment(Pos.TOP_CENTER); // Alinha ao topo direito
        topBox.setPrefHeight(screenHeight * 0.27);

        // Criando a caixa horizontal para saída
        HBox outputBox = new HBox(10, outputArea, memoryBox);
        outputBox.setPadding(new Insets(5, 15, 45, 55));
        outputBox.setAlignment(Pos.BOTTOM_CENTER);
        outputBox.setPrefHeight(screenHeight * 0.3);

        // Criando o layout principal
        BorderPane contentPane = new BorderPane();
        contentPane.setTop(topBox);  // Agora a entrada e registradores ficam no topo
        contentPane.setBottom(outputBox);  // A saída fica na parte inferior

        // Atualiza as tabelas
        updateRegisterTable(machine);
        updateMemoryTable(machine);

        // Criar a cena
        Scene scene = new Scene(contentPane, screenWidth * 0.6, screenHeight * 0.6);

        // Configura a janela
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();

        // Carregar a imagem de plano de fundo
        Image backgroundImage = new Image(getClass().getResource("/background.png").toExternalForm());

        ImageView imageView = new ImageView(backgroundImage);
        imageView.setFitWidth(contentPane.getWidth());  // Ajusta a largura
        imageView.setFitHeight(contentPane.getHeight()); // Ajusta a altura
        imageView.setOpacity(0.75);

        // Criar o BackgroundImage
        BackgroundImage background = new BackgroundImage(
            backgroundImage,
            BackgroundRepeat.NO_REPEAT,
            BackgroundRepeat.NO_REPEAT,
            BackgroundPosition.CENTER,
            new BackgroundSize(100, 100, true, true, false, true)
        );

        // Adicionar a imagem ao contentPane
        contentPane.getChildren().add(0, imageView);

        // Músicas de fundo
        List<String> musicFiles = List.of(
                getClass().getResource("/Crawling.mp3").toExternalForm(),
                getClass().getResource("/Numb.mp3").toExternalForm(),
                getClass().getResource("/In The End.mp3").toExternalForm()
        );
        Random random = new Random();
        playRandomMusic(musicFiles, random);

        // Criar um painel para a interface (não necessário para a música, mas para um layout básico)
        StackPane root = new StackPane();

        // Redireciona a saída do console para a area de texto
        PrintStream printStream = new PrintStream(new TextAreaOutputStream(outputArea));
        console.setOutput(printStream);

        PauseTransition pause1 = new PauseTransition(Duration.seconds(1));
        pause1.setOnFinished(event -> {
            outputArea.appendText("Bem-vindo ao Simulador SIC/XE!\n");
        });

        PauseTransition pause2 = new PauseTransition(Duration.seconds(2));
        pause2.setOnFinished(event -> {
            outputArea.appendText("Para comecar digite um codigo e clique no botao \"Montar\"." +
            "\nApos isso, use os botoes \"Executar\" para executar o programa de uma so vez, " +
            "ou \"Proximo\" para executar o programa passo a passo. Use tambom o o botao \"Parar\" " +
            "para parar a execucao ou o botao \"Sair\" para finalizar o programa.\n\n");
        });

        // Primeiro exibe a mensagem de boas-vindas, depois a segunda mensagem
        pause1.play();
        pause2.play();
    }

    private void handleInputAction(javafx.event.ActionEvent event) {
        String command = inputField.getText();
        inputField.clear();
        processCommand(command);
    }

    // TODO ajustar a montagem para ser feita com o código de imput do usuário
    private void handleMontageAction() {
        outputArea.appendText("> montar\n");
        PauseTransition pause = new PauseTransition(Duration.seconds(1));
        pause.setOnFinished(event -> {
            console.treatCommand("montar");
            updateRegisterTable(console.getMachine());
        });
        pause.play();
    }

    private void handleExecuteAction() {
        outputArea.appendText("> exec\n");
        PauseTransition pause = new PauseTransition(Duration.seconds(1));
        pause.setOnFinished(event -> {
            console.treatCommand("exec");
            updateRegisterTable(console.getMachine());
        });
        pause.play();
    }

    private void handleNextAction() {
        outputArea.appendText("> prox\n");
        PauseTransition pause = new PauseTransition(Duration.seconds(1));
        pause.setOnFinished(event -> {
            console.treatCommand("prox");
            updateRegisterTable(console.getMachine());
        });
        pause.play();
    }

    // TODO função não implementada "limpar"
    private void handleClearAction() {
        outputArea.appendText("> limpar\n");
        PauseTransition pause = new PauseTransition(Duration.seconds(1));
        pause.setOnFinished(event -> {
            console.treatCommand("sair");
            updateRegisterTable(console.getMachine());
        });
        pause.play();
    }

    private void handleExitAction() {
        outputArea.appendText("> sair\n");
        PauseTransition pause = new PauseTransition(Duration.seconds(1));
        pause.setOnFinished(event -> {
            console.treatCommand("sair");
            updateRegisterTable(console.getMachine());
        });
        pause.play();
    }

    private void processCommand(String command) {
        outputArea.appendText("> " + command + "\n");
        PauseTransition pause = new PauseTransition(Duration.seconds(1));
        pause.setOnFinished(event -> {
            console.treatCommand(command);
            updateRegisterTable(console.getMachine());
        });
        pause.play();
    }

    private void updateRegisterTable(Machine machine) {
        List<String> registerNames = Arrays.asList("A", "X", "L", "PC", "B", "S", "T", "F", "SW");
        registerTable.getItems().clear();
        for (String name : registerNames) {
            String value = machine.getRegister(name).getValue();
            registerTable.getItems().add(new RegisterEntry(name, value));
        }
    }

    private void updateMemoryTable(Machine machine) {
        memoryTable.getItems().clear();
        for (int address = 0; address < machine.getMemory().getSize(); address++) {
            String value = machine.getMemory().read(address);
            memoryTable.getItems().add(new MemoryEntry(String.format("%04X", address), value));
        }
    }

    private void playRandomMusic(List<String> musicFiles, Random random) {
        // Criar uma cópia da lista de músicas para manipulação
        List<String> availableMusic = new ArrayList<>(musicFiles);
        
        // Se houver uma música anterior, remova-a da lista para não tocá-la novamente
        if (!previousMusicFile.isEmpty()) {
            availableMusic.remove(previousMusicFile);
        }
        
        // Escolher aleatoriamente uma música da lista de músicas restantes
        String musicFile = availableMusic.get(random.nextInt(availableMusic.size()));
        
        // Atualiza a música anterior
        previousMusicFile = musicFile;

        // Criar o Media e MediaPlayer
        Media media = new Media(musicFile);
        mediaPlayer = new MediaPlayer(media);

        // Definir o volume, se necessário
        mediaPlayer.setVolume(0.15); // Ajuste o volume conforme necessário

        // Adicionar ouvinte para quando a música terminar
        mediaPlayer.setOnEndOfMedia(() -> {
            // Quando a música acabar, toca uma nova música aleatória
            playRandomMusic(musicFiles, random);
        });

        // Iniciar a reprodução da música
        mediaPlayer.play();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
