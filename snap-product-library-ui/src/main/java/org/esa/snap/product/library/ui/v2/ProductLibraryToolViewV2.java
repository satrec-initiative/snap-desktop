/*
 * Copyright (C) 2016 by Array Systems Computing Inc. http://www.array.ca
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see http://www.gnu.org/licenses/
 */
package org.esa.snap.product.library.ui.v2;

import com.bc.ceres.core.ServiceRegistry;
import com.bc.ceres.core.ServiceRegistryManager;
import org.esa.snap.core.dataio.ProductIO;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.product.library.ui.v2.repository.AbstractProductsRepositoryPanel;
import org.esa.snap.product.library.ui.v2.repository.local.AllLocalProductsRepositoryPanel;
import org.esa.snap.product.library.ui.v2.repository.remote.DownloadProductsTimerRunnable;
import org.esa.snap.product.library.ui.v2.repository.remote.DownloadRemoteProductsQueue;
import org.esa.snap.product.library.ui.v2.repository.remote.RemoteProductDownloader;
import org.esa.snap.product.library.ui.v2.repository.remote.RemoteProductsRepositoryPanel;
import org.esa.snap.product.library.ui.v2.repository.RepositorySelectionPanel;
import org.esa.snap.product.library.ui.v2.thread.AbstractProgressTimerRunnable;
import org.esa.snap.product.library.ui.v2.thread.ProgressBarHelperImpl;
import org.esa.snap.product.library.ui.v2.worldwind.PolygonMouseListener;
import org.esa.snap.product.library.ui.v2.worldwind.WorldWindowPanelWrapper;
import org.esa.snap.product.library.v2.database.SaveProductData;
import org.esa.snap.rcp.SnapApp;
import org.esa.snap.rcp.windows.ToolTopComponent;
import org.esa.snap.remote.products.repository.ProductRepositoryDownloader;
import org.esa.snap.remote.products.repository.RemoteProductsRepositoryProvider;
import org.esa.snap.remote.products.repository.RepositoryProduct;
import org.esa.snap.ui.AppContext;
import org.esa.snap.ui.loading.CustomFileChooser;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

import javax.swing.JFileChooser;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.geom.Path2D;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

@TopComponent.Description(
        preferredID = "ProductLibraryTopComponentV2",
        iconBase = "org/esa/snap/productlibrary/icons/search.png",
        persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(
        mode = "rightSlidingSide",
        openAtStartup = true,
        position = 0
)
@ActionID(category = "Window", id = "org.esa.snap.product.library.ui.v2.ProductLibraryToolViewV2")
@ActionReferences({
        @ActionReference(path = "Menu/View/Tool Windows"),
        @ActionReference(path = "Menu/File", position = 17)
})
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_ProductLibraryTopComponentV2Name",
        preferredID = "ProductLibraryTopComponentV2"
)
@NbBundle.Messages({
        "CTL_ProductLibraryTopComponentV2Name=Product Library v2",
        "CTL_ProductLibraryTopComponentV2Description=Product Library v2",
})
public class ProductLibraryToolViewV2 extends ToolTopComponent implements ComponentDimension {

    private Path lastSelectedFolderPath;
    private RepositoryProductListPanel repositoryProductListPanel;
    private RepositorySelectionPanel repositorySelectionPanel;
    private CustomSplitPane horizontalSplitPane;

    private AbstractProgressTimerRunnable<?> searchProductListThread;
    private DownloadProductsTimerRunnable downloadProductsThread;
    private int textFieldPreferredHeight;
    private WorldWindowPanelWrapper worldWindowPanel;
    private DownloadRemoteProductsQueue downloadRemoteProductsQueue;

    public ProductLibraryToolViewV2() {
        super();

        setDisplayName(Bundle.CTL_ProductLibraryTopComponentV2Name());
    }

    @Override
    protected void componentOpened() {
        if (this.downloadRemoteProductsQueue == null) {
            initialize();
        }
    }

    @Override
    public int getGapBetweenRows() {
        return 5;
    }

    @Override
    public int getGapBetweenColumns() {
        return 5;
    }

    @Override
    public int getTextFieldPreferredHeight() {
        return this.textFieldPreferredHeight;
    }

    private void initialize() {
        Insets defaultTextFieldMargins = new Insets(3, 2, 3, 2);
        JTextField productNameTextField = new JTextField();
        productNameTextField.setMargin(defaultTextFieldMargins);
        this.textFieldPreferredHeight = productNameTextField.getPreferredSize().height;

        createWorldWindowPanel();
        createRepositorySelectionPanel();
        createProductListPanel();

        int gapBetweenRows = getGapBetweenRows();
        int gapBetweenColumns = getGapBetweenRows();
        Color transparentDividerColor = new Color(255, 255, 255, 0);
        this.horizontalSplitPane = new CustomSplitPane(JSplitPane.HORIZONTAL_SPLIT, gapBetweenColumns-2, 0, transparentDividerColor);
        this.horizontalSplitPane.setLeftComponent(this.repositorySelectionPanel.getSelectedRepository());
        this.horizontalSplitPane.setRightComponent(this.repositoryProductListPanel);


        setLayout(new BorderLayout(0, gapBetweenRows));
        setBorder(new EmptyBorder(gapBetweenRows, gapBetweenColumns, gapBetweenRows, gapBetweenColumns));
        add(this.repositorySelectionPanel, BorderLayout.NORTH);
        add(this.horizontalSplitPane, BorderLayout.CENTER);

        this.repositorySelectionPanel.refreshRepositoryParameterComponents();

        this.downloadRemoteProductsQueue = new DownloadRemoteProductsQueue();
    }

    private void createProductListPanel() {
        ActionListener stopDownloadingProductsButtonListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                stopDownloadingProducts();
            }
        };
        this.repositoryProductListPanel = new RepositoryProductListPanel(this.repositorySelectionPanel, this, stopDownloadingProductsButtonListener);
        this.repositoryProductListPanel.setBorder(new EmptyBorder(0, 1, 0, 0));
        this.repositoryProductListPanel.setListDataListener(new ListDataListener() {
            @Override
            public void intervalAdded(ListDataEvent listDataEvent) {
                productListChanged();
            }

            @Override
            public void intervalRemoved(ListDataEvent listDataEvent) {
                productListChanged();
            }

            @Override
            public void contentsChanged(ListDataEvent listDataEvent) {
                productListChanged();
            }
        });
        this.repositoryProductListPanel.setProductListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent listSelectionEvent) {
                if (!listSelectionEvent.getValueIsAdjusting()) {
                    newSelectedRepositoryProducts();
                }
            }
        });
        addListeners();
    }

    private void createRepositorySelectionPanel() {
        ItemListener repositoriesItemListener = new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent itemEvent) {
                if (itemEvent.getStateChange() == ItemEvent.SELECTED) {
                    refreshRepositoryParameterComponents();
                }
            }
        };
        MissionParameterListener missionParameterListener = new MissionParameterListener() {
            @Override
            public void newSelectedMission(String mission, AbstractProductsRepositoryPanel parentDataSource) {
                if (parentDataSource == repositorySelectionPanel.getSelectedRepository()) {
                    refreshRepositoryMissionParameters();
                } else {
                    throw new IllegalStateException("The selected mission '"+mission+"' does not belong to the visible data source.");
                }
            }
        };
        ActionListener searchButtonListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                searchButtonPressed();
            }
        };
        ActionListener stopDownloadingProductListButtonListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                stopSearchingProductList();
            }
        };

        Set<RemoteProductsRepositoryProvider> repositoryProductsProviders = getRemoteProductsRepositoryProviders();
        RemoteProductsRepositoryProvider[] remoteRepositoryProductProviders = new RemoteProductsRepositoryProvider[repositoryProductsProviders.size()];
        Iterator<RemoteProductsRepositoryProvider> it = repositoryProductsProviders.iterator();
        int index = 0;
        while (it.hasNext()) {
            RemoteProductsRepositoryProvider productsProvider = it.next();
            remoteRepositoryProductProviders[index++] = productsProvider;
        }

        if (remoteRepositoryProductProviders.length > 1) {
            // sort alphabetically by repository name
            Comparator<RemoteProductsRepositoryProvider> comparator = new Comparator<RemoteProductsRepositoryProvider>() {
                @Override
                public int compare(RemoteProductsRepositoryProvider o1, RemoteProductsRepositoryProvider o2) {
                    return o1.getRepositoryName().compareToIgnoreCase(o2.getRepositoryName());
                }
            };
            for (int i=0; i<remoteRepositoryProductProviders.length-1; i++) {
                for (int j=i+1; j<remoteRepositoryProductProviders.length; j++) {
                    int result = comparator.compare(remoteRepositoryProductProviders[i], remoteRepositoryProductProviders[j]);
                    if (result > 0) {
                        RemoteProductsRepositoryProvider aux = remoteRepositoryProductProviders[i];
                        remoteRepositoryProductProviders[i] = remoteRepositoryProductProviders[j];
                        remoteRepositoryProductProviders[j] = aux;
                    }
                }
            }
        }
        this.repositorySelectionPanel = new RepositorySelectionPanel(remoteRepositoryProductProviders, this, missionParameterListener, this.worldWindowPanel);
        this.repositorySelectionPanel.setRepositoriesItemListener(repositoriesItemListener);
        this.repositorySelectionPanel.setSearchButtonListener(searchButtonListener);
        this.repositorySelectionPanel.setStopButtonListener(stopDownloadingProductListButtonListener);
        this.repositorySelectionPanel.setDataSourcesBorder(new EmptyBorder(0, 0, 0, 1));
    }

    private void createWorldWindowPanel() {
        PolygonMouseListener worldWindowMouseListener = new PolygonMouseListener() {
            @Override
            public void leftMouseButtonClicked(List<Path2D.Double> polygonPaths) {
                ProductLibraryToolViewV2.this.leftMouseButtonClicked(polygonPaths);
            }
        };
        this.worldWindowPanel = new WorldWindowPanelWrapper();
        this.worldWindowPanel.setPreferredSize(new Dimension(400, 250));
        this.worldWindowPanel.addWorldWindowPanelAsync(false, true, worldWindowMouseListener);
    }

    private void addListeners() {
        ActionListener openLocalProductListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                openSelectedProducts();
            }
        };
        ActionListener deleteLocalProductListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
            }
        };
        this.repositorySelectionPanel.setOpenAndDeleteLocalProductListeners(openLocalProductListener, deleteLocalProductListener);

        ActionListener downloadRemoteProductListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                downloadSelectedProductAsync();
            }
        };
        this.repositorySelectionPanel.setDownloadRemoteProductListener(downloadRemoteProductListener);
    }

    private void openSelectedProducts() {
        RepositoryProduct[] selectedProducts = this.repositoryProductListPanel.getSelectedProducts();
        String productPath = selectedProducts[0].getDownloadURL();
        try {
            Product product = ProductIO.readProduct(productPath);
            if (product != null) {
                AppContext appContext = SnapApp.getDefault().getAppContext();
                appContext.getProductManager().addProduct(product);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void leftMouseButtonClicked(List<Path2D.Double> polygonPaths) {
        this.repositoryProductListPanel.selectProductsByPolygonPath(polygonPaths);
    }

    private void productListChanged() {
        Path2D.Double[] polygonPaths = this.repositoryProductListPanel.getPolygonPaths();
        this.worldWindowPanel.setPolygons(polygonPaths);
    }

    private void newSelectedRepositoryProducts() {
        RepositoryProduct[] selectedProducts = this.repositoryProductListPanel.getSelectedProducts();
        Path2D.Double[] polygonPaths = new Path2D.Double[selectedProducts.length];
        for (int i = 0; i < selectedProducts.length; i++) {
            polygonPaths[i] = selectedProducts[i].getPolygon().getPath();
        }
        this.worldWindowPanel.highlightPolygons(polygonPaths);
        if (polygonPaths.length == 1) {
            this.worldWindowPanel.setEyePosition(polygonPaths[0]);
        }
    }

    private void stopSearchingProductList() {
        this.repositorySelectionPanel.getProgressBarHelper().hideProgressPanel();
        if (this.searchProductListThread != null) {
            this.searchProductListThread.stopRunning(); // stop the thread
        }
    }

    private void refreshRepositoryParameterComponents() {
        stopSearchingProductList();
        int dividerLocation = this.horizontalSplitPane.getDividerLocation();
        this.horizontalSplitPane.setLeftComponent(this.repositorySelectionPanel.getSelectedRepository());
        this.horizontalSplitPane.setDividerLocation(dividerLocation);
        this.horizontalSplitPane.revalidate();
        this.horizontalSplitPane.repaint();
        this.repositorySelectionPanel.refreshRepositoryParameterComponents();
        this.repositoryProductListPanel.clearProducts();
    }

    private void refreshRepositoryMissionParameters() {
        stopSearchingProductList();
        this.repositorySelectionPanel.refreshRepositoryParameterComponents();
        this.repositoryProductListPanel.clearProducts();
    }

    private void stopDownloadingProducts() {
        synchronized (this.downloadRemoteProductsQueue) {
            this.downloadRemoteProductsQueue.clear();
        }
        this.repositoryProductListPanel.getListModel().removePendingDownloadProducts();
        this.repositoryProductListPanel.getProgressBarHelper().hideProgressPanel();
        if (this.downloadProductsThread != null) {
            this.downloadProductsThread.stopRunning(); // stop the thread
        }
    }

    private void downloadSelectedProductAsync() {
        CustomFileChooser fileChooser = buildFileChooser("Select folder to download the product", false, JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setAcceptAllFileFilterUsed(false);
        if (this.lastSelectedFolderPath != null) {
            fileChooser.setCurrentDirectoryPath(this.lastSelectedFolderPath);
        }
        int result = fileChooser.showDialog(this, "Select");
        if (result == JFileChooser.APPROVE_OPTION) {
            this.lastSelectedFolderPath = fileChooser.getSelectedPath();
            AbstractProductsRepositoryPanel selectedRepository = this.repositorySelectionPanel.getSelectedRepository();
            if (selectedRepository instanceof RemoteProductsRepositoryPanel) {
                RemoteProductsRepositoryProvider productsRepositoryProvider = ((RemoteProductsRepositoryPanel) selectedRepository).getProductsRepositoryProvider();
                RepositoryProduct[] selectedProducts = this.repositoryProductListPanel.getSelectedProducts();

                this.repositoryProductListPanel.getListModel().addPendingDownloadProducts(selectedProducts);

                int queueSizeBeforeAddingProducts;
                synchronized (this.downloadRemoteProductsQueue) {
                    queueSizeBeforeAddingProducts = this.downloadRemoteProductsQueue.getSize();
                    for (int i=0; i<selectedProducts.length; i++) {
                        ProductRepositoryDownloader productRepositoryDownloader = productsRepositoryProvider.buidProductDownloader(selectedProducts[i].getMission());
                        RemoteProductDownloader remoteProductDownloader = new RemoteProductDownloader(selectedProducts[i], productRepositoryDownloader, this.lastSelectedFolderPath);
                        this.downloadRemoteProductsQueue.push(remoteProductDownloader);
                    }
                }

                boolean startProductsDownloadThread = false;
                if (queueSizeBeforeAddingProducts == 0 || this.downloadProductsThread == null) {
                    startProductsDownloadThread = true;
                }
                if (startProductsDownloadThread) {
                    ProgressBarHelperImpl progressBarHelper = this.repositoryProductListPanel.getProgressBarHelper();
                    int threadId = progressBarHelper.incrementAndGetCurrentThreadId();
                    this.downloadProductsThread = new DownloadProductsTimerRunnable(progressBarHelper, threadId, this.downloadRemoteProductsQueue, this.repositoryProductListPanel, this) {
                        @Override
                        protected void onStopExecuting() {
                            ProductLibraryToolViewV2.this.downloadProductsThread = null; // reset
                        }

                        @Override
                        protected void onFinishSavingProduct(SaveProductData saveProductData) {
                            finishSavingProduct(saveProductData);
                        }
                    };
                    this.downloadProductsThread.executeAsync();
                } else {
                    this.downloadProductsThread.updateDownloadedProgressPercentLater();
                }
            } else {
                throw new IllegalStateException("The selected repository is not a remote repository.");
            }
        }
    }

    private void finishSavingProduct(SaveProductData saveProductData) {
        AllLocalProductsRepositoryPanel allLocalProductsRepositoryPanel = this.repositorySelectionPanel.getAllLocalProductsRepositoryPanel();
        allLocalProductsRepositoryPanel.addMissionIfMissing(saveProductData.getRemoteMission());
    }

    private void searchButtonPressed() {
        ThreadListener threadListener = new ThreadListener() {
            @Override
            public void onStopExecuting() {
                ProductLibraryToolViewV2.this.searchProductListThread = null; // reset
            }
        };
        ProgressBarHelperImpl progressBarHelper = this.repositorySelectionPanel.getProgressBarHelper();
        int threadId = progressBarHelper.incrementAndGetCurrentThreadId();
        AbstractProductsRepositoryPanel selectedRepository = this.repositorySelectionPanel.getSelectedRepository();
        AbstractProgressTimerRunnable<?> thread = selectedRepository.buildThreadToSearchProducts(progressBarHelper, threadId, threadListener, this.repositoryProductListPanel);
        if (thread != null) {
            this.repositoryProductListPanel.clearProducts();
            this.searchProductListThread = thread;
            this.searchProductListThread.executeAsync(); // start the thread
        }
    }

    private static CustomFileChooser buildFileChooser(String dialogTitle, boolean multiSelectionEnabled, int fileSelectionMode) {
        boolean previousReadOnlyFlag = UIManager.getDefaults().getBoolean(CustomFileChooser.FILE_CHOOSER_READ_ONLY_KEY);
        CustomFileChooser fileChooser = new CustomFileChooser(previousReadOnlyFlag);
        fileChooser.setDialogTitle(dialogTitle);
        fileChooser.setMultiSelectionEnabled(multiSelectionEnabled);
        fileChooser.setFileSelectionMode(fileSelectionMode);
        return fileChooser;
    }

    public static Set<RemoteProductsRepositoryProvider> getRemoteProductsRepositoryProviders() {
        ServiceRegistryManager serviceRegistryManager = ServiceRegistryManager.getInstance();
        ServiceRegistry<RemoteProductsRepositoryProvider> serviceRegistry = serviceRegistryManager.getServiceRegistry(RemoteProductsRepositoryProvider.class);
        return serviceRegistry.getServices();
    }
}
