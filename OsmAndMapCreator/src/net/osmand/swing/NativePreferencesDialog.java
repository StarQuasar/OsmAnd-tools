package net.osmand.swing;

import java.awt.Component;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

/*
 * Currently supported rendering params
 * 
 * Base parameters:
 * density
 * lang
 * textSize
 * nightMode
 * languagePreference
 * 
 * 
 * New Core params
 * debugStageEnabled					
 * excludeOnPathSymbols					
 * excludeBillboardSymbols					
 * excludeOnSurfaceSymbols					
 * skipSymbolsIntersection					
 * showSymbolsBBoxesAccByIntersection					
 * showSymbolsBBoxesRejByIntersection					
 * skipSymbolsMinDistance					
 * showSymbolsBBoxesRejectedByMinDist					
 * showSymbolsCheckBBoxesRejectedByMinDist					
 * skipSymbolsPresentationModeCheck					
 * showSymbolsBBoxesRejectedByPresentationMode					
 * showOnPathSymbolsRenderablesPaths					
 * showOnPath2dSymbolGlyphDetails					
 * showOnPath3dSymbolGlyphDetails					
 * allSymbolsTransparentForIntersectionLookup					
 * showTooShortOnPathSymbolsRenderablesPaths					
 * showAllPaths
 */
public class NativePreferencesDialog extends JDialog {

	private static final long serialVersionUID = -4862884032977071296L;

	private JButton okButton;
	private JButton cancelButton;

	private JTextField nativeFilesDirectory;
	private JTextField renderingStyleFile;
	private JTextField renderingGenStyleFile;
	private Map<String, JCheckBox> checks = new TreeMap<String, JCheckBox>();
	private Map<String, JComboBox<String>> combos = new TreeMap<String, JComboBox<String>>();
	private boolean okPressed;

	private JTextField renderingPropertiesTxt;

	public NativePreferencesDialog(Component parent) {
		super(JOptionPane.getFrameForComponent(parent), true);
		setTitle(Messages.getString("OsmExtractionPreferencesDialog.PREFERENCES")); //$NON-NLS-1$
		initDialog();

	}

	public void showDialog() {
		setSize(800, 700);
		double x = getParent().getBounds().getCenterX();
		double y = getParent().getBounds().getCenterY();
		setLocation((int) x - getWidth() / 2, (int) y - getHeight() / 2);
		setVisible(true);
	}

	private void initDialog() {
		JPanel pane = new JPanel();
		pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));
		pane.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
		add(pane);

		createGeneralSection(pane);
		pane.add(Box.createVerticalGlue());
		FlowLayout l = new FlowLayout(FlowLayout.RIGHT);
		JPanel buttonsPane = new JPanel(l);
		okButton = new JButton(Messages.getString("OsmExtractionPreferencesDialog.OK")); //$NON-NLS-1$
		buttonsPane.add(okButton);
		cancelButton = new JButton(Messages.getString("OsmExtractionPreferencesDialog.CANCEL")); //$NON-NLS-1$
		buttonsPane.add(cancelButton);

		buttonsPane
				.setMaximumSize(new Dimension(Short.MAX_VALUE, (int) l.preferredLayoutSize(buttonsPane).getHeight()));
		pane.add(buttonsPane);

		addListeners();
	}

	private void createGeneralSection(JPanel root) {
		JPanel panel = new JPanel();
		// panel.setLayout(new GridLayout(3, 1, 5, 5));
		GridBagLayout l = new GridBagLayout();
		panel.setLayout(l);
		panel.setBorder(BorderFactory.createTitledBorder(Messages.getString("OsmExtractionPreferencesDialog.GENERAL"))); //$NON-NLS-1$
		root.add(panel);

		JLabel label = new JLabel("Directory with binary files : ");
		panel.add(label);
		GridBagConstraints constr = new GridBagConstraints();
		constr.ipadx = 5;
		constr.gridx = 0;
		constr.gridy = 1;
		constr.anchor = GridBagConstraints.WEST;
		l.setConstraints(label, constr);

		nativeFilesDirectory = new JTextField();

		nativeFilesDirectory.setText(DataExtractionSettings.getSettings().getBinaryFilesDir());
		panel.add(nativeFilesDirectory);
		constr = new GridBagConstraints();
		constr.weightx = 1;
		constr.fill = GridBagConstraints.HORIZONTAL;
		constr.ipadx = 5;
		constr.gridx = 1;
		constr.gridy = 1;
		l.setConstraints(nativeFilesDirectory, constr);

		renderingStyleFile = addTextField(panel, l, "Rendering style file : ", 3, DataExtractionSettings.getSettings()
				.getRenderXmlPath());
		renderingGenStyleFile = addTextField(panel, l, "Rendering gen file: ", 4, DataExtractionSettings.getSettings()
				.getRenderGenXmlPath());
		String prps = DataExtractionSettings.getSettings().getRenderingProperties();
		renderingPropertiesTxt = addTextField(panel, l, "Rendering properties : ", 5, prps);
		String cutPrps = "";
		String[] vls = prps.split(",");
		int k = 6;
		for (String v : vls) {
			String[] spl = v.split("=");
			if (spl.length < 2) {
				continue;
			}
			String name = spl[0].trim();
			String vl = spl[1].trim();
			if (vl.toLowerCase().equals("true") || vl.toLowerCase().equals("false")) {
				boolean value = Boolean.parseBoolean(vl);
				JCheckBox box = addCheckBox(panel, l, name, k++, value);
				checks.put(name, box);
			} else {
				String[] vs = vl.split(";");
				JComboBox<String> cb = addComboBox(panel, l, name, k++, vs[0], vs);
				combos.put(name, cb);
			}
		}

		renderingPropertiesTxt.setText(cutPrps);

		panel.setMaximumSize(new Dimension(Short.MAX_VALUE, panel.getPreferredSize().height));
	}

	protected JCheckBox addCheckBox(JPanel panel, GridBagLayout l, String labelText, int rowId, boolean value) {
		GridBagConstraints constr;
		JCheckBox check = new JCheckBox();
		check.setText(labelText);
		check.setSelected(value);
		panel.add(check);
		constr = new GridBagConstraints();
		constr.weightx = 0;
		constr.fill = GridBagConstraints.HORIZONTAL;
		constr.ipadx = 5;
		constr.gridx = 0;
		constr.anchor = GridBagConstraints.WEST;
		constr.gridy = rowId;
		l.setConstraints(check, constr);
		return check;
	}

	protected JComboBox<String> addComboBox(JPanel panel, GridBagLayout l, String labelText, int rowId, String value,
			String[] otherValues) {
		JLabel label;
		GridBagConstraints constr;
		label = new JLabel(labelText);
		panel.add(label);
		constr = new GridBagConstraints();
		constr.ipadx = 5;
		constr.gridx = 0;
		constr.gridy = rowId;
		constr.anchor = GridBagConstraints.WEST;
		l.setConstraints(label, constr);

		JComboBox<String> textField = new JComboBox<String>(otherValues);
		textField.setEditable(true);
		textField.setSelectedItem(value);
		panel.add(textField);
		constr = new GridBagConstraints();
		constr.weightx = 1;
		constr.fill = GridBagConstraints.HORIZONTAL;
		constr.ipadx = 5;
		constr.gridx = 1;
		constr.gridy = rowId;
		l.setConstraints(textField, constr);
		return textField;
	}

	protected JTextField addTextField(JPanel panel, GridBagLayout l, String labelText, int rowId, String value) {
		JLabel label;
		GridBagConstraints constr;
		label = new JLabel(labelText);
		panel.add(label);
		constr = new GridBagConstraints();
		constr.ipadx = 5;
		constr.gridx = 0;
		constr.gridy = rowId;
		constr.anchor = GridBagConstraints.WEST;
		l.setConstraints(label, constr);

		JTextField textField = new JTextField();
		textField.setText(value);
		panel.add(textField);
		constr = new GridBagConstraints();
		constr.weightx = 1;
		constr.fill = GridBagConstraints.HORIZONTAL;
		constr.ipadx = 5;
		constr.gridx = 1;
		constr.gridy = rowId;
		l.setConstraints(textField, constr);
		return textField;
	}

	private void addListeners() {
		okButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				saveProperties();
				okPressed = true;

				setVisible(false);
			}

		});
		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
		});

	}

	public void saveProperties() {
		DataExtractionSettings settings = DataExtractionSettings.getSettings();

		if (!settings.getBinaryFilesDir().equals(nativeFilesDirectory.getText())) {
			settings.setBinaryFilesDir(nativeFilesDirectory.getText());
		}

		if (!settings.getRenderXmlPath().equals(renderingStyleFile.getText())) {
			settings.setRenderXmlPath(renderingStyleFile.getText());
		}

		if (!settings.getRenderGenXmlPath().equals(renderingGenStyleFile.getText())) {
			settings.setRenderGenXmlPath(renderingGenStyleFile.getText());
		}
		String res = renderingPropertiesTxt.getText();
		int i = res.length();
		for (String s : checks.keySet()) {
			if (i > 0) {
				res += ",";
			}
			i++;
			JCheckBox cb = checks.get(s);
			if (!res.contains(s + "=")) {
				res += s + "=" + cb.isSelected();
			}
		}
		for (String s : combos.keySet()) {
			if (i > 0) {
				res += ",";
			}
			i++;
			JComboBox<String> cb = combos.get(s);
			String item = cb.getSelectedItem().toString();
			res += s + "=" + item;
			for (int ij = 0; ij < cb.getItemCount(); ij++) {
				String kk = cb.getItemAt(ij);
				if (!item.equals(kk)) {
					res += ";" + kk;
				}
			}
		}
		DataExtractionSettings.getSettings().setRenderingProperties(res);
	}

	public boolean isOkPressed() {
		return okPressed;
	}

}
