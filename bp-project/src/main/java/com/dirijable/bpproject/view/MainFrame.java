package com.dirijable.bpproject.view;


import com.dirijable.bpproject.model.BloodPressureModel;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.beans.PropertyChangeEvent;

public class MainFrame extends JFrame { //значит что MainFrame и есть окно

    private final JButton enterDataButton = new JButton("Ввести данные");
    private final JLabel resultLabel = new JLabel(" ");
    private final JLabel estimationLabel = new JLabel(" ");

    public MainFrame(BloodPressureModel model) {
        super("Идеальное артериальное давление");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); //тобы при нажатии крестика все приложеине завершалось
        setLayout(new BorderLayout(10, 10)); //BL елит окно на 5 зон, а 10 10 - верт. и гор. отступ

        JPanel topPanel = new JPanel();//создаётся простая панель в кот. компоненты подряд л -> п
        topPanel.add(enterDataButton);
        add(topPanel, BorderLayout.NORTH);//добавляем в главное окно вверх окна

        JPanel centerPanel = new JPanel(new GridLayout(2, 1, 5, 5)); //компоновка - 2 строки, 1столбец и 5на5 отступ г/в
        resultLabel.setHorizontalAlignment(SwingConstants.CENTER);
        estimationLabel.setHorizontalAlignment(SwingConstants.CENTER);
        estimationLabel.setFont(estimationLabel.getFont().deriveFont(Font.BOLD, 16f));
        centerPanel.add(resultLabel);
        centerPanel.add(estimationLabel);
        add(centerPanel, BorderLayout.CENTER);

        // Активная модель сама уведомляет — View просто подписывается
        model.addPropertyChangeListener(this::onModelChanged);

        setPreferredSize(new Dimension(420, 180)); //жежлаемый размер окна
        pack(); //подгоняет реальный размер под содержимое
        setLocationRelativeTo(null);
    }

    public JButton getEnterDataButton() {
        return enterDataButton; //чтобы контроллер повесил обработчик
    }

    private void onModelChanged(PropertyChangeEvent evt) {
        BloodPressureModel model = (BloodPressureModel) evt.getNewValue();
        resultLabel.setText(String.format(
                "Идеальное давление: %.0f / %.0f",
                model.getIdealSystolic(), model.getIdealDiastolic()));
        estimationLabel.setText(model.getEstimation());

        switch (model.getEstimation()) {
            case "Норма" -> estimationLabel.setForeground(new Color(0, 128, 0));
            case "Повышенное давление" -> estimationLabel.setForeground(Color.RED);
            case "Пониженное давление" -> estimationLabel.setForeground(new Color(200, 130, 0));
            default -> estimationLabel.setForeground(Color.BLACK);
        }
    }

}
