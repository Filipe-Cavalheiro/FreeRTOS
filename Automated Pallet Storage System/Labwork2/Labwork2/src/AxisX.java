public class AxisX implements Axis{
    private int currentPos;

    public AxisX() {
        currentPos = 2;
    }

    @Override
    public void moveFoward(){
        Storage.moveXRight();
    }

    @Override
    public void moveBack(){
        Storage.moveXLeft();
    }

    @Override
    public void stop(){
        Storage.stopX();
    }

    @Override
    public int getPos(){
        return Storage.getXPos();
    }

    @Override
    public void gotoPos(int pos){
        if (pos == currentPos) {return;}
        else if (pos > currentPos) {this.moveFoward();}    
        else {this.moveBack();}

        while(this.getPos() != pos);
        this.stop();

        currentPos = pos;
        return;
    }
}
