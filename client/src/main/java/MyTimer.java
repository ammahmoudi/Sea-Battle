import java.util.concurrent.TimeUnit;

public
class MyTimer {
    boolean x=true;

    long startTime;
    long timePassed;
    long secondsPassed;
    public
    MyTimer() {

    }
    public void start() throws InterruptedException {

        while(true) {
            this.startTime=System.currentTimeMillis();
            TimeUnit.SECONDS.sleep(1);
             timePassed = System.currentTimeMillis() - startTime;
             secondsPassed = timePassed / 1000;
        }
    }
}
