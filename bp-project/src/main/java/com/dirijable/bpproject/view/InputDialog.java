package com.dirijable.bpproject.view;

import com.dirijable.bpproject.model.BloodPressureInput;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.util.function.Consumer;

public class InputDialog extends JDialog {

    //выпадающие параметризованные списки, которые создаются в конструкторе
    private final JComboBox<Integer> dayBox;
    private final JComboBox<String> monthBox;
    private final JComboBox<Integer> yearBox;
    //односттрочные текстовые поля для ввода
    private final JTextField weightField;
    private final JTextField systolicField;
    private final JTextField diastolicField;

    private static final String[] MONTHS = {
            "Январь", "Февраль", "Март", "Апрель", "Май", "Июнь",
            "Июль", "Август", "Сентябрь", "Октябрь", "Ноябрь", "Декабрь"
    };

    public InputDialog(
            JFrame owner, //владелец диалога. сюда идет MF из контроллера
            BloodPressureInput initialData,// последние введенные данные
            Consumer<BloodPressureInput> onSubmit) {
        super(owner, "Ввести данные", true); //true-не можем взаимодействовать с овнер
        dayBox = new JComboBox<>();
        monthBox = new JComboBox<>(MONTHS);
        yearBox = new JComboBox<>();
        weightField = new JTextField(10);
        systolicField = new JTextField(10);
        diastolicField = new JTextField(10);

        JButton okButton = new JButton("ОК");
        JButton cancelButton = new JButton("Отмена");

        initializeDateSelectors();
        buildForm(okButton, cancelButton);
        restoreData(initialData);
        configureListeners(okButton, cancelButton, onSubmit);
        configureWindow(owner);
    }

    //заполняются спсики дней
    private void initializeDateSelectors() {
        //просто добавляем 31 день в список (подрежется через adjustDayCount)
        for (int day = 1; day <= 31; day++) {
            dayBox.addItem(day);
        }

        //список годов от текущего до старого
        int currentYear = LocalDate.now().getYear();
        for (int year = currentYear; year >= currentYear - 130; year--) {
            yearBox.addItem(year);
        }
    }

    //расставляются все компоненты по окну
    private void buildForm(JButton okButton, JButton cancelButton) {
        setLayout(new GridBagLayout());

        //панель для 3 выпадающих списеов
        //прижаты к лев. краю с 4 пикс. гориз. отступ и 0 - верт.
        JPanel datePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        datePanel.add(dayBox);
        datePanel.add(monthBox);
        datePanel.add(yearBox);

        addRow("Дата рождения:", datePanel, 0);
        addRow("Вес (кг):", weightField, 1);
        addRow("Текущее давление, верхнее:", systolicField, 2);
        addRow("Текущее давление, нижнее:", diastolicField, 3);

        //панель для й выровненных по центру кнопок
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonsPanel.add(okButton);
        buttonsPanel.add(cancelButton);

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 4;
        constraints.gridwidth = 2;
        constraints.insets = new Insets(5, 5, 5, 5);
        add(buttonsPanel, constraints);
    }

//принимает текст подписи, сам компонент (текстовое поле или панель) и номер строки в сетке.
    private void addRow(String labelText, JComponent component, int row) {
        GridBagConstraints labelConstraints = new GridBagConstraints();
        labelConstraints.gridx = 0;
        labelConstraints.gridy = row;
        labelConstraints.insets = new Insets(5, 5, 5, 5);
        labelConstraints.fill = GridBagConstraints.HORIZONTAL;
        add(new JLabel(labelText), labelConstraints);

        GridBagConstraints fieldConstraints = new GridBagConstraints();
        fieldConstraints.gridx = 1;
        fieldConstraints.gridy = row;
        fieldConstraints.insets = new Insets(5, 5, 5, 5);
        fieldConstraints.fill = GridBagConstraints.HORIZONTAL;
        add(component, fieldConstraints);
    }

    //подставляем старые значения  после создания и расстановки всех компонентов
    private void restoreData(BloodPressureInput initialData) {
        if (initialData != null) {
            LocalDate bd = initialData.birthDate();
            dayBox.setSelectedItem(bd.getDayOfMonth());
            monthBox.setSelectedIndex(bd.getMonthValue() - 1);
            yearBox.setSelectedItem(bd.getYear());
            weightField.setText(String.valueOf(initialData.weightKg()));
            systolicField.setText(String.valueOf(initialData.systolic()));
            diastolicField.setText(String.valueOf(initialData.diastolic()));
        }
    }

    //вешаются обработчики выбора после того, как всё создано (иначе не на что вешать)
    private void configureListeners(
            JButton okButton,
            JButton cancelButton,
            Consumer<BloodPressureInput> onSubmit) {
        monthBox.addActionListener(e -> adjustDayCount());
        yearBox.addActionListener(e -> adjustDayCount());
        cancelButton.addActionListener(e -> dispose());//диспос закрывает окно диалога не вызывая никаких данных
        okButton.addActionListener(e -> submitData(onSubmit));//вызывается колбэк, переданный из контроллера
    }

    //настройка размеров и позиции окна
    private void configureWindow(JFrame owner) {
        pack();
        setResizable(false);
        setLocationRelativeTo(owner); //по центру относитльно окна-овнера
    }

    private void submitData(Consumer<BloodPressureInput> onSubmit) {
        try {
            onSubmit.accept(readInputData());
            dispose();
        } catch (NumberFormatException ex) {
            showError("Вес и давление должны быть числами");
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        }
    }

    private BloodPressureInput readInputData() {
        int day = (Integer) dayBox.getSelectedItem();
        int month = monthBox.getSelectedIndex() + 1;
        int year = (Integer) yearBox.getSelectedItem();
        LocalDate birthDate = LocalDate.of(year, month, day);

        double weight = Double.parseDouble(weightField.getText().trim().replace(',', '.'));
        int systolic = Integer.parseInt(systolicField.getText().trim());
        int diastolic = Integer.parseInt(diastolicField.getText().trim());

        return new BloodPressureInput(birthDate, weight, systolic, diastolic);
    }

    private void showError(String message) {
        //параметры окно-овнер, относительно кот. позиц. всплыв. окно (InputDialog в данном случае), сообщение, заголовок окна и константа, задающая тип сообщения - при этом типе свинг автоматически рисует иконку с красным крестиком
        JOptionPane.showMessageDialog(this, message, "Ошибка", JOptionPane.ERROR_MESSAGE);
    }

    //вызывается каждый раз при смене месяца или года и пересчитвает, сколько дней должно быть в списке
    private void adjustDayCount() {
        int month = monthBox.getSelectedIndex() + 1;
        Integer yearObj = (Integer) yearBox.getSelectedItem();
        int year = yearObj != null ? yearObj : LocalDate.now().getYear();
        int maxDay = YearMonth.of(year, Month.of(month)).lengthOfMonth();

        Integer selectedDay = (Integer) dayBox.getSelectedItem();
        dayBox.removeAllItems();
        for (int d = 1; d <= maxDay; d++) dayBox.addItem(d);
        if (selectedDay != null && selectedDay <= maxDay) {
            dayBox.setSelectedItem(selectedDay);
        }
    }

}
