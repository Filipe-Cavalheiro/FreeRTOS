import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Iterator;

public class SystemOut extends Thread {
    private ArrayBlockingQueue<Pallet> pallet2remove;
    private DateThread dateThread;
    private Mechanism mechanism;
    private LedBlincking1 ledBlincking;
    private int remPal;
    private Semaphore semaphore;
    private boolean interrupted;
    private boolean emergencySate;

    public SystemOut(DateThread dateThread, Mechanism mechanism){
        this.dateThread = dateThread;
        this.pallet2remove = new ArrayBlockingQueue<Pallet>(9);
        this.semaphore = new Semaphore(0);
        this.mechanism = mechanism;
        remPal = 0;
        emergencySate = false;
        interrupted = false;
        this.ledBlincking = new LedBlincking1();
        ledBlincking.start();
    }

    public void printPalettes() {
        System.out.print("Pallets in system:");
        if(pallet2remove.size() == 0)
            System.out.println("No Pallets in the System");
        System.out.print("\n");
        for (Pallet pallet : pallet2remove) {
            System.out.println(pallet.toString());
        }
    }

    public void printUniquePalettes() {
        System.out.println("Pallets to remove:");
        if(pallet2remove.size() == 0)
            System.out.println("No Pallets to remove");
        Set<String> uniquePalettes = new HashSet<>();
        for (Pallet pallet : pallet2remove) {
            String key = pallet.get_referenceCode();
            if (uniquePalettes.add(key)) {
                System.out.println(pallet.toString());
            }
        }
    }

    public void printRefCode(String referenceCode) {
        System.out.println("Pallets to remove:");
        boolean none = true;
        for (Pallet pallet : pallet2remove) {
            if (pallet.get_referenceCode().equals(referenceCode)) {
                System.out.println("X Coordinate: " + pallet.get_X());
                System.out.println("Z Coordinate: " + pallet.get_Z());
                System.out.println("Shipment Date: " + pallet.get_shipmentDate());
                none = false;
            }
        }
        if(none)
            System.out.println("Palette with reference code \"" + referenceCode + "\" not found to be removed.");
    }

    public int size(){
        return pallet2remove.size();
    }

    public void removeRefCode(String referenceCode) throws InterruptedException{
        System.out.print("Pallets to remove:");
        Iterator<Pallet> iter = pallet2remove.iterator();
        Pallet pallet;
        while (iter.hasNext()) {
            if (interrupted) {return;}
            while(emergencySate) {continue;}
            pallet = iter.next();
            if (pallet.get_referenceCode().equals(referenceCode)) {
                System.out.println(pallet.toString());
                dateThread.removePallet(pallet);
                pallet2remove.remove(pallet);
                mechanism.takePartFromCell(pallet.pos1, pallet.pos2);
                --remPal;
            }
        }
    }

    public void removePos(int posX, int posZ) throws InterruptedException{
        System.out.print("Pallets to remove:");
        Iterator<Pallet> iter = pallet2remove.iterator();
        Pallet pallet;
        while (iter.hasNext()) {
            if (interrupted) {return;}
            while(emergencySate) {continue;}
            pallet = iter.next();
            if (pallet.pos1 == posX && pallet.pos2 == posZ) {
                System.out.println(pallet.toString());
                dateThread.removePallet(pallet);
                pallet2remove.remove(pallet);
                mechanism.takePartFromCell(pallet.pos1, pallet.pos2);
                --remPal;
            }
        }
    }

    public boolean posOccupied(int posX, int posZ){
        for (Pallet pallet : pallet2remove) {
            if(pallet.pos1 == posX && pallet.pos2 == posZ)
                return true;
        }
        return false;
    }

    public Semaphore getSemaphore() {
        return semaphore;
    }

    public void setEmergency() {
        emergencySate = true;
    }

    public void exitEmergency() {
        emergencySate = true;
    }

    public void setInterrupted() {
        interrupted = true;
        ledBlincking.setInterrupted(true);
    }

    public void add2pallet2remove(Pallet box) {
        pallet2remove.add(box);
        ++remPal;
        if (remPal == 1) {
            ledBlincking.getSemaphore().release();
        }
    }

    private void removeSystemPallet() {
        Pallet box;
        while (remPal > 0){
            if(interrupted) {return;}
            if(emergencySate) {continue;}

            try {
                box = pallet2remove.take();
                dateThread.removePallet(box);
                mechanism.takePartFromCell(box.pos1, box.pos2);
                --remPal;
            } catch (InterruptedException e) {
                System.out.println(e);
            }
        }
    }

    @Override
    public void run() {
        while (!interrupted) {
            if (emergencySate) {continue;}
            try {
                semaphore.acquire();
                this.removeSystemPallet();
            } catch (InterruptedException e) {
                Logger.getLogger(AxisThread.class.getName()).log(Level.SEVERE, "System out thread crashed", e);
            }
        }
    }

    private class LedBlincking1 extends Thread {
        private Semaphore semaphore;
        private boolean interrupted;

        public LedBlincking1() {
            this.semaphore = new Semaphore(0);
            this.interrupted = false;
            this.setDaemon(true);
        }

        public void setInterrupted(boolean interrupted) {
            this.interrupted = interrupted;
        }

        public boolean isInterrupted() {
            return this.interrupted;
        }

        public Semaphore getSemaphore() {
            return this.semaphore;
        }

        private void blinckLed() throws InterruptedException{
            while (remPal != 0) {
                if(interrupted) {return;}
                if(emergencySate) {continue;}
                Storage.ledOn(1);
                Thread.sleep(1000);
                Storage.ledsOff();
                Thread.sleep(1000);
            }
        }

        public void run() {
            while(!interrupted) {
                try {
                    semaphore.acquire();
                    blinckLed();
                } catch (InterruptedException e) {
                    Logger.getLogger(AxisThread.class.getName()).log(Level.SEVERE, "Axis thread crashed", e);
                }
            }
        }
    }
}
