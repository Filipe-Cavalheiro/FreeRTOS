import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class DateThread extends Thread{  
    private ArrayList<Pallet> storedBoxes;
    private SystemOut systemOut; 
    private static boolean emergencySate = false;
    private boolean interrupted = false;

    public void printPalettes() {
        System.out.print("Pallets in system:");
        if(storedBoxes.size() == 0)
            System.out.println("No Pallets in the System");
        System.out.print("\n");
        for (Pallet pallet : storedBoxes) {
            System.out.println(pallet.toString());
        }
    }

    public void printUniquePalettes() {
        System.out.print("Pallets in system:");
        if(storedBoxes.size() == 0)
            System.out.println("No Pallets in the System");
        System.out.print("\n");
        Set<String> uniquePalettes = new HashSet<>();
        for (Pallet pallet : storedBoxes) {
            String key = pallet.get_referenceCode();
            if (uniquePalettes.add(key)) {
                System.out.println(pallet.toString());
            }
        }
    }

    public void printRefCode(String referenceCode) {
         System.out.println("Pallets in system:");
        boolean none = true;
        for (Pallet pallet : storedBoxes) {
            if (pallet.get_referenceCode().equals(referenceCode)) {
                System.out.println("X Coordinate: " + pallet.get_X());
                System.out.println("Z Coordinate: " + pallet.get_Z());
                System.out.println("Shipment Date: " + pallet.get_shipmentDate());
                none = false;
            }
        }
        if(none)
            System.out.println("Palette with reference code \"" + referenceCode + "\" not found in system.");
    }

    public void setEmergency(){
        emergencySate = true;
    }

    public void exitEmergency(){
        emergencySate = false;
    }

    public void setInterrupted() {
        interrupted = true;
    }

    public DateThread() {
       storedBoxes = new ArrayList<Pallet>();
    }

    public void setSystemOut(SystemOut sys) {
        this.systemOut = sys;
    }

    public void addPallet(Pallet box){
        storedBoxes.add(box);
    }

    public void removePallet(Pallet box){
        storedBoxes.remove(box);
    }

    public int size(){
        return storedBoxes.size();
    }

    public Pallet getindex(int index){
        return storedBoxes.get(index);
    }

    public boolean usedRefCode(String refCode) {
        for (Pallet pallet : storedBoxes) {
            if (pallet.reference_code.equals(refCode)) {
                return true;
            }
        }
        return false;
    }

    public boolean deliverWithRefCode(String refCode) throws InterruptedException {
        boolean exists = false;
        for (Pallet pallet : storedBoxes) {
            if (pallet.reference_code.equals(refCode)) {
                systemOut.add2pallet2remove(pallet);
                exists = true;
            }
        }
        return exists;
    }

    public boolean deliverWithPos(int posX, int posZ) throws InterruptedException {
        boolean exists = false;
        for (Pallet pallet : storedBoxes) {
            if (pallet.pos1 == posX && pallet.pos2 == posZ) {
                systemOut.add2pallet2remove(pallet);
                exists = true;
            }
        }
        return exists;
    }

    private void deliverPallet(){
        LocalDateTime now = LocalDateTime.now();
        Iterator<Pallet> iterator = storedBoxes.iterator();

        while (iterator.hasNext()) {
            Pallet box = iterator.next();
            if (box == null) {return;}
            if (box.get_shipmentDate().isBefore(now)) {
                systemOut.add2pallet2remove(box);
                iterator.remove();
            }
        }
    }

    @Override
    public void run(){
        while(!interrupted){
            if(emergencySate) continue;
            this.deliverPallet();
        }
    }
}
