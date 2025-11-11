package igu;
import java.awt.Color;
/**
 *
 * @author beacardozo
 */

import javax.swing.*;
import java.awt.*;

public class SDBlockPanel extends JPanel {
    private String[] blocks;     // Arreglo que representa el estado de los bloques
    private Color[] blockColors;  // Arreglo que almacena el color de cada bloque
    private static final int BLOCK_SIZE = 35;
    private static final int TOTAL_BLOCKS = 64;
    private static final int SPACING = 10;

    public SDBlockPanel(String[] blocks, Color[] blockColors) {
        this.blocks = blocks;
        this.blockColors = blockColors;
        setPreferredSize(new Dimension(460, 360));
        setBackground(Color.WHITE);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Posición de los bloques
        for (int i = 0; i < TOTAL_BLOCKS; i++) {
            int x = (i % 10) * (BLOCK_SIZE + SPACING);
            int y = (i / 10) * (BLOCK_SIZE + SPACING);

            // Dibuja el bloque con el color correspondiente
            if (blocks[i]!=null) {
                g.setColor(blockColors[i]); // Usa el color del archivo
            } else {
                g.setColor(Color.GREEN); // Bloque libre
            }
            g.fillRect(x + 5, y + 20, BLOCK_SIZE - 10, BLOCK_SIZE - 10);
            g.setColor(Color.BLACK);
            g.drawRect(x + 5, y + 20, BLOCK_SIZE - 10, BLOCK_SIZE - 10);

            // Dibuja el número del bloque
            g.setFont(new Font("Arial", Font.PLAIN, 12));
            g.drawString(String.valueOf(i), x + 12, y + 15);
        }
    }

    public void updateBlocks(String[] newBlocks, Color[] newBlockColors) {
        System.arraycopy(newBlocks, 0, this.blocks, 0, Math.min(newBlocks.length, TOTAL_BLOCKS));
        System.arraycopy(newBlockColors, 0, this.blockColors, 0, Math.min(newBlockColors.length, TOTAL_BLOCKS));
        repaint();
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setMinimumSize(new java.awt.Dimension(370, 210));
        setSize(new java.awt.Dimension(370, 210));
        setLayout(new java.awt.GridLayout());
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
