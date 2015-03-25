package UI;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;

import org.apache.commons.math3.fraction.Fraction;

import HPA.CreateHPA;
import HPA.HPA;
import HPA.PA;
import HPA.Vali_Probk;
import Util.TarjanSCC;
import Util.io;
import Util.util;

@SuppressWarnings("serial")
public class UI extends javax.swing.JPanel {
	public JFrame frmHpaDemo;
	public JMenuBar menuBar;
	public JMenu menu, submenu;
	public JMenuItem menuItem, openMenu, openMenu1, saveMenu;
	public JTextArea log;
	public JButton btnOpenGFile, btnOpenPFile, btnOpenHPAFile,
			btnSelectOutputFolder, btnGenerateHpa, btnRunBKWAlg, btnRunFWDAlg,
			btnFindThreshold;
	public JTextField textProcessFile, textPropertyFile, textHPAFile,
			textOPFolder, textX, textL, textXPrecision;
	public JScrollPane scrollPane_jt, scrollPane_log;
	public JTable jt;
	public JLabel lblPriorSAfterFailure;
	@SuppressWarnings("rawtypes")
	public JComboBox CBoxFailedSession, CBoxPriorSAfterFailure,
			CBoxIsInterleaving;

	static String ProcessFile = null, PropertyFile = null, hpaFile = null;
	static String failedserver = null;
	static ArrayList<String> failedInputs = new ArrayList<String>();
	static ArrayList<Fraction> r = new ArrayList<Fraction>(); // failure prob
	static ArrayList<Integer> failedStateIDs = new ArrayList<Integer>();
	static boolean g2g1AfterFailure = true, Interleaving = false;
	static File opFolderDir = null;
	static Fraction x = Fraction.ONE_HALF, xprecision = Fraction
			.getReducedFraction(1, 1000); // 0<=x<=1, 0<xprecision<1
	static long L = 50;

	static File dir = null; // store last location for OpenDialog

	static PA hpa = null;

	/**
	 * Create the application.
	 */
	public UI() {
		initComponents();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void initComponents() {
		frmHpaDemo = new JFrame();
		frmHpaDemo.setResizable(false);
		frmHpaDemo.setTitle("HPA Demo");
		Dimension wd = new Dimension(1000,700);
		frmHpaDemo.setMinimumSize(wd);
		frmHpaDemo.setMaximumSize(wd);
		frmHpaDemo.setSize(frmHpaDemo.getPreferredSize());
		frmHpaDemo.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmHpaDemo.setLocationRelativeTo(null);
		frmHpaDemo.getContentPane().setMaximumSize(wd);
		frmHpaDemo.getContentPane().setMinimumSize(wd);
		frmHpaDemo.getContentPane().setLayout(null);

		log = new JTextArea();
		log.setFont(new Font("Monospaced", Font.PLAIN, 13));
		log.setRows(5);
		log.setLineWrap(true);
		log.setAlignmentY(Component.TOP_ALIGNMENT);
		log.setAlignmentX(Component.LEFT_ALIGNMENT);
		log.setAutoscrolls(true);
		log.setMargin(new Insets(5, 5, 5, 5));
		log.setName("Log");
		scrollPane_log = new JScrollPane(log);
		scrollPane_log.setBounds(411, 25, 570, 570);
		scrollPane_log.setWheelScrollingEnabled(true);
		frmHpaDemo.getContentPane().add(scrollPane_log);

		btnOpenGFile = new JButton("Open Process File:");
		btnOpenGFile.setFont(new Font("Arial", Font.PLAIN, 12));
		btnOpenGFile.setBounds(10, 9, 149, 23);
		btnOpenGFile.setVerticalAlignment(SwingConstants.TOP);
		btnOpenGFile.setHorizontalAlignment(SwingConstants.LEFT);
		btnOpenGFile.setAlignmentY(Component.TOP_ALIGNMENT);
		btnOpenGFile.setActionCommand("g");
		btnOpenGFile.addActionListener(new OpenFileButtonListener());
		frmHpaDemo.getContentPane().add(btnOpenGFile);

		textProcessFile = new JTextField();
		textProcessFile.setBounds(169, 9, 224, 24);
		frmHpaDemo.getContentPane().add(textProcessFile);
		textProcessFile.setColumns(25);

		JLabel lblFailedSession = new JLabel("Which session may fail?");
		lblFailedSession.setFont(new Font("Arial", Font.PLAIN, 12));
		lblFailedSession.setBounds(10, 41, 270, 14);
		lblFailedSession.setVisible(true);
		lblFailedSession.setHorizontalAlignment(SwingConstants.LEFT);
		frmHpaDemo.getContentPane().add(lblFailedSession);

		CBoxFailedSession = new JComboBox();
		CBoxFailedSession.setFont(new Font("Arial", Font.PLAIN, 12));
		CBoxFailedSession.setBounds(290, 41, 111, 20);
		CBoxFailedSession.setModel(new DefaultComboBoxModel(new String[] {
				"Session 1", "Session 2" }));
		CBoxFailedSession.setSelectedIndex(0);
		frmHpaDemo.getContentPane().add(CBoxFailedSession);

		lblPriorSAfterFailure = new JLabel(
				"Which session has priority after failure?");
		lblPriorSAfterFailure.setFont(new Font("Arial", Font.PLAIN, 12));
		lblPriorSAfterFailure.setBounds(10, 69, 270, 14);
		frmHpaDemo.getContentPane().add(lblPriorSAfterFailure);
		lblPriorSAfterFailure.setHorizontalAlignment(SwingConstants.LEFT);

		CBoxPriorSAfterFailure = new JComboBox();
		CBoxPriorSAfterFailure.setFont(new Font("Arial", Font.PLAIN, 12));
		CBoxPriorSAfterFailure.setBounds(290, 66, 111, 20);
		frmHpaDemo.getContentPane().add(CBoxPriorSAfterFailure);
		CBoxPriorSAfterFailure.setModel(new DefaultComboBoxModel(new String[] {
				"Session 2", "Session 1" }));
		CBoxPriorSAfterFailure.setSelectedIndex(0);

		JLabel lblIsItInterleaving = new JLabel(
				"Interleaving execution after failure?");
		lblIsItInterleaving.setVisible(true);
		lblIsItInterleaving.setFont(new Font("Arial", Font.PLAIN, 12));
		lblIsItInterleaving.setBounds(10, 94, 270, 14);
		lblIsItInterleaving.setHorizontalAlignment(SwingConstants.LEFT);
		frmHpaDemo.getContentPane().add(lblIsItInterleaving);

		CBoxIsInterleaving = new JComboBox<String>();
		CBoxIsInterleaving.setMaximumRowCount(2);
		CBoxIsInterleaving.setFont(new Font("Arial", Font.PLAIN, 12));
		CBoxIsInterleaving.setBounds(292, 91, 109, 20);

		CBoxIsInterleaving.setModel(new DefaultComboBoxModel(new String[] {
				"No", "Yes" }));
		CBoxIsInterleaving.setSelectedIndex(0);
		frmHpaDemo.getContentPane().add(CBoxIsInterleaving);

		jt = new JTable();
		jt.setCellSelectionEnabled(true);
		jt.setColumnSelectionAllowed(true);
		jt.setToolTipText("");
		jt.setFillsViewportHeight(true);
		jt.setModel(new DefaultTableModel(new Object[][] { { "", "", "" }, },
				new String[] { "Failed Input", "Failure Pr.",
						"Failed State ID(Optional)" }) {
			Class[] columnTypes = new Class[] { String.class, String.class,
					Integer.class };

			public Class getColumnClass(int columnIndex) {
				return columnTypes[columnIndex];
			}
		});
		jt.getColumnModel().getColumn(0).setPreferredWidth(132);
		jt.getColumnModel().getColumn(1).setPreferredWidth(80);
		jt.getColumnModel().getColumn(2).setPreferredWidth(162);
		jt.setBackground(Color.WHITE);
		jt.setBorder(null);
		scrollPane_jt = new JScrollPane(jt);
		scrollPane_jt
				.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane_jt.setBounds(10, 119, 391, 110);
		frmHpaDemo.getContentPane().add(scrollPane_jt);

		JButton btnAddRow = new JButton("\u2191 Add Row");
		btnAddRow.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				((DefaultTableModel) jt.getModel()).addRow(new Vector(3));
			}
		});
		btnAddRow.setFont(new Font("Arial", Font.PLAIN, 12));
		btnAddRow.setBounds(290, 232, 111, 23);

		btnOpenPFile = new JButton("Open Property File:");
		btnOpenPFile.setFont(new Font("Arial", Font.PLAIN, 12));
		btnOpenPFile.setVisible(true);
		frmHpaDemo.getContentPane().add(btnAddRow);
		btnOpenPFile.setBounds(10, 266, 149, 23);
		btnOpenPFile.setVerticalAlignment(SwingConstants.TOP);
		btnOpenPFile.setHorizontalAlignment(SwingConstants.LEFT);
		btnOpenPFile.setAlignmentY(0.0f);
		btnOpenPFile.setActionCommand("p");
		btnOpenPFile.addActionListener(new OpenFileButtonListener());
		frmHpaDemo.getContentPane().add(btnOpenPFile);

		textPropertyFile = new JTextField();
		textPropertyFile.setBounds(169, 264, 232, 24);
		textPropertyFile.setColumns(25);
		frmHpaDemo.getContentPane().add(textPropertyFile);

		btnGenerateHpa = new JButton("Generate HPA!");
		btnGenerateHpa.setFont(new Font("Arial", Font.PLAIN, 12));
		btnGenerateHpa.setBounds(10, 321, 391, 23);
		btnGenerateHpa.setVisible(true);
		frmHpaDemo.getContentPane().add(btnGenerateHpa);

		JLabel lbl = new JLabel("For decidability:");
		lbl.setEnabled(false);
		lbl.setFont(new Font("Arial", Font.PLAIN, 12));
		lbl.setBounds(10, 432, 270, 14);
		lbl.setVisible(true);
		lbl.setVerticalAlignment(SwingConstants.TOP);
		frmHpaDemo.getContentPane().add(lbl);

		JLabel lblThresholdPr = new JLabel(
				"Threshold probability \"x\" (e.g. 0.5 or 2/5):");
		lblThresholdPr.setFont(new Font("Arial", Font.PLAIN, 12));
		lblThresholdPr.setBounds(10, 447, 270, 14);
		lblThresholdPr.setVisible(true);
		frmHpaDemo.getContentPane().add(lblThresholdPr);

		textX = new JTextField();
		textX.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				updateX();
			}
		});
		textX.setFont(new Font("Arial", Font.PLAIN, 12));
		textX.setBounds(315, 444, 86, 20);
		textX.setVisible(true);
		textX.setText("0.5");
		textX.setColumns(10);
		frmHpaDemo.getContentPane().add(textX);

		JLabel lblMaxRunLength = new JLabel("Max. run length \"L\":");
		lblMaxRunLength.setFont(new Font("Arial", Font.PLAIN, 12));
		lblMaxRunLength.setBounds(10, 472, 270, 14);
		lblMaxRunLength.setVisible(true);
		frmHpaDemo.getContentPane().add(lblMaxRunLength);

		textL = new JTextField();
		textL.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				updateL();
			}
		});
		textL.setFont(new Font("Arial", Font.PLAIN, 12));
		textL.setBounds(315, 469, 86, 20);
		textL.setVisible(true);
		textL.setText("50");
		textL.setColumns(10);
		frmHpaDemo.getContentPane().add(textL);

		btnRunBKWAlg = new JButton("Run Backward Alg.");
		btnRunBKWAlg.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				log.append("\n----------DECIDABILITY ALGORITHM: BACKWARD----------\n");
				if (hpa == null) {
					// loadHPA();
					log.append("ERROR: no HPA file loaded. Exit.");
					return;
				}
				updateX();
				updateL();

				HPA HPA = new HPA();
				int qs = (int) hpa.Q0.toArray()[0];// TODO
				long UserTimeNano0 = 0, UserTimeNano1 = 0;
				try {
					UserTimeNano0 = util.getCPUTime();
					log.append(HPA.bkdCheckX_withTracingBack(hpa, qs, x, L)); // HPA.bkdCheckX(hpa,
																				// qs,
																				// x,
																				// L)
					UserTimeNano1 = util.getCPUTime();
					log.append("*Time spent on backward algorithm:"
							+ " CPU time=" + (UserTimeNano1 - UserTimeNano0)
							+ "\n");
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});
		btnRunBKWAlg.setFont(new Font("Arial", Font.PLAIN, 12));
		btnRunBKWAlg.setBounds(10, 492, 179, 23);
		btnRunBKWAlg.setVisible(true);
		frmHpaDemo.getContentPane().add(btnRunBKWAlg);

		btnRunFWDAlg = new JButton("Run Forward Alg.");
		btnRunFWDAlg.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				log.append("\n----------DECIDABILITY ALGORITHM: FORWARD----------\n");
				if (hpa == null) {
					// loadHPA();
					log.append("ERROR: no HPA file loaded. Exit.");
					return;
				}
				updateX();
				updateL();

				HPA HPA = new HPA();
				int qs = (int) hpa.Q0.toArray()[0];// TODO
				long UserTimeNano0 = 0, UserTimeNano1 = 0;
				try {
					UserTimeNano0 = util.getCPUTime();
					log.append(HPA.fwdCheckX(hpa, qs, x, L));
					UserTimeNano1 = util.getCPUTime();
					log.append("*Time spent on forward algorithm:"
							+ " CPU time=" + (UserTimeNano1 - UserTimeNano0)
							+ "\n");
				} catch (Exception e1) {
					log.append(e1.getMessage());
					e1.printStackTrace();
					return;
				}
			}
		});
		btnRunFWDAlg.setFont(new Font("Arial", Font.PLAIN, 12));
		btnRunFWDAlg.setBounds(222, 492, 179, 23);
		btnRunFWDAlg.setVisible(true);
		frmHpaDemo.getContentPane().add(btnRunFWDAlg);

		JLabel lblLog = new JLabel("Log:");
		lblLog.setFont(new Font("Arial", Font.PLAIN, 12));
		lblLog.setBounds(411, 9, 46, 14);
		frmHpaDemo.getContentPane().add(lblLog);

		JButton btnClearLog = new JButton("Clear log\u2193");
		btnClearLog.setFont(new Font("Arial", Font.PLAIN, 11));
		btnClearLog.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				log.setText("");
				log.repaint();
			}
		});
		btnClearLog.setBounds(873, 5, 108, 23);
		frmHpaDemo.getContentPane().add(btnClearLog);

		btnOpenHPAFile = new JButton("Load HPA:");
		btnOpenHPAFile.setFont(new Font("Arial", Font.PLAIN, 12));

		btnOpenHPAFile.setHorizontalAlignment(SwingConstants.LEFT);
		btnOpenHPAFile.setBounds(10, 368, 149, 23);
		btnOpenHPAFile.setActionCommand("h");
		btnOpenHPAFile.addActionListener(new OpenFileButtonListener());
		frmHpaDemo.getContentPane().add(btnOpenHPAFile);

		textHPAFile = new JTextField();
		// textHPAFile.setText("data/op/HPA_hpa");
		textHPAFile.setColumns(25);
		textHPAFile.setBounds(10, 397, 391, 24);
		frmHpaDemo.getContentPane().add(textHPAFile);

		JSeparator separator = new JSeparator();
		separator.setBackground(Color.BLACK);
		separator.setForeground(Color.DARK_GRAY);
		separator.setBounds(10, 355, 391, 6);
		frmHpaDemo.getContentPane().add(separator);

		JLabel lbl1 = new JLabel("For robustness:");
		lbl1.setEnabled(false);
		lbl1.setFont(new Font("Arial", Font.PLAIN, 12));
		lbl1.setBounds(10, 532, 270, 14);
		lbl1.setVisible(true);
		lbl1.setVerticalAlignment(SwingConstants.TOP);
		frmHpaDemo.getContentPane().add(lbl1);

		JLabel lblPrecision = new JLabel(
				"Specify precision \"xprecision\" (e.g. 0.1, 0.001):");
		lblPrecision.setFont(new Font("Arial", Font.PLAIN, 12));
		lblPrecision.setBounds(10, 546, 295, 14);
		lblPrecision.setVisible(true);
		frmHpaDemo.getContentPane().add(lblPrecision);

		textXPrecision = new JTextField(); // for xprecision
		textXPrecision.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				updateXPrecision();
			}
		});
		textXPrecision.setFont(new Font("Arial", Font.PLAIN, 12));
		textXPrecision.setBounds(315, 543, 86, 20);
		textXPrecision.setVisible(true);
		textXPrecision.setText("0.001");
		textXPrecision.setColumns(10);
		frmHpaDemo.getContentPane().add(textXPrecision);

		btnFindThreshold = new JButton("Decide Robustness.");
		btnFindThreshold.setFont(new Font("Arial", Font.PLAIN, 12));
		btnFindThreshold.setBounds(10, 568, 391, 23);
		frmHpaDemo.getContentPane().add(btnFindThreshold);

		btnSelectOutputFolder = new JButton("Select Output Folder:");
		btnSelectOutputFolder.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser fc = new JFileChooser();
				if (dir != null){
					fc.setCurrentDirectory(dir);
				}else{
					fc.setCurrentDirectory(new java.io.File("."));
				}
				fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				fc.setDialogTitle("Select Output Folder");
				fc.setAcceptAllFileFilterUsed(false);
				if (fc.showOpenDialog(UI.this) == JFileChooser.APPROVE_OPTION) {
					opFolderDir = dir = fc.getSelectedFile();
					textOPFolder.setText(dir.toString());
					// textHPAFile.setText(file.toString());//let user select
				}
			}
		});
		btnSelectOutputFolder.setFont(new Font("Arial", Font.PLAIN, 12));
		btnSelectOutputFolder.setBounds(10, 293, 149, 23);
		btnSelectOutputFolder.setActionCommand("op");
		frmHpaDemo.getContentPane().add(btnSelectOutputFolder);

		textOPFolder = new JTextField();
		textOPFolder.setBounds(169, 290, 232, 24);
		textOPFolder.setColumns(25);
		frmHpaDemo.getContentPane().add(textOPFolder);

		frmHpaDemo.pack(); // add this statement after adding all components!
		frmHpaDemo.setVisible(true);

	}

	protected void updateX() {
		x = util.parseFractionPr(textX.getText().toString());
		if (x == null) {
			log.append("Warning: invalid x defined, should be number between [0,1], reset to 0.5.\n");
			textX.setText("0.5");
			x = Fraction.ONE_HALF;
		}
	}

	protected void updateL() {
		try {
			L = Integer.parseInt(textL.getText().toString());
		} catch (java.lang.Exception el) {
			L = 50;
			log.append("Warning: invalid L defined, should be positive integer, reset to "
					+ L + ".\n");
			textL.setText(String.valueOf(L));

		}
		if (L < 0) {
			L = 50;
			log.append("Warning: invalid L defined, should be positive integer, reset to "
					+ L + ".\n");
			textL.setText(String.valueOf(L));

		}
	}

	protected void updateXPrecision() {
		xprecision = util.parseFractionPr(textXPrecision.getText().toString());
		if (xprecision == null || xprecision.compareTo(Fraction.ZERO) <= 0
				|| xprecision.compareTo(Fraction.ONE) >= 0) {
			log.append("Warning: invalid xprecision defined, should be number within (0,1), thus reset to 0.001.\n");
			textXPrecision.setText("0.001");
			xprecision = Fraction.getReducedFraction(1, 1000);
		}

	}

	public static void main(String[] args) throws Exception {
		UI window = new UI();
		window.action();
	}

	public void action() {
		this.CBoxFailedSession.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				failedserver = CBoxFailedSession.getSelectedItem().toString()
						.contains("1") ? "1S" : "2S";
			}
		});

		this.CBoxPriorSAfterFailure.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				g2g1AfterFailure = CBoxPriorSAfterFailure.getSelectedItem()
						.toString().contains("2") ? true : false;
			}
		});

		this.CBoxIsInterleaving.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Interleaving = CBoxIsInterleaving.getSelectedItem().toString()
						.contains("No") ? false : true;
			}
		});

		this.btnGenerateHpa.addActionListener(new CreateHPABtnListener());
		this.btnFindThreshold.addActionListener(new RobustnessBtnListener());
	}

	private class OpenFileButtonListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			String command = e.getActionCommand();
			JFileChooser fc = new JFileChooser(); // Create a file chooser
			if (dir != null){
				fc.setCurrentDirectory(dir);
			}else{
				fc.setCurrentDirectory(new java.io.File("."));
			}
			
			switch (command) {
			case "g":
				fc.setDialogTitle("Open process file");
				break;
			case "p":
				fc.setDialogTitle("Open property file");
				break;
			case "h":
				fc.setDialogTitle("Open HPA file");
				break;
			}
			
			 if (command.equals("h") && 
					 hpaFile!=null
					 && hpa==null
					 && ! textOPFolder.getText().isEmpty()) {
				 //=>updated in GenerateHPA
				loadHPA();
				return;//no longer open dialog
			}
			
			int returnVal = fc.showOpenDialog(UI.this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = fc.getSelectedFile();
				dir = fc.getCurrentDirectory();
				if (command.equals("g")) {
					textProcessFile.setText(file.toString());
					ProcessFile = textProcessFile.getText();
				} else if (command.equals("p")) {
					textPropertyFile.setText(file.toString());
					PropertyFile = textPropertyFile.getText();
				} else if (command.equals("h")) {
					textHPAFile.setText(file.toString());
					hpaFile = textHPAFile.getText();
					loadHPA();
				}
			} // else Open command cancelled by user
		}

	}

	private class CreateHPABtnListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			log.append("\n----------GENERATE 1HPA----------\n");
			failedserver = CBoxFailedSession.getSelectedItem().toString()
					.contains("1") ? "1S" : "2S";
			g2g1AfterFailure = CBoxPriorSAfterFailure.getSelectedItem()
					.toString().contains("2") ? true : false;
			Interleaving = CBoxIsInterleaving.getSelectedItem().toString()
					.contains("No") ? false : true;
			failedInputs.clear();
			r.clear();
			failedStateIDs.clear();
			log.setText("");// reset log textarea
			
			if(opFolderDir==null){
				log.append("Please specify output folder. Exit.\n");
				return;
			}
			if (ProcessFile == null| PropertyFile == null) {
				log.append("Please specify both process and property files. Exit.\n");
				return;
			}
			log.append("Note: intermediate files will be output to the folder "
					+ opFolderDir + ".\n");

			ProcessFile = textProcessFile.getText();
			PropertyFile = textPropertyFile.getText();

			/* Create HPA */
			File f = new File(ProcessFile);
			if (!f.exists()) {
				log.append("ERROR: Process file doesn't exist.Exit.\n");
				return;
			}
			edu.princeton.cs.introcs.In in = new edu.princeton.cs.introcs.In(f);
			CreateHPA g0 = null;
			try {
				g0 = new CreateHPA(in);
			} catch (Exception e3) {
				e3.printStackTrace();
			}
			// g0.RemoveUnreachable();
			in.close();
			g0.name = "g0";
			if (!g0.is0HPA) {
				log.append("ERROR: g0 is not 1HPA.");
				return;
			}
			String message = g0.checkT();
			if (message != null) {
				log.append("ERROR: \n" + g0.name
						+ " violating checkT, at state " + message+"Exit.");
				return;
			}

			try {
				io.writePA2file(g0, opFolderDir + "/file_" + g0.name, "");
				io.writeNFA2FAT(g0, opFolderDir + "/FAT_" + g0.name, "");
			} catch (IOException e2) {
				e2.printStackTrace();
			}
			log.append("Process file: " + f.getName() + "\n");
			log.append("Single process g0 successfully read and stored into files "
					+ g0.name + ".\n");

			CreateHPA g1 = new CreateHPA(g0), g2 = new CreateHPA(g0);
			g1.name = "g1";
			g1.prefix = "1S";
			g1.renameStates(g1.prefix, "");
			g1.renameStateProp(g1.prefix, "");
			g1.iniTransPropSatisfied();
			g2.name = "g2";
			g2.prefix = "2S";
			g2.renameStates(g2.prefix, "");
			g2.renameStateProp(g2.prefix, "");
			g2.iniTransPropSatisfied();

			Set<String> com = new HashSet<String>();
			com.add("T");
			com = util.intersection(com, g0.symbols);

			log.append("Shared/Common symbols (synced) between processes:"
					+ com.toString() + "\n");

			g1.renameSymbolsExceptCom(g1.prefix, "", com);
			g2.renameSymbolsExceptCom(g2.prefix, "", com);

			CreateHPA g = null;
			try {
				io.writePA2file(g1, opFolderDir + "/file_g1", "");
				io.writePA2file(g2, opFolderDir + "/file_g2", "");
				g = CreateHPA.PSCompPOR(g1, g2);
				io.writePA2file(g, opFolderDir + "/file_" + g.name, "");
				io.writeNFA2FAT(g, opFolderDir + "/FAT_" + g.name, "");
			} catch (Exception e2) {
				e2.printStackTrace();
			}
			log.append("Processes composition successfully created and stored into files "
					+ g.name + ".\n");
			// Define Failures. Two ways: failed states specified or not.
			DefaultTableModel dtm = (DefaultTableModel) jt.getModel();
			int nRow = dtm.getRowCount(), nCol = dtm.getColumnCount();
			boolean noStateIDforFailure = false;
			for (int i = 0; i < nRow; i++) {
				String input = "";
				Fraction pr = Fraction.ZERO;
				int sid = -1;
				boolean valid = true;
				for (int j = 0; j < nCol; j++) {

					String cell = "";
					try {
						cell = dtm.getValueAt(i, j).toString().trim();
					} catch (NullPointerException ee) {
						cell = "";
					}
					if ((cell.equals(null) | cell.isEmpty()) && j != 2) {
						valid = false;
						break;
					}

					switch (j) {
					case 0:
						input = cell;
						if (input.equals(null)
								| input.replaceAll(" ", "").isEmpty()) {
							log.append("Warning: Failure line " + i
									+ " omited because of null input. \n");
							valid = false;
						}
						if (!com.contains(input))
							input = failedserver + input;
						if (noStateIDforFailure && failedInputs.contains(input)) {
							log.append("Warining: Failure line "
									+ i
									+ " omited because of duplicate defined input while no state IDs are defined. \n");
							valid = false;
						}
						if (!g.symbols.contains(input) || com.contains(input)) {
							log.append("Warining: Failure line " + i
									+ " omited because of invalid input. \n");
							valid = false;
						}
						break;
					case 1:
						pr = util.parseFractionPr(cell);
						if (pr.compareTo(Fraction.ZERO) < 0
								| pr.compareTo(Fraction.ONE) > 0) {
							valid = false;
							log.append("Warning: Failure line " + i
									+ " omited because of invalid Pr. \n");
						}
						break;
					case 2:
						// once in a row stateID is disabled, the same in other
						// rows
						if (noStateIDforFailure) {
							break;
						}
						if (cell.equals(null)
								| cell.replaceAll(" ", "").isEmpty()) {
							if (i == 0) {
								noStateIDforFailure = true;
								log.append("Note: Failure are not defined for specific states.\n");
								// failedStateIDs.clear();//true
							} else {
								valid = false;
								log.append("Warning: Failure line "
										+ i
										+ " omited because of missing state ID. \n");
							}
							break;
						} else {
							sid = Integer.parseInt(cell);
							if (sid < 0 | sid >= g.V.size()) {
								valid = false;
								log.append("Warning: Failure line "
										+ i
										+ " omited because of invalid state ID defined. \n");
							}
						}

						break;
					}
				}
				if (valid) {
					failedInputs.add(input);
					r.add(pr);
					if (!noStateIDforFailure)
						failedStateIDs.add(sid);
				}
			}

			log.append("Failure definitions: " + failedInputs.toString()
					+ r.toString() + failedStateIDs.toString() + ".\n");

			CreateHPA gf = new CreateHPA(g); // deep copy
			// Note only one server can fail!
			try {
				if (g2g1AfterFailure)
					gf.addFailure(g2, g1, failedserver, failedStateIDs,
							failedInputs, r, "f", Interleaving);
				else
					gf.addFailure(g1, g2, failedserver, failedStateIDs,
							failedInputs, r, "f", Interleaving);
				String comment = ": g with failure " + failedserver
						+ failedStateIDs + failedInputs.toString()
						+ r.toString();
				io.writePA2file(gf, opFolderDir + "/file_" + gf.name, comment);
				io.writeNFA2FAT(gf, opFolderDir + "/FAT_" + gf.name, comment);
				log.append("Process composition with failure successfully generated and stored into files "
						+ gf.name + ".\n");
				message = gf.checkT();
				if (message != null) {
					log.append("ERROR: " + gf.name
							+ " violating checkT, at state " + message + "Exit.\n");
					return;
				}
			} catch (Exception e1) {
				e1.printStackTrace();
			}

			CreateHPA gp = null;
			try {
				f = new File(PropertyFile);
				if (!f.exists()) {
					log.append("ERROR: Property file doesn't exist. Exit.\n");
					return;
				}
				in = new edu.princeton.cs.introcs.In(f);
				gp = new CreateHPA(in);
				in.close();
				gp.name = "gp" + failedserver;
				gp.prefix = "P" + failedserver;
				gp.renameStates(gp.prefix, "");
				gp.renameStateProp(gp.prefix, "");
				gp.renameSymbolsExceptCom(failedserver, "", com);
				// add ERROR state with ERROR proposition:
				// checkT() is false
				io.writePA2file(gp, opFolderDir + "/file_" + gp.name, "");
				io.writeNFA2FAT(gp, opFolderDir + "/FAT_" + gp.name,
						"Property automata");
				log.append("Property file: " + f.getName() + "\n");
				log.append("Property file successfully read and stored into files "
						+ gp.name + ".\n");

				gp.DeterminePropertyAutomata(g.symbols, com, failedserver);
				gp.iniTransPropSatisfied();
				io.writePA2file(gp, opFolderDir + "/file_" + gp.name + "_d", "");
				io.writeNFA2FAT(gp, opFolderDir + "/FAT_" + gp.name + "_d",
						"Property automata determinized");
				log.append("Property file successfully determinized and stored into files "
						+ gp.name + "_d.\n");
			} catch (Exception e1) {
				e1.printStackTrace();
			}

			String temphpaFile = opFolderDir + "/HPA_hpa";
			CreateHPA gfp = null;
			try {
				gfp = CreateHPA.PSCompMergeERROR(gf, gp, failedserver); // 1-HPA
				temphpaFile = opFolderDir + "/HPA_"+gfp.name;
				io.writePA2file(gfp, opFolderDir + "/file_" + gfp.name, "");
				io.writeNFA2FAT(gfp, opFolderDir + "/FAT_" + gfp.name, "");
				// io.write2HPA(gfp, "data/"+opFolder+"/HPA_" + gfp.name, "");
				log.append("gfp successfully generated and stored into files "
						+ gfp.name + ".\n");

				// elimitate T transitions.
				// gf.checkT()->gfp.checkT(), and gp.checkT() not required
				message = gfp.checkT();
				if (message != null) {
					throw new Exception("What happened to gfp?" + "@" + message);
				}
				message = gfp.hasAbeforeT();
				if (message != null) {
					// ensure every T-transition has an external input
					// before T
					log.append("ERROR: not every T-state comes after input-state, such as state "
							+ message + ".\n");
					return;
				}
				gfp.removeTTransitions();
				io.writePA2file(gfp, opFolderDir + "/file_" + gfp.name
						+ "_no_T", "");
				io.writeNFA2FAT(gfp,
						opFolderDir + "/FAT_" + gfp.name + "_no_T", "");
				// io.write2HPA(gfp, "data/"+opFolder+"/HPA_" + gfp.name +
				// "_no_T", "");
				log.append("gfp without T-transitions successfully generated and stored as "
						+ gfp.name + "_no_T.\n");

				// deterministic
				gfp.Determine();
				io.writePA2file(gfp, opFolderDir + "/file_hpa_"+gfp.name, "");
				io.writeNFA2FAT(gfp, opFolderDir + "/FAT_hpa_"+gfp.name, "");
				io.write2HPA(gfp, temphpaFile, "");
				log.append("After determinization, 1-HPA generated and stored as "+temphpaFile+".\n");

			} catch (Exception e1) {
				e1.printStackTrace();
			}

			hpaFile = temphpaFile;
			textHPAFile.setText(hpaFile);
			hpa=null;
		}// actionPerformed

	}

	/**
	 * Find MAX non-empty threshold, with defined precision and execution
	 * length. Assumption: L is large enough.
	 * */
	private class RobustnessBtnListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			log.append("\n----------ROBUSTNESS ALGORITHM----------\n");
			if (hpa == null) {
				// loadHPA();
				log.append("ERROR: no HPA file loaded. Exit.");
				return;
			}
			updateXPrecision();
			L = hpa.L(); // L=4rn8^n
			log.append("Note:L_UpperBound = " + L + ".\n");
			HPA HPA = new HPA();
			int qs = (int) hpa.Q0.toArray()[0];// TODO
			try {
				long ut0 = util.getCPUTime();
				if (!(hpa.BKD_x != null && hpa.BKD_k <= L)) {
					HPA.bkd(hpa, qs, L);
				}
				if (hpa.BKD_x != null) {
					log.append("Minimum empty is " + hpa.BKD_x + " at L="
							+ hpa.BKD_k + ", thus robustness value is "
							+ (Fraction.ONE.subtract(hpa.BKD_x)) + ".\n");
					if (hpa.BKD_k == L) {
						log.append("Warning: max L reached.\n");
					}
					return;
				}
				// hpa.BKD_x == null: bkd empty (negative) non-convergence
				log.append("Exception in backward alg: Integer overflow.\n"
						+ "Now use forward algorithm and binary search to decide robustness. \n");
				updateXPrecision();
				Fraction ub = Fraction.ONE;// upper bound, empty
				Fraction lb = Fraction.ZERO;// lower bound, non-empty
				// robustness: min empty, x>=XX, empty; x<XX, non-empty
				x = ub.add(lb).divide((int) 2);
				while (ub.subtract(lb).compareTo(xprecision) >= 0) {
					// keep reducing distance between ub and lb until
					// (ub-lb)<=xprecision
					String m = HPA.fwdCheckX(hpa, qs, x, L);
					log.append(m);
					if (m.contains("non-empty")) {
						lb = x;
						x = lb.add(ub).divide((int) 2);
					} else {
						ub = x;
						x = lb.add(ub).divide((int) 2);
					}
				}
				log.append("x found by forward algorithm is within (" + lb
						+ "," + ub + "];\n" + "Thus robustness falls within ("
						+ (Fraction.ONE.subtract(ub)) + ", "
						+ (Fraction.ONE.subtract(lb)) + "] "
						+ "with xprecision=" + xprecision + ".\n");

				long ut1 = util.getCPUTime();
				log.append("*Time spent in calculating robustness:"
						+ " CPU time=" + (ut1 - ut0) + "\n");
			} catch (Exception e1) {
				e1.printStackTrace();
			}

		}
	}

	public Object[][] readJTableData() {
		DefaultTableModel dtm = (DefaultTableModel) jt.getModel();
		int nRow = dtm.getRowCount(), nCol = dtm.getColumnCount();
		Object[][] tableData = new Object[nRow][nCol];
		for (int i = 0; i < nRow; i++)
			for (int j = 0; j < nCol; j++)
				tableData[i][j] = dtm.getValueAt(i, j);
		return tableData;
	}

	public void loadHPA() {
		hpa = null;
		log.append("\n----------Load HPA----------\n");
		long UserTimeNano0 = util.getCPUTime();
		HPA HPA = new HPA();
		File f = new File(hpaFile);
		log.append("HPA file: " + f + "\n");
		if (!f.exists()) {
			log.append("ERROR: specified HPA file doesn't exist. Return.\n");
			return;
		}
		edu.princeton.cs.introcs.In in = new edu.princeton.cs.introcs.In(f);
		try {
			hpa = new PA(in);
		} catch (Exception e1) {
			log.append("Exception:"+e1.getMessage() + "\n");
			e1.printStackTrace();
			return;
		}
		in.close();
		
/*		try {
			io.writePA2FAT(hpa, f.getParentFile()+ "/FAT_hpa", "");
			//need to
		}catch (IOException e) {
			log.append("Exception:"+e.getMessage() + "\n");
			e.printStackTrace();
			return;
		}*/

		if (hpa.F.isEmpty()) {
			log.append("Warning: Selected 1HPA has no final states, "
					+ "thus no violation of specified property will occur,"
					+ "and robustness is 1. \n");
			// return;
		}
		if (hpa.Q0.size() > 1) {
			log.append("ERROR: There is more than one inisital state. Return.\n");
			hpa = null;
			return;
		}

		long UserTimeNano1 = util.getCPUTime();
		log.append("*Time spent to read data:  CPU time="
				+ (UserTimeNano1 - UserTimeNano0) + "\n");

		/*
		 * // Validate the PA for determination if(g.isDeterministic() != -1){
		 * throw new Exception("ERROR: g is non-deterministic.\n"); }
		 * 
		 * SystemTimeNano0 = util.getSystemTime( ); UserTimeNano0 =
		 * util.getCPUTime( );
		 * log.append("*Time spent to validate determination:" +
		 * " system time="+(SystemTimeNano0-SystemTimeNano1)+";" + " CPU time="+
		 * (UserTimeNano0-UserTimeNano1));
		 */

		/* Compute SCCs in the DG */
		TarjanSCC tjscc = new TarjanSCC(hpa.adj_out_int);

		try {
			// Check for reachability of the states
			if (tjscc.unReachableState != -1) {
				log.append("ERROR: Not all states in the given graph are reachable from initial state, e.g. state "
						+ tjscc.unReachableState + ". Return.\n");
				hpa = null;
				return;
			}
			/* Decide if a DG is HPA */
			if (!HPA.isHPA(hpa.conflict_node_group, tjscc.id())) {
				log.append("ERROR: The given PA is not an HPA. Return.\n");
				hpa = null;
				return;
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		// int M = tjscc.count();//M < V -> there is/are directed cycle(s)
		// ArrayList<Integer>[] sccs = tjscc.sccs(); // # of SCCs, size M
		// int[] sccHead = tjscc.head(); // size V

		/* assign level to the SCCS, then each node */
		// assign level to scc
		int[] levelSCC = HPA.levelSCCs(hpa, tjscc);// size M
		// assign level to nodes
		HPA.levelNodes(hpa, tjscc, levelSCC);

		log.append("The given PA is a " + util.max(levelSCC)
				+ "-level HPA with " + hpa.V.size() + " states.\n");
		log.append("Initial states: " + hpa.Q0 + "\n");
		log.append("Final states: " + hpa.F + "\n");
		for (int i = 0; i <= util.max(levelSCC); i++) {
			log.append("Level " + i + " nodes: " + hpa.obtainLevel(i) + "\n");
		}
		log.append("Input symbols: " + hpa.symbols + "\n");

		if (util.max(levelSCC) > 1) {
			log.append("ERROR: This HPA has more than two levels. Return.\n");
			hpa = null;
			return;
		}

		// after assigning levels, obtain witness sets now
		// Note the order of the functions cannot be exchanged
		try {
			hpa.obtainFiniteGoodSWSs();
			hpa.obtainFiniteGoodWSs();
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		log.append("SemiWitnessSet size=" + hpa.SWSs.size()
				+ ", WitnessSet size=" + hpa.WSs.size() + "\n");

		for (int i = 0; i < hpa.SWSs.size(); i++) {
			log.append("SemiWitnessSet " + i + ": L1Nodes= "
					+ hpa.SWS_Q1.get(hpa.SWSs.get(i).L1_id) + "; superL0Nodes="
					+ hpa.SWSs.get(i).super_L0_nodes + "\n");
		}

		for (int i = 0; i < hpa.WSs.size(); i++) {
			log.append("WitnessSet " + i + ": " + hpa.WSNodes(i)
					+ "is superWS: " + hpa.WSs.get(i).isSuperGoodWS + "\n");
		}
		UserTimeNano1 = util.getCPUTime();
		log.append("*Time spent to obtain levels and witness sets:"
				+ " CPU time=" + (UserTimeNano1 - UserTimeNano0) + "\n");

	}
}
