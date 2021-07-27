package com.plus.mevanspn.udgdesigner;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.*;

final public class UDGEditor extends JFrame implements WindowListener {
    private final UDGEditingPane editingPane;
    private final JLabel outputLabel;
    private UDGImage image;

    public UDGEditor() {
        super("UDG Editor - By Morgan Evans");

        JToolBar appToolBar = new JToolBar();
        this.editingPane = new UDGEditingPane(this);
        JScrollPane editingPaneScrollPane = new JScrollPane(this.editingPane);
        this.outputLabel = new JLabel(" ");

        this.setLayout(new BorderLayout());
        this.add(appToolBar, BorderLayout.NORTH);
        this.add(editingPaneScrollPane, BorderLayout.CENTER);
        this.add(outputLabel, BorderLayout.SOUTH);

        JMenuBar appMenuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenuItem newImageMenuItem = new JMenuItem("New Image...");
        newImageMenuItem.addActionListener(e -> {
           new NewImageDialog(this, null).setVisible(true);
        });
        fileMenu.add(newImageMenuItem);
        JMenuItem openImageMenuItem = new JMenuItem("Open Image...");
        openImageMenuItem.addActionListener(e -> {
            JFileChooser openFileChooser = new JFileChooser();
            openFileChooser.addChoosableFileFilter(new FileNameExtensionFilter("UDG Image",".udg"));
            int result = openFileChooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                try {
                    DataInputStream dis = new DataInputStream(new FileInputStream(openFileChooser.getSelectedFile()));
                    final int imageWidth = dis.readInt();
                    final int imageHeight = dis.readInt();
                    final float pixelRatio = dis.readFloat();
                    byte[] newRasterData = new byte[imageWidth * imageHeight];
                    dis.read(newRasterData);
                    image = new UDGImage(imageWidth, imageHeight);
                    image.getRaster().setDataElements(0, 0, imageWidth, imageHeight, newRasterData);
                    editingPane.refresh();
                    outputLabel.setText("");
                } catch (IOException ioex) {
                    ioex.printStackTrace();
                }
            }
        });
        fileMenu.add(openImageMenuItem);
        JMenuItem saveImageMenuItem = new JMenuItem("Save Image...");
        saveImageMenuItem.addActionListener(e -> {
            JFileChooser saveFileChooser = new JFileChooser();
            saveFileChooser.addChoosableFileFilter(new FileNameExtensionFilter("UDG Image",".udg"));
            int result = saveFileChooser.showSaveDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                try {
                    DataOutputStream dos = new DataOutputStream(new FileOutputStream(saveFileChooser.getSelectedFile()));
                    dos.writeInt(image.getWidth());
                    dos.writeInt(image.getHeight());
                    dos.writeFloat(editingPane.getHRatio());
                    byte[] rasterData = new byte[image.getWidth() * image.getHeight()];
                    image.getRaster().getDataElements(0, 0, image.getWidth(), image.getHeight(), rasterData);
                    dos.write(rasterData);
                } catch (IOException ioex) {
                    ioex.printStackTrace();
                }
            }
        });
        fileMenu.add(saveImageMenuItem);
        fileMenu.add(new JSeparator());
        JMenuItem exitAppMenuItem = new JMenuItem("Quit");
        exitAppMenuItem.addActionListener(e -> dispose());
        fileMenu.add(exitAppMenuItem);
        appMenuBar.add(fileMenu);

        JMenu editMenu = new JMenu("Edit");
        JMenuItem resizeImageMenuItem = new JMenuItem("Resize image...");
        resizeImageMenuItem.addActionListener(e -> {
            if (image != null) new NewImageDialog(this, image).setVisible(true);
        });
        editMenu.add(resizeImageMenuItem);
        appMenuBar.add(editMenu);

        this.setJMenuBar(appMenuBar);

        ColourButton blackButton = new ColourButton(Color.BLACK, 0);
        blackButton.addActionListener(e -> editingPane.setColourIndex(blackButton.indexValue));
        ColourButton whiteButton = new ColourButton(Color.WHITE, 1);
        whiteButton.addActionListener(e -> editingPane.setColourIndex(whiteButton.indexValue));
        appToolBar.add(blackButton);
        appToolBar.add(whiteButton);

        JComboBox<ZoomLevel> zoomLevels = new JComboBox<>(new ZoomLevel[] {
                new ZoomLevel("25%", 0.25f),
                new ZoomLevel("50%", 0.5f),
                new ZoomLevel("100%", 1.0f),
                new ZoomLevel("200%", 2.0f),
                new ZoomLevel("400%", 4.0f),
                new ZoomLevel("800%", 8.0f),
                new ZoomLevel("1600%", 16.0f),
                new ZoomLevel("3200%", 32.0f)
        });
        zoomLevels.addActionListener(e -> editingPane.setZoom(((ZoomLevel) zoomLevels.getSelectedItem()).zoom));
        zoomLevels.setSelectedIndex(6);
        appToolBar.add(zoomLevels);

        this.pack();

        this.addWindowListener(this);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new UDGEditor().setVisible(true));
    }

    public UDGImage getImage() { return image; }

    public void refresh() {
        editingPane.refresh();
        updateOutput();
    }

    public void updateOutput() {
        outputLabel.setText(image != null ? image.toString() : " ");
    }

    @Override
    public void windowOpened(WindowEvent e) {

    }

    @Override
    public void windowClosing(WindowEvent e) {
        System.exit(0);
    }

    @Override
    public void windowClosed(WindowEvent e) {

    }

    @Override
    public void windowIconified(WindowEvent e) {

    }

    @Override
    public void windowDeiconified(WindowEvent e) {

    }

    @Override
    public void windowActivated(WindowEvent e) {

    }

    @Override
    public void windowDeactivated(WindowEvent e) {

    }

    static class ColourButton extends JButton {
        private final int indexValue;
        private final Color colour;

        ColourButton(Color colour, int indexValue) {
            super();
            this.colour = colour != null ? colour : Color.BLACK;
            this.indexValue = indexValue >= 0 && indexValue <= 1 ? indexValue : 0;
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(24, 24);
        }

        @Override
        public Dimension getMinimumSize() {
            return getPreferredSize();
        }

        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            Rectangle insets = getVisibleRect();
            g.setColor(colour);
            g.fillRect(insets.x, insets.y, insets.width, insets.height);
        }
    }

    static class ZoomLevel {
        private final String text;
        private final float zoom;

        ZoomLevel(String text, float zoom) {
            this.text = text;
            this.zoom = zoom;
        }

        @Override
        public String toString() {
            return text;
        }
    }

    static class NewImageDialog extends JDialog {
        NewImageDialog(UDGEditor udgEditor, UDGImage currentImage) {
            super(udgEditor, currentImage == null ? "Create New Image" : "Edit Image", true);
            JPanel sizePanel = new JPanel();
            sizePanel.setLayout(new BoxLayout(sizePanel, BoxLayout.X_AXIS));
            JSpinner widthSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 40, 1));
            if (currentImage != null) widthSpinner.setValue(currentImage.getWidth() / 8);
            JLabel widthLabel = new JLabel("Width (chars):");
            JSpinner heightSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 25, 1));
            JLabel heightLabel = new JLabel("Height (chars):");
            if (currentImage != null) heightSpinner.setValue(currentImage.getHeight() / 8);
            JComboBox<HRatio> pixelRatioComboBox = new JComboBox<>(new HRatio[]{
                    new HRatio("Half Pixel", 0.5f),
                    new HRatio("Normal", 1.0f),
                    new HRatio("Double Pixel", 2.0f)
            });
            pixelRatioComboBox.setSelectedItem(currentImage == null ? 1.0f : udgEditor.editingPane.getHRatio());
            JLabel pixelRatioLabel = new JLabel("Pixel Ratio:");
            JButton okButton = new JButton("OK");
            okButton.addActionListener(e -> {
                final int newWidth = 8 * (int) widthSpinner.getValue();
                final int newHeight = 8 * (int) heightSpinner.getValue();
                if (currentImage == null) {
                    udgEditor.image = new UDGImage(newWidth, newHeight);
                    udgEditor.editingPane.setHRatio(((HRatio) pixelRatioComboBox.getSelectedItem()).value);
                } else {
                    if (newWidth != currentImage.getWidth() || newHeight != currentImage.getHeight()) {
                        UDGImage newImage = new UDGImage(newWidth, newHeight);
                        final int xoffset = (newWidth - currentImage.getWidth()) / 2;
                        final int yoffset = (newHeight - currentImage.getHeight()) / 2;
                        Graphics2D g2 = newImage.createGraphics();
                        g2.drawImage(currentImage, xoffset, yoffset, null);
                        udgEditor.image = newImage;
                    }
                }
                if (((HRatio) pixelRatioComboBox.getSelectedItem()).value != udgEditor.editingPane.getHRatio()) {
                    udgEditor.editingPane.setHRatio(((HRatio) pixelRatioComboBox.getSelectedItem()).value);
                }
                udgEditor.refresh();
                dispose();
            });
            JButton cancelButton = new JButton("Cancel");
            cancelButton.addActionListener(e -> {
                dispose();
            });
            sizePanel.add(widthLabel);
            sizePanel.add(widthSpinner);
            sizePanel.add(heightLabel);
            sizePanel.add(heightSpinner);
            sizePanel.add(pixelRatioLabel);
            sizePanel.add(pixelRatioComboBox);
            sizePanel.add(okButton);
            sizePanel.add(cancelButton);
            sizePanel.setBorder(new EmptyBorder(4, 4, 4, 4));
            this.add(sizePanel);
            this.pack();
        }

        static class HRatio {
            float value;
            private String name;

            HRatio(String name, float value) {
                this.name = name;
                this.value = value < 0 || value > 2 ? 1 : value < 1 ? 0.5f : value > 1 ? 2 : 1;
            }

            @Override
            public String toString() { return name; }
        }
    }
}
