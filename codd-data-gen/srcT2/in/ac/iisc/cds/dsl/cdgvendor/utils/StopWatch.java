package in.ac.iisc.cds.dsl.cdgvendor.utils;

public class StopWatch {

    private final String name;
    private long         startTime;
    private long         pauseStartTime;
    private boolean      isPaused;
    private boolean      isTerminated;

    public StopWatch(String name) {
        this.name = name;
        startTime = System.currentTimeMillis();
        isPaused = false;
        isTerminated = false;
    }

    public void pause() {
        checkNotTerminated();
        if (isPaused)
            throw new RuntimeException("Trying to take pause when already paused");
        pauseStartTime = System.currentTimeMillis();
        isPaused = true;
    }

    public void resume() {
        checkNotTerminated();
        if (!isPaused)
            throw new RuntimeException("Trying to take resme when not paused");
        startTime += System.currentTimeMillis() - pauseStartTime;
        isPaused = false;
    }

    public void displayTimeAndDispose() {
        checkNotTerminated();
        if (isPaused)
            throw new RuntimeException("Trying to take snapshot when paused");
        long timeElapsed = System.currentTimeMillis() - startTime;
        DebugHelper.printInfo("StopWatch " + name + " took " + timeElapsed + "ms");
        isTerminated = true;
    }

    public long getTimeAndDispose() {
        checkNotTerminated();
        if (isPaused)
            throw new RuntimeException("Trying to take snapshot when paused");
        long timeElapsed = System.currentTimeMillis() - startTime;
        isTerminated = true;
        return timeElapsed;
    }

    private void checkNotTerminated() {
        if (isTerminated)
            throw new RuntimeException("Trying to operate terminated StopWatch");
    }
}
