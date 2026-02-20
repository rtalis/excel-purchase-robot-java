package com.rt.robotexcel.demo.robot;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;

public class RobotUtil {
    private Robot robot;
    private static final long SLEEP_TIME_DEFAULT = 500;

    public RobotUtil() throws AWTException {
        this.robot = new Robot();
    }

    public void copyToClipboard() {
        try {
            Thread.sleep(SLEEP_TIME_DEFAULT);

            robot.keyPress(KeyEvent.VK_CONTROL);
            robot.keyPress(KeyEvent.VK_C);

            Thread.sleep(SLEEP_TIME_DEFAULT);

            robot.keyRelease(KeyEvent.VK_C);
            robot.keyRelease(KeyEvent.VK_CONTROL);

            Thread.sleep(SLEEP_TIME_DEFAULT);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void pasteFromClipboard() {
        try {
            robot.keyPress(KeyEvent.VK_CONTROL);
            robot.keyPress(KeyEvent.VK_V);

            robot.keyRelease(KeyEvent.VK_V);
            robot.keyRelease(KeyEvent.VK_CONTROL);

            Thread.sleep(SLEEP_TIME_DEFAULT);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void pressEnter() {
        try {
            // Pressiona ENTER
            robot.keyPress(KeyEvent.VK_ENTER);
            robot.keyRelease(KeyEvent.VK_ENTER);

            Thread.sleep(SLEEP_TIME_DEFAULT);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void enterCell() {
        try {
            robot.keyPress(KeyEvent.VK_F2);
            robot.keyRelease(KeyEvent.VK_F2);

            Thread.sleep(SLEEP_TIME_DEFAULT);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void selectAll() {
        try {
            robot.keyPress(KeyEvent.VK_CONTROL);
            robot.keyPress(KeyEvent.VK_A);

            robot.keyRelease(KeyEvent.VK_A);
            robot.keyRelease(KeyEvent.VK_CONTROL);

            Thread.sleep(SLEEP_TIME_DEFAULT);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void pressRightArrow() {
        try {
            robot.keyPress(KeyEvent.VK_RIGHT);
            robot.keyRelease(KeyEvent.VK_RIGHT);

            Thread.sleep(SLEEP_TIME_DEFAULT);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void pressLeftArrow() {
        try {
            robot.keyPress(KeyEvent.VK_LEFT);
            robot.keyRelease(KeyEvent.VK_LEFT);

            Thread.sleep(SLEEP_TIME_DEFAULT);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void pressDownArrow() {
        try {
            robot.keyPress(KeyEvent.VK_DOWN);
            robot.keyRelease(KeyEvent.VK_DOWN);

            Thread.sleep(SLEEP_TIME_DEFAULT);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void pressEsc() {
        try {
            robot.keyPress(KeyEvent.VK_ESCAPE);
            robot.keyRelease(KeyEvent.VK_ESCAPE);

            Thread.sleep(SLEEP_TIME_DEFAULT);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}