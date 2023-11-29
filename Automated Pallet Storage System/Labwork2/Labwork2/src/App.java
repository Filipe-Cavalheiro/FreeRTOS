import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class App {
    private static final  int MAX_SYSTEM_CAPACITY = 9;
    
    static Pallet box = new Pallet();
    static Scanner scan = new Scanner(System.in);
    static DateThread dateThread = new DateThread();
    static boolean emergencyState = false;
    static SystemOut systemOut;
    static SystemIn systemIn;
    static int pos1 = 0;
    static int pos2 = 0;
    
    private static AxisThread axisX = new AxisThread(new AxisX());
    private static AxisThread axisY = new AxisThread(new AxisY());
    private static AxisThread axisZ = new AxisThread(new AxisZ());
    private static Mechanism mechanism;

    private static boolean positionAvailability(Pallet box){
        box.pos1 = 0;
        while((box.pos1 < 1) || (box.pos1 > 3)){
            System.out.print("X pos [1, 3]: ");       
            box.pos1 = scan.nextInt();
        }
        box.pos2 = 0;
        while((box.pos2 < 1) || (box.pos2 > 3)){
            System.out.print("Y pos [1, 3]: ");
            box.pos2 = scan.nextInt();
        }
        for(int i = 0; i <= dateThread.size()-1; ++i){
            if((box.pos1 == dateThread.getindex(i).get_X()) && (box.pos2 == dateThread.getindex(i).get_Z())){
                return false;
            }
        }
        if(systemOut.posOccupied(box.pos1, box.pos2))
                return false;
        return true;
    }

    public static void main(String[] args) throws Exception {
        Storage.initializeHardwarePorts();
        System.out.println("\u001B[31m    ___         __         \u001B[32m  ____        ____     __     _____ __                                 _____            __               ");
        System.out.println("\u001B[31m   /   | __  __/ /_____     \u001B[32m/ __ \\____ _/ / /__  / /_   / ___// /_____  _________ _____ ____     / ___/__  _______/ /____  ____ ___ ");
        System.out.println("\u001B[31m  / /| |/ / / / __/ __ \\   \u001B[32m/ /_/ / __ `/ / / _ \\/ __/   \\__ \\/ __/ __ \\/ ___/ __ `/ __ `/ _ \\    \\__ \\/ / / / ___/ __/ _ \\/ __ `__ \\");
        System.out.println("\u001B[31m / ___ / /_/ / /_/ /_/ /  \u001B[32m/ ____/ /_/ / / /  __/ /_    ___/ / /_/ /_/ / /  / /_/ / /_/ /  __/   ___/ / /_/ (__  ) /_/  __/ / / / / /");
        System.out.println("\u001B[31m/_/  |_/__,_/\\__/\\____/ \u001B[32m /_/    \\__,_/_/_/\\___/\\__/   /____/\\__/\\____/_/   \\__,_/\\__, /\\___/   /____/\\__, /____/\\__/\\___/_/ /_/ /_/ ");
        System.out.println("\u001B[31m                          \u001B[32m                                                      /____/              /____/                          \u001B[0m");

        axisX.start();
        axisY.start();
        axisZ.start();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        mechanism = new Mechanism(axisX, axisY, axisZ);
        scan.useDelimiter(System.getProperty("line.separator"));

        Runnable switches = () -> {
            while (true) {
                if(mechanism.bothSwitchesPressed() && !emergencyState){
                    System.out.println("Entered Emergency State");
                    emergencyState = true;
                    mechanism.setEmergency();
                    dateThread.setEmergency();
                    systemIn.setEmergency();
                    systemOut.setEmergency();
                }
                else if(emergencyState){
                    if(mechanism.swicth1Pressed() && !mechanism.swicth2Pressed()){
                        System.out.println("Exited Emergency State");
                        while (mechanism.swicth1Pressed());
                        emergencyState = false;
                        mechanism.exitEmergency();
                        systemIn.exitEmergency();
                        systemOut.exitEmergency();
                        dateThread.exitEmergency();
                    }
                    else if (!mechanism.swicth1Pressed() && mechanism.swicth2Pressed()){
                        while (mechanism.swicth2Pressed());
                        System.out.println("Reseting System");
                        dateThread.setInterrupted();
                        systemOut.setInterrupted();
                        systemIn.setInterrupted();
                        axisX.setInterrupted(true);
                        axisZ.setInterrupted(true);
                        axisY.setInterrupted(true);

                        AxisThread axisX = new AxisThread(new AxisX());
                        AxisThread axisY = new AxisThread(new AxisY());
                        AxisThread axisZ = new AxisThread(new AxisZ());
                        
                        axisX.start();
                        axisY.start();
                        axisZ.start();
                        
                        mechanism.exitEmergency();
                        mechanism = new Mechanism(axisX, axisZ, axisY);
                        
                        CalibrationThread calibrationThread = new CalibrationThread(axisX, axisZ, axisY);
                        calibrationThread.start();
                        try {
                            calibrationThread.join();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        
                        dateThread = new DateThread();
                        systemOut = new SystemOut(dateThread, mechanism);
                        systemIn = new SystemIn(dateThread, mechanism);
                        dateThread.setSystemOut(systemOut);
                        
                        dateThread.start();
                        systemOut.start();
                        systemIn.start();
                        
                        emergencyState = false;
                    }
                    continue; 
                }
                else if (mechanism.swicth1Pressed() && !mechanism.swicth2Pressed()) {
                    while (mechanism.swicth1Pressed());
                    systemOut.getSemaphore().release();
                }
                else if (!mechanism.swicth1Pressed() && mechanism.swicth2Pressed()) {
                    while (mechanism.swicth2Pressed());
                    mechanism.relSemaphoreSync();
                }
            }
        };
            
        Thread switchesThread = new Thread(switches);
        switchesThread.setName("Switches");
        switchesThread.start();
        
        CalibrationThread calibrationThread = new CalibrationThread(axisX, axisZ, axisY);
        calibrationThread.start();
        calibrationThread.join();
        
        systemOut = new SystemOut(dateThread, mechanism);
        systemIn = new SystemIn(dateThread, mechanism);
        dateThread.setSystemOut(systemOut);
        systemOut.start();
        systemIn.start();
        dateThread.start();
        
        int op = -1;
        boolean invalidDate = false;
        while (op != 0) {
            while (emergencyState);
            System.out.println("0:Exit 1:Add a Pallet 2:Remove a Pallet with reference 3:Remove a Pallet with position 4:General Info");
            System.out.print("Enter an option: ");
            op = scan.nextInt();
            if (emergencyState) {continue;}

            switch(op){
                case 0: System.out.println("GoodBye ..."); break;

                case 1: 
                    if((dateThread.size() + systemOut.size() + systemIn.size()) == MAX_SYSTEM_CAPACITY){
                        System.out.println("\u001B[31mSystem is full\u001B[0m");
                        break;
                    }
                    box = new Pallet();
                    System.out.print("pallet load name: ");
                    box.load_name = scan.next();
                    System.out.print("reference code: ");
                    box.reference_code = scan.next();

                    do{
                        invalidDate = false;
                        System.out.print("Enter shipment date: ");
                        String dateString = scan.next();
                        try {
                            box.shipment_date = LocalDateTime.parse(dateString, formatter);
                            //String toremove = scan.next();
                            //box.shipment_date = LocalDateTime.now().plusSeconds(30);
                        } catch(Exception exception) {
                            invalidDate = true;
                            System.out.println("\u001B[31mPlease enter the date in the format dd/MM/yyyy HH:mm:ss.\u001B[0m");
                        }
                    }while(invalidDate);
                    while(positionAvailability(box) == false) System.out.println("\u001B[31mpallet already in that position\u001B[0m"); 
                    systemIn.addPallet(box);
                break;

                case 2:
                    if(dateThread.size() == 0){
                        System.out.println("\u001B[31mNo pallets in the system\u001B[0m");
                        break;
                    }System.out.print("Reference code: ");
                    String refCode2remove = scan.next();

                    new Thread(() -> {
                        try {
                            if (!dateThread.deliverWithRefCode(refCode2remove)) {
                                System.out.println("\u001B[31mNo pallets with that reference code\u001B[0m");
                                return;
                            }
                            systemOut.removeRefCode(refCode2remove); 
                        } catch (InterruptedException e) {
                            Logger.getLogger(AxisThread.class.getName()).log(Level.SEVERE, "Delivery by reference code thread crashed", e);
                        }
                    }).start();
                break;

                case 3:
                if(dateThread.size() == 0){
                    System.out.println("\u001B[31mNo pallets in the system\u001B[0m");
                    break;
                }
                while((pos1 < 1) || (pos1 > 3)){
                    System.out.print("X pos [1, 3]: ");       
                    pos1 = scan.nextInt();
                }
                while((pos2 < 1) || (pos2 > 3)){
                    System.out.print("Y pos [1, 3]: ");
                    pos2 = scan.nextInt();
                }
                
                new Thread(() -> {
                    try {
                        if (!dateThread.deliverWithPos(pos1, pos2)) {
                            System.out.println("\u001B[31mNo pallets in that position\u001B[0m");
                            return;
                        }
                        systemOut.removePos(pos1, pos2);
                    } catch (InterruptedException e) {
                        Logger.getLogger(AxisThread.class.getName()).log(Level.SEVERE, "Delivery by position code thread crashed", e);
                    }
                }).start();
                break;

                case 4:
                    System.out.println("0:Back 1:Pallets in the System 2:Pallets with reference code 3:System Info");
                    System.out.print("Enter an option:");
                    int op2 = scan.nextInt();
                    switch(op2){
                        case 0: break;
                        case 1: dateThread.printUniquePalettes(); systemOut.printUniquePalettes(); break;
                        case 2:
                            System.out.print("\rReference code: ");
                            String refCode2check = scan.next();
                            dateThread.printRefCode(refCode2check);
                            systemOut.printRefCode(refCode2check);
                        break;
                        case 3: 
                            System.out.println("In storage: ");
                            System.out.println("Occupied: " + dateThread.size()); 
                            System.out.println("Free: " + (MAX_SYSTEM_CAPACITY - dateThread.size()));
                            dateThread.printPalettes();
                            System.out.println("To remove: ");
                            System.out.println("Occupied: " + systemOut.size()); 
                            System.out.println("Free: " + (MAX_SYSTEM_CAPACITY - systemOut.size()));
                            systemOut.printPalettes();
                        break;
                        default: System.out.print("\u001B[31mNot a valid option\u001B[0m"); break;
                    }
                    break;
                default: System.out.print("\u001B[31mNot a valid option\u001B[0m"); break;
            }
        }
        scan.close();
        System.exit(0);
    }
}
