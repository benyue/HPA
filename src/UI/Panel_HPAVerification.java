package UI;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.math3.fraction.Fraction;

import HPA.HPAMC;
import HPA.PA;
import HPA.WitnessSet_0Q0;
import HPA.WitnessSet_1Q0;
import Util.RobustMessage;
import Util.VerificationMessage;
import Util.io;
import Util.message;
import Util.util;
import edu.princeton.cs.introcs.In;

/**
 * @author Cindy Yue Ben
 */
@SuppressWarnings("serial")
public class Panel_HPAVerification extends JSplitPane {
	private Fraction x = Fraction.ONE_HALF, // 0<=x<=1
	        xprecision = Fraction.getReducedFraction(1, 100); // 0<xprecision<1
	private JTextField textHPAFile, textX, textXPrecision;
	JRadioButton rdbtnFile, rdbtnURL, rdbtnPfa, rdbtnPba, rdbtnQ1,
	        rdbtnQ0;
	Panel_log logPane;
	public long L = 50;

	public PA hpa = null;
	static File dir = null; // store last location for OpenDialog

	/**
	 * Constructor
	 */
	Panel_HPAVerification() {
		setLayout(null);
		JPanel p = new JPanel();
		p.setLayout(null);

		rdbtnFile = new JRadioButton("File");
		rdbtnFile.setFont(new Font("Tahoma", Font.PLAIN, 15));
		rdbtnFile
		        .setToolTipText(
		                "<html>Please push button \"Load HPA\" below to load HPA. "
		                        + "<br>Supported file formats: <br>"
		                        + "Default HPA files: plain text file, .txt, .hpa </html>");
		rdbtnFile.setSelected(true);
		rdbtnFile.setBounds(160, 10, 68, 29);
		p.add(rdbtnFile);

		rdbtnURL = new JRadioButton("Web URL");
		rdbtnURL.setFont(new Font("Tahoma", Font.PLAIN, 15));
		rdbtnURL.setToolTipText(
		        "<html>Please type url in the text field below. <br>"
		                + "Then push button \"Load HPA\".</html>");
		rdbtnURL.setBounds(235, 10, 102, 29);
		p.add(rdbtnURL);

		ButtonGroup btg = new ButtonGroup();
		btg.add(rdbtnFile);
		btg.add(rdbtnURL);

		JLabel lblAcceptanceType = new JLabel("Acceptance Type:");
		lblAcceptanceType.setFont(new Font("Tahoma", Font.PLAIN, 16));
		lblAcceptanceType.setBounds(10, 35, 159, 20);
		p.add(lblAcceptanceType);

		rdbtnPfa = new JRadioButton("Finite");
		rdbtnPfa.setFont(new Font("Tahoma", Font.PLAIN, 15));
		rdbtnPfa.setToolTipText("For PA defined with Finite Acceptance");
		rdbtnPfa.setSelected(true);
		rdbtnPfa.setBounds(160, 35, 75, 29);
		p.add(rdbtnPfa);

		rdbtnPba = new JRadioButton("Buchi (Beta)");
		rdbtnPba.setFont(new Font("Tahoma", Font.PLAIN, 15));
		rdbtnPba.setToolTipText("For PA defined with Buchi Acceptance");
		rdbtnPba.setBounds(235, 35, 129, 29);
		p.add(rdbtnPba);

		ButtonGroup btgAcc = new ButtonGroup();
		btgAcc.add(rdbtnPfa);
		btgAcc.add(rdbtnPba);

		JLabel lblHpaLevelingOptions = new JLabel("HPA Leveling:");
		lblHpaLevelingOptions.setToolTipText(
		        "Choose HPA levevling strategy: most states on higher level(s) "
		                + "or more on lower levels(s).");
		lblHpaLevelingOptions.setFont(new Font("Tahoma", Font.PLAIN, 16));
		lblHpaLevelingOptions.setBounds(10, 60, 135, 20);
		p.add(lblHpaLevelingOptions);

		rdbtnQ1 = new JRadioButton("Higher");
		rdbtnQ1.setToolTipText("");
		rdbtnQ1.setFont(new Font("Tahoma", Font.PLAIN, 15));
		rdbtnQ1.setBounds(160, 60, 75, 29);
		p.add(rdbtnQ1);

		rdbtnQ0 = new JRadioButton("Lower");
		rdbtnQ0.setToolTipText("");
		rdbtnQ0.setSelected(true);
		rdbtnQ0.setFont(new Font("Tahoma", Font.PLAIN, 15));
		rdbtnQ0.setBounds(235, 60, 75, 29);
		p.add(rdbtnQ0);

		ButtonGroup btgLeveling = new ButtonGroup();
		btgLeveling.add(rdbtnQ1);
		btgLeveling.add(rdbtnQ0);

		JButton btnLoadHPA = new JButton("Load HPA:");
		btnLoadHPA.setFont(new Font("Tahoma", Font.PLAIN, 16));
		btnLoadHPA.setHorizontalAlignment(SwingConstants.LEFT);
		btnLoadHPA.setBounds(10, 90, 116, 23);
		btnLoadHPA.addActionListener(new LoadHPAButtonListener());
		p.add(btnLoadHPA);

		textHPAFile = new JTextField();
		textHPAFile.setColumns(25);
		textHPAFile.setBounds(133, 90, 280, 23);
		p.add(textHPAFile);

		JLabel lbl = new JLabel("Decidability:");
		lbl.setFont(new Font("Tahoma", Font.BOLD, 16));
		lbl.setEnabled(false);
		lbl.setBounds(10, 192, 147, 23);
		lbl.setVerticalAlignment(SwingConstants.TOP);
		p.add(lbl);

		JLabel lblThresholdPr = new JLabel("Threshold probability:");
		lblThresholdPr.setFont(new Font("Tahoma", Font.PLAIN, 16));
		lblThresholdPr.setBounds(10, 220, 159, 23);
		p.add(lblThresholdPr);

		textX = new JTextField();
		textX.setFont(new Font("Tahoma", Font.PLAIN, 16));
		textX.setToolTipText(
		        "<html>Please input a valid probability value in format of decimal or fraction.</html>");
		textX.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				updateX();
			}
		});
		textX.setBounds(184, 219, 96, 23);
		textX.setText("0.5");
		textX.setColumns(10);
		p.add(textX);

		JButton btnBKD = new JButton("Run Backward Alg.");
		btnBKD.setFont(new Font("Tahoma", Font.PLAIN, 16));
		btnBKD.addActionListener(new BKDlistener());
		btnBKD.setBounds(10, 251, 191, 23);
		p.add(btnBKD);

		JButton btnFWD = new JButton("Run Forward Alg.");
		btnFWD.setFont(new Font("Tahoma", Font.PLAIN, 16));
		btnFWD.addActionListener(new FWDlistener());
		btnFWD.setBounds(222, 251, 191, 23);
		p.add(btnFWD);

		JLabel lbl1 = new JLabel("Robustness:");
		lbl1.setFont(new Font("Tahoma", Font.BOLD, 16));
		lbl1.setEnabled(false);
		lbl1.setBounds(10, 290, 147, 23);
		lbl1.setVerticalAlignment(SwingConstants.TOP);
		p.add(lbl1);

		JLabel lblPrecision = new JLabel("Precision:");
		lblPrecision.setFont(new Font("Tahoma", Font.PLAIN, 16));
		lblPrecision.setToolTipText("");
		lblPrecision.setBounds(10, 313, 159, 23);
		p.add(lblPrecision);

		textXPrecision = new JTextField(); // for xprecision
		textXPrecision.setFont(new Font("Tahoma", Font.PLAIN, 16));
		textXPrecision
		        .setToolTipText(
		                "<html>The precision value should be a small number between 0 and 1, "
		                        + "<br>in format of decimal or fraction.</html>");
		textXPrecision.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				updateXPrecision();
			}
		});
		textXPrecision.setBounds(184, 312, 96, 23);
		textXPrecision.setText("0.01");
		textXPrecision.setColumns(10);
		p.add(textXPrecision);

		JButton btnRobust = new JButton("Decide Robustness.");
		btnRobust.setFont(new Font("Tahoma", Font.PLAIN, 16));
		btnRobust.setBounds(10, 344, 191, 23);
		btnRobust.addActionListener(new RobustnessBtnListener());
		p.add(btnRobust);

		JLabel lblLoadHpa = new JLabel("HPA source format:");
		lblLoadHpa.setFont(new Font("Tahoma", Font.PLAIN, 16));
		lblLoadHpa
		        .setToolTipText(
		                "<html>Select from one of the two applicable source types "
		                        + "where HPA will be loaded from: <br>"
		                        + "either local File, or Internet URL.");
		lblLoadHpa.setBounds(10, 10, 159, 20);
		p.add(lblLoadHpa);

		JButton btnSave2FAT = new JButton("Output PA to FAT");
		btnSave2FAT.setFont(new Font("Tahoma", Font.PLAIN, 15));
		btnSave2FAT
		        .setToolTipText("<html>Output PA to a .fat file. <br>"
		                + "Note this output is for debugging and graphical presentation only, <br>"
		                + "and is NOT for use in the Verification panel.");
		btnSave2FAT.setBounds(42, 117, 178, 24);
		btnSave2FAT.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (hpa == null) {
					JOptionPane.showMessageDialog(new JFrame(),
		                    "ERROR: please load HPA first.");
					return;
				}
				JFileChooser fc = new JFileChooser();
				if (dir != null) {
					fc.setCurrentDirectory(dir);
				} else {
					fc.setCurrentDirectory(new java.io.File("."));
				}
				fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
				fc.setDialogTitle("Output HPA to FAT File");
				fc.setAcceptAllFileFilterUsed(true);
				if (fc.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
					// dir = fc.getCurrentDirectory();
					File fileToSave = fc.getSelectedFile();
					try {
						hpa.writePA2FAT(fileToSave.getAbsolutePath(), "");
						appendLog("PA successfully output to file "
		                        + fileToSave.getAbsolutePath() + "_fat\n");
					} catch (IOException e1) {
						appendLog("ERROR: PA output exception "
		                        + e1.getMessage() + "\n");
						e1.printStackTrace();
					}
				}
			}
		});
		p.add(btnSave2FAT);

		JButton btnSave2file = new JButton("Output PA to plain file");
		btnSave2file.setFont(new Font("Tahoma", Font.PLAIN, 15));
		btnSave2file
		        .setToolTipText("<html>Output PA to a plain file. <br>"
		                + "Note this output is for debugging purpose only, <br>"
		                + "and is NOT for use in the Verification panel.");
		btnSave2file.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (hpa == null) {
					JOptionPane.showMessageDialog(new JFrame(),
		                    "ERROR: please load HPA first.");
					return;
				}
				JFileChooser fc = new JFileChooser();
				if (dir != null) {
					fc.setCurrentDirectory(dir);
				} else {
					fc.setCurrentDirectory(new java.io.File("."));
				}
				fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
				fc.setDialogTitle("Output HPA to Plain File");
				fc.setAcceptAllFileFilterUsed(true);
				if (fc.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
					// dir = fc.getCurrentDirectory();
					File fileToSave = fc.getSelectedFile();
					try {
						hpa.writeHPA2file(fileToSave.getAbsolutePath(), "");
						appendLog("PA successfully output to file "
		                        + fileToSave.getAbsolutePath() + "_plain\n");
					} catch (IOException e1) {
						appendLog("ERROR: PA output to file exception "
		                        + e1.getMessage() + "\n");
						e1.printStackTrace();
					}
				}
			}
		});
		btnSave2file.setBounds(235, 117, 178, 24);
		p.add(btnSave2file);

		JButton btnGenerateGWS = new JButton("Generate good witness sets.");
		btnGenerateGWS.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (hpa == null) {
					JOptionPane.showMessageDialog(new JFrame(),
		                    "ERROR: please load HPA first.");
					return;
				}
				hpa.resetWS();
				try {
					generateWS();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		btnGenerateGWS.setToolTipText(
		        "<html>Click this button to generate good witness sets (WSs) "
		                + "before proceeding to verification, <br> "
		                + "only if WSs were neither generated nor loaded while loading HPA.");
		btnGenerateGWS.setFont(new Font("Tahoma", Font.PLAIN, 15));
		btnGenerateGWS.setBounds(10, 152, 230, 24);
		p.add(btnGenerateGWS);

		logPane = new Panel_log();
		setOrientation(JSplitPane.VERTICAL_SPLIT);
		setTopComponent(p);

		setBottomComponent(logPane);
		setDividerLocation(400);
		setEnabled(false);
	}

	private class LoadHPAButtonListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (rdbtnURL.isSelected()) {
				String u = textHPAFile.getText();
				// e.g.http://www.cs.uic.edu/~yben/tools/HPA/dist/examples/2Verification/HPA_OnlineShopping1
				URL fileURL;
				try {
					fileURL = new URL(u);
				} catch (MalformedURLException e1) {
					appendLog("Invalid URL: " + u + "\n");
					// e1.printStackTrace();
					return;
				}
				edu.princeton.cs.introcs.In in =
				        new edu.princeton.cs.introcs.In(
				                fileURL);
				appendLog("HPA URL: " + fileURL + "\n");
				try {
					hpa = new HPAMC().loadHPA(in,
					        org.apache.commons.io.FilenameUtils.getExtension(u),
					        rdbtnPfa.isSelected(), rdbtnQ1.isSelected());
				} catch (Exception e1) {
					hpa = null;
					e1.printStackTrace();
				}
			} else { // rdbtnFile.isSelected()
				JFileChooser fc = new JFileChooser(); // Create a file chooser
				if (dir != null) {
					fc.setCurrentDirectory(dir);
				} else {
					fc.setCurrentDirectory(new java.io.File("."));
				}
				fc.setDialogTitle("Open HPA file");
				fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
				FileFilter filter = new FileNameExtensionFilter(
				        "Default HPA Files", "hpa", "txt");
				fc.addChoosableFileFilter(filter);

				int returnVal = fc.showOpenDialog(fc);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File f = fc.getSelectedFile();
					dir = fc.getCurrentDirectory();
					textHPAFile.setText(f.toString());
					appendLog("HPA file: " + f + "\n");
					if (!f.exists()) {
						appendLog("ERROR: non-existing HPA file.\n");
						return;
					}
					edu.princeton.cs.introcs.In in =
					        new edu.princeton.cs.introcs.In(
					                f);
					try {
						hpa = new HPAMC().loadHPA(in,
						        org.apache.commons.io.FilenameUtils
						                .getExtension(f.toString()),
						        rdbtnPfa.isSelected(), rdbtnQ1.isSelected());
					} catch (Exception e1) {
						appendLog(e1.getMessage());
						hpa = null;
						return;
						// e1.printStackTrace();
					}
				} // else Open command cancelled by user
			}

			if (hpa == null) {
				appendLog("ERROR in loading HPA.\n");
				return;
			}
			// valid 1HPA, appendLog on basic info
			appendLog(hpa.basicInfo());

			// valid 1HPA, generate or load good witness sets
			int sel = JOptionPane
			        .showConfirmDialog(new JFrame(),
			                "Given PA is a valid 1-HPA. Good witness sets (WS)"
			                        + " are needed for verification. \n"
			                        + "Click YES to generate WS,\n"
			                        + "Click NO or CANCEL to load WS file for this PA.");
			if (sel == 0) {// YES=0, generate WSs and save in a file
				try {
					generateWS();
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			} else { // load a ws file
				loadWS();
			}

		}

	}

	private class BKDlistener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (hpa == null) {
				JOptionPane.showMessageDialog(new JFrame(),
				        "ERROR: please load HPA first.");
				return;
			}
			updateX();
			try {
				VerificationMessage vm =
				        new HPAMC().bkdCheckX_wTrace(hpa, x, L);
				appendLog(vm);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
	}

	private class FWDlistener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (hpa == null) {
				JOptionPane.showMessageDialog(new JFrame(),
				        "ERROR: please load HPA first.");
				return;
			}
			updateX();
			try {
				VerificationMessage vm =
				        new HPAMC().fwdCheckX(hpa, x, L, false);
				appendLog(vm);
			} catch (Exception e1) {
				appendLog(e1.getMessage());
				e1.printStackTrace();
				return;
			}
		}
	}

	private class RobustnessBtnListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (hpa == null) {
				JOptionPane.showMessageDialog(new JFrame(),
				        "ERROR: please load HPA first.");
				return;
			}
			L = hpa.L();
			updateXPrecision();
			HPAMC HPA = new HPAMC();
			RobustMessage rm = HPA.robust(hpa, L, xprecision);
			if (rm.ErrorMessage != null) {
				JOptionPane.showMessageDialog(new JFrame(), rm.ErrorMessage);
			} else {
				appendLog(rm.toStringBuffer());
			}
		}
	}

	private void updateX() {
		x = util.parseFractionPr(textX.getText().toString());
		if (x == null) {
			textX.setText("0.5");
			x = Fraction.ONE_HALF;
		} else {
			textX.setText(x.toString());
		}
	}

	private void updateXPrecision() {
		xprecision = util.parseFractionPr(textXPrecision.getText().toString());
		if (xprecision == null || xprecision.compareTo(Fraction.ZERO) <= 0
		        || xprecision.compareTo(Fraction.ONE) >= 0) {
			textXPrecision.setText("0.01");
			xprecision = Fraction.getReducedFraction(1, 100);
		} else {
			textXPrecision.setText(xprecision.toString());
		}
	}

	private void appendLog(message m) {
		logPane.appendLog(m.toStringBuffer());
	}
	private void appendLog(StringBuffer s) {
		logPane.appendLog(s);
	}
	private void appendLog(String s) {
		logPane.appendLog(s);
	}

	/**
	 * generate good WSs for a valid 1HPA and save WSs in a formatted .ws file
	 * 
	 * @throws Exception
	 */
	private void generateWS() throws Exception {
		JFileChooser fc = new JFileChooser();
		if (dir != null) {
			fc.setCurrentDirectory(dir);
		} else {
			fc.setCurrentDirectory(new java.io.File("."));
		}
		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fc.setDialogTitle("Output "
		        + (rdbtnPfa.isSelected() ? "finite" : "Buchi")
		        + " WS to .ws file");
		fc.setAcceptAllFileFilterUsed(true);
		if (fc.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
			File fileToSave = fc.getSelectedFile();
			long t0 = util.getCPUTime();
			hpa.witnessSets();
			long t1 = util.getCPUTime();
			StringBuilder m = new StringBuilder();
			m.append("//Leveling option: More ");
			m.append(rdbtnQ1.isSelected() ? "Q1" : "Q0");
			m.append("\n");
			m.append("//CPU time to generate WSs: " + (t1 - t0) + " ns.\n");
			// appendLog(m);
			try {
				String f = fileToSave.getAbsolutePath();
				f += rdbtnQ1.isSelected() ? "1" : "0";
				hpa.writeWS2file(f, m);
				appendLog("WSs (WS0 size " + hpa.WS0s.size() + ", WS1 size "
				        + hpa.WS1s.size() + ") in "
				        + fileToSave.getAbsolutePath()
				        + ".ws\n");
			} catch (IOException e1) {
				appendLog("ERROR saving WSs to file:\n"
				        + e1.getMessage() + "\n");
				// e1.printStackTrace();
			}
		}
	}

	/**
	 * UI for loading WSs from a .ws file.
	 */
	private void loadWS() {
		JFileChooser fc = new JFileChooser(); // Create a file chooser
		if (dir != null) {
			fc.setCurrentDirectory(dir);
		} else {
			fc.setCurrentDirectory(new java.io.File("."));
		}
		fc.setDialogTitle("Load .ws file for current hpa");
		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fc.setAcceptAllFileFilterUsed(false);
		FileFilter filter = new FileNameExtensionFilter(
		        "HPAMC-generated Witness Set files", "ws");
		fc.setFileFilter(filter);
		int returnVal = fc.showOpenDialog(fc);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File f = fc.getSelectedFile();
			dir = fc.getCurrentDirectory();
			if (!f.exists()) {
				appendLog("ERROR: invalid ws file.\n");
				hpa = null;
				return;
			}
			edu.princeton.cs.introcs.In in = new edu.princeton.cs.introcs.In(f);
			try {
				loadHPAWSs(in);
				appendLog("Good witness sets loaded from " + f + ".\n");
			} catch (Exception e1) {
				appendLog("ERROR: invalid ws file. HPA=null.\n"
				        + e1.toString());
				hpa = null;
				return;
			}
		} else {// user clicked cancel, ws neither generated nor calculated
			appendLog("Warning: good witness sets "
			        + "neither generated nor loaded. "
			        + "Verification result will be inaccurate.");
		}
	}

	/**
	 * Given a valid 1HPA g, reset and load good witness sets for g.
	 * 
	 * @throws Exception
	 */
	private void loadHPAWSs(In in) throws Exception {
		String s = io.readNextNonemptyLine(in);
		if (s == null) {// empty file
			throw new Exception("ERROR: empty ws file.");
		}
		String[] line = s.split(",");
		if (line.length != 2) {
			throw new Exception("ERROR in ws file: invalid ws file at line "
			        + s + ".");
		}
		int nWS0 = Integer.parseInt(line[0]);
		int nWS1 = Integer.parseInt(line[1]);
		hpa.resetWS();

		for (int i = 0; i < nWS0; ++i) {
			s = io.readNextNonemptyLine(in);
			if (s == null) {
				throw new Exception(
				        "ERROR in ws file: fewer valid WS0 than announced.");
			}
			// WS0: [nodes][superQ0][goodQ0]in_symbol:in_WS0
			hpa.WS0s.add(new WitnessSet_0Q0(i));

			s = s.replace("[", "");
			String[] ss = s.split("]");// size 4
			if (ss.length != 4) {
				throw new Exception("ERROR in ws file: invalid WS0"
				        + " at line " + s);
			}
			// nodes in WS0, can for once be empty
			HashSet<Integer> ws = new HashSet<Integer>();
			String[] ss0 = ss[0].split(", ");
			for (String node : ss0) {
				if (node.isEmpty())
					continue;
				int n = Integer.parseInt(node);
				if (n < 0 || n >= hpa.V.size()) {
					// TODO: warn if hpa was reduced before loading ws, state
					// number will be different
					throw new Exception(
					        "ERROR in ws file: invalid WS0 with node " + n
					                + " at line " + s);
				}
				ws.add(n);
			}
			hpa.WS0nodes.add(ws);
			hpa.indexInWS0nodes.put(ws, i);
			// superQ0, can often be empty
			if (!ss[1].isEmpty()) {
				String[] ss1 = ss[1].split(", ");
				for (String node : ss1) {
					if (node.isEmpty())
						continue;
					int n = Integer.parseInt(node);
					if (n < 0 || n >= hpa.V.size()) {
						throw new Exception(
						        "ERROR in ws file: invalid WS0 with superQ0 "
						                + n + " at line " + s);
					}
					hpa.WS0s.get(i).superQ0.add(n);
				}
			}
			// goodQ0, can often be empty
			if (!ss[2].isEmpty()) {
				String[] ss2 = ss[2].split(", ");
				for (String node : ss2) {
					if (node.isEmpty())
						continue;
					int n = Integer.parseInt(node);
					if (n < 0 || n >= hpa.V.size()) {
						throw new Exception(
						        "ERROR in ws file: invalid WS0 with goodQ0 "
						                + n + " at line " + s);
					}
					hpa.WS0s.get(i).superQ0.add(n);
				}
			}
			// in_symbol:in_WS0
			if (ss[3].isEmpty())
				continue;
			String[] ss2 = ss[3].split(",");
			for (String pre : ss2) {
				if (pre.isEmpty())
					continue;
				String[] p = pre.split(":");
				if (p.length != 2)
					continue;
				int n = Integer.parseInt(p[1]);
				if (n < 0 || n >= nWS0) {
					throw new Exception(
					        "ERROR in ws file: invalid WS0 with inSWS0 " + n
					                + " at line " + s);
				}
				hpa.WS0s.get(i).inWS0.put(p[0], n);
			}
		}

		for (int i = 0; i < nWS1; ++i) {
			s = io.readNextNonemptyLine(in);
			if (s == null) {
				throw new Exception(
				        "ERROR in ws file: fewer valid WS1 than announced.");
			}
			// WS1: q0,WS0id,isSuperGoodWS(1 for true and 0 for false)
			String[] ss = s.split(",");// size 3
			if (ss.length != 3) {
				throw new Exception("ERROR in ws file: invalid WS1"
				        + " at line " + s);
			}
			WitnessSet_1Q0 ws1 = new WitnessSet_1Q0(i);
			hpa.WS1s.add(ws1);
			int n = Integer.parseInt(ss[0]);
			if (n < 0 || n >= hpa.V.size()) {
				throw new Exception(
				        "ERROR in ws file: invalid WS1 with q0 " + n
				                + " at line " + s);
			}
			ws1.q0 = n;
			n = Integer.parseInt(ss[1]);
			if (n < 0 || n >= nWS0) {
				throw new Exception(
				        "ERROR in ws file: invalid WS1 with WS0 id " + n
				                + " at line " + s);
			}
			ws1.WS0id = n;
			n = Integer.parseInt(ss[2]);
			if (n < 0 || n > 1) {
				throw new Exception(
				        "ERROR in ws file: invalid WS1 with isSuperGoodWS value "
				                + n
				                + " at line " + s);
			}
			if (n == 1)
				ws1.isSuperGoodWS1 = true;
		}

	}
}
