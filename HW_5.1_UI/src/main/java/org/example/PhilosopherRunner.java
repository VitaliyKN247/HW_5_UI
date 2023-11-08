package org.example;

/*
Условие:
Пять безмолвных философов сидят вокруг круглого стола, перед каждым философом стоит тарелка спагетти.
Вилки лежат на столе между каждой парой ближайших философов.
Каждый философ может либо есть, либо размышлять.
Приём пищи не ограничен количеством оставшихся спагетти — подразумевается бесконечный запас.
Тем не менее, философ может есть только тогда, когда держит две вилки — взятую справа и слева
(альтернативная формулировка проблемы подразумевает миски с рисом и палочки для еды вместо тарелок со спагетти и вилок).
Каждый философ может взять ближайшую вилку (если она доступна), или положить — если он уже держит её.
Взятие каждой вилки и возвращение её на стол являются раздельными действиями, которые должны выполняться одно за другим.
Решение:
Введем понятия раунда.
Раунд - промежуток времени в течении которого могут обедать философы. Раундов может быть 5 (по количеству философов).
Каждый раунд выбираются 2 философа, которые будут обедать, остальные 3 размышляют. Выборка производится по следующему
алгоритму. Берется номер раунда. И начиная с этого номера через одного выбираются философы, принимающие пищу. Новый философ
не приступает к еде до начала следующего раунда.
 */

class Round {
    /**
     * Номер раунда
     */
    private volatile int round;
    /**
     * Счетчик определившихся философов
     */
    private volatile int counter;
    /**
     * Число философов
     */
    private final int maxCounter;

    /**
     * Констрктор
     *
     * @param maxCounter Число философов
     */
    public Round(int maxCounter) {
        this.round = 0;
        this.counter = 0;
        this.maxCounter = maxCounter;
    }

    /**
     * Вернуть номер раунда
     *
     * @return номер раунда
     */
    public int getRound() {
        return round;
    }

    /**
     * Следующий раунд
     *
     * @throws InterruptedException
     */
    public synchronized void next() throws InterruptedException {
        int nNow = round;
        int valNextRound = counter;
        if (valNextRound + 1 == maxCounter) {
            if (nNow + 1 < maxCounter) {
                round = nNow + 1;
            } else {
                round = 0;
            }
            System.out.println("Round " + nNow);
            counter = 0;
            notifyAll();
        } else {
            counter = valNextRound + 1;
            wait();
        }
    }
}

/**
 * Философ
 */
class Philosopher implements Runnable {
    /**
     * Число философов
     */
    private final int maxPhilosofields;
    /**
     * Количество приемов пищи
     */
    private int eatCounter;
    /**
     * Имя философа
     */
    private final String name;
    /**
     * Раунд
     */
    private Round round;
    /**
     * Номер философа
     */
    private int id;
    /**
     * Время нужное философу для приема пищи
     */
    private int eatTime;

    /**
     * Конструктор
     *
     * @param name             имя философа
     * @param id               номер философа
     * @param maxPhilosofields всего философов
     * @param round            раунд
     * @param eatTime          время для еды
     */
    public Philosopher(String name, int id, final int maxPhilosofields, final Round round, int eatTime) {
        this.name = name;
        this.round = round;
        this.maxPhilosofields = maxPhilosofields;
        this.id = id;
        this.eatTime = eatTime;
        eatCounter = 0;
    }

    @Override
    public void run() {
        final int variations = (int) Math.floor(maxPhilosofields / 2.0);
        while (true) {
            try {
                int nRound = round.getRound();
                for (int i = 0; i < variations; ++i) {
                    int nIndex = nRound + i * 2;
                    if (nIndex >= maxPhilosofields) {
                        nIndex -= maxPhilosofields;
                    }
                    if (id == nIndex) {
                        eat();
                    }
                }
                round.next();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Прием пищи
     *
     * @throws InterruptedException
     */
    private void eat() throws InterruptedException {
        this.eatCounter += 1;
        System.out.println(name + " поел " + eatCounter + " раз(а)");
        if (eatTime > 0) {
            Thread.sleep(eatTime);
        }
    }
}

public class PhilosopherRunner {
    public static void main(String[] args) {
        final int nCounter = 5;
        String[] names = {
                "Аристотель", "Пифагор", "Платон", "Сократ", "Цицерон", "Кафка", "Кант"
        };

        Thread[] philosophers = new Thread[nCounter];
        Round round = new Round(nCounter);
        for (int i = 0; i < nCounter; ++i) {
            philosophers[i] = new Thread(
                    new Philosopher(names[i], i, nCounter, round, 1000*(1+i))
            );
        }
        System.out.println("Раунд " + round.getRound());
        for (Thread th : philosophers) {
            th.start();
        }
    }
}