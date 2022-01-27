import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class PrimeFinder implements Runnable{
    private final List<Integer> numbers;
    private final AtomicBoolean readyToGive;
    private final AtomicBoolean readyToStore;
    private final AtomicInteger storePrime;
    private final AtomicBoolean completionBoolean;

    public PrimeFinder(List<Integer> numbers, AtomicBoolean readyToGive, AtomicBoolean readyToStore,
                       AtomicInteger storePrime, AtomicBoolean completionBoolean){
        this.numbers = numbers;
        this.readyToGive = readyToGive;
        this.readyToStore = readyToStore;
        this.storePrime = storePrime;
        this.completionBoolean = completionBoolean;
    }

    static boolean isPrime(int n) {
        if (n <= 1)
            return false;
        else if (n == 2)
            return true;
        else if (n % 2 == 0)
            return false;

        for (int i = 3; i <= Math.sqrt(n); i += 2) {
            if (n % i == 0){
                return false;
            }
        }
        return true;
    }

    // 1. there are no other threads readyToGive, acquire it.
    // 2. the storageManager is ready to accept primes
    private boolean givePrimeToStorage(Integer prime){
        if(readyToStore.get() && readyToGive.compareAndSet(false, true)){
            System.out.println("Giving prime " + prime + " to storage.");
            storePrime.set(prime);
            readyToGive.set(false);
            readyToStore.set(false);
            return true;
        }
        return false;
    }

    public void run(){
        System.out.println("Started thread " + Thread.currentThread().getId());
        Queue<Integer> givingQueue = new LinkedList<>();
        for(Integer num: numbers){
            boolean numIsPrime = isPrime(num);
            if(numIsPrime){
                givingQueue.add(num);
//                System.out.println("Found prime: " + num);
            }
            if(!givingQueue.isEmpty()){
                if(givePrimeToStorage(givingQueue.element())){
                    givingQueue.remove();
                }
            }
        }

        System.out.println(Thread.currentThread().getId() + ": " + givingQueue.size() + " to process");
        while(!givingQueue.isEmpty()){
            if(givePrimeToStorage(givingQueue.element())){
                givingQueue.remove();
            }
        }
        completionBoolean.set(true);
    }
}
