package app;

import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Classe que representa uma pessoa, contendo RG, nome e Data de nascimento
 */
public class Pessoa implements Comparable {
    
    protected int RG;
    protected String nome;
    protected String dataNasc;

    /***
     * Construtor da classe, recebendo com parametro, RG, nome e Data de nascimento
     * @param rg Inteiro de 8 digitos
     * @param nome String de tamanho variavel
     * @param nasc String de tamanho fixo no formato DD/MM/AAAA
     */
    public Pessoa(int rg, String nome, String nasc){
        this.RG = rg;
        this.nome = nome;
        this.dataNasc = nasc;

    }

    /**
     * Metodo que cria uma Pessoa separador, pessoa nula
     * @return Pessoa invalida
     */
    public static Pessoa separador(){
        return new Pessoa(-1, ""," ");
    }

    /***
     * Metodo para salvar no arquivo
     * @param file Arquivo de acesso aleatorio
     * @return True ou False
     * @throws IOException Exceção de entrada / saída
     */
    public boolean saveToFile(RandomAccessFile file) throws IOException{
        file.seek(file.length());
        file.writeInt(this.RG);
        file.writeUTF(this.nome);
        file.writeUTF(this.dataNasc);
        return true;
    }

    /***
     * Salvar em uma posição especifica do arquivo
     * @param file Arquivo de acesso aleatorio
     * @param pos posição 
     * @return True ou False
     * @throws IOException Exceção de entrada e saida
     */
    public boolean saveToFile(RandomAccessFile file, long pos) throws IOException{
        file.seek(pos);
        file.writeInt(this.RG);
        file.writeUTF(this.nome);
        file.writeUTF(this.dataNasc);
        return true;
    }

    /***
     * Metodo para ler um objeto Pessoa do arquivo
     * @param dados Arquivo de acesso aleatorio
     * @return Objeto Pessoa
     * @throws IOException Exceção de entrada / saída
     */ 
    public static Pessoa readFromFile(RandomAccessFile dados) throws IOException{
        Pessoa nova = null;

        int rg = dados.readInt();
        String nome = dados.readUTF();
        String nasc = dados.readUTF();

        nova = new Pessoa(rg,nome,nasc);



        return nova;
    }

    /**
     * 
     * @return RG da Pessoa
     */
    public int getRG() {
        return RG;
    }

    
    @Override
    public int compareTo(Object o) { //-1 se for menor; 1 se for maior ou 0 em empate
        Pessoa outra = (Pessoa)o;

        if(this.RG < outra.RG) return -1;
        else if(this.RG>outra.RG) return 1;

        return 0;
    }

}