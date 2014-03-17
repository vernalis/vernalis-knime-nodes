package com.vernalis.nodes.smartsviewer;

import org.knime.core.node.NodeView;

/**
 * <code>NodeView</code> for the "SmartsViewer" Node.
 * Retrieves a SMARTSViewer visualisation of a columns of SMARTS strings using the service at www.smartsviewer.de
 *
 * @author SDR
 */
public class SmartsViewer2NodeView extends NodeView<SmartsViewer2NodeModel> {

    /**
     * Creates a new view.
     * 
     * @param nodeModel The model (class: {@link SmartsViewer2NodeModel})
     */
    protected SmartsViewer2NodeView(final SmartsViewer2NodeModel nodeModel) {
        super(nodeModel);
        // TODO: generated method stub
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void modelChanged() {
        // TODO: generated method stub
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onClose() {
        // TODO: generated method stub
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onOpen() {
        // TODO: generated method stub
    }

}

