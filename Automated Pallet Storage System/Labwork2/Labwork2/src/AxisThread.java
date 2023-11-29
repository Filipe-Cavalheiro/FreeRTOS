import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AxisThread extends Thread {
    private final Axis axis;
    private boolean interrupted;
    private Semaphore semaphoreCount = null;
    private Semaphore semaphoreSync = null;
    private ArrayBlockingQueue<Integer> queue = null;
    private static boolean emergencySate = false;

    public AxisThread(Axis axis){
        this.axis = axis;
        this.semaphoreCount = new Semaphore(0);
        this.semaphoreSync = new Semaphore(0);
        this.queue = new ArrayBlockingQueue<Integer>(10);
    }

    public void setEmergency(){
        emergencySate = true;
    }

    public void exitEmergency(){
        emergencySate = false;
    }

    public boolean isInterrupted() {
        return interrupted;
    }

    public void setInterrupted(boolean interrupted) {
        this.interrupted = interrupted;
    }

    public Semaphore getSemaphore() {
        return this.semaphoreCount;
    }

    public Semaphore getSemaphoreSync() {
        return this.semaphoreSync;
    }

    public ArrayBlockingQueue<Integer> getQueue() {
        return this.queue;
    }

    public void gotoPos(int pos){
        axis.gotoPos(pos);
        this.semaphoreSync.release();
    }
    
    @Override
    public void run(){
        interrupted = false;
        int pos;

        while(!interrupted){
            try {
                if(emergencySate) continue;
                this.semaphoreCount.acquire();
                pos = this.queue.take();
                this.gotoPos(pos);
            } catch (InterruptedException e) {
                Logger.getLogger(AxisThread.class.getName()).log(Level.SEVERE, "Axis thread crashed", e);
            }
        }
    }
}
