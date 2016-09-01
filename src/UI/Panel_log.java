package UI;

import java.awt.Component;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * The UI.Panel_log is a log panel for displaying log.<br>
 * It's used as an element in other panels.
 * 
 * @author Cindy Yue Ben
 */
@SuppressWarnings("serial")
public class Panel_log extends JPanel {
	public JTextArea log;

	Panel_log() {
		setLayout(null);
		setVisible(true);

		log = new JTextArea();
		log.setFont(new Font("Monospaced", Font.PLAIN, 13));
		log.setRows(5);
		log.setLineWrap(true);
		log.setAlignmentY(Component.TOP_ALIGNMENT);
		log.setAlignmentX(Component.LEFT_ALIGNMENT);
		log.setAutoscrolls(true);
		log.setMargin(new Insets(5, 5, 5, 5));
		log.setName("Log");

		JScrollPane logScrollPane = new JScrollPane(log);
		logScrollPane.setBounds(5, 5, 500, 200);
		logScrollPane.setWheelScrollingEnabled(true);
		add(logScrollPane);

		JButton btnClearLog = new JButton("Clear Log");
		btnClearLog.setFont(new Font("Arial", Font.PLAIN, 11));
		btnClearLog.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				clearLog();
			}
		});
		btnClearLog.setBounds(506, 5, 80, 23);
		add(btnClearLog);

		JButton btnSaveLog = new JButton("Save Log");
		btnSaveLog.setFont(new Font("Arial", Font.PLAIN, 11));
		btnSaveLog.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveLog();
			}
		});
		btnSaveLog.setBounds(506, 25, 80, 23);
		add(btnSaveLog);
	}

	public void appendLog(StringBuilder m) {
		log.append(m.toString());
		repaint();
	}
	public void appendLog(StringBuffer sb) {
		log.append(sb.toString());
		repaint();
	}
	public void appendLog(String s) {
		log.append(s);
		repaint();
	}
	
	public void clearLog() {
		log.setText("");
		repaint();
	}

	public void saveLog() {
		JFileChooser fc = new JFileChooser();
		fc.setCurrentDirectory(new java.io.File("."));
		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fc.setDialogTitle("Output Log");
		fc.setAcceptAllFileFilterUsed(true);
		if (fc.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
			String pattern = "yyyy.MM.dd.H.m.s";
			SimpleDateFormat dateFormatter = new SimpleDateFormat(pattern);
			Date d = new Date();
			File f = new File(fc.getSelectedFile().toString() + "_"
			        + dateFormatter.format(d) + "_log");
			appendLog(writeLog(f));
		}
	}

	/**
	 * Write the content of LogPanel to file f.
	 */
	public String writeLog(File f) {
		try {
			BufferedWriter o = new BufferedWriter(new FileWriter(
			        f));
			o.write(log.getText());
			o.close();
			return ("Log successfully output to file "
			        + f.getAbsolutePath() + "\n");
		} catch (IOException e1) {
			return ("ERROR: Log output to file exception "
			        + e1.getMessage() + "\n");
			// e1.printStackTrace();
		}
	}

}
