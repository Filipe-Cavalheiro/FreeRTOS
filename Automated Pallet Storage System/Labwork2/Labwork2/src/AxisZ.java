public class AxisZ implements Axis{
    private int currentPos;

    public AxisZ() {
        currentPos = 10;
    }

    @Override
    public void moveFoward(){
        Storage.moveZUp();
    }

    @Override
    public void moveBack(){
        Storage.moveZDown();
    }

    @Override
    public void stop(){
        Storage.stopZ();
    }

    @Override
    public int getPos(){
        return Storage.getZPos();
    }

    @Override
    public void gotoPos(int pos){
        if (pos == currentPos) {return;}
        else if (pos > currentPos) {this.moveFoward();} 
        else {this.moveBack();}
        
        while(this.getPos() != pos);
        this.stop();

        currentPos = pos;
    }
}
