public interface Axis{
    public void moveFoward();
    public void moveBack();
    public void stop();
    public int getPos();

    public void gotoPos(int pos);
}