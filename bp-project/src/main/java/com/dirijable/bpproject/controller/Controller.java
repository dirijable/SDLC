package com.dirijable.bpproject.controller;

import com.dirijable.bpproject.model.BloodPressureInput;
import com.dirijable.bpproject.model.BloodPressureModel;
import com.dirijable.bpproject.view.InputDialog;
import com.dirijable.bpproject.view.MainFrame;

public class Controller {

    private final BloodPressureModel model;
    private final MainFrame mainFrame;

    public Controller(BloodPressureModel model, MainFrame mainFrame) {
        this.model = model;
        this.mainFrame = mainFrame;

        this.mainFrame.getEnterDataButton().addActionListener(e -> openInputDialog());
    }

    private void openInputDialog() {
        InputDialog dialog = new InputDialog(mainFrame, model.getLastInput(), this::updateModel);
        dialog.setVisible(true);
    }

    private void updateModel(BloodPressureInput data) {
        model.updateData(
                data.birthDate(),
                data.weightKg(),
                data.systolic(),
                data.diastolic());
    }
}
