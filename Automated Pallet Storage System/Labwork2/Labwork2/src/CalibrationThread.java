import java.util.logging.Level;
import java.util.logging.Logger;

public class CalibrationThread extends Thread{
    private final AxisThread axisX;
    private final AxisThread axisZ;
    private final AxisThread axisY;

    public CalibrationThread(AxisThread axisX, AxisThread axisZ, AxisThread axisY){
        this.axisX = axisX;
        this.axisZ = axisZ;
        this.axisY = axisY;
    }

    public void initializecalibration() throws InterruptedException {
        axisY.getQueue().add(2);
        axisX.getQueue().add(1);
        axisZ.getQueue().add(1);
        
        axisY.getSemaphore().release();
        axisY.getSemaphoreSync().acquire();	

        axisX.getSemaphore().release();
        axisZ.getSemaphore().release();
        
        axisX.getSemaphoreSync().acquire();
        axisZ.getSemaphoreSync().acquire();
    }
    
    @Override
    public void run(){
    try {
        this.initializecalibration();
    } catch (InterruptedException e) {
            Logger.getLogger(AxisThread.class.getName()).log(Level.SEVERE, "Axis thread crashed", e);
        }
    }
}