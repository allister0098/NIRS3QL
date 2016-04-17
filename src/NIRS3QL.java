import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.Timer;

import java.io.*;
import java.util.*;

import org.jfree.chart.*;
import org.jfree.chart.axis.*;
import org.jfree.chart.plot.*;
import org.jfree.chart.renderer.xy.*;
import org.jfree.data.xy.*;
import org.jfree.ui.*;

public class NIRS3QL extends JFrame implements ActionListener {

	private int id = 1;
	private ArrayList<String[]> list = new ArrayList<String[]>();
	private final DefaultXYDataset dataset = new DefaultXYDataset();
	private JLabel[] labels;
	private JFrame cf;
	Timer timer;

	public NIRS3QL(String title) {
		super(title);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		timer = new Timer(500, this);
		timer.setActionCommand("timer");

		JMenuBar mb = createMenuBar();
		this.setJMenuBar(mb);

		JPanel lp = createLabelPanel();
		lp.setPreferredSize(new Dimension(950, 240));
		this.add(lp);

		double[][] series = new double[2][128];
		for (int i = 0; i < 128; i++) {
			series[0][i] = i + 1.0;
			series[1][i] = 0.0;
		}
		dataset.addSeries("s1", series);
		JFreeChart chart = createChart(dataset);
		ChartPanel cp = new ChartPanel(chart, false);
		cp.setPreferredSize(new Dimension(950, 320));
		this.add(cp);

		cf = createControllerFrame();
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == timer) {
			id++;
			update();
		}
	}

	private JMenuBar createMenuBar() {
		JMenuBar menuBar = new JMenuBar();

		JMenu menuFile = new JMenu("File");
		menuBar.add(menuFile);

		JMenuItem menuOpen = new JMenuItem("Open File");
		menuFile.add(menuOpen);
		menuOpen.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				openFile();
			}
		});

		JMenuItem menuClose = new JMenuItem("Close");
		menuFile.add(menuClose);
		menuClose.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});

		JMenu menuView = new JMenu("View");
		menuBar.add(menuView);

		JMenuItem menuShow = new JMenuItem("Show Controller");
		menuView.add(menuShow);
		menuShow.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				cf.setVisible(true);
			}
		});

		return menuBar;
	}

	private JPanel createLabelPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(12, 6));
		labels = new JLabel[48];
		for (int i = 0; i < 48; i++) {
			String[] item = getItem(i);
			labels[i] = new JLabel(item[1]);
			labels[i].setFont(new Font("SansSerif", Font.PLAIN, 12));
			panel.add(labels[i]);
		}
		return panel;
	}

	private JFreeChart createChart(XYDataset dataset) {
		JFreeChart chart = ChartFactory.createXYLineChart(" ", // chart title
				"Pixel Number", // domain axis label
				"DN", // range axis label
				dataset, PlotOrientation.VERTICAL, // orientation
				false, // include legend
				false, // tooltips?
				false // URLs?
				);
		chart.setBackgroundPaint(Color.white);

		// set a few custom plot features
		XYPlot plot = (XYPlot) chart.getPlot();
		plot.setBackgroundPaint(Color.white);
		plot.setDomainGridlinesVisible(true);
		plot.setDomainGridlinePaint(Color.lightGray);
		plot.setRangeGridlinePaint(Color.lightGray);
		plot.setAxisOffset(new RectangleInsets(0, 0, 0, 0));

		// set the plot's axes to display integers
		NumberAxis domain = (NumberAxis) plot.getDomainAxis();
		domain.setLabelFont(new Font("SansSerif", Font.PLAIN, 12));
		domain.setTickUnit(new NumberTickUnit(16));
		domain.setMinorTickCount(8);
		domain.setRange(1, 128);
		domain.setMinorTickMarksVisible(true);

		NumberAxis range = (NumberAxis) plot.getRangeAxis();
		range.setLabelFont(new Font("SansSerif", Font.PLAIN, 12));
		TickUnitSource ticks = NumberAxis.createIntegerTickUnits();
		range.setStandardTickUnits(ticks);
		range.setAutoRangeIncludesZero(false);

		// render shapes and lines
		XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(true, true);
		plot.setRenderer(renderer);
		renderer.setBaseShapesVisible(false);
		renderer.setBaseShapesFilled(false);

		return chart;
	}

	private JFrame createControllerFrame() {
		JFrame frame = new JFrame("Controller");
		JPanel panel = new JPanel();

		JButton backwardButton = new JButton("|<<");
		backwardButton.setFont(new Font("SansSerif", Font.PLAIN, 12));
		backwardButton.setPreferredSize(new Dimension(60, 30));
		panel.add(backwardButton);
		backwardButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				id--;
				update();
			}
		});
		
		JButton stopButton = new JButton("||");
		stopButton.setFont(new Font("SansSerif", Font.PLAIN, 12));
		stopButton.setPreferredSize(new Dimension(60, 30));
		panel.add(stopButton);
		stopButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				timer.stop();
			}
		});

		JButton startButton = new JButton(">>");
		startButton.setFont(new Font("SansSerif", Font.PLAIN, 12));
		startButton.setPreferredSize(new Dimension(60, 30));
		panel.add(startButton);
		startButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				timer.start();
			}
		});

		JButton forwardButton = new JButton(">>|");
		forwardButton.setFont(new Font("SansSerif", Font.PLAIN, 12));
		forwardButton.setPreferredSize(new Dimension(60, 30));
		panel.add(forwardButton);
		forwardButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				id++;
				update();
			}
		});

		frame.setSize(350, 80);
		frame.add(panel, BorderLayout.CENTER);
		frame.setVisible(false);
		return frame;
	}

	public void readFile(File file) {
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line = null;
			while ((line = br.readLine()) != null) {
				String[] arrayLine = line.split(",");
				list.add(arrayLine);
			}
			br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void openFile() {
		JFileChooser fileChooser = new JFileChooser();

		int selected = fileChooser.showOpenDialog(this);
		if (selected == JFileChooser.APPROVE_OPTION) {
			File file = fileChooser.getSelectedFile();
			readFile(file);
			update();
		}
	}

	public void update() {
		String[] data = list.get(id);

		for (int i = 0; i < 48; i++) {
			String[] item = getItem(i);
			labels[i].setText(String.format("%-15s%9s", item[1], data[Integer.parseInt(item[0])-1]));
		}

		dataset.removeSeries("s1");
		double[][] series = new double[2][128];
		for (int i = 0; i < 128; i++) {
			double x = i + 1.0;
			double y = Double.parseDouble(data[i + 48].trim());
			if (y > 60000)
				y -= 65536;
			series[0][i] = x;
			series[1][i] = y;
		}
		dataset.addSeries("s1", series);
	}

	public String[] getItem(int i) {
		String[][] items = {
				{"1", "SRL_CNT"},
				{"2", "RPL_TIME"},
				{"3", "OBS_TIME32"},
				{"4", "OBS_TIME6"},
				{"5", "STACK"},
				{"7", "OBS_MODE1"},
				{"8", "OBS_MODE2"},
				{"9", "OBS_MODE3"},
				{"6", "CMD_CNT"},
				{"10", "CMD_ERR"},
				{"48", "SEND_PCKT_CNT"},
				{"47", "RJCT_PCKT_CNT"},
				{"12", "BUF_STAT"},
				{"11", "MEM_ERR"},
				{"41", "PRAMP_SNSR_CUR"},
				{"15", "HTR_PWR_COMM"},
				{"20", "HTR_PWR_REAL"},
				{"23", "HTR_SET_VAL"},
				{"18", "HTR_MODE"},
				{"36", "HTR_CUR"},
				{"32", "OPT_TMP"},
				{"34", "S_TMP"},
				{"35", "AE_TMP"},
				{"26", "INTEG_TRIG"},
				{"29", "INTEG_TIME"},
				{"27", "STK_MINMAX_OUT"},
				{"28", "STK_VAR_OUT"},
				{"30", "STK_INDEX"},
				{"14", "CHP_STAT"},
				{"31", "CHP_FREQ"},
				{"39", "CHP_CUR"},
				{"40", "CHP_AMP"},
				{"13", "CHP_PWR_COMM"},
				{"19", "CHP_PWR_REAL"},
				{"25", "SNSR_GAIN"},
				{"33", "SNSR_TMP"},
				{"42", "SNSR_CH20"},
				{"43", "SNSR_CH40"},
				{"44", "SNSR_CH60"},
				{"45", "SNSR_CH80"},
				{"46", "SNSR_CH100"},
				{"16", "RADLMP_PWR_COMM"},
				{"21", "RADLMP_PWR_REAL"},
				{"24", "RADLMP_GAIN"},
				{"37", "RADLMP_CUR"},
				{"17", "WAVLMP_PWR_COMM"},
				{"22", "WAVLMP_PWR_REAL"},
				{"38", "WAVLMP_CUR"}
				};

		return items[i];
	}

	public static void main(String args[]) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				NIRS3QL ql = new NIRS3QL("NIRS3QL");
				ql.setSize(1000, 640);
				ql.setLayout(new FlowLayout());
				ql.setLocationRelativeTo(null);
				ql.setVisible(true);
			}
		});
	}
}
