## BloodPressureModel.java - разбор по кускам

### Кусок 1: поля класса

```java
public class BloodPressureModel {

    public static final String PROP_DATA_CHANGED = "dataChanged";

    private final PropertyChangeSupport support = new PropertyChangeSupport(this);

    private LocalDate birthDate;
    private double weightKg;
    private int systolic;
    private int diastolic;

    private boolean hasData = false;

    private double idealSystolic;
    private double idealDiastolic;
    private String estimation = "";
```

- `public static final String PROP_DATA_CHANGED = "dataChanged";` - это просто именованная константа-строка. `PropertyChangeSupport` идентифицирует разные типы событий по строковому имени (как ключ), поэтому вместо того, чтобы писать `"dataChanged"` в разных местах кода руками (и рискнуть где-то опечататься), это имя один раз задано константой. `static` - значит, принадлежит классу, а не конкретному объекту; `final` - нельзя переприсвоить.

- `private final PropertyChangeSupport support = new PropertyChangeSupport(this);` - создаётся объект-помощник из стандартной библиотеки Java (`java.beans.PropertyChangeSupport`). Он **внутри себя** хранит список подписчиков и умеет их оповещать - модели не нужно писать эту логику самой (список + перебор + вызов метода у каждого), всё уже готово в этом классе. Параметр `this` передаётся конструктору `PropertyChangeSupport`, чтобы события, которые он будет создавать, содержали ссылку на "источник" - саму модель (это используется потом внутри `PropertyChangeEvent.getSource()`, хотя в вашем коде вы используете `getNewValue()`, а не `getSource()`, но конструктору `this` всё равно нужен).

- `private LocalDate birthDate; private double weightKg; private int systolic; private int diastolic;` - четыре поля, куда сохраняются последние **корректные** введённые данные. Именно эти четыре поля потом собираются обратно в `BloodPressureInput` в методе `getLastInput()` для восстановления диалога.

- `private boolean hasData = false;` - флаг "были ли вообще данные хоть раз введены". Нужен, чтобы отличить ситуацию "давление ещё ни разу не пересчитывалось" (при первом запуске приложения, до первого ввода) от ситуации "уже что-то есть". Используется в `getLastInput()`, чтобы не пытаться собрать `BloodPressureInput` из пустых/дефолтных полей (`birthDate == null` и т.д.).

- `private double idealSystolic; private double idealDiastolic; private String estimation = "";` - три поля для **результата** последнего расчёта: идеальные значения давления и текстовая оценка ("Норма"/"Повышенное давление"/"Пониженное давление"). `estimation` изначально пустая строка (а не `null`), чтобы избежать `NullPointerException`, если вдруг кто-то вызовет `getEstimation()` до первого расчёта.

Спрашивайте, если что-то из этого куска непонятно, либо пишите "дальше" - и перейдём к методу `addPropertyChangeListener` и началу `updateData`.

### Кусок 2: подписка на модель

```java
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }
```

- `public void addPropertyChangeListener(PropertyChangeListener listener)` - публичный метод самой модели, через который **снаружи** (из `MainFrame`) можно подписаться на изменения. Обратите внимание: метод ничего не возвращает (`void`), просто регистрирует подписчика.
- `PropertyChangeListener listener` - параметр типа интерфейса `PropertyChangeListener` (тоже из стандартной библиотеки `java.beans`). У этого интерфейса всего один метод: `void propertyChange(PropertyChangeEvent evt)`. Любой объект (или лямбда/ссылка на метод), который умеет реагировать на такое событие, подходит сюда.
- `support.addPropertyChangeListener(listener);` - модель **не хранит** список подписчиков сама, она делегирует это своему полю-помощнику `support` (тому самому `PropertyChangeSupport` из куска 1). То есть модель просто "прокидывает" вызов дальше - как обёртка/фасад.

Именно этот метод вызывается в конструкторе `MainFrame`:
```java
model.addPropertyChangeListener(this::onModelChanged);
```
Здесь `this::onModelChanged` - это **ссылка на метод** (method reference), синтаксический сахар вместо написания:
```java
new PropertyChangeListener() {
    public void propertyChange(PropertyChangeEvent evt) {
        onModelChanged(evt);
    }
}
```
Java понимает, что `onModelChanged(PropertyChangeEvent evt)` по сигнатуре совпадает с единственным методом интерфейса `PropertyChangeListener`, и автоматически оборачивает ссылку на метод в реализацию этого интерфейса.

---

### Кусок 3: метод updateData - начало

```java
    public void updateData(LocalDate birthDate, double weightKg, int systolic, int diastolic) {
        validate(birthDate, weightKg, systolic, diastolic);

        this.birthDate = birthDate;
        this.weightKg = weightKg;
        this.systolic = systolic;
        this.diastolic = diastolic;
        this.hasData = true;

        recalculate();

        support.firePropertyChange(PROP_DATA_CHANGED, null, this);
    }
```

- `public void updateData(LocalDate birthDate, double weightKg, int systolic, int diastolic)` - это единственный публичный метод, через который **вообще можно** изменить данные модели снаружи (Controller вызывает именно его). Параметры имеют **те же имена**, что и поля класса (`birthDate`, `weightKg` и т.д.) - это называется "затенение" (shadowing): внутри метода имя `birthDate` без `this.` означает параметр, а `this.birthDate` - поле объекта.

- `validate(birthDate, weightKg, systolic, diastolic);` - вызов приватного метода проверки (разберём его дальше). Если там что-то не так, будет брошено исключение (`throw new IllegalArgumentException(...)`), и выполнение метода `updateData` **немедленно прервётся** - строчки ниже (`this.birthDate = ...` и так далее) вообще не выполнятся. Именно поэтому "ошибочный ввод не портит старые данные" - до присвоения полей просто не доходит.

- `this.birthDate = birthDate;` и следующие три строки - если валидация прошла, параметры копируются в поля объекта. `this.` тут обязательно нужен, чтобы отличить поле от одноимённого параметра.

- `this.hasData = true;` - теперь модель "знает", что данные есть (это важно для `getLastInput()`, который мы разберём позже).

- `recalculate();` - вызов ещё одного приватного метода, который на основе только что сохранённых полей вычисляет `idealSystolic`, `idealDiastolic` и `estimation`. Он не принимает параметров и не возвращает значение - просто читает и пишет поля того же объекта.

- `support.firePropertyChange(PROP_DATA_CHANGED, null, this);` - самая важная строка для "активности" модели. Метод `firePropertyChange` у `PropertyChangeSupport` принимает три аргумента:
    1. `PROP_DATA_CHANGED` ("dataChanged") - имя/тип события;
    2. `null` - "старое значение" (oldValue) - в вашем случае оно не используется, поэтому передан `null`;
    3. `this` - "новое значение" (newValue) - здесь передаётся **сама модель целиком**, то есть подписчик сможет обратиться прямо к ней и вызвать её геттеры.

  Внутри `firePropertyChange` создаёт объект `PropertyChangeEvent` с этими данными и **проходит по всем подписчикам**, вызывая у каждого `propertyChange(event)`. Именно тут и происходит "самостоятельное" уведомление модели о своём изменении - без участия контроллера.

Дальше - метод `validate`, разберём его?


### getLastInput (и остальные геттеры кратко)

```java
    public BloodPressureInput getLastInput() {
        if (!hasData) {
            return null;
        }
        return new BloodPressureInput(birthDate, weightKg, systolic, diastolic);
    }
```

- `public BloodPressureInput getLastInput()` - метод возвращает объект типа `BloodPressureInput` (тот самый record из первого разбора).
- `if (!hasData) { return null; }` - если данные ещё ни разу не вводились (флаг `hasData` мы выставили в `true` только внутри `updateData`), метод сразу возвращает `null`. Это тот самый случай "приложение только что запущено, пользователь ещё не разу не жал ОК в диалоге".
- `return new BloodPressureInput(birthDate, weightKg, systolic, diastolic);` - если данные есть, собирается **новый** `BloodPressureInput` из текущих полей модели и возвращается наружу.

Зачем вообще нужен этот метод: `Controller` вызывает его при открытии диалога -
```java
InputDialog dialog = new InputDialog(mainFrame, model.getLastInput(), this::updateModel);
```
- если `null` - `InputDialog.restoreData(null)` просто ничего не делает (там есть проверка `if (initialData != null)`), поля диалога остаются пустыми/по умолчанию;
- если не `null` - поля диалога заполняются последними значениями.

Оставшиеся геттеры - `getIdealSystolic()`, `getIdealDiastolic()`, `getEstimation()` - тривиальны, просто возвращают соответствующее приватное поле без всякой логики (`return idealSystolic;` и т.п.), их вызывает `MainFrame` в `onModelChanged`.

---
