import org.apache.commons.math3.genetics.*;
import org.apache.commons.math3.exception.*;
import java.util.*;
import jp.crestmuse.cmx.filewrappers.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class MyMutation implements MutationPolicy {
  int min, max;

  // 28-50
  MyMutation(int min, int max) {
    this.min = min;
    this.max = max;
  }

  public Chromosome mutate(Chromosome original) throws MathIllegalArgumentException {
    MyChromosome c = (MyChromosome) original;
    List<Integer> newrep = new ArrayList<Integer>(c.getRepresentation());
    int i = GeneticAlgorithm.getRandomGenerator().nextInt(c.getLength());
    newrep.set(i, min + (int) ((max - min) * Math.random()));
    return c.newFixedLengthChromosome(newrep);
  }
}
