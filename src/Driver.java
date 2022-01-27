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

    static final List<Integer> primes = new ArrayList<>();

    static ArrayList<AtomicBoolean> completionBooleans = new ArrayList<>();

    private static boolean allThreadsComplete(){
        for(AtomicBoolean completionBoolean: completionBooleans){
            if (!completionBoolean.get()) {
                return false;
            }
        }
        return true;
    }

    public synchronized static void sendPrimesToStorage(List<Integer> localPrimes){
        primes.addAll(localPrimes);
    }

    public static void main(String args[]){
        List<Integer> sequentialValues = IntStream.rangeClosed(lowerBound, upperBound).boxed().collect(Collectors.toList());

        ArrayList<Thread> threads = new ArrayList<>();

        AtomicBoolean readyToGive = new AtomicBoolean(false);
        AtomicBoolean readyToStore = new AtomicBoolean(false);
        AtomicInteger storePrime = new AtomicInteger();

        ArrayList<List<Integer>> slices = new ArrayList<>();
        int numSlices = 1000;
        int sliceSize = upperBound/numSlices;
        // might not capture all
        for(int i = 0; i < upperBound; i += sliceSize){
            System.out.println(i + ", " + i+sliceSize);
            slices.add(sequentialValues.subList(i, i+sliceSize));
        }

        List<Integer> allValues = new ArrayList<>();
        Collections.shuffle(slices);

        for(List<Integer> slice: slices){
            allValues.addAll(slice);
        }

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

        while(!allThreadsComplete()){}

        Collections.sort(primes, Collections.reverseOrder());

        System.out.println("Number of primes: " + primes.size() + "\nPrimes sum: " +
                primes.stream().mapToLong(Integer::intValue).sum());

        System.out.println("Highest 10 primes:");

        for(Integer prime: primes.subList(0, 9)){
            System.out.print(prime + " ");
        }

    }
}
