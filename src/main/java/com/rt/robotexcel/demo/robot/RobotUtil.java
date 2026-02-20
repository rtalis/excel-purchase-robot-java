package com.rt.robotexcel.demo.robot;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import io.github.cdimascio.dotenv.Dotenv;

public class RobotUtil {
    private Robot robot;
    private long sleepTime;

    public RobotUtil() throws AWTException {
        this(getDefaultSleepTime());
    }

    public RobotUtil(long sleepTimeMs) throws AWTException {
        this.robot = new Robot();
        this.sleepTime = sleepTimeMs;
    }

    private static long getDefaultSleepTime() {
        try {
            Dotenv dotenv = Dotenv.load();
            String delay = dotenv.get("DELAY_TIME");
            if (delay != null && !delay.isEmpty()) {
                return Long.parseLong(delay);
            }
        } catch (Exception e) {
            // Use default if loading fails
        }
        return 500; // Default delay in milliseconds
    }

    public void copyToClipboard() {
        try {
            Thread.sleep(sleepTime);

            robot.keyPress(KeyEvent.VK_CONTROL);
            robot.keyPress(KeyEvent.VK_C);

            Thread.sleep(sleepTime);

            robot.keyRelease(KeyEvent.VK_C);
            robot.keyRelease(KeyEvent.VK_CONTROL);

            Thread.sleep(sleepTime);
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

            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void pressEnter() {
        try {
            // Pressiona ENTER
            robot.keyPress(KeyEvent.VK_ENTER);
            robot.keyRelease(KeyEvent.VK_ENTER);

            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void enterCell() {
        try {
            robot.keyPress(KeyEvent.VK_F2);
            robot.keyRelease(KeyEvent.VK_F2);

            Thread.sleep(sleepTime);
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

            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void pressRightArrow() {
        try {
            robot.keyPress(KeyEvent.VK_RIGHT);
            robot.keyRelease(KeyEvent.VK_RIGHT);

            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void pressLeftArrow() {
        try {
            robot.keyPress(KeyEvent.VK_LEFT);
            robot.keyRelease(KeyEvent.VK_LEFT);

            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void pressDownArrow() {
        try {
            robot.keyPress(KeyEvent.VK_DOWN);
            robot.keyRelease(KeyEvent.VK_DOWN);

            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void pressEsc() {
        try {
            robot.keyPress(KeyEvent.VK_ESCAPE);
            robot.keyRelease(KeyEvent.VK_ESCAPE);

            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}