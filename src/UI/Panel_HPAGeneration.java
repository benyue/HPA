package UI;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
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
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;

import org.apache.commons.math3.fraction.Fraction;

import HPA.CreateHPA;
import Util.io;
import Util.util;
/**
 * @author Cindy Yue Ben
 * */
@SuppressWarnings("serial")
public class Panel_HPAGeneration extends JSplitPane {
	private JTable jt;
	private File ProcessFile = null, PropertyFile = null;
	private String failedserver = null;
	private ArrayList<String> failedInputs = new ArrayList<String>();
	private ArrayList<Fraction> r = new ArrayList<Fraction>(); // failure prob
	private ArrayList<Integer> failedStateIDs = new ArrayList<Integer>();
	private boolean g2g1AfterFailure = true, Interleaving = false;
	@SuppressWarnings("rawtypes")
	private JComboBox CBoxFailedSession, CBoxPriorSAfterFailure,
			CBoxIsInterleaving;
	private JTextField textProcessFile, textPropertyFile,textOPFolder;
	private static File opFolderDir = null;
	private static File dir = null; // store last location for OpenDialog

	private Panel_log logPane;
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	Panel_HPAGeneration(){
		this.setLayout(null);
		
		JPanel p = new JPanel();
		p.setLayout(null);
		
		JButton btnOpenGFile = new JButton("Process File:");
		btnOpenGFile.setHorizontalAlignment(SwingConstants.LEFT);
		btnOpenGFile.setFont(new Font("Tahoma", Font.PLAIN, 16));
		btnOpenGFile.setToolTipText("<html>Please load the file for the single process. <br>\r\nValid file format: plain text file, .txt file.");
		btnOpenGFile.setBounds(10, 9, 149, 24);
		btnOpenGFile.setActionCommand("g");
		btnOpenGFile.addActionListener(new OpenFileButtonListener());
		p.add(btnOpenGFile);

		textProcessFile = new JTextField();
		textProcessFile.setBounds(169, 9, 335, 24);
		textProcessFile.setColumns(25);
		p.add(textProcessFile);

		JLabel lblFailedSession = new JLabel("Which session may fail?");
		lblFailedSession.setFont(new Font("Tahoma", Font.PLAIN, 16));
		lblFailedSession.setBounds(10, 40, 303, 24);
		lblFailedSession.setHorizontalAlignment(SwingConstants.LEFT);
		p.add(lblFailedSession);

		CBoxFailedSession = new JComboBox();
		CBoxFailedSession.setFont(new Font("Tahoma", Font.PLAIN, 15));
		CBoxFailedSession.setBounds(318, 40, 111, 24);
		CBoxFailedSession.setModel(new DefaultComboBoxModel(new String[] {
				"Session 1", "Session 2" }));
		CBoxFailedSession.setSelectedIndex(0);
		CBoxFailedSession.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				failedserver = CBoxFailedSession.getSelectedItem().toString()
						.contains("1") ? "1S" : "2S";
			}
		});
		p.add(CBoxFailedSession);

		JLabel lblPriorSAfterFailure = new JLabel(
				"Which session has priority after failure?");
		lblPriorSAfterFailure.setFont(new Font("Tahoma", Font.PLAIN, 16));
		lblPriorSAfterFailure.setBounds(10, 70, 303, 24);
		p.add(lblPriorSAfterFailure);
		lblPriorSAfterFailure.setHorizontalAlignment(SwingConstants.LEFT);

		CBoxPriorSAfterFailure = new JComboBox();
		CBoxPriorSAfterFailure.setFont(new Font("Tahoma", Font.PLAIN, 15));
		CBoxPriorSAfterFailure.setBounds(318, 70, 111, 24);
		p.add(CBoxPriorSAfterFailure);
		CBoxPriorSAfterFailure.setModel(new DefaultComboBoxModel(new String[] {
				"Session 2", "Session 1" }));
		CBoxPriorSAfterFailure.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				g2g1AfterFailure = CBoxPriorSAfterFailure.getSelectedItem()
						.toString().contains("2") ? true : false;
			}
		});
		CBoxPriorSAfterFailure.setSelectedIndex(0);

		JLabel lblIsItInterleaving = new JLabel(
				"Interleaving execution after failure?");
		lblIsItInterleaving.setFont(new Font("Tahoma", Font.PLAIN, 16));
		lblIsItInterleaving.setBounds(10, 100, 303, 24);
		lblIsItInterleaving.setHorizontalAlignment(SwingConstants.LEFT);
		p.add(lblIsItInterleaving);

		CBoxIsInterleaving = new JComboBox<String>();
		CBoxIsInterleaving.setFont(new Font("Tahoma", Font.PLAIN, 15));
		CBoxIsInterleaving.setMaximumRowCount(2);
		CBoxIsInterleaving.setBounds(318, 100, 111, 24);

		CBoxIsInterleaving.setModel(new DefaultComboBoxModel(new String[] {
				"No", "Yes" }));
		CBoxIsInterleaving.setSelectedIndex(0);
		CBoxIsInterleaving.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Interleaving = CBoxIsInterleaving.getSelectedItem().toString()
						.contains("No") ? false : true;
			}
		});
		p.add(CBoxIsInterleaving);

		jt = new JTable();
		jt.setFont(new Font("Tahoma", Font.PLAIN, 15));
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
		JScrollPane scrollPane_jt = new JScrollPane(jt);
		scrollPane_jt
				.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane_jt.setBounds(10, 131, 494, 122);
		p.add(scrollPane_jt);

		JButton btnAddRow = new JButton("\u2191 Add Row");
		btnAddRow.setFont(new Font("Arial", Font.ITALIC, 15));
		btnAddRow.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				((DefaultTableModel) jt.getModel()).addRow(new Vector(3));
			}
		});
		btnAddRow.setBounds(365, 258, 139, 24);
		p.add(btnAddRow);

		JButton btnOpenPFile = new JButton("Property File:");
		btnOpenPFile.setFont(new Font("Tahoma", Font.PLAIN, 16));
		btnOpenPFile.setToolTipText("<html>Please load the file for the property. <br>\r\nValid file format: plain text file, .txt file.");
		btnOpenPFile.setBounds(10, 295, 149, 24);
		btnOpenPFile.setHorizontalAlignment(SwingConstants.LEFT);
		btnOpenPFile.setAlignmentY(0.0f);
		btnOpenPFile.setActionCommand("p");
		btnOpenPFile.addActionListener(new OpenFileButtonListener());
		p.add(btnOpenPFile);

		textPropertyFile = new JTextField();
		textPropertyFile.setBounds(169, 295, 335, 24);
		textPropertyFile.setColumns(25);
		p.add(textPropertyFile);

		JButton btnSelectOutputFolder = new JButton("Output Folder:");
		btnSelectOutputFolder.setFont(new Font("Tahoma", Font.PLAIN, 16));
		btnSelectOutputFolder.setToolTipText("<html>All intermediate files and final HPA files will be output into selected folder.");
		btnSelectOutputFolder.setHorizontalAlignment(SwingConstants.LEFT);
		btnSelectOutputFolder.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser fc = new JFileChooser();
				if (dir != null) {
					fc.setCurrentDirectory(dir);
				} else {
					fc.setCurrentDirectory(new java.io.File("."));
				}
				fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				fc.setDialogTitle("Select Output Folder");
				fc.setAcceptAllFileFilterUsed(false);
				if (fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
					opFolderDir = dir = fc.getSelectedFile();
					textOPFolder.setText(dir.toString());
					// textHPAFile.setText(file.toString());//let user select
				}
			}
		});
		btnSelectOutputFolder.setBounds(10, 325, 149, 24);
		btnSelectOutputFolder.setActionCommand("op");
		p.add(btnSelectOutputFolder);

		textOPFolder = new JTextField();
		textOPFolder.setBounds(169, 325, 335, 24);
		textOPFolder.setColumns(25);
		p.add(textOPFolder);

		JButton btnGenerateHpa = new JButton("Generate HPA!");
		btnGenerateHpa.setFont(new Font("Tahoma", Font.PLAIN, 16));
		btnGenerateHpa.setBounds(10, 354, 494, 28);
		btnGenerateHpa.addActionListener(new CreateHPABtnListener());
		p.add(btnGenerateHpa);
		
		logPane = new Panel_log();
		
		this.setOrientation(JSplitPane.VERTICAL_SPLIT);
		this.setTopComponent(p);
		this.setBottomComponent(logPane);
		this.setDividerLocation(400);
		this.setEnabled(false);
	}
	
	private class OpenFileButtonListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			String command = e.getActionCommand();
			JFileChooser fc = new JFileChooser(); // Create a file chooser
			if (dir != null) {
				fc.setCurrentDirectory(dir);
			} else {
				fc.setCurrentDirectory(new java.io.File("."));
			}

			switch (command) {
			case "g":
				fc.setDialogTitle("Open process file");
				break;
			case "p":
				fc.setDialogTitle("Open property file");
				break;
			}

			int returnVal = fc.showOpenDialog(fc);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = fc.getSelectedFile();
				dir = fc.getCurrentDirectory();
				if (command.equals("g")) {
					ProcessFile = file;
					textProcessFile.setText(file.toString());
				} else if (command.equals("p")) {
					PropertyFile = file;
					textPropertyFile.setText(file.toString());
				}
			} // else Open command cancelled by user
		}

	}
	
	private class CreateHPABtnListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (ProcessFile == null || PropertyFile == null || opFolderDir == null) {
				JOptionPane.showMessageDialog(new JFrame(),
						"ERROR: Please specify process file, property file, and output folder.");
				return;
			}
			
			appendLog("\n----------GENERATE 1HPA----------\n");
			failedserver = CBoxFailedSession.getSelectedItem().toString()
					.contains("1") ? "1S" : "2S";
			g2g1AfterFailure = CBoxPriorSAfterFailure.getSelectedItem()
					.toString().contains("2") ? true : false;
			Interleaving = CBoxIsInterleaving.getSelectedItem().toString()
					.contains("No") ? false : true;
			failedInputs.clear();
			r.clear();
			failedStateIDs.clear();
			clearLog();

			/* Create HPA */
			if (!ProcessFile.exists()) {
				appendLog("ERROR: Process file doesn't exist.Exit.\n");
				return;
			}
			edu.princeton.cs.introcs.In in = new edu.princeton.cs.introcs.In(ProcessFile);
			CreateHPA g0 = null;
			try {
				g0 = new CreateHPA(in);
			} catch (Exception e3) {
				appendLog("ERROR in loading process file.\n"
						+ e3.getMessage());
				return;
			}
			// g0.RemoveUnreachable();
			in.close();
			g0.name = "g0";
			if (!g0.is0HPA) {
				appendLog("ERROR: g0 is not 1HPA.");
				return;
			}
			String message = g0.checkT();
			if (message != null) {
				appendLog("ERROR: \n" + g0.name
						+ " violating checkT, at state " + message + "Exit.");
				return;
			}
	
			try {
				g0.writeCreateHPA2file( opFolderDir + "/file_" + g0.name, "");
				g0.writeCreateHPA2FAT( opFolderDir + "/FAT_" + g0.name, "");
			} catch (IOException e2) {
				e2.printStackTrace();
			}
			appendLog("Process file: " + ProcessFile.getName() + "\n");
			appendLog("Single process g0 successfully read and stored into files "
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
	
			appendLog("Shared/Common symbols (synced) between processes:"
					+ com.toString() + "\n");
	
			g1.renameSymbolsExceptCom(g1.prefix, "", com);
			g2.renameSymbolsExceptCom(g2.prefix, "", com);
	
			CreateHPA g = null;
			try {
				g1.writeCreateHPA2file(  opFolderDir + "/file_g1", "");
				g2.writeCreateHPA2file(  opFolderDir + "/file_g2", "");
				g = CreateHPA.PSCompPOR(g1, g2);
				g.writeCreateHPA2file(  opFolderDir + "/file_" + g.name, "");
				g.writeCreateHPA2FAT(opFolderDir + "/FAT_" + g.name, "");
			} catch (Exception e2) {
				e2.printStackTrace();
			}
			appendLog("Processes composition successfully created and stored into files "
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
							appendLog("Warning: Failure line " + i
									+ " omited because of null input. \n");
							valid = false;
						}
						if (!com.contains(input))
							input = failedserver + input;
						if (noStateIDforFailure && failedInputs.contains(input)) {
							appendLog("Warining: Failure line "
									+ i
									+ " omited because of duplicate defined input while no state IDs are defined. \n");
							valid = false;
						}
						if (!g.symbols.contains(input) || com.contains(input)) {
							appendLog("Warining: Failure line " + i
									+ " omited because of invalid input. \n");
							valid = false;
						}
						break;
					case 1:
						pr = util.parseFractionPr(cell);
						if (pr==null | pr.compareTo(Fraction.ZERO) < 0
								| pr.compareTo(Fraction.ONE) > 0) {
							valid = false;
							appendLog("Warning: Failure line " + i
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
								appendLog("Note: Failure are not defined for specific states.\n");
								// failedStateIDs.clear();//true
							} else {
								valid = false;
								appendLog("Warning: Failure line "
										+ i
										+ " omited because of missing state ID. \n");
							}
							break;
						} else {
							sid = Integer.parseInt(cell);
							if (sid < 0 | sid >= g.V.size()) {
								valid = false;
								appendLog("Warning: Failure line "
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
	
			appendLog("Failure definitions: " + failedInputs.toString()
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
				gf.writeCreateHPA2file(opFolderDir + "/file_" + gf.name, comment);
				gf.writeCreateHPA2FAT(opFolderDir + "/FAT_" + gf.name, comment);
				appendLog("Process composition with failure successfully generated and stored into files "
						+ gf.name + ".\n");
				message = gf.checkT();
				if (message != null) {
					appendLog("ERROR: " + gf.name
							+ " violating checkT, at state " + message
							+ "Exit.\n");
					return;
				}
			} catch (Exception e1) {
				e1.printStackTrace();
			}
	
			CreateHPA gp = null;
			try {
				if (!PropertyFile.exists()) {
					appendLog("ERROR: Property file doesn't exist. Exit.\n");
					return;
				}
				in = new edu.princeton.cs.introcs.In(PropertyFile);
				gp = new CreateHPA(in);
				in.close();
				gp.name = "gp" + failedserver;
				gp.prefix = "P" + failedserver;
				gp.renameStates(gp.prefix, "");
				gp.renameStateProp(gp.prefix, "");
				gp.renameSymbolsExceptCom(failedserver, "", com);
				// add ERROR state with ERROR proposition:
				// checkT() is false
				gp.writeCreateHPA2file(opFolderDir + "/file_" + gp.name, "");
				gp.writeCreateHPA2FAT(opFolderDir + "/FAT_" + gp.name,
						"Property automata");
				appendLog("Property file: " + PropertyFile.getName() + "\n");
				appendLog("Property file successfully read and stored into files "
						+ gp.name + ".\n");
	
				gp.DeterminePropertyAutomata(g.symbols, com, failedserver);
				gp.iniTransPropSatisfied();
				gp.writeCreateHPA2file(opFolderDir + "/file_" + gp.name + "_d", "");
				gp.writeCreateHPA2FAT(opFolderDir + "/FAT_" + gp.name + "_d",
						"Property automata determinized");
				appendLog("Property file successfully determinized and stored into files "
						+ gp.name + "_d.\n");
			} catch (Exception e1) {
				appendLog("ERROR in loading property file.\n"
						+ e1.getMessage());
				return;
			}
	
			String temphpaFile = opFolderDir + "/HPA_hpa";
			CreateHPA gfp = null;
			try {
				gfp = CreateHPA.PSCompMergeERROR(gf, gp, failedserver); // 1-HPA
				temphpaFile = opFolderDir + "/HPA_" + gfp.name;
				gfp.writeCreateHPA2file(opFolderDir + "/file_" + gfp.name, "");
				gfp.writeCreateHPA2FAT(opFolderDir + "/FAT_" + gfp.name, "");
				// io.write2HPA(gfp, "data/"+opFolder+"/HPA_" + gfp.name, "");
				appendLog("gfp successfully generated and stored into files "
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
					appendLog("ERROR: not every T-state comes after input-state, such as state "
							+ message + ".\n");
					return;
				}
				gfp.removeTTransitions();
				gfp.writeCreateHPA2file(opFolderDir + "/file_" + gfp.name
						+ "_no_T", "");
				gfp.writeCreateHPA2FAT(opFolderDir + "/FAT_" + gfp.name + "_no_T", "");
				// io.write2HPA(gfp, "data/"+opFolder+"/HPA_" + gfp.name +
				// "_no_T", "");
				appendLog("gfp without T-transitions successfully generated and stored as "
						+ gfp.name + "_no_T.\n");
	
				// deterministic
				gfp.Determine();
				gfp.writeCreateHPA2file(opFolderDir + "/file_hpa_" + gfp.name, "");
				gfp.writeCreateHPA2FAT(opFolderDir + "/FAT_hpa_" + gfp.name, "");
				gfp.writeCreateHPA2HPA(temphpaFile, "");
				appendLog("After determinization, 1-HPA generated and stored as "
						+ temphpaFile + ".\n");
	
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}// actionPerformed
	
	}

	public Object[][] readTableData() {
		DefaultTableModel dtm = (DefaultTableModel) jt.getModel();
		int nRow = dtm.getRowCount(), nCol = dtm.getColumnCount();
		Object[][] tableData = new Object[nRow][nCol];
		for (int i = 0; i < nRow; i++)
			for (int j = 0; j < nCol; j++)
				tableData[i][j] = dtm.getValueAt(i, j);
		return tableData;
	}
	
	public void appendLog(String s){
		this.logPane.appendLog(s);
	}
	
	public void clearLog(){
		this.logPane.clearLog();
	}
}
