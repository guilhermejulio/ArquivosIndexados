package app;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Map;
import java.util.Scanner;
import java.io.FileNotFoundException;


public class App {

    //caso deseje mudar o caminho do arquivo binario, utilize essa variavel
    static final String path = "Pessoas.bin";
    //o indice é criado junto com a criação do arquivo binario, evitando leituras a mais no programa.
    public static index indice=null;
 

    /**
     * Metodo que cria um registro de uma pessoa
     * @param dados vetor de dados contendo: RG, nome e data de nascimento
     * @param pos variavel da posição doa arquivo
     * @return Objeto pessoa
     */
    public static Pessoa criarRegistroPessoa(String [] dados, long pos){
        Pessoa registroPessoa;
        try{
            int rg = Integer.parseInt(dados[0]);
            String nome = dados[1];
            String dataNasc = dados[2];
            registroPessoa = new Pessoa(rg,nome,dataNasc);
        }catch(Exception e){
            System.out.println("Ocorreu um erro na posição: "+pos);
            registroPessoa = null;
        }

        return registroPessoa;
    }
    
    /***
     * Metodo que retorna um Objeto Pessoa de dentro de um arquivo de acesso aleatorio, baseado numa posição
     * @param file Arquivo de acesso aleatorio contendo Pessoas
     * @param pos Posição em que se deseja extrair uma pessoa
     * @return Objeto Pessoa
     */
    public static Pessoa retornaPessoa(RandomAccessFile file, long pos){
        try{
            file.seek(pos);
            Pessoa aux= null;
            int rg = file.readInt();
            String nome = file.readUTF();
            String dataNasc = file.readUTF();
            aux = new Pessoa(rg,nome,dataNasc);
            return aux;
        }catch (IOException e) {
            System.out.println("Erro na leitura:" + e.getMessage());
            return null;
        }
    }

    /**
     * Metodo que converte um arquivo do formato TXT, para formato BINARIO 
     * @param arqTexto path do arquivo TXT
     * @param arqBin path do arquivo binario
     * @throws IOException Exceção de entrada / saída
     */
    public static void converterArquivo(String arqTexto, String arqBin) throws IOException{
        File arqEntrada = new File (arqTexto);
        Scanner leitor = new Scanner(arqEntrada);

        Scanner teclado = new Scanner(System.in); // variavel para "pausar" e ver o que está acontecendo

        int contador = 0;

        try{
            File arqSaida = new File(arqBin);
            RandomAccessFile dadosSaida = new RandomAccessFile(arqSaida, "rw");
            indice = new index();

            dadosSaida.setLength(0);
            dadosSaida.writeInt(0);
            System.out.println("\nArquivo binario e indice sendo criados, aguarde...");
            while(leitor.hasNextLine()){
                String linha = leitor.nextLine();
                String [] dados = linha.split(";");
                long pos = dadosSaida.getFilePointer();  //controlando a posição no arquivo
                indice.salvarPos(Integer.parseInt(dados[0]),pos); //salvando a posição no arquivo
                Pessoa novoRegistro = criarRegistroPessoa(dados, pos);
                novoRegistro.saveToFile(dadosSaida);
                contador++;
                

            }
            leitor.close();

            dadosSaida.seek(0);
            dadosSaida.writeInt(contador); // salvando quantos dados existem no cabeçalho do arquivo
            indice.setQtdRegistros(contador); //salvando quantos dados existem no indice
            dadosSaida.close();
            System.out.println("\nArquivo criado com sucesso, prescione <enter>");
            teclado.nextLine();

        }catch (FileNotFoundException e){
            System.out.println("Erro encontrado: "+e.getMessage());
        }
    }

    /**
     * Metodo que cria um indice na memoria baseado em um arquivo binario
     * @param arqBin path do arquivo binario
     * @throws IOException Exceção de entrada / saída
     */
    public static void criarIndice(String arqBin) throws IOException {
        Scanner teclado = new Scanner(System.in); // variavel para "pausar" e ver o que está acontecendo
        if(indice == null){
            Pessoa aux = null;
            boolean fimDeArquivo = false;
            int contador =0;
            
            File arqSaida = new File(arqBin);
            RandomAccessFile dadosSaida = new RandomAccessFile(arqSaida, "rw");
            indice = new index();
            dadosSaida.seek(Integer.BYTES);
            long pos;

            try{
                System.out.println("\nIndice sendo criado aguarde...");
                while(!fimDeArquivo){
                    pos = dadosSaida.getFilePointer();
                    aux = Pessoa.readFromFile(dadosSaida);
                    indice.salvarPos(aux.RG, pos);
                    contador++;
                }
                indice.setQtdRegistros(contador);
                
                System.out.println("\nIndice criado com sucesso!! Prescione <enter> para continuar...");
                
            }catch(EOFException ex){
                aux = null; fimDeArquivo = true;
            }
            teclado.nextLine();
        }else {
            System.out.println("\n\nO indice já existe!!!!!!!!!!");
            teclado.nextLine();
            
        }
       
        
    }

    /***
     * Metodo pesquisa uma pessoa por RG, baseado no indice em memoria
     * @param RG numero inteiro contendo o RG da Pessoa
     * @param registros arquivo de acesso aleatorio contendo os registros de Pessoas
     * @return Objeto Pessoa requisitado
     * @throws FileNotFoundException Exceção de arquivo não encontrado
     */
    public static Pessoa pesquisarRG(int RG, RandomAccessFile registros) throws FileNotFoundException {
        Pessoa aux = null;

        if(indice.registroExistente(RG)){
            aux = retornaPessoa(registros, indice.retornaPosicaoArquivo(RG));
        }

        return aux;
    }

    
    /**
     * Metodo que pesquisa por um RG, sem a utilização do indice como base
     * @param rgPesquisado  RG que se deseja pesquisar
     * @param registros Arquivo de acesso aleatorio com os registros de todas as pessoas
     * @return  Objeto Pessoa requisitado
     * @throws IOException
     */
    public static Pessoa pesquisaSemIndice(int rgPesquisado, RandomAccessFile registros)throws IOException {
        Pessoa aux = null;
        boolean fimDeArquivo = false;
        registros.seek(Integer.BYTES);  // == 4 bytes

        try{
            while(!fimDeArquivo){
                aux = Pessoa.readFromFile(registros);
                if(aux.getRG() == rgPesquisado){
                    return aux;
                }
            }
        }catch(EOFException ex){
            aux = null;
            fimDeArquivo = true;
        }
        return aux;
    }

    /***
     * Metodo extra que imprime os dados de todas as pessoas, utilizando com base o indice
     * @param registros Arquivo de acesso aleatorio com os registros de todas as Pessoas.
     */
    public static void ImprimirComIndice(RandomAccessFile registros){
        Map<Integer, Long> mapAux = indice.getMap();
        Pessoa aux;

        for (Map.Entry<Integer, Long> entry : mapAux.entrySet()) {
            Long posicaoArq = entry.getValue();
            aux = retornaPessoa(registros, posicaoArq);
            System.out.println("Registro na posição: "+posicaoArq+" RG: "+aux.RG+" Nome: "+aux.nome+" Data de nascimento: "+aux.dataNasc);

            
        }
        
    }

    /***
     * Metodo que imprime todas as pessoas sem a utilização do indice
     * @param registros Arquivo de acesso aleatorio com os dados de todas as pessoas
     * @throws IOException  Exceção de entrada e saida
     */
    public static void ImprimirSemIndice(RandomAccessFile registros) throws IOException {
        Pessoa aux = null;
        boolean fimDeArquivo = false; 
        registros.seek(Integer.BYTES); // == 4 bytes
 
        try{
            while(!fimDeArquivo){
                aux = Pessoa.readFromFile(registros);
                System.out.println("Registro na posição: "+registros.getFilePointer()+" RG: "+aux.RG+" Nome: "+aux.nome+" Data de nascimento: "+aux.dataNasc);
            }
        }catch(EOFException ex){
            aux = null;
            fimDeArquivo = true;
        }
    }

    /***
     * Metodo que mostra um menu de opções para o usuario
     * @param leitor Opção escolhida
     * @return
     */
    public static int menu(Scanner leitor){
        System.out.println();
        System.out.println("\tArquivo Indexado");
        System.out.println("\n0. Fim do programa");
        System.out.println("1. Criar arquivo binario indexado (Caso o arquivo já existir, cria só o indice)");
        System.out.println("2. Realizar pesquisa por RG (USANDO o indice)");
        System.out.println("3. Realizar pesquisa por RG (SEM o indice)");
        //os metodos abaixos são extras só para ver o funcionamento   
        System.out.println("-----------------------------------------");
        System.out.println("\tMetodos Extras:");
        System.out.println("4. Imprimir quantidade de registros");
        System.out.println("5. Imprimir todos os registros UTILIZANDO o indice"); //imprime os registros de forma ordenada
        System.out.println("6. Imprimir todos os registros SEM o indice");
        System.out.println("\nOpcao:");
        int opcao = Integer.parseInt(leitor.nextLine());
        return opcao;
    }

    /***
     * Metodo que retorna se o arquivo binario existe, baseado no path informado
     * @param path  caminho do arquivo
     * @return True ou False
     */
    public static boolean arqBinarioFoiCriado(String path){
        return new File(path).isFile();
    }

    /***
     * Retorna um arquivo de acesso aleatorio baseado em um arquivo binario
     * @param binario arquivo binario
     * @return Arquivo de acesso aleatorio
     * @throws FileNotFoundException Exceção de arquivo não encontrado
     */
    public static RandomAccessFile retornaAcessoAleatorio(File binario) throws FileNotFoundException {
        return new RandomAccessFile(binario,"rw");
    }
    
    /***
     * Retorna se os registros foram inicializados
     * @param registros
     * @return True ou False
     */
    public static boolean registrosInicializados(RandomAccessFile registros){
        return registros!=null;
    }
    public static void main(String[] args) throws Exception {
        int opcao;
        Scanner entrada = new Scanner(System.in);
        Scanner teclado = new Scanner(System.in);
        //variaveis para medir o tempo
        long timeStart;
        long timeFinal;

        RandomAccessFile registros = null;
        try{
            
            do{
                opcao = menu(entrada);
                switch(opcao){
                    case 1: 
                        
                       // cria um objeto na memoria do path do arquivo binario
                       //se existe arquivo binario, eu crio somente o indice
                       //se não existir arquivo binario, eu crio o arquivo binario + indice;

                        if(arqBinarioFoiCriado(path)) criarIndice("Pessoas.bin");
                        else converterArquivo("PessoasPAA.txt", "Pessoas.bin");

                        //seta um arquivo de acesso aleatorio a partir do arquivo binario
                        if(!registrosInicializados(registros)) registros = retornaAcessoAleatorio(new File(path));
            
                        break;
                    case 2:
                        if(indice!=null){
                            
                            System.out.println("\nDigite o RG desejado: ");
                            int RG = Integer.parseInt(entrada.nextLine());
                            timeStart = System.currentTimeMillis();
                            Pessoa alvo = pesquisarRG(RG,registros);
                            timeFinal = System.currentTimeMillis() - timeStart;
                            System.out.println("\n>>>Tempo gasto na busca: "+timeFinal+"ms");

                            if(alvo != null){
                                System.out.println("\nRegistro encontrado:\nRG: "+alvo.RG+"\nNome: "+alvo.nome+"\nData de nascimento: "+alvo.dataNasc);
                                System.out.println("\nPrescione <enter>...");
                                teclado.nextLine();
                            } 
                            else {
                                System.out.println("O RG Informado não existe!!");
                                System.out.println("\nPrescione <enter>...");
                                teclado.nextLine();
                            }
                        }else {
                            System.out.println("\nO indice não foi criado!! Selecione a opção <1> primeiro");
                            teclado.nextLine();
                        }
                        break;
                    case 3:
                        
                        if(arqBinarioFoiCriado(path)){
                            //verifica se o arquivo de acesso aleatorio foi inicializado
                            if(!registrosInicializados(registros)) registros = retornaAcessoAleatorio(new File(path));

                            System.out.println("\nDigite o RG desejado: ");
                            int RG = Integer.parseInt(entrada.nextLine());
                            timeStart = System.currentTimeMillis();
                            Pessoa alvo = pesquisaSemIndice(RG,registros);
                            timeFinal = System.currentTimeMillis() - timeStart;
                            System.out.println("\n>>>Tempo gasto na busca: "+timeFinal+"ms");
                            
                            if(alvo != null){
                                System.out.println("\nRegistro encontrado:\nRG: "+alvo.RG+"\nNome: "+alvo.nome+"\nData de nascimento: "+alvo.dataNasc);
                                System.out.println("\nPrescione <enter>...");
                                teclado.nextLine();
                            }
                            else {
                                System.out.println("O RG Informado não existe!!");
                                System.out.println("\nPrescione <enter>...");
                                teclado.nextLine();
                            }
                        }else {
                            System.out.println("\nO arquivo não foi criado, selecione a opção <1> primeiro");
                            teclado.nextLine();
                        }
                        break;
                    case 4:
                        if(indice != null) {
                            System.out.println("Numero de registros no arquivo: "+indice.getQtdRegistros());
                            System.out.println("\nPrescione <enter>...");
                            teclado.nextLine();
                        }else {
                            System.out.println("\nO indice não foi criado!! Selecione a opção <1> primeiro");
                            teclado.nextLine();
                        }
                        break;
                    case 5: 
                        
                        if(indice != null){
                            System.out.println("\nO metodo imprime todos os registros de forma ordenada pelo RG\nTempo aproximado para impressão de -> 12 minutos");
                            System.out.println("\nPrescione <enter> para continuar...");
                            teclado.nextLine();
                            timeStart = System.currentTimeMillis();
                            ImprimirComIndice(registros);
                            timeFinal = System.currentTimeMillis() - timeStart;
                            System.out.println("\n>>> Tempo gasto para impressão: "+timeFinal+"ms");
                        }else{
                            System.out.println("\nO indice não foi criado!! Selecione a opção <1> primeiro");
                            teclado.nextLine();
                        }
                        break;
                    case 6:
                        
                        if(arqBinarioFoiCriado(path)){
                            if(!registrosInicializados(registros)) registros = retornaAcessoAleatorio(new File(path));
                            System.out.println("\nO metodo imprime todos os registros de forma não ordenada\nTempo aproximado para impressão de -> 12 minutos");
                            System.out.println("\nPrescione <enter> para continuar...");
                            teclado.nextLine();
                            timeStart = System.currentTimeMillis();
                            ImprimirSemIndice(registros);
                            timeFinal = System.currentTimeMillis() - timeStart;
                            System.out.println("\n>>> Tempo gasto para impressão: "+timeFinal+"ms");
                        }else{
                            System.out.println("\nO arquivo não foi criado!! Selecione a opção <1> primeiro");
                            teclado.nextLine();
                        }
                        break;

                    default:
                        System.out.println("Adeus!!");
                        break;
                }
            }while(opcao != 0);      
            
    }catch (IOException ex) {
        System.out.println("Ocorreu um erro: "+ex.getMessage());
    }
    entrada.close(); 
    registros.close();  
    }
}
