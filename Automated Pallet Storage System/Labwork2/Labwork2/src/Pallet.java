import java.time.LocalDateTime;

public class Pallet {
    String load_name;
    String reference_code;
    LocalDateTime shipment_date; 
    int pos1;
    int pos2;

    public LocalDateTime get_shipmentDate(){
        return shipment_date;
    }

    public String get_referenceCode(){
        return reference_code;
    }

    public int get_X(){
        return pos1;
    }

    public int get_Z(){
        return pos2;
    }

    public boolean equals(Pallet pallet) {
        if (this.pos1 == pallet.pos1 && this.pos2 == pallet.pos2) return true;
        return false;
    }

    @Override
    public String toString() {
        return "load_name: " + load_name + "\nreference_code: " + reference_code + "\nshipment_date: " + shipment_date + "\nX Coordinate: " + pos1 + "\nZ Coordinate: " +pos2;
    }
}
