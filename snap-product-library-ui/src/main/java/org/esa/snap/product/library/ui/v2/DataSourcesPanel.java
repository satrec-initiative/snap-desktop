package org.esa.snap.product.library.ui.v2;

import org.esa.snap.product.library.ui.v2.thread.IProgressPanel;
import org.esa.snap.ui.loading.IComponentsEnabled;
import org.esa.snap.ui.loading.ILoadingIndicator;
import org.esa.snap.ui.loading.LabelListCellRenderer;
import org.esa.snap.ui.loading.SwingUtils;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.border.Border;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.net.URL;
import java.util.Stack;

/**
 * Created by jcoravu on 22/8/2019.
 */
public class DataSourcesPanel extends JPanel implements IProgressPanel {

    private JComboBox<AbstractProductsDataSource> dataSourcesComboBox;
    private final JButton searchButton;
    private final JButton helpButton;
    private JLabel dataSourceLabel;
    private JButton stopButton;
    private JProgressBar progressBar;
    private int currentThreadId;
    private IComponentsEnabled componentsEnabled;

    public DataSourcesPanel(IComponentsEnabled componentsEnabled, Insets defaultListItemMargins, int textFieldPreferredHeight, int gapBetweenRows, int gapBetweenColumns,
                            ActionListener searchButtonListener, ItemListener dataSourceListener, ActionListener stopButtonListener,
                            IMissionParameterListener missionParameterListener) {

        super(new GridBagLayout());

        this.componentsEnabled = componentsEnabled;
        this.currentThreadId = 0;

        createDataSourcesComboBox(textFieldPreferredHeight, defaultListItemMargins, gapBetweenRows, gapBetweenColumns, dataSourceListener, missionParameterListener);

        Dimension buttonSize = new Dimension(textFieldPreferredHeight, textFieldPreferredHeight);

        this.searchButton = buildButton("/org/esa/snap/productlibrary/icons/search24.png", searchButtonListener, buttonSize);

        ActionListener helpButtonListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            }
        };
        this.helpButton = buildButton("/org/esa/snap/resources/images/icons/Help24.gif", helpButtonListener, buttonSize);

        this.stopButton = buildButton("/org/esa/snap/productlibrary/icons/stop20.gif", stopButtonListener, buttonSize);

        this.dataSourceLabel = new JLabel("Data source");

        this.progressBar = new JProgressBar(JProgressBar.HORIZONTAL);
        this.progressBar.setIndeterminate(true);
        this.progressBar.setPreferredSize(new Dimension(100, 10));
        this.progressBar.setMinimumSize(new Dimension(100, 10));

        refreshDataSourceLabelWidth();
        setProgressPanelVisible(false);

        GridBagConstraints c = SwingUtils.buildConstraints(0, 0, GridBagConstraints.NONE, GridBagConstraints.WEST, 1, 1, 0, 0);
        add(this.dataSourceLabel, c);
        c = SwingUtils.buildConstraints(1, 0, GridBagConstraints.BOTH, GridBagConstraints.WEST, 1, 1, 0, gapBetweenColumns);
        add(this.dataSourcesComboBox, c);
        c = SwingUtils.buildConstraints(2, 0, GridBagConstraints.VERTICAL, GridBagConstraints.WEST, 1, 1, 0, gapBetweenColumns);
        add(this.searchButton, c);
        c = SwingUtils.buildConstraints(3, 0, GridBagConstraints.VERTICAL, GridBagConstraints.WEST, 1, 1, 0, gapBetweenColumns);
        add(this.helpButton, c);
        c = SwingUtils.buildConstraints(4, 0, GridBagConstraints.VERTICAL, GridBagConstraints.WEST, 1, 1, 0, gapBetweenColumns);
        add(progressBar, c);
        c = SwingUtils.buildConstraints(5, 0, GridBagConstraints.VERTICAL, GridBagConstraints.WEST, 1, 1, 0, gapBetweenColumns);
        add(this.stopButton, c);
    }

    @Override
    public boolean isCurrentThread(int threadId) {
        if (EventQueue.isDispatchThread()) {
            return (this.currentThreadId == threadId);
        } else {
            throw new IllegalStateException("The method must be invoked from the AWT dispatch thread.");
        }
    }

    @Override
    public boolean hideProgressPanel(int threadId) {
        if (EventQueue.isDispatchThread()) {
            if (this.currentThreadId == threadId) {
                this.currentThreadId++;
                setProgressPanelVisible(false);
                setEnabledControls(true);
                return true;
            }
            return false;
        } else {
            throw new IllegalStateException("The method must be invoked from the AWT dispatch thread.");
        }
    }

    @Override
    public boolean showProgressPanel(int threadId) {
        if (EventQueue.isDispatchThread()) {
            if (this.currentThreadId == threadId) {
                setProgressPanelVisible(true);
                setEnabledControls(false);
                return true;
            }
            return false;
        } else {
            throw new IllegalStateException("The method must be invoked from the AWT dispatch thread.");
        }
    }

    public final int incrementAndGetCurrentThreadId() {
        if (EventQueue.isDispatchThread()) {
            return ++this.currentThreadId;
        } else {
            throw new IllegalStateException("The method must be invoked from the AWT dispatch thread.");
        }
    }

    public void hideProgressPanel() {
        if (EventQueue.isDispatchThread()) {
            this.currentThreadId++;
            setProgressPanelVisible(false);
            setEnabledControls(true);
        } else {
            throw new IllegalStateException("The method must be invoked from the AWT dispatch thread.");
        }
    }

    public AbstractProductsDataSource getSelectedDataSource() {
        return (AbstractProductsDataSource)this.dataSourcesComboBox.getSelectedItem();
    }

    public void refreshDataSourceMissionParameters() {
        getSelectedDataSource().refreshMissionParameters();
        refreshDataSourceLabelWidth();
    }

    public void setDataSourcesBorder(Border border) {
        int count = this.dataSourcesComboBox.getModel().getSize();
        for (int i=0; i<count; i++) {
            AbstractProductsDataSource productsDataSource = this.dataSourcesComboBox.getModel().getElementAt(i);
            productsDataSource.setBorder(border);
        }
    }

    public void setParametersEnabledWhileDownloading(boolean enabled) {
        this.searchButton.setEnabled(enabled);
        AbstractProductsDataSource selectedDataSource = getSelectedDataSource();
        Stack<JComponent> stack = new Stack<JComponent>();
        stack.push(selectedDataSource);
        while (!stack.isEmpty()) {
            JComponent component = stack.pop();
            component.setEnabled(enabled);
            int childrenCount = component.getComponentCount();
            for (int i=0; i<childrenCount; i++) {
                Component child = component.getComponent(i);
                if (child instanceof JComponent) {
                    JComponent childComponent = (JComponent) child;
                    // add the component in the stack to be enabled/disabled
                    stack.push(childComponent);
                }
            }
        }
    }

    private void setEnabledControls(boolean enabled) {
        if (this.componentsEnabled != null) {
            this.componentsEnabled.setComponentsEnabled(enabled);
        }
    }

    private void setProgressPanelVisible(boolean visible) {
        this.progressBar.setVisible(visible);
        this.stopButton.setVisible(visible);
    }

    private void refreshDataSourceLabelWidth() {
        int maximumLabelWidth = getSelectedDataSource().computeLeftPanelMaximumLabelWidth();
        SciHubProductsDataSource.setLabelSize(this.dataSourceLabel, maximumLabelWidth);
        Container parentContainer = this.dataSourceLabel.getParent();
        if (parentContainer != null) {
            parentContainer.revalidate();
            parentContainer.repaint();
        }
    }

    private void createDataSourcesComboBox(int textFieldPreferredHeight, Insets defaultListItemMargins, int gapBetweenRows, int gapBetweenColumns,
                                           ItemListener dataSourceListener, IMissionParameterListener missionParameterListener) {

        AbstractProductsDataSource[] availableDataSources = new AbstractProductsDataSource[2];
        availableDataSources[0] = new SciHubProductsDataSource(textFieldPreferredHeight, defaultListItemMargins, gapBetweenRows, gapBetweenColumns, missionParameterListener);
        availableDataSources[1] = new LocalProductsDataSource();

        this.dataSourcesComboBox = new JComboBox<AbstractProductsDataSource>(availableDataSources);
        Dimension comboBoxSize = this.dataSourcesComboBox.getPreferredSize();
        comboBoxSize.height = textFieldPreferredHeight;
        this.dataSourcesComboBox.setPreferredSize(comboBoxSize);
        LabelListCellRenderer<AbstractProductsDataSource> renderer = new LabelListCellRenderer<AbstractProductsDataSource>(defaultListItemMargins) {
            @Override
            protected String getItemDisplayText(AbstractProductsDataSource value) {
                return (value == null) ? "" : value.getName();
            }
        };
        this.dataSourcesComboBox.setRenderer(renderer);
        this.dataSourcesComboBox.setMaximumRowCount(5);
        this.dataSourcesComboBox.setBackground(new Color(0, 0, 0, 0)); // set the transparent color
        this.dataSourcesComboBox.setOpaque(true);
        this.dataSourcesComboBox.setSelectedIndex(0);
        this.dataSourcesComboBox.addItemListener(dataSourceListener);
    }

    private static JButton buildButton(String resourceImagePath, ActionListener buttonListener, Dimension buttonSize) {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URL imageURL = classLoader.getResource(resourceImagePath);
        ImageIcon icon = new ImageIcon(imageURL);
        JButton button = new JButton(icon);
        button.setFocusable(false);
        button.addActionListener(buttonListener);
        button.setPreferredSize(buttonSize);
        button.setMinimumSize(buttonSize);
        button.setMaximumSize(buttonSize);
        return button;
    }
}
