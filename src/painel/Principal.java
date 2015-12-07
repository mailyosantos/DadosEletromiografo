package painel;

import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.LineBorder;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.ui.ApplicationFrame;

@SuppressWarnings("serial")
public class Principal extends ApplicationFrame implements SerialPortEventListener{

	// Criacao das variaveis que compoem a interface do programa
	public JFrame frmPainel;
	public JLabel lblIntervalo;
	public JTextField textFieldIntervalo;
	public JPanel panel_Original;
	public JPanel panel_Integral;
	public JPanel panel_MMovel;
	public JPanel panel_Etc;
	public JButton btnIniciar;
	public JButton btnLimpar;
	public JButton btnParar;
	public JLabel labelResultadoMax;
	public JLabel labelResultadoMin;
	public JLabel labelResultadoMed;
	public JLabel labelResultadoModa;
	public JLabel labelResultadoDesvio;
	
	// Variaveis do tipo arquivo para criar diretorios
	public static File subdir, subdir2;
	
	// Cria Arrays para armazenar dados lidos pela porta serial
	public ArrayList<Integer> al = new ArrayList<Integer>();
	
	// Strings para armazenar o nome dos diretorios a serem criados/consultados
	public static String sdir, sdir2;
	// Strings para armazenar o nome dos arquivos a serem criados/consultados
	public static String nome_arq = "dados_originais.txt", nome_arq_int = "dados_filtro_integral.txt", nome_arq_mmovel = "dados_filtro_mmovel.txt";
	
	// Contadores
	public int c1 = 0, c2 = 0, contador = 0;
	
	// Armazena o intervalo digitado na textfield pelo usuario
	public int intervalo;
	
    // Series de dados, uma para cada grafico
    private TimeSeriesCollection datasets_original;
    private TimeSeriesCollection datasets_integral;
    private TimeSeriesCollection datasets_mmovel;

    // Valores mais recentemente adicionados nas series
    private Integer lastValue_original;
    private double lastValue_integral;
    private double lastValue_mmovel;

    // Nome das possiveis portas
    private static final String PORT_NAMES[] = {
    	"COM","COM1","COM2","COM3","COM4","COM5","COM6","COM7","COM8","COM9","COM10","COM11","COM12","COM13","COM14","COM15"
    };
    
    // Declara porta serial
    SerialPort serialPort;

    // Alimentado pelo InputStreamReader que converte os bytes em caracteres
    private BufferedReader input;
    
    // Fluxo de saida pra serial
    @SuppressWarnings("unused")
	private OutputStream output;
    
    // Milisegundos para bloquear a porta
    private static final int TIME_OUT = 250;

    // Velocidade da porta/Taxa de dados
    private static final int DATA_RATE = 9600;

 
	@SuppressWarnings("deprecation")
	public Principal(String titulo) {
		super(titulo);
		
		// Design da Janela
		frmPainel = new JFrame();
		// Icone
		frmPainel.setIconImage(Toolkit.getDefaultToolkit().getImage(Principal.class.getResource("/eb_painel/_iconred.png")));
		frmPainel.setTitle("Leitura de Dados - Eletromi\u00F3grafo");
		frmPainel.setResizable(false);
		frmPainel.setBounds(100, 100, 1024, 730);
		frmPainel.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frmPainel.getContentPane().setLayout(null);
		// Listener para o botao de fechar [x] e confirmar saida
		frmPainel.addWindowListener(new java.awt.event.WindowAdapter() {
					public void windowClosing(java.awt.event.WindowEvent e) {
							if (e.getID() == WindowEvent.WINDOW_CLOSING){
									int selectedOption = JOptionPane.showConfirmDialog(null,"Deseja realmente sair?", "Sair", JOptionPane.YES_NO_OPTION);
										if(selectedOption == JOptionPane.YES_OPTION){
											System.exit(0);  	                	
										}	else textFieldIntervalo.setEnabled(true);
							}	
					}
		});
		
		// Cria painel para o grafico gerado a partir dos dados originais
		panel_Original = new JPanel();
		panel_Original.setBounds(10, 11, 490, 335);
		panel_Original.setBackground(Color.LIGHT_GRAY);
		frmPainel.getContentPane().add(panel_Original);
		panel_Original.setLayout(new BorderLayout());
		
		// Cria painel para o grafico gerado a partir dos dados gerados apos passar pelo filtro integral
		panel_Integral = new JPanel();
		panel_Integral.setBounds(514, 11, 490, 335);
		panel_Integral.setBackground(Color.LIGHT_GRAY);
		frmPainel.getContentPane().add(panel_Integral);
		panel_Integral.setLayout(new BorderLayout());
		
		// Cria painel para o grafico gerado a partir dos dados gerados apos passar pelo filtro media movel
		panel_MMovel = new JPanel();
		panel_MMovel.setBounds(10, 357, 490, 335);
		panel_MMovel.setBackground(Color.LIGHT_GRAY);
		frmPainel.getContentPane().add(panel_MMovel);
		panel_MMovel.setLayout(new BorderLayout());
		
		// Cria painel para as informacoes da 'tabela' de estatistica
		panel_Etc = new JPanel();
		panel_Etc.setBorder(new LineBorder(new Color(0, 0, 0)));
		panel_Etc.setBounds(514, 357, 490, 335);
		panel_Etc.setBackground(UIManager.getColor("Button.background"));
		frmPainel.getContentPane().add(panel_Etc);
		panel_Etc.setLayout(null);
		
		btnIniciar = new JButton("Iniciar");
		// Listener: Quando acionado libera o botao de parar, bloqueia o textfield e chama o initialize
		btnIniciar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				intervalo = Integer.parseInt(textFieldIntervalo.getText());
				btnParar.setEnabled(true);
				btnIniciar.setEnabled(false);
				btnLimpar.setEnabled(false);
				textFieldIntervalo.setEnabled(false);
				// Comeca a ler os dados da serial
				initialize();
				
			}
		});
		btnIniciar.setBounds(221, 306, 73, 23);
    	btnIniciar.setEnabled(false);
    	panel_Etc.add(btnIniciar);
		
		btnLimpar = new JButton("Limpar");
		// Limpa todos os dados gerados, bloqueia/libera os botoes programados, limpa as series de dados
		btnLimpar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				btnParar.setEnabled(false);
				btnLimpar.setEnabled(false);
				btnIniciar.setEnabled(false);
				textFieldIntervalo.setText("");
				textFieldIntervalo.setEnabled(true);
				
		    	labelResultadoMax.setText("0");
		    	labelResultadoMin.setText("0");
		    	labelResultadoMed.setText("0");
		    	labelResultadoModa.setText("0");
		    	labelResultadoDesvio.setText("0");
		    	
		    	datasets_integral.getSeries(0).clear();
		    	datasets_mmovel.getSeries(0).clear();
		    	datasets_original.getSeries(0).clear();

			}
		});
		btnLimpar.setBounds(304, 306, 81, 23);
		btnLimpar.setEnabled(false);
		panel_Etc.add(btnLimpar);
		
		// Para a leitura de dados vindos da serial
		btnParar = new JButton("Parar");
		btnParar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				btnParar.setEnabled(false);
				btnLimpar.setEnabled(true);
				btnIniciar.setEnabled(false);
				textFieldIntervalo.setText("");
				textFieldIntervalo.setEnabled(false);
				
				close();
			}
		});
		btnParar.setBounds(395, 306, 73, 23);
		btnParar.setEnabled(false);
		panel_Etc.add(btnParar);
		
		lblIntervalo = new JLabel("Intervalo");
		lblIntervalo.setFont(new Font("Tahoma", Font.PLAIN, 12));
		lblIntervalo.setBounds(21, 307, 75, 19);
		panel_Etc.add(lblIntervalo);
		
		textFieldIntervalo = new JTextField();
		// Listener: Espera ate que um valor seja digitado para liberar o inicio do programa
		textFieldIntervalo.addKeyListener(new java.awt.event.KeyListener() {  
            public void keyTyped(java.awt.event.KeyEvent e) {  
                
            }  
            public void keyPressed(java.awt.event.KeyEvent e) {  
            }  
            public void keyReleased(java.awt.event.KeyEvent e) {  
                if(textFieldIntervalo.getText().length() >= 1){                       
                    btnIniciar.setEnabled(true);  
                } else btnIniciar.setEnabled(false);
            }  
        });  
		textFieldIntervalo.setBounds(106, 307, 73, 20);
		panel_Etc.add(textFieldIntervalo);
		textFieldIntervalo.setColumns(10);
		
		// Demais itens da interface
		JSeparator separator = new JSeparator();
		separator.setBounds(11, 265, 460, 2);
		separator.setVisible(true);
		panel_Etc.add(separator);
		
		JLabel lblEstatistica = new JLabel("Estatísticas");
		lblEstatistica.setFont(new Font("Tahoma", Font.BOLD, 16));
		lblEstatistica.setBounds(200, 10, 120, 30);
		panel_Etc.add(lblEstatistica);
		
		JLabel lblMaximo = new JLabel("Máximo");
		lblMaximo.setFont(new Font("Tahoma", Font.PLAIN, 16));
		lblMaximo.setBounds(47, 72, 55, 23);
		panel_Etc.add(lblMaximo);
		
		JLabel lblMinimo = new JLabel("Mínimo");
		lblMinimo.setFont(new Font("Tahoma", Font.PLAIN, 16));
		lblMinimo.setBounds(47, 106, 55, 23);
		panel_Etc.add(lblMinimo);
		
		JLabel lblMedia = new JLabel("Média");
		lblMedia.setFont(new Font("Tahoma", Font.PLAIN, 16));
		lblMedia.setBounds(47, 140, 46, 23);
		panel_Etc.add(lblMedia);
		
		JLabel lblModa = new JLabel("Moda");
		lblModa.setFont(new Font("Tahoma", Font.PLAIN, 16));
		lblModa.setBounds(47, 174, 54, 23);
		panel_Etc.add(lblModa);
		
		JLabel lblDesvio = new JLabel("Desvio Padrão");
		lblDesvio.setFont(new Font("Tahoma", Font.PLAIN, 16));
		lblDesvio.setBounds(47, 208, 113, 23);
		panel_Etc.add(lblDesvio);
		
		labelResultadoMax = new JLabel("0");
		labelResultadoMax.setFont(new Font("Tahoma", Font.PLAIN, 16));
		labelResultadoMax.setBounds(221, 76, 99, 14);
		panel_Etc.add(labelResultadoMax);
		
		labelResultadoMin = new JLabel("0");
		labelResultadoMin.setFont(new Font("Tahoma", Font.PLAIN, 16));
		labelResultadoMin.setBounds(221, 110, 99, 14);
		panel_Etc.add(labelResultadoMin);
		
		labelResultadoMed = new JLabel("0");
		labelResultadoMed.setFont(new Font("Tahoma", Font.PLAIN, 16));
		labelResultadoMed.setBounds(221, 144, 99, 14);
		panel_Etc.add(labelResultadoMed);
		
		labelResultadoModa = new JLabel("0");
		labelResultadoModa.setFont(new Font("Tahoma", Font.PLAIN, 16));
		labelResultadoModa.setBounds(221, 178, 99, 14);
		panel_Etc.add(labelResultadoModa);
		
		labelResultadoDesvio = new JLabel("0");
		labelResultadoDesvio.setFont(new Font("Tahoma", Font.PLAIN, 16));
		labelResultadoDesvio.setBounds(221, 212, 99, 14);
		panel_Etc.add(labelResultadoDesvio);
		
		JLabel lblInfo = new JLabel("Para iniciar o programa, digite o intervalo da m\u00E9dia m\u00F3vel:");
		lblInfo.setFont(new Font("Tahoma", Font.PLAIN, 13));
		lblInfo.setBounds(21, 278, 379, 17);
		panel_Etc.add(lblInfo);

		
		// Gráfico 1 - Dados Originais
		// Extensao do XYPlot que contem varios subplots que compartilham um eixo
		final CombinedDomainXYPlot plot_original = new CombinedDomainXYPlot(new DateAxis("Tempo"));
        this.datasets_original = new TimeSeriesCollection(); 
        // Inicia variavel
        this.lastValue_original = 100;
        // Sequencia de valores definidos por um periodo de tempo, no caso, milisegundos
        final TimeSeries series_original = new TimeSeries("Valor lido", Millisecond.class);
        this.datasets_original = new TimeSeriesCollection(series_original);
        // Cria o eixo para exibir os dados lidos
        final NumberAxis rangeAxis_original = new NumberAxis("Força");
        rangeAxis_original.setAutoRangeIncludesZero(false);
        // Plota os dados em pares (valorLido, tempo)
    	final XYPlot subplot_original = new XYPlot(
                this.datasets_original, null, rangeAxis_original, new StandardXYItemRenderer()
        );
        subplot_original.setBackgroundPaint(Color.lightGray);
        subplot_original.setDomainGridlinePaint(Color.white);
        subplot_original.setRangeGridlinePaint(Color.white);
        plot_original.add(subplot_original);

        final JFreeChart chart_original = new JFreeChart("Dados Originais", plot_original);
        chart_original.setBorderPaint(Color.black);
        chart_original.setBorderVisible(true);
        chart_original.setBackgroundPaint(Color.white);

        plot_original.setBackgroundPaint(Color.lightGray);
        plot_original.setDomainGridlinePaint(Color.white);
        plot_original.setRangeGridlinePaint(Color.white);
        final ValueAxis axis_original = plot_original.getDomainAxis();
        axis_original.setAutoRange(true);
        // Tempo para um ponto 'sair' da area do grafico
        axis_original.setFixedAutoRange(60000.0);  

        final ChartPanel chartPanel_original = new ChartPanel(chart_original);
        panel_Original.add(chartPanel_original, BorderLayout.EAST);

        chartPanel_original.setPreferredSize(new java.awt.Dimension(490, 335));
        chartPanel_original.setBorder(new LineBorder(new Color(0, 0, 0)));
        
        
        // Gráfico 2 - Dados com Filtro Média Móvel
      	final CombinedDomainXYPlot plot_mmovel = new CombinedDomainXYPlot(new DateAxis("Tempo"));
        this.datasets_mmovel = new TimeSeriesCollection();

        this.lastValue_mmovel = 100.0;
        final TimeSeries series_mmovel = new TimeSeries("Valor com Filtro", Millisecond.class);
        this.datasets_mmovel = new TimeSeriesCollection(series_mmovel);
        final NumberAxis rangeAxis_mmovel = new NumberAxis("Força");
        rangeAxis_mmovel.setAutoRangeIncludesZero(false);
    	final XYPlot subplot_mmovel = new XYPlot(
                 this.datasets_mmovel, null, rangeAxis_mmovel, new StandardXYItemRenderer()
        );
        subplot_mmovel.setBackgroundPaint(Color.lightGray);
        subplot_mmovel.setDomainGridlinePaint(Color.white);
        subplot_mmovel.setRangeGridlinePaint(Color.white);
        plot_mmovel.add(subplot_mmovel);

        final JFreeChart chart_mmovel = new JFreeChart("Filtro Média Móvel", plot_mmovel);
        chart_mmovel.setBorderPaint(Color.black);
        chart_mmovel.setBorderVisible(true);
        chart_mmovel.setBackgroundPaint(Color.white);

        plot_mmovel.setBackgroundPaint(Color.lightGray);
        plot_mmovel.setDomainGridlinePaint(Color.white);
        plot_mmovel.setRangeGridlinePaint(Color.white);
        final ValueAxis axis_mmovel = plot_mmovel.getDomainAxis();
        axis_mmovel.setAutoRange(true);
        axis_mmovel.setFixedAutoRange(60000.0); 

        final ChartPanel chartPanel_mmovel = new ChartPanel(chart_mmovel);
        panel_MMovel.add(chartPanel_mmovel);

        chartPanel_mmovel.setPreferredSize(new java.awt.Dimension(490, 335));
        chartPanel_mmovel.setBorder(new LineBorder(new Color(0, 0, 0)));
        
        
        // Gráfico 3 - Dados com Filtro Integral
     	final CombinedDomainXYPlot plot_integral = new CombinedDomainXYPlot(new DateAxis("Tempo"));
        this.datasets_integral = new TimeSeriesCollection();

        this.lastValue_integral = 100.0;
        final TimeSeries series_integral = new TimeSeries("Valor com Filtro", Millisecond.class);
        this.datasets_integral = new TimeSeriesCollection(series_integral);
        final NumberAxis rangeAxis_integral = new NumberAxis("Força");
        rangeAxis_integral.setAutoRangeIncludesZero(false);
    	final XYPlot subplot_integral = new XYPlot(
                this.datasets_integral, null, rangeAxis_integral, new StandardXYItemRenderer()
        );
        subplot_integral.setBackgroundPaint(Color.lightGray);
        subplot_integral.setDomainGridlinePaint(Color.white);
        subplot_integral.setRangeGridlinePaint(Color.white);
        plot_integral.add(subplot_integral);

        final JFreeChart chart_integral = new JFreeChart("Filtro Integral", plot_integral);
        chart_integral.setBorderPaint(Color.black);
        chart_integral.setBorderVisible(true);
        chart_integral.setBackgroundPaint(Color.white);
    
        plot_integral.setBackgroundPaint(Color.lightGray);
        plot_integral.setDomainGridlinePaint(Color.white);
        plot_integral.setRangeGridlinePaint(Color.white);
        final ValueAxis axis_integral = plot_integral.getDomainAxis();
        axis_integral.setAutoRange(true);
        axis_integral.setFixedAutoRange(60000.0);

        final ChartPanel chartPanel_integral = new ChartPanel(chart_integral);
        panel_Integral.add(chartPanel_integral);

        chartPanel_integral.setPreferredSize(new java.awt.Dimension(490, 335));
        chartPanel_integral.setBorder(new LineBorder(new Color(0, 0, 0)));
        chartPanel_integral.repaint();
             
	}

	// Inicia a comunicacao com a porta serial
	public void initialize() {
        CommPortIdentifier portId = null;
        @SuppressWarnings("rawtypes")
		Enumeration portEnum = CommPortIdentifier.getPortIdentifiers();

        // Procura qual e a porta
        while (portEnum.hasMoreElements()) {
            CommPortIdentifier currPortId = (CommPortIdentifier) portEnum.nextElement();
            for (String portName : PORT_NAMES) {
                if (currPortId.getName().equals(portName)) {
                    portId = currPortId;
                    break;
                }
            }
        }
        
        // Se nao encontrar, mostra erro
        if (portId == null) {
        	textFieldIntervalo.setEnabled(false);
        	JOptionPane.showMessageDialog(new JFrame(), "Não foi possível conectar a porta COM.", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        } else {
        	textFieldIntervalo.setEnabled(true);
        }
		
		
        try {
            // Abre a porta serial
            serialPort = (SerialPort) portId.open(this.getClass().getName(),
                    TIME_OUT);

    		// Cria os diretorios se nao houver
    		criaDiretorios();
    		
    		
            // Define parametros
            serialPort.setSerialPortParams(DATA_RATE,
                    SerialPort.DATABITS_8,
                    SerialPort.STOPBITS_1,
                    SerialPort.PARITY_NONE);
    		
            // Abre os caminhos de entrada e saida de dados
            input = new BufferedReader(new InputStreamReader(serialPort.getInputStream()));
            output = serialPort.getOutputStream();

            // Adiciona listeners a porta
            serialPort.addEventListener(this);
            serialPort.notifyOnDataAvailable(true);
        } catch (Exception e) {
            System.err.println("Exceção: " + e.toString());
        }

    }

   
	// Fecha conexao com a porta serial
    public synchronized void close() {
        if (serialPort != null) {
            serialPort.removeEventListener();
            serialPort.close();
        }
    }

    // Quando o evento e disparado, le o dado que vem da serial 
    @Override
    public synchronized void serialEvent(SerialPortEvent oEvent) {
    	// Define string que armazena o dado lido 
    	String inputLine = null;
    	if (oEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
            try {
            	// Le o dado vindo da serial
                inputLine = input.readLine();
                // Escreve no arquivo de dados originais o dado que acabou de ser lido
                escreveNoArquivo(inputLine, nome_arq);
                // Converte o valor para inteiro
                this.lastValue_original = new Integer(inputLine).intValue();
                // Adiciona valor na serie de dados
                this.datasets_original.getSeries(0).add(new Millisecond(), this.lastValue_original);
                // Adiciona valor na ArrayList
                al.add(this.lastValue_original);
                
                // Armazena retorno do filtro integral
                this.lastValue_integral = filtroIntegral(al);
                // Escreve valor no arquivo correspondente
                escreveNoArquivo(String.valueOf(this.lastValue_integral), nome_arq_int);
                // Adiciona valor na serie de dados
                this.datasets_integral.getSeries(0).add(new Millisecond(), this.lastValue_integral);
                
                // Armazena retorno do filtro de media movel
                this.lastValue_mmovel = filtroMMovel(al);
                // Escreve valor no arquivo correspondente
                escreveNoArquivo(String.valueOf(this.lastValue_mmovel), nome_arq_mmovel);
                // Adiciona valor na serie de dados
                this.datasets_mmovel.getSeries(0).add(new Millisecond(), this.lastValue_mmovel);
                
                // Imprime valor
                //System.out.println(inputLine);
            } catch (Exception e) {
                System.err.println("Exceção: " + e.toString());
            } 
            finally{
            	atualizaEstatistica(contador);
            	contador++;
            }
        }
    }
    
    
	/**
	 * Filtro 1: Integra o valor
	 * @param al 
	 */

    public double filtroIntegral(ArrayList<Integer> al){
    	double aux1 = 0, aux2 = 0;
		double parcial = 0;
		int tamanho = al.size();
		if(tamanho == 0){
			return 0;
		} else if(tamanho == 1){
			aux2 = al.get(c1);
		} else {	
			aux1 = al.get(c1 - 1);
			aux2= al.get(c1);	
		}
		parcial = (aux1 + aux2) / 2;
		c1++;
    	return parcial;
    }
    
    /**
	 * Filtro 2: Escolhido o intervalo, faz a media movel dos valores
	 * @param al
	 */
    public double filtroMMovel(ArrayList<Integer> al){
    	double resultado = 0, soma = 0;
    	int c = c2;
		int tamanho = al.size();
		// Se o tamanho do ArrayList for menor que o intervalo definido
		if(tamanho < intervalo){
			// Vai rodar enquanto houver itens
			while(tamanho > 0){
				soma += al.get(c);
				tamanho--;
				c--;
			}
			resultado = soma / intervalo;
			// Se o tamanho do ArrayList for maior ou igual que o intervalo definido
		}else if(tamanho >= intervalo){
					// Vai rodar enquanto o tamanho do contador for igual ao intervalo		
			int contador = 0;
					while(contador < intervalo){	
						soma += al.get(c);
						contador++;
						c--;
					}
					resultado = soma / intervalo;
				} 
		c2++;
    	return resultado;
    }
    
    /**
     *  Atualiza as informações da estatistica
     * @param c 
     */
    public void atualizaEstatistica(int c){
    	double max, min, media, moda, desvio;
    	
    	Estatistica estatistica = new Estatistica(al, c);
    	DecimalFormat df = new DecimalFormat( "0.####" );
    	
    	// Armazena valores calculados
    	max = estatistica.getMaximo();
    	min = estatistica.getMinimo();
    	media = estatistica.getMediaAritmetica();
    	moda = estatistica.getModa();
    	desvio = estatistica.getDesvioPadrao();
    	
    	// Atualiza valores do label
    	labelResultadoMax.setText(String.valueOf(df.format(max)));
    	labelResultadoMin.setText(String.valueOf(df.format(min)));
    	labelResultadoMed.setText(String.valueOf(df.format(media)));
    	labelResultadoModa.setText(String.valueOf(df.format(moda)));
    	labelResultadoDesvio.setText(String.valueOf(df.format(desvio)));

    }
    
    /**
     * Pega mês e ano para criar nome da pasta
     */
    public static String[] defineNomeSubDir(){
    	String nomes[] = new String[2];
 	   	String mes[] = {"Janeiro", "Fevereiro", "Marco", "Abril", "Maio", "Junho", 
 			   			"Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"};	   
 	   	Calendar agora = Calendar.getInstance(); 
 	   	nomes[0] =  mes[agora.get(Calendar.MONTH)] + " " + agora.get(Calendar.YEAR); 
 	   	nomes[1] = "Experimento_0";
    	return nomes;
    }
    
    public static void criaDiretorios(){
		String nomes[] = defineNomeSubDir();
    	sdir = nomes[0];
    	sdir2 = nomes[1];
		// Nome do diretório
		File diretorio = new File("c:\\DadosEletromiografo\\Dados");
		// Nome do subdiretório
		subdir = new File("c:\\DadosEletromiografo\\Dados\\"+ sdir); 
		subdir2 = new File("c:\\DadosEletromiografo\\Dados\\"+ sdir + "\\" + sdir2);
		
		// mkdirs() cria diretórios e subdiretórios.
		if (!diretorio.exists()) {
				diretorio.mkdirs();
		} else {
			System.out.println("Diretório já existente");
		}
		if (!subdir.exists()) {
			 	subdir.mkdirs();
		} else {
			System.out.println("Diretório já existente");
		}
		if (!subdir2.exists()) {
		 		subdir2.mkdirs();
		} else {
				System.out.println("Diretório já existente");
				for(int i = 0; subdir2.exists(); i++){
					subdir2 = new File("c:\\DadosEletromiografo\\Dados\\"+ sdir + "\\Experimento_" + i);
				}
				subdir2.mkdirs();
		}
			
    }
        
	public void escreveNoArquivo(String valor_lido, String nome){
		try{
			// O true no final garante que o arquivo nao vai ser sobrescrito e sim continuado
	    	FileWriter fw = new FileWriter(subdir2 + "\\" + nome, true);
			BufferedWriter bw = new BufferedWriter(fw); 
			bw.newLine();
			bw.append(valor_lido + "\n"); 
			// Limpa
			bw.flush();
			bw.close();
			fw.close();
		} catch (IOException e) { 
			System.err.printf("Erro ao abrir arquivo: %s.\n", e.getMessage()); 			
		}
	}
	
	
	public static void main(String[] args) throws Exception {

		final Principal tela = new Principal("Original");
		tela.pack();
		tela.frmPainel.setLocationRelativeTo(null); 
		tela.frmPainel.setVisible(true);	 
		
	}
}
