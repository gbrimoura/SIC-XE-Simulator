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
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.image.ImageView;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
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
    private TableView<SymbolEntry> symbolTable;
    private TableView<MemoryEntry> memoryTable;
    private MediaPlayer mediaPlayer;
    private String previousMusicFile = ""; 

    @SuppressWarnings("unchecked")
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
        outputArea.setPrefWidth(screenWidth * 0.25);
        outputArea.setPrefHeight(screenHeight * 0.3);

        // Aba de entrada
        inputField = new TextArea();
        inputField.setPromptText("Digite um programa...");
        inputField.setWrapText(true); // Permite que o texto quebre automaticamente
        inputField.setStyle("-fx-alignment: top-left; -fx-padding: 5px;");
        inputField.setPrefWidth(screenWidth * 0.2);
        inputField.setPrefHeight(screenHeight * 0.3);

        // Configura a açao para enviar apenas quando Ctrl+Enter for pressionado
        inputField.setOnKeyPressed(event -> {
            if (event.isControlDown() && event.getCode() == KeyCode.ENTER) {
                handleInputAction(null); // Envia a mensagem
            }
        });

        // Criando os botoes
        Button openFileButton = new Button("Abrir Arquivo");
        openFileButton.setOnAction(event -> openFileChooser());
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
        openFileButton.setPrefWidth(buttonWidth);
        montarButton.setPrefWidth(buttonWidth);
        executeButton.setPrefWidth(buttonWidth);
        proxButton.setPrefWidth(buttonWidth);
        limparButton.setPrefWidth(buttonWidth);
        sairButton.setPrefWidth(buttonWidth);

        // Criando a coluna para os botoes
        VBox buttonColumn = new VBox(15, openFileButton, montarButton, executeButton, proxButton, limparButton, sairButton);
        buttonColumn.setAlignment(Pos.CENTER_LEFT); // Alinhamento vertical ao centro
        buttonColumn.setPadding(new Insets(0,155,0,40));

        // Cria a tabela de registradores
        registerTable = new TableView<>();
        TableColumn<RegisterEntry, String> nameColumn = new TableColumn<>("Registrador");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        TableColumn<RegisterEntry, String> valueColumn = new TableColumn<>("Valor");
        valueColumn.setCellValueFactory(new PropertyValueFactory<>("value"));
        registerTable.getColumns().addAll(nameColumn, valueColumn);
        registerTable.setPrefWidth(screenWidth * 0.12);
        registerTable.setPrefHeight(screenHeight * 0.8);
        registerTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY); // Impede o redimensionamento das colunas pelo usuorio

        // Cria a tabela de endereços de memoria
        memoryTable = new TableView<>();
        TableColumn<MemoryEntry, String> addressColumn = new TableColumn<>("Endereço");
        addressColumn.setCellValueFactory(new PropertyValueFactory<>("address"));
        TableColumn<MemoryEntry, String> memoryValueColumn = new TableColumn<>("Valor");
        memoryValueColumn.setCellValueFactory(new PropertyValueFactory<>("value"));
        memoryTable.getColumns().addAll(addressColumn, memoryValueColumn);
        memoryTable.setPrefWidth(screenWidth * 0.12);
        memoryTable.setPrefHeight(screenHeight * 0.8);
        memoryTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY); // Impede o redimensionamento das colunas pelo usuorio

        // Cria a tabela de símbolos
        symbolTable = new TableView<>();
        TableColumn<SymbolEntry, String> symbolNameColumn = new TableColumn<>("Símbolo");
        symbolNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        TableColumn<SymbolEntry, String> symbolValueColumn = new TableColumn<>("Endereço");
        symbolValueColumn.setCellValueFactory(new PropertyValueFactory<>("address"));
        symbolTable.getColumns().addAll(symbolNameColumn, symbolValueColumn);
        symbolTable.setPrefWidth(screenWidth * 0.12);
        symbolTable.setPrefHeight(screenHeight * 0.7);
        symbolTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY); // Impede o redimensionamento das colunas pelo usuorio

        // Criando um container para a tabela de registradores
        VBox registerBox = new VBox(registerTable);
        registerBox.setPadding(new Insets(10));
        registerBox.setAlignment(Pos.TOP_RIGHT); // Alinha ao topo direito

        // Criando um container para a tabela de endereços de memoria
        VBox memoryBox = new VBox(memoryTable);
        memoryBox.setPadding(new Insets(10));
        memoryBox.setAlignment(Pos.TOP_RIGHT); // Alinha ao topo direito

        // Criando a caixa horizontal para entrada e registradores (CORREÇAO)
        HBox topBox = new HBox(10, inputField, buttonColumn, registerBox); // Coloca a tabela junto com a entrada
        topBox.setPadding(new Insets(40,0,0,0));
        topBox.setAlignment(Pos.TOP_CENTER); // Alinha ao topo direito
        topBox.setPrefHeight(screenHeight * 0.3);

        // Criando a caixa horizontal para saída
        HBox outputBox = new HBox(10, outputArea, memoryBox, symbolTable);
        outputBox.setPadding(new Insets(0,0,40,0));
        outputBox.setAlignment(Pos.BOTTOM_CENTER);
        outputBox.setPrefHeight(screenHeight * 0.3);

        // Criando o layout principal
        BorderPane contentPane = new BorderPane();
        contentPane.setTop(topBox);         // Agora a entrada e registradores ficam no topo
        contentPane.setBottom(outputBox);   // A saída fica na parte inferior

        // Atualiza as tabelas
        updateRegisterTable(machine);
        updateMemoryTable(machine);

        // Criar a cena
        Scene scene = new Scene(contentPane, screenWidth * 0.7, screenHeight * 0.7);

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

        // Redireciona a saída do console para a area de texto
        PrintStream printStream = new PrintStream(new TextAreaOutputStream(outputArea));
        console.setOutput(printStream);

        PauseTransition pause1 = new PauseTransition(Duration.seconds(1));
        pause1.setOnFinished(event -> {
            outputArea.appendText("Bem-vindo ao Simulador SIC/XE!\n\n");
        });

        PauseTransition pause2 = new PauseTransition(Duration.seconds(2));
        pause2.setOnFinished(event -> {
            outputArea.appendText("Para comecar digite um codigo e clique no botao \"Montar\"." +
            "\nApos isso, use os botoes \"Executar\" para executar o programa de uma so vez, " +
            "ou \"Proximo\" para executar o programa passo a passo.\nUse tambem o o botao \"Parar\"" +
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

    private void openFileChooser() {
        // Criação do FileChooser
        FileChooser fileChooser = new FileChooser();

        // Definindo filtros para tipos de arquivos, como arquivos de texto (.txt)
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Text Files (*.txt)", "*.txt", "*.asm");
        fileChooser.getExtensionFilters().add(extFilter);

        // Exibir a caixa de diálogo para abrir o arquivo
        File selectedFile = fileChooser.showOpenDialog(null);

        if (selectedFile != null) {
            // Se um arquivo for selecionado, vamos ler o conteúdo e colocar no TextArea
            readFileAndDisplay(selectedFile);
        } else {
            System.out.println("Nenhum arquivo selecionado.");
        }
    }

    private void readFileAndDisplay(File file) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            StringBuilder content = new StringBuilder();
            String line;
            
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
    
            inputField.setText(content.toString());
        } catch (IOException e) {
            outputArea.appendText("Erro ao ler o arquivo: " + e.getMessage() + "\n");
        }
    }

    // TODO ajustar a montagem para ser feita com o código de imput do usuário
    private void handleMontageAction() {
        // Captura o conteúdo do campo de entrada
        String inputCode = inputField.getText();  // Get text from the input field
    
        // Limpa a área de saída
        outputArea.clear();
        outputArea.appendText("> montar\n");
    
        // Passa o código de entrada para o método de montagem da classe Console
        PauseTransition pause = new PauseTransition(Duration.seconds(1));
        pause.setOnFinished(event -> {
            // Pass the inputCode to the treatCommand method
            console.treatCommand("montar", inputCode);  // Passa o código de entrada para a função
            updateRegisterTable(console.getMachine());  // Update the register table
            updateSymbolTable(console.getAssembler());  // Update the symbol table
        });
        pause.play();
    }

    private void handleExecuteAction() {
        outputArea.clear();
        outputArea.appendText("> exec\n");
        PauseTransition pause = new PauseTransition(Duration.seconds(1));
        pause.setOnFinished(event -> {
            console.treatCommand("exec", "");
            updateRegisterTable(console.getMachine());
        });
        pause.play();
    }

    private void handleNextAction() {
        outputArea.clear();
        outputArea.appendText("> prox\n");
        PauseTransition pause = new PauseTransition(Duration.seconds(1));
        pause.setOnFinished(event -> {
            console.treatCommand("prox", "");
            updateRegisterTable(console.getMachine());
        });
        pause.play();
    }

    private void handleClearAction() {
        outputArea.clear();  // Limpa a área de saída
        inputField.clear();  // Limpa o campo de entrada
        outputArea.appendText("> limpar\n");  // Exibe no log de saída
    
        registerTable = new TableView<RegisterEntry>();
        symbolTable = new TableView<SymbolEntry>();
        memoryTable = new TableView<MemoryEntry>();
    
        // Atualiza as tabelas para garantir que estejam limpas
        updateRegisterTable(console.getMachine());
        updateSymbolTable(console.getAssembler());
        updateMemoryTable(console.getMachine());
    
        // Pausa e reinicia a execução
        PauseTransition pause = new PauseTransition(Duration.seconds(1));
        pause.setOnFinished(event -> {
            console.treatCommand("limpar", "");  // Chama o comando de limpar no console
            updateRegisterTable(console.getMachine());  // Atualiza a tabela de registradores
        });
        pause.play();
    }
    

    private void handleExitAction() {
        outputArea.clear();
        outputArea.appendText("> sair\n");
        PauseTransition pause = new PauseTransition(Duration.seconds(1));
        pause.setOnFinished(event -> {
            console.treatCommand("sair", "");
            updateRegisterTable(console.getMachine());
        });
        pause.play();
    }

    private void processCommand(String command) {
        outputArea.appendText("> " + command + "\n");
        PauseTransition pause = new PauseTransition(Duration.seconds(1));
        pause.setOnFinished(event -> {
            console.treatCommand(command, "");
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

    private void updateSymbolTable(Assembler assembler) {
        symbolTable.getItems().clear();
        assembler.getSymbolTable().forEach((name, address) -> {
            symbolTable.getItems().add(new SymbolEntry(name, String.format("%04X", address)));
        });
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
