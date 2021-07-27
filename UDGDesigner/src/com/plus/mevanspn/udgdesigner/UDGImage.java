package com.plus.mevanspn.udgdesigner;

import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;

final public class UDGImage extends BufferedImage {
    public UDGImage(int width, int height) {
        super(width % 8 != 0 ? ((width / 8) + 1) * 8 : width,
                height % 8 != 0 ? ((height / 8) + 1) * 8 : height,
                BufferedImage.TYPE_BYTE_BINARY, new IndexColorModel(1, 2,
                new byte[] { (byte) 0, (byte) 255 },
                new byte[] { (byte) 0, (byte) 255 },
                new byte[] { (byte) 0, (byte) 255 }));
    }

    public void setPixel(int x, int y, byte value) {
        if (x < 0 || x >= getWidth() || y < 0 || y >= getHeight() || value < 0 || value > 1) return;
        getRaster().setDataElements(x, y, new byte[] { value });
    }

    @Override
    public String toString() {
        final int sampleCount = getWidth() * getHeight();
        byte[] rasterValues = new byte[sampleCount];
        getRaster().getDataElements(0, 0, getWidth(), getHeight(), rasterValues);
        StringBuilder sb = new StringBuilder();
        for (int y = 0; y < getHeight(); y += 8) {
            if (y > 0 && y < getHeight()) sb.append(",\n");
            for (int x = 0; x < getWidth(); x += 8) {
                if (x != 0) sb.append(',');
                for (int hy = y; hy < y + 8; hy += 4) {
                    if (hy % 8 != 0) sb.append(',');
                    sb.append('{');
                    long value = 0, bitValue = (1 << 31) & 0xFFFFFFFFL;
                    for (int yy = hy; yy < hy + 4; yy++) {
                        int offset = (yy * getWidth()) + x;
                        for (int xx = x; xx < x + 8; xx++) {
                            if (rasterValues[offset] > 0) {
                                value += bitValue;
                            }
                            offset++;
                            bitValue /= 2;
                        }
                    }
                    String valueString = Long.toHexString(value);
                    sb.append("&" + (valueString.length() > 8 ? valueString.substring(0,8) : valueString));
                    sb.append('}');
                }
            }
        }

        return sb.toString();
    }
}
