package com.plus.mevanspn.udgdesigner;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

final public class UDGEditingPane extends JPanel implements MouseListener, MouseMotionListener {
    private UDGEditor udgEditor;
    private float zoom = 16.0f;
    private float hratio = 1.0f;
    private byte colourIndex = 1;

    public UDGEditingPane(UDGEditor udgEditor) {
        super();
        this.udgEditor = udgEditor;
        this.addMouseListener(this);
        this.addMouseMotionListener(this);
    }

    public void refresh() {
        this.revalidate();
        this.repaint();
        this.getParent().revalidate();
    }

    public void setZoom(float zoom) {
        if (zoom == 0 || zoom < 0.125 || zoom > 32) return;
        if (zoom < this.zoom) {
            while (this.zoom > zoom) this.zoom = this.zoom / 2.0f;
        } else if (zoom > this.zoom) {
            while (this.zoom < zoom) this.zoom = this.zoom * 2.0f;
        }
        refresh();
    }

    public float getZoom() { return zoom; }

    public void setHRatio(float hratio) {
        if (hratio < 0.5 || hratio > 2) return;
        this.hratio = Math.round(hratio);
    }

    public float getHRatio() { return hratio; }

    public void setColourIndex(int colourIndex) {
        if (colourIndex < 0 || colourIndex > 1) return;
        this.colourIndex = (byte) colourIndex;
    }

    private Dimension getZoomedImageSize() {
        final UDGImage image = udgEditor.getImage();
        final int imageWidth = image != null ? (int) (image.getWidth() * zoom * hratio) : 0;
        final int imageHeight = image != null ? (int) (image.getHeight() * zoom) : 0;
        return (imageWidth > 0 && imageHeight > 0) ? new Dimension(imageWidth, imageHeight) : null;
    }

    private void paintPixel(int x, int y) {
        final UDGImage image = udgEditor.getImage();
        if (image != null) {
            final Dimension zoomedImageSize = getZoomedImageSize();
            if (zoomedImageSize != null) {
                final int xoffset = (getWidth() - zoomedImageSize.width) / 2;
                final int yoffset = (getHeight() - zoomedImageSize.height) / 2;
                final float hzoom = zoom * hratio;
                final int pixelX = (int) ((x - xoffset) / hzoom);
                final int pixelY = (int) ((y - yoffset) / zoom);
                if (pixelX >= 0 && pixelX < image.getWidth() && pixelY >= 0 && pixelY < image.getHeight()) {
                    image.setPixel(pixelX, pixelY, colourIndex);
                    repaint();
                }
            }
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        final UDGImage image = udgEditor.getImage();
        if (image != null) {
            g.setColor(Color.LIGHT_GRAY);
            g.fillRect(0, 0, getWidth(), getHeight());
            Graphics2D g2 = (Graphics2D) g;
            final Dimension zoomedImageSize = getZoomedImageSize();
            if (zoomedImageSize != null) {
                final int xoffset = (getWidth() - zoomedImageSize.width) / 2;
                final int yoffset = (getHeight() - zoomedImageSize.height) / 2;
                final float hzoom = zoom * hratio;
                g2.drawImage(image, xoffset, yoffset, zoomedImageSize.width, zoomedImageSize.height, null);
                if (zoom > 2) {
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.25f));
                    for (float x = xoffset + hzoom; x < xoffset + zoomedImageSize.width; x += hzoom) {
                        g2.setColor((x - xoffset) % (8 * hzoom) == 0 ? Color.WHITE : Color.GRAY);
                        g2.drawLine((int) x, yoffset, (int) x, yoffset + zoomedImageSize.height);
                    }
                    for (float y = yoffset + zoom; y < yoffset + zoomedImageSize.height; y += zoom) {
                        g2.setColor((y - yoffset) % (8 * zoom) == 0 ? Color.WHITE : Color.GRAY);
                        g2.drawLine(xoffset, (int) y, xoffset + zoomedImageSize.width, (int) y);
                    }
                    g2.setComposite(AlphaComposite.SrcOver);
                }
                g2.setColor(Color.BLACK);
                g2.drawRect(xoffset - 1, yoffset - 1, zoomedImageSize.width + 1, zoomedImageSize.height + 1);
            }
        }
    }

    @Override
    public Dimension getPreferredSize() {
        final Dimension zoomedImageSize = getZoomedImageSize();
        return new Dimension(zoomedImageSize != null && zoomedImageSize.width > 320 ? zoomedImageSize.width : 320,
                zoomedImageSize != null && zoomedImageSize.height > 240 ? zoomedImageSize.height : 240);
    }

    @Override
    public Dimension getMinimumSize() {
        return getPreferredSize();
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        paintPixel(e.getX(), e.getY());
        udgEditor.updateOutput();
    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {
        udgEditor.updateOutput();
    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    @Override
    public void mouseDragged(MouseEvent e) {
        paintPixel(e.getX(), e.getY());
    }

    @Override
    public void mouseMoved(MouseEvent e) {

    }
}
