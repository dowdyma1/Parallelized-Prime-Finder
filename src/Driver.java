import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
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

    public static void main(String[] args) throws IOException {
        List<Integer> sequentialValues = IntStream.rangeClosed(lowerBound, upperBound).boxed().collect(Collectors.toList());
        List<Integer> allValues = new ArrayList<>();

        // "lazy" shuffling, alternative to Collections.shuffle(sequentialValues)
        ArrayList<List<Integer>> slices = new ArrayList<>();
        int numSlices = 100000;
        int sliceSize = upperBound/numSlices;

        for(int i = 0; i < upperBound; i += sliceSize){
            slices.add(sequentialValues.subList(i, i+sliceSize));
        }
        Collections.shuffle(slices);

        for(List<Integer> slice: slices){
            allValues.addAll(slice);
        }
        // "lazy" shuffling complete

        int bucketSize = allValues.size()/numThreads;

        long startTime = System.nanoTime();
        for(int i = 0; i < numThreads; i++){
            AtomicBoolean completionBoolean = new AtomicBoolean(false);

            List<Integer> bucket = allValues.subList(i*bucketSize, (i+1)*bucketSize);

            PrimeFinder curPrimeFinder = new PrimeFinder(bucket, completionBoolean);
            completionBooleans.add(completionBoolean);

            new Thread(curPrimeFinder).start();
        }

        while(!allThreadsComplete()){}
        long endTime = System.nanoTime();

        primes.sort(Collections.reverseOrder());
        long primesSum = primes.stream().mapToLong(Integer::intValue).sum();


        FileWriter fileWriter = new FileWriter("primes.txt");
        PrintWriter printWriter = new PrintWriter(fileWriter);

        printWriter.print((endTime-startTime) + " " + primes.size() + " " + primesSum + "\n");

        ArrayList<Integer> topTen = new ArrayList<>(primes.subList(0, 10));
        Collections.reverse(topTen);
        for(Integer prime: topTen){
            printWriter.print(prime + " ");
        }
        printWriter.close();
    }
}
