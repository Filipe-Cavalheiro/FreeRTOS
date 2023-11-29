import java.util.concurrent.Semaphore;

public interface LongThread {
    public boolean isInterrupted();
    public void setInterrupted(boolean interrupted);
    public void run();
    public Semaphore getSemaphore();
}
