package com.vernalis.pdbconnector2.query.text.dialog;

import java.awt.Color;
import java.awt.Font;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.vernalis.pdbconnector2.dialogcomponents.swing.CountClearButtonBox;
import com.vernalis.pdbconnector2.dialogcomponents.swing.RemoveMeButton;
import com.vernalis.pdbconnector2.query.RemovableQueryPanel;
import com.vernalis.pdbconnector2.query.text.dialog.QueryFieldModel.InvalidQueryFieldModel;

/**
 * A simple {@link RemovableQueryPanel} implementation to display the saved
 * settings of an invalid field, along with a 'RemoveMe' button
 * 
 * @author S.Roughley knime@vernalis.com
 * @since 1.30.3
 */
public class InvalidQueryFieldPanel extends Box implements
		RemovableQueryPanel<InvalidQueryFieldModel, QueryGroupPanel> {

	private static final Color BACKGROUND = new JFrame().getBackground();
	private static final long serialVersionUID = 1L;
	private final QueryGroupPanel parent;
	private final InvalidQueryFieldModel model;

	private final Box queryFieldValuesBox = new Box(BoxLayout.Y_AXIS);
	private final CountClearButtonBox countButton;

	/**
	 * Constructor
	 * 
	 * @param model
	 *            The model for the field
	 * @param parent
	 *            The parent Query Group
	 */
	public InvalidQueryFieldPanel(InvalidQueryFieldModel model,
			QueryGroupPanel parent) {
		super(BoxLayout.X_AXIS);
		this.parent = parent;
		this.model = model;

		// Because we are in a white background potentially, we need to set
		// these:
		setOpaque(true);
		setBackground(BACKGROUND);
		resetBorder();

		// 'X' button to remove (broken!) query
		add(new RemoveMeButton(this));
		add(queryFieldValuesBox);

		// Add the a label to the queryFieldValues box
		final JLabel lbl = new JLabel("INVALID QUERY");
		lbl.setForeground(Color.RED);
		lbl.setFont(lbl.getFont().deriveFont(Font.BOLD | Font.ITALIC));
		queryFieldValuesBox.add(lbl);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		String queryXml;
		try {
			getQueryModel().getSettings().saveToXML(baos);
			queryXml = new String(baos.toByteArray(), StandardCharsets.UTF_8);
		} catch (IOException e) {
			queryXml = String.format("Error writing Node Settings XML:%n%n%s",
					e.getMessage());
		}

		final JTextArea queryTextPane = new JTextArea(queryXml, 5, 70);
		queryTextPane.setEditable(false);
		JScrollPane scroll = new JScrollPane(queryTextPane,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		queryFieldValuesBox.add(scroll);

		countButton = new CountClearButtonBox(this, true);
		add(countButton);
		resetCountClearButtons();
	}

	@Override
	public CountClearButtonBox getButtons() {
		return countButton;
	}

	@Override
	public InvalidQueryFieldModel getQueryModel() {
		return model;
	}

	@Override
	public JComponent getComponent() {
		return this;
	}

	@Override
	public void removeMe() {
		if (!isRoot()) {
			getParentGroup().removeQueryField(getQueryModel());
		}

	}

	@Override
	public QueryGroupPanel getParentGroup() {
		return parent;
	}

}
