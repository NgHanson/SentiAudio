package com.microsoft.projectoxford.emotionsample.thread;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.WorkerThread;

import com.microsoft.projectoxford.emotionsample.tarsos.HandleMachineLearn;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ProcessManager {

    private Handler mHandler;
    private static int NUMBER_OF_CORES =
            Runtime.getRuntime().availableProcessors();
    private final BlockingQueue<Runnable> mDecodeWorkQueue;
    // Sets the amount of time an idle thread waits before terminating
    private static final int KEEP_ALIVE_TIME = 1;
    // Sets the Time Unit to seconds
    private static final TimeUnit KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;

    private ThreadPoolExecutor mDecodeThreadPool;

    public ProcessManager(){
        // Defines a Handler object that's attached to the UI thread
        mHandler = new Handler(Looper.getMainLooper()) {
            /*
             * handleMessage() defines the operations to perform when
             * the Handler receives a new Message to process.
             */
            @Override
            public void handleMessage(Message inputMessage) {

            }
        };
        // Instantiates the queue of Runnables as a LinkedBlockingQueue
        mDecodeWorkQueue = new LinkedBlockingQueue<Runnable>();
        // Creates a thread pool manager
        mDecodeThreadPool = new ThreadPoolExecutor(
                NUMBER_OF_CORES,       // Initial pool size
                NUMBER_OF_CORES,       // Max pool size
                KEEP_ALIVE_TIME,
                KEEP_ALIVE_TIME_UNIT,
                mDecodeWorkQueue);
    }

    public void executeTask(HandleMachineLearn decodeTask){
        mDecodeThreadPool.execute(decodeTask);
        //mDecodeThreadPool.submit(decodeTask); Don't run this twice
    }

}