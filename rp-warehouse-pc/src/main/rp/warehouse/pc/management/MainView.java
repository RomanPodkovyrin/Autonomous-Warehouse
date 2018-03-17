package rp.warehouse.pc.management;

import rp.warehouse.pc.data.robot.Robot;
import rp.warehouse.pc.management.panels.InfoPanel;
import rp.warehouse.pc.management.panels.WarehouseMapPanel;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class MainView extends JFrame {


    public MainView(List<Robot> robots) {
        JPanel top = new JPanel();
        top.setMinimumSize(new Dimension(900, 600));
        top.setLayout(new FlowLayout());
        top.add(new WarehouseMapPanel(robots));
        top.add(new InfoPanel(robots));

        this.add(top);
        this.setSize(top.getPreferredSize());
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.pack();
        this.setVisible(true);
    }
}
