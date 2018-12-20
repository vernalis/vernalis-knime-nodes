package com.vernalis.nodes.smartsviewer;

import org.knime.core.node.NodeView;

/**
 * <code>NodeView</code> for the "SmartsViewer" Node.
 * Retrieves a SMARTSViewer visualisation of a columns of SMARTS strings using the service at www.smartsviewer.de
 *
 * @author SDR
 */
public class SmartsViewerNodeView extends NodeView<SmartsViewerNodeModel> {

    /**
     * Creates a new view.
     * 
     * @param nodeModel The model (class: {@link SmartsViewerNodeModel})
     */
    protected SmartsViewerNodeView(final SmartsViewerNodeModel nodeModel) {
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

