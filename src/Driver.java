import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Driver {
    static final int upperBound = 100000000;
    static final int lowerBound = 1;
    static final int numThreads = 8;

    static ArrayList<AtomicBoolean> completionBooleans = new ArrayList<>();

    private static boolean allThreadsComplete(){
        for(AtomicBoolean completionBoolean: completionBooleans){
            if (!completionBoolean.get()) {
                return false;
            }
        }
        return true;
    }

    public static void main(String args[]){
        List<Integer> allValues = IntStream.rangeClosed(lowerBound, upperBound).boxed().collect(Collectors.toList());
        //int[] vals = IntStream.range(lowerBound, upperBound).toArray();
        //List<Integer> allValues = Arrays.stream(vals).boxed().toList();
//        ArrayList<Integer> allValues = new ArrayList<>();
        ArrayList<Thread> threads = new ArrayList<>();

//        for(int i=1; i < upperBound; i++){
//            allValues.add(i);
//        }
//
        AtomicBoolean readyToGive = new AtomicBoolean(false);
        AtomicBoolean readyToStore = new AtomicBoolean(false);
        AtomicInteger storePrime = new AtomicInteger();

        Collections.shuffle(allValues);

        int bucketSize = allValues.size()/numThreads;
        for(int i = 0; i < numThreads; i++){
            AtomicBoolean completionBoolean = new AtomicBoolean(false);

            List<Integer> bucket = allValues.subList(i*bucketSize, (i+1)*bucketSize);

            PrimeFinder curPrimeFinder = new PrimeFinder(bucket, readyToGive, readyToStore, storePrime,
                    completionBoolean);
            completionBooleans.add(completionBoolean);

            Thread thread = new Thread(curPrimeFinder);
            threads.add(thread);
            thread.start();
        }

        // storage manager
        ArrayList<Integer> primes = new ArrayList<>();
        readyToStore.set(true);
        while(!allThreadsComplete()){
            if(readyToGive.get()){
                primes.add(storePrime.get());
                readyToStore.set(true);
            }
        }

        Collections.sort(primes, Collections.reverseOrder());

        System.out.println("Number of primes: " + primes.size() + "\nPrimes sum: " +
                primes.stream().mapToLong(Integer::intValue).sum());

        System.out.println("Highest 10 primes:");

        for(Integer prime: primes.subList(0, 9)){
            System.out.print(prime + " ");
        }

    }
}
