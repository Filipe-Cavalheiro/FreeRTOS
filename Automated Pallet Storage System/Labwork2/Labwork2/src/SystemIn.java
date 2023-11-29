import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SystemIn extends Thread {
    private boolean interrupted; 
    private boolean emergencyState;
    private ArrayBlockingQueue<Pallet> palletsToAdd;
    private Mechanism mechanism;
    private DateThread dateThread;

    public SystemIn(DateThread dateThread, Mechanism mechanism) {
        interrupted = false;
        emergencyState = false;
        palletsToAdd = new ArrayBlockingQueue<Pallet>(9);
        this.mechanism = mechanism;
        this.dateThread = dateThread;
    }

    public boolean addPallet(Pallet pallet) {
        if (!palletsToAdd.isEmpty()) {
            for (Pallet pal : palletsToAdd) {
                if (pal.equals(pallet)) {
                    return false;
                }
            }
        }
        
        palletsToAdd.add(pallet);
        return true;
    }

    public int size(){
        return palletsToAdd.size();
    }

    public void setEmergency() {
        emergencyState = true;
    }

    public void exitEmergency() {
        emergencyState = false;
    }

    public void setInterrupted() {
        interrupted = true;
    }

    private void placePallet() throws InterruptedException {
        Pallet pallet = palletsToAdd.take();
        mechanism.putPartInCell(pallet.pos1, pallet.pos2);
        dateThread.addPallet(pallet);
    }

    @Override
    public void run() {
        while (!interrupted) {
            if (emergencyState) {continue;}
            try {
                placePallet();
            } catch (InterruptedException e) {
                Logger.getLogger(AxisThread.class.getName()).log(Level.SEVERE, "System In thread crashed", e);
            }
        }
    }
}
