public class Storage {
    static{
    System.load("C:\\STR\\Labwork2\\storage\\x64\\Debug\\storage.dll");
    }
    /* DAQ Initialization */
    static synchronized native public void initializeHardwarePorts();

    /* X Axis */
    static synchronized native public void moveXRight();
    static synchronized native public void moveXLeft();
    static synchronized native public void stopX();
    static synchronized native public int getXPos();

    /* Z Axis*/
    static synchronized native public void moveZUp();
    static synchronized native public void moveZDown();
    static synchronized native public void stopZ();
    static synchronized native public int getZPos();

    /* Y Axis*/
    static synchronized native public void moveYInside();
    static synchronized native public void moveYOutside();
    static synchronized native public void stopY();
    static synchronized native public int getYPos();

    /* Switches */
    static synchronized native public int getSwitch1();
    static synchronized native public int getSwitch2();
    static synchronized native public int getSwitch1_2();

    /* Leds */
    static synchronized native public void ledOn(int led);
    static synchronized native public void ledsOff();
}