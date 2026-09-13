package com.dirijable.bpproject;

import com.dirijable.bpproject.controller.Controller;
import com.dirijable.bpproject.model.BloodPressureModel;
import com.dirijable.bpproject.view.MainFrame;

import javax.swing.SwingUtilities;


public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            BloodPressureModel model = new BloodPressureModel();
            MainFrame mainFrame = new MainFrame(model);
            new Controller(model, mainFrame);
            mainFrame.setVisible(true);
        });
    }
}
