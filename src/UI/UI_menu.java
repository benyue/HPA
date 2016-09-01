package UI;

import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.event.KeyEvent;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import java.awt.Font;

/**
 * The UI.UI_menu is the main entry of the tool.<br>
 * It's a tabbed panel constructed of other component panels.
 * 
 * @author Cindy Yue Ben
 * */
@SuppressWarnings("serial")
public class UI_menu extends javax.swing.JPanel {
	JFrame frmHPA;
	Panel_HPAVerification panel_hpaVerification;
	Panel_HPAGeneration panel_hpaGeneration;
	Panel_HPAGeneration_PRISM Panel_HPAGeneration_PRISM;

	UI_menu() {
		frmHPA = new JFrame();
		frmHPA.getContentPane().setFont(new Font("Tahoma", Font.BOLD, 17));
		Dimension wd = new Dimension(600, 700);
		frmHPA.setTitle("HiPAM - Hierarchical PA Model checker");
		frmHPA.setMaximumSize(wd);
		frmHPA.setMinimumSize(wd);
		frmHPA.setResizable(false);
		frmHPA.setSize(wd);
		frmHPA.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmHPA.setLocationRelativeTo(null);

		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);

		panel_hpaVerification = new Panel_HPAVerification();
		tabbedPane.addTab("HPA Analysis and Verification", panel_hpaVerification);
		tabbedPane.setEnabledAt(0, true);
		tabbedPane.setMnemonicAt(0, KeyEvent.VK_0);
		
		//panel_hpaConfig = new Panel_HPAConfig();
		//tabbedPane.addTab("HPA Configuration",panel_hpaConfig);
		
		panel_hpaGeneration = new Panel_HPAGeneration();
		tabbedPane.addTab("HPA Generation",panel_hpaGeneration);
		tabbedPane.setEnabledAt(1, true);
		tabbedPane.setMnemonicAt(1, KeyEvent.VK_1);
		
		Panel_HPAGeneration_PRISM = new Panel_HPAGeneration_PRISM();
		tabbedPane.add("HPA Generation from PRISM",Panel_HPAGeneration_PRISM);
		tabbedPane.setEnabledAt(2, true);
		tabbedPane.setMnemonicAt(2,KeyEvent.VK_2);
	
		frmHPA.getContentPane().add(tabbedPane);
		
		/*TODO pass hpa among panels, add action to menu.
		// Menu Bar
		JMenuBar menuBar = new JMenuBar();
		frmHPA.setJMenuBar(menuBar);
		
		JMenu mnModel = new JMenu("Model");
		menuBar.add(mnModel);
		
		JMenu mnSaveTo = new JMenu("Save to ...");
		mnSaveTo.addActionListener(new SaveModelActionListener());
		mnModel.add(mnSaveTo);
		
		JCheckBoxMenuItem chckbxmntmHPA = new JCheckBoxMenuItem("HPA Format");
		chckbxmntmHPA.setSelected(true);
		mnSaveTo.add(chckbxmntmHPA);
		
		JCheckBoxMenuItem chckbxmntmFAT = new JCheckBoxMenuItem("FAT Format");
		mnSaveTo.add(chckbxmntmFAT);
		
		JCheckBoxMenuItem chckbxmntmPlain = new JCheckBoxMenuItem("Plain File as Record");
		mnSaveTo.add(chckbxmntmPlain);
		*/
		
		/* Pack */
		frmHPA.pack(); // add this statement after adding all components!
		frmHPA.setVisible(true);

	}


	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		UI_menu window = new UI_menu();
		
	}
	
}


