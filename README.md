# Simulador SIC/XE
Este programa simula um sistema SIC/XE em Java. Ele possui as funções de máquina virtual e montador.

Para executar o simulador SIC/XE, siga as etapas abaixo:

-Certifique-se de que o Maven e o Java estejam instalados em seu sistema.
-Em seguida, instale as dependências do projeto executando o comando 'mvn install' no terminal.
-Após a instalação das dependências, execute o comando 'mvn clean compile' para limpar qualquer artefato de builds anteriores e compilar o código-fonte do projeto.
-Por último, para iniciar o simulador SIC/XE com a interface gráfica, use o comando 'mvn javafx:run'.

Dentro da interface você encontrará as seguintes opções:

-Abrir Arquivo: Permite que você abra um arquivo de código-fonte para ser processado pelo simulador.
-Montar: Com esta opção, o código-fonte carregado será montado pelo montador de duas passagens, gerando o código de máquina correspondente.
-Executar: Após montar o código, você pode executar a simulação do código de máquina, visualizando os resultados do processamento.
-Próximo: Avança para a próxima instrução, permitindo a execução passo a passo do código.
-Limpar: Limpa os dados atuais, preparando o simulador para uma nova execução ou carregamento de outro arquivo.
-Sair: Encerra o simulador.


## Créditos
Arthur Alves - Organização, discussão, ajustes.  
Fabricio Bartz - Organização, discussão, ajustes.  
Gabriel Moura - Construção, definição e ajustes dos registradores e memória.  
Leonardo Braga - Ajustes nas flags de operações.  
Luis Eduardo Rasch - Construção e ajuste do console, leitura e analise dos arquivos, e testes.  
Renan Pinho - Construção das instruções e simulador, ajustes em todo o código e transpiler.
