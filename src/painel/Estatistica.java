package painel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

 

public class Estatistica{

	// Recebe ArrayList da classe principal para manipular os dados durante os calculos
    private ArrayList<Integer> array = new ArrayList<Integer>();
    // Posicao do ArrayList
    private int contador;
    
    // O ArrayList e o contador sao passados quando cria o objeto estatistica
	public Estatistica(ArrayList<Integer> al, int c){
		this.array = al;
		this.contador = c;
	}

	// Define valores maximo e minimo iniciais
      int maximo = 0, minimo = 0;
      
      /**
       * Calcula valor máximo
       */
  	  public double getMaximo() {
  		int aux = array.get(contador);
  		if (aux > maximo){
  			maximo = aux;
  		}
		return maximo;
	  }
	
      /**
       * Calcula valor mínimo
       */
  	  public double getMinimo() {
      	int aux = array.get(contador);
      	if (aux < minimo){
      		minimo = aux;
      	}
		return minimo;
	  }
       
      /**
       * Calcula a media aritmetica
       */
      public double getMediaAritmetica() {
            double total = 0;
            int c;
            int tam = array.size();
            for(c = contador; c > 0; c--){
            	total += array.get(c);
            }

            return total / tam;
      }

      /**
       * Soma todos os elementos
       */
      public double getSomaDosElementos() {
           double total = 0;
           for (int counter = contador; counter > 0; counter--)
        	   total += array.get(counter);
            return total;
      } 
      
      /**
       * Soma todos os elementos, passando array como parametro
       */
      public double getSomaDosElementos(ArrayList<Integer> array) {
            double total = 0;
            for (int counter = contador; counter > 0; counter--)
                  total += array.get(counter);
            return total;
      }
 
      /**
       * Soma todos os elementos ao quadrado (Para calcular a variancia)
       */
     public double getSomaDosElementosAoQuadrado() {
            double total = 0;
            for (int counter = contador; counter > 0; counter--)
                  total += Math.pow(array.get(counter), 2);
            return total;
      }
 
     /**
      * Calcula a media aritmetica dos valores
      */
      public double getMediaAritmetica(int[] array) {
            double total = 0;
            for (int counter = contador; counter > 0; counter--)
                  total += array[counter];
            return total / array.length;
      }

      /**
       * Verifica se o valor ja existe no array
       */
      public int buscaPor(int value) {
            return Collections.binarySearch(array, value);                
      }

      /**
       * Calcula a variância amostral
       */
      public double getVariancia() {
            double p1 = 1 / Double.valueOf(array.size() - 1);
            double p2 = getSomaDosElementosAoQuadrado() - (Math.pow(getSomaDosElementos(), 2) / Double.valueOf(array.size()));
            return p1 * p2;
      }

      /**
       * Calcula o desvio padrao dos valores
       */
      public double getDesvioPadrao() {
            return Math.sqrt(getVariancia());
      }
 
      /**
       * Calcula a moda dos valores
       */
      public double getModa() {
    	  // Usa o HashMap para armazenar o valor lido, e quantas vezes ele ja foi lido
            HashMap<Double, Integer> map = new HashMap<Double, Integer>();
            Integer i;
            Double moda = 0.0;
            Integer numAtual, numMaior = 0;
            for (int count = contador; count > 0; count--) {
                  i = (Integer) map.get(new Double(array.get(count)));
                  if (i == null) {
                        map.put(new Double(array.get(count)), new Integer(1));
                  } else {
                        map.put(new Double(array.get(count)), new Integer(i.intValue() + 1));
                        numAtual = i.intValue() + 1;
                        if (numAtual > numMaior) {
                             numMaior = numAtual;
                             moda = new Double(array.get(count));
                        }
                  }
            }
            return moda;
      }
 
      /**
       * Get e Set do Array
       */
      public ArrayList<Integer> getArray() {
            return array;
      }

      public void setArray(ArrayList<Integer> al) {
            this.array = al;
      }

}
