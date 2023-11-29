public class AxisY implements Axis{
    private int currentPos;

    public AxisY() {
        currentPos = 2;
    }

    @Override
    public void moveFoward(){
        Storage.moveYInside();
    }

    @Override
    public void moveBack(){
        Storage.moveYOutside();
    }

    @Override
    public void stop(){
        Storage.stopY();
    }

    @Override
    public int getPos(){
        return Storage.getYPos();
    }

    @Override
    public void gotoPos(int pos){
        if (currentPos == pos) {return;}
        else if (pos > currentPos) {this.moveFoward();}
        else {this.moveBack();}

        while(this.getPos() != pos);
        this.stop();

        currentPos = pos;
        return;
    }
}
