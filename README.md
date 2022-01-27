# Parallelized Prime Finder

## System requirements:
- maven
- Java 11 or higher

## Instructions:

### Build from source
`mvn install`

### Run from jar
`java -jar target\Parallelized-Prime-Finder-1.0-SNAPSHOT.jar`

## Proof of Correctness
The Driver enumerates a list of integers from 1 to 10^8, then shuffles them.
It then splits this list into eight sections and gives each `PrimeFinder` a section
to work on. The `Driver` also shares an `AtomicBoolean` with each `PrimeFinder` so
that it will know when each `PrimeFinder` is finished. Each thread is started, and the `Driver` waits until all `PrimeFinder` threads
are complete.

Upon start, each `PrimeFinder` iterates over each `Integer` in its section and calls
a function called `isPrime(n)`. Once a prime is found, it adds it to a list. After
`PrimeFinder` has iterated over all integers in its section, it sends its list
to the `Driver` using a `synchronized` method called `sendPrimesToStorage(list)`.
By the Java definition of the `synchronized` keyword, this function has mutual 
exclusion, so only one thread at a time will be able to use this function. Then the
completion `AtomicBoolean` is set to true.

`isPrime(n)` was inspired from [this website](https://www.geeksforgeeks.org/java-program-to-check-if-a-number-is-prime-or-not/).
It is correct because:
1. it takes care of the corner cases, `n = 1, 2, 3`
2. It only checks until `sqrt(n)` because anything after that is redundant
3. All primes are of the form `6k +- 1` (except 2 and 3)

Finding primes is a well known problem and engineering a new and faster algorithm for this
problem is outside the scope of this class.

## Experimental Evaluation
Runtime on eustis server:

| Number of threads | Runtime |
| ----------------- | ------- |
| 8 | 14.625 | 
| 1 | 76.363 |

## Efficiency

Speed-up = `76.363 / 14.625` = 5.22

Using Amdahl's law, `p`, the fraction of the job that was executed in parallel
`p` = 0.9239
