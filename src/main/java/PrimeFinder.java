import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class PrimeFinder implements Runnable{
    private final List<Integer> numbers;
    private final AtomicBoolean completionBoolean;

    public PrimeFinder(List<Integer> numbers, AtomicBoolean completionBoolean){
        this.numbers = numbers;
        this.completionBoolean = completionBoolean;
    }

    static boolean isPrime(int n) {
        if (n <= 1)
            return false;
        if (n <= 3)
            return true;

        if (n % 2 == 0 || n % 3 == 0)
            return false;

        for (int i = 5; i * i <= n; i = i + 6)
            if (n % i == 0 || n % (i + 2) == 0)
                return false;

        return true;
    }

    public void run(){
//        System.out.println("Started thread " + Thread.currentThread().getId());
        List<Integer> localPrimes = new ArrayList<>();
        for(Integer num: numbers){
            boolean numIsPrime = isPrime(num);
            if(numIsPrime){
                localPrimes.add(num);
            }
        }
        Driver.sendPrimesToStorage(localPrimes);

        completionBoolean.set(true);
    }
}
