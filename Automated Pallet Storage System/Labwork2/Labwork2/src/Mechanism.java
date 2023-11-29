import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Mechanism {
    AxisThread axisX;
    AxisThread axisY;
    AxisThread axisZ;
    private Semaphore semaphoreCount;
    private Semaphore semaphoreSync;
    private boolean emergencyState;

    public Mechanism(AxisThread axisX, AxisThread axisY, AxisThread axisZ) {
        this.axisX = axisX;
        this.axisY = axisY;
        this.axisZ = axisZ;
        this.emergencyState = false;
        semaphoreCount = new Semaphore(1);
        semaphoreSync = new Semaphore(0);
    }

    public void relSemaphoreSync() {
        semaphoreSync.release();
    }

    public void setEmergency(){
        Thread emergencyLed = new Thread(EmergencyLed);
        this.emergencyState = true;
        emergencyLed.start();
    }

     public void exitEmergency(){
        this.emergencyState = false;
    }

    public void ledOn(int ledNumber){
        Storage.ledOn(ledNumber);
    }

    public void ledsOff(){
        Storage.ledsOff();
    }

    public boolean swicth1Pressed(){
        if(Storage.getSwitch1() == 1) return true;
        return false;
    }

    public boolean swicth2Pressed(){
        if(Storage.getSwitch2() == 1) return true;
        return false;
    }

    public boolean bothSwitchesPressed(){
        if(Storage.getSwitch1_2() == 1) return true;
        return false;
    }

    Runnable EmergencyLed = () -> {
        while(this.emergencyState){
            try {
                Storage.ledOn(1);
                Thread.sleep(500);
                Storage.ledOn(2);
                Thread.sleep(500);
                Storage.ledsOff();
            } catch (InterruptedException e) {
                Logger.getLogger(AxisThread.class.getName()).log(Level.SEVERE, "EmergencyLed thread crashed", e);
            }
        }  
    };

    public synchronized boolean putPartInCell(int posX, int posZ) throws InterruptedException {
        if(this.emergencyState) return true;
        semaphoreCount.acquire();

        axisY.getQueue().add(1);
        axisY.getSemaphore().release();
        axisY.getSemaphoreSync().acquire();

        semaphoreSync.acquire();

        axisY.getQueue().add(2);
        axisY.getSemaphore().release();
        axisY.getSemaphoreSync().acquire();

        axisX.getQueue().add(posX);    
        axisZ.getQueue().add(posZ * 10);
        axisY.getQueue().add(3);
        axisZ.getQueue().add(posZ);
        axisY.getQueue().add(2);
        axisX.getQueue().add(1);
        axisZ.getQueue().add(1);
        
        axisX.getSemaphore().release();
        axisZ.getSemaphore().release();
        
        axisX.getSemaphoreSync().acquire();
        axisZ.getSemaphoreSync().acquire();

        axisY.getSemaphore().release();
        
        axisY.getSemaphoreSync().acquire();
        axisZ.getSemaphore().release();

        axisZ.getSemaphoreSync().acquire();
        axisY.getSemaphore().release();
        
        axisY.getSemaphoreSync().acquire();

        axisX.getSemaphore().release();
        axisZ.getSemaphore().release();

        axisX.getSemaphoreSync().acquire();
        axisZ.getSemaphoreSync().acquire();

        semaphoreCount.release();
        return false;
    }
    
    public synchronized boolean takePartFromCell(int posX, int posZ) throws InterruptedException {
        if(this.emergencyState) return true;
        semaphoreCount.acquire();

        axisX.getQueue().add(posX);
        axisZ.getQueue().add(posZ);
        axisY.getQueue().add(3);
        axisZ.getQueue().add(posZ*10);
        axisY.getQueue().add(2);
        axisX.getQueue().add(1);
        axisZ.getQueue().add(1);
        
        axisZ.getSemaphore().release();
        axisX.getSemaphore().release();
        
        axisX.getSemaphoreSync().acquire();
        axisZ.getSemaphoreSync().acquire();
        axisY.getSemaphore().release();
        
        axisY.getSemaphoreSync().acquire();
        axisZ.getSemaphore().release();

        axisZ.getSemaphoreSync().acquire();
        axisY.getSemaphore().release();

        axisY.getSemaphoreSync().acquire();

        axisX.getSemaphore().release();
        axisZ.getSemaphore().release();

        axisX.getSemaphoreSync().acquire();
        axisZ.getSemaphoreSync().acquire();

        axisY.getQueue().add(1);
        axisY.getSemaphore().release();
        axisY.getSemaphoreSync().acquire();

        semaphoreSync.acquire();

        axisY.getQueue().add(2);
        axisY.getSemaphore().release();
        axisY.getSemaphoreSync().acquire();

        semaphoreCount.release();
        return false;
    }

}
