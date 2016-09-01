package UI;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import HPA.HPAMC;
import HPA.PA;
import SCC.ComponentGraph;
import SCC.TarjanSCC;

/**
 * @author Cindy Yue Ben
 * */
@SuppressWarnings("serial")
public class Panel_HPAGeneration_PRISM extends JSplitPane {
	public File traFile = null, labFile = null, staFile = null;
	private JTextField textTraFile, textLabFile, textStaFile;
	private static File dir = null; // store last location for OpenDialog
	public PA hpa = null;
	private Panel_log logPane;

	Panel_HPAGeneration_PRISM() {
		this.setLayout(null);

		JPanel p = new JPanel();
		p.setLayout(null);

		JLabel lblPleaseLoadPrism = new JLabel(
				"Load PRISM output to generate HPA:");
		lblPleaseLoadPrism.setFont(new Font("Tahoma", Font.PLAIN, 16));
		lblPleaseLoadPrism
				.setToolTipText("<html>Please first load all relevant files, then click \"Generate HPA!\".");
		lblPleaseLoadPrism.setBounds(10, 16, 419, 20);
		p.add(lblPleaseLoadPrism);

		JButton btnOpenTFile = new JButton("[Required] .tra(rows) File:");
		btnOpenTFile.setFont(new Font("Tahoma", Font.PLAIN, 16));
		btnOpenTFile
				.setToolTipText("<html>Please load .tra(rows) File from PRISM, which contains transition matrix.");
		btnOpenTFile.setBounds(10, 50, 260, 25);
		btnOpenTFile.setHorizontalAlignment(SwingConstants.LEFT);
		btnOpenTFile.setActionCommand("t");
		btnOpenTFile.addActionListener(new OpenFileButtonListener());
		p.add(btnOpenTFile);

		textTraFile = new JTextField();
		textTraFile.setBounds(10, 80, 420, 24);
		textTraFile.setColumns(25);
		p.add(textTraFile);

		JButton btnOpenLFile = new JButton("[Recommended] .lab File:");
		btnOpenLFile.setFont(new Font("Tahoma", Font.PLAIN, 16));
		btnOpenLFile
				.setToolTipText("<html>Please load .lab File from PRISM, which contains states' propositions.<br>\r\nSpecial states, such as INITIAL states and FINAL states, are identified using propositions.");
		btnOpenLFile.setBounds(10, 120, 260, 25);
		btnOpenLFile.setHorizontalAlignment(SwingConstants.LEFT);
		btnOpenLFile.setActionCommand("l");
		btnOpenLFile.addActionListener(new OpenFileButtonListener());
		p.add(btnOpenLFile);

		textLabFile = new JTextField();
		textLabFile.setColumns(25);
		textLabFile.setBounds(10, 150, 420, 24);
		p.add(textLabFile);

		JButton btnOpenSFile = new JButton("[Optional] .sta File:");
		btnOpenSFile.setFont(new Font("Tahoma", Font.PLAIN, 16));
		btnOpenSFile
				.setToolTipText("<html>Please load .sta File from PRISM, which contains states' names.");
		btnOpenSFile.setBounds(10, 190, 260, 25);
		btnOpenSFile.setHorizontalAlignment(SwingConstants.LEFT);
		btnOpenSFile.setActionCommand("s");
		btnOpenSFile.addActionListener(new OpenFileButtonListener());
		p.add(btnOpenSFile);

		textStaFile = new JTextField();
		textStaFile.setBounds(10, 220, 420, 24);
		textStaFile.setColumns(25);
		p.add(textStaFile);

		JButton btnResetFiles = new JButton("Reset input files.");
		btnResetFiles.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				resetFiles();
			}
		});
		btnResetFiles.setToolTipText("Reset all input files above.");
		btnResetFiles.setFont(new Font("Tahoma", Font.ITALIC, 16));
		btnResetFiles.setBounds(265, 251, 164, 24);
		p.add(btnResetFiles);

		JButton btnGenerateHpa = new JButton("Generate HPA!");
		btnGenerateHpa.setFont(new Font("Tahoma", Font.PLAIN, 16));
		btnGenerateHpa
				.setToolTipText("<html>Output PA to a plain/.txt/.hpa file. <br>"
						+ "Ready for importation in the Verification part of the tool.");
		btnGenerateHpa.setBounds(10, 280, 420, 28);
		btnGenerateHpa.addActionListener(new CreateHPABtnListener());
		p.add(btnGenerateHpa);

		JButton btnSave2FAT = new JButton("Output PA to FAT");
		btnSave2FAT.setFont(new Font("Tahoma", Font.PLAIN, 15));
		btnSave2FAT
				.setToolTipText("<html>Output PA to a .fat file. <br>"
						+ "Note this output is for debugging and graphical presentation purpose only, <br>"
						+ "and is NOT for importation in the Verification part of the tool.");
		btnSave2FAT.setBounds(10, 320, 190, 24);
		btnSave2FAT.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser fc = new JFileChooser();
				if (dir != null) {
					fc.setCurrentDirectory(dir);
				} else {
					fc.setCurrentDirectory(new java.io.File("."));
				}
				fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
				fc.setDialogTitle("Output HPA to FAT File");
				fc.setAcceptAllFileFilterUsed(true);
				if (fc.showSaveDialog(null) == JFileChooser.APPROVE_OPTION
						&& hpa != null) {
					// dir = fc.getCurrentDirectory();
					File fileToSave = fc.getSelectedFile();
					try {
						hpa.writePA2FAT(fileToSave.getAbsolutePath(), "");
						appendLog("PA successfully output to file "
								+ fileToSave.getAbsolutePath() + "_fat\n");
					} catch (IOException e1) {
						appendLog("ERROR: PA output to file exception "
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
						+ "and is NOT for importation in the Verification part of the tool.");
		btnSave2file.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser fc = new JFileChooser();
				if (dir != null) {
					fc.setCurrentDirectory(dir);
				} else {
					fc.setCurrentDirectory(new java.io.File("."));
				}
				fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
				fc.setDialogTitle("Output HPA to Plain File");
				fc.setAcceptAllFileFilterUsed(true);
				if (fc.showSaveDialog(null) == JFileChooser.APPROVE_OPTION
						&& hpa != null) {
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
		btnSave2file.setBounds(215, 320, 214, 24);
		p.add(btnSave2file);

		logPane = new Panel_log();
		this.setBottomComponent(logPane);
		this.setOrientation(JSplitPane.VERTICAL_SPLIT);
		this.setTopComponent(p);
		this.setDividerLocation(400);
		this.setEnabled(false);
	}

	private class OpenFileButtonListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			String command = e.getActionCommand();
			JFileChooser fc = new JFileChooser(); // Create a file chooser
			fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
			fc.setAcceptAllFileFilterUsed(false);
			if (dir != null) {
				fc.setCurrentDirectory(dir);
			} else {
				fc.setCurrentDirectory(new java.io.File("."));
			}

			switch (command) {
			case "t":
				fc.setDialogTitle("Load .tra(rows) file");
				FileFilter filter = new FileNameExtensionFilter(
						"PRISM-generated .tra(rows) files", "tra");
				fc.setFileFilter(filter);
				break;
			case "l":
				fc.setDialogTitle("Open .lab file");
				filter = new FileNameExtensionFilter(
						"PRISM-generated .lab files", "lab");
				fc.setFileFilter(filter);
				break;
			case "s":
				fc.setDialogTitle("Open .sta file");
				filter = new FileNameExtensionFilter(
						"PRISM-generated .sta files", "sta");
				fc.setFileFilter(filter);
				break;
			}

			int returnVal = fc.showOpenDialog(fc);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = fc.getSelectedFile();
				dir = fc.getCurrentDirectory();
				if (command.equals("t")) {
					traFile = file;
					textTraFile.setText(file.toString());
				} else if (command.equals("l")) {
					labFile = file;
					textLabFile.setText(file.toString());
				} else if (command.equals("s")) {
					staFile = file;
					textStaFile.setText(file.toString());
				}
			} // else Open command cancelled by user
		}

	}

	private class CreateHPABtnListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (traFile == null) {
				JOptionPane.showMessageDialog(new JFrame(),
						"ERROR: .tra file doesn't exist.");
				return;
			}

			/* Generate HPA */
			appendLog("\n------------Generate HPA from PRISM output------------"
					// + "\nInput file(s): \n"
					// +".tra(rows) file: "+traFile+";\n"
					// +".lab file: "+labFile+";\n"
					// +".sta file: "+staFile+";\n"
					+ "\n");

			edu.princeton.cs.introcs.In in = new edu.princeton.cs.introcs.In(
					traFile);
			try {
				hpa = null;
				try {
					hpa = new PA(in,
							org.apache.commons.io.FilenameUtils
									.getExtension(traFile.toString()));
				} catch (Exception e1) {
					appendLog("ERROR in loading HPA:\n" + e1.getMessage());
					// e1.printStackTrace();
					return;
				}
				in.close();
			} catch (Exception e1) {
				e1.printStackTrace();
			}

			// update HPA using .lab
			if (labFile != null) {
				in = new edu.princeton.cs.introcs.In(labFile);
				if (hpa == null) {
					appendLog("ERROR: load valid hpa before loading .lab files. "
							+ "No HPA to be updated.\n");
					return;
				}
				try {
					if (!hpa.updatePAusingPRISMlabel(in)) {
						appendLog("hpa not updated using .lab.\n");
					} else {
						appendLog("hpa succefully updated using .lab.\n");
					}
					in.close();
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}

			// update HPA using .sta
			if (staFile != null) {
				in = new edu.princeton.cs.introcs.In(staFile);
				if (hpa == null) {
					appendLog("ERROR: load valid hpa before loading .sta files. "
							+ "No HPA to be updated.\n");
					return;
				}
				try {
					if (!hpa.updatePAusingPRISMStates(in)) {
						appendLog("hpa not updated using .sta.\n");
					} else {
						appendLog("hpa succefully updated using .sta.\n");
					}
					in.close();
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}

			/* UI */
			JFileChooser fc = new JFileChooser();
			if (dir != null) {
				fc.setCurrentDirectory(dir);
			} else {
				fc.setCurrentDirectory(new java.io.File("."));
			}
			fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
			fc.setDialogTitle("Save generated HPA:");
			fc.setAcceptAllFileFilterUsed(false);
			FileFilter filter = new FileNameExtensionFilter(".hpa files", "hpa");
			fc.setFileFilter(filter);
			if (fc.showSaveDialog(null) == JFileChooser.APPROVE_OPTION
					&& hpa != null) {
				// dir = fc.getCurrentDirectory();
				File fileToSave = fc.getSelectedFile();// .hpa
				try {
					hpa.writePA2HPA(fileToSave.getAbsolutePath(), "");
					appendLog("PA successfully output to file "
							+ fileToSave.getAbsolutePath() + ".hpa.\n");
				} catch (IOException e1) {
					appendLog("ERROR: PA output to file exception "
							+ e1.getMessage() + "\n");
					e1.printStackTrace();
				}
			}

		}
	}

	/**
	 * Reset all input files above.
	 * */
	public void resetFiles() {
		traFile = null;
		labFile = null;
		staFile = null;
		this.textTraFile.setText("");
		this.textLabFile.setText("");
		this.textStaFile.setText("");
		// appendLog("Note: All PRISM input files are reset to null.\n");
	}

	public void appendLog(String s) {
		this.logPane.appendLog(s);
	}

	public void clearLog() {
		this.logPane.clearLog();
	}
}
