package app;



import java.util.Map;
import java.util.TreeMap;


public class index {
    private TreeMap<Integer, Long> posArquivos;
    private int qtdRegistros;

    public index() {
        this.posArquivos = new TreeMap<Integer, Long>();
    }

    public void setQtdRegistros(int qtdRegistros) {
        this.qtdRegistros = qtdRegistros;
    }

    public int getQtdRegistros() {
        return qtdRegistros;
    }

    public void salvarPos(int RG, long pos) {
        // verifica se já não existe um registro
        if (!registroExistente(RG))
            posArquivos.put(RG, pos);
    }

    public boolean registroExistente(int RG) {
        if (posArquivos.containsKey(RG))
            return true;
        else
            return false;
    }

    public long retornaPosicaoArquivo(int RG) {
        if (registroExistente(RG))
            return posArquivos.get(RG);
        else
            return -1;
    }


    public Map<Integer,Long> getMap(){
        return posArquivos;
    }



    
}
