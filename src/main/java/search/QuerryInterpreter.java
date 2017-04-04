package search;

import java.io.IOException;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

/**
 * Created by Gheorghe on 3/13/2017.
 */
public class QuerryInterpreter {

    public static Set<String> interpretQuerry(String querry) throws IOException {
        Set<String> finalSet = new HashSet<>();
        Queue<String> querryQueue = QuerryParser.parse(querry);

        //ToDo forma canonica

        Operations operations = new Operations();

        if(querryQueue.size() == 1){
            return operations.getSetForKey(querryQueue.poll());
        } else {
            String cuv1 = querryQueue.poll();
            String cuv2 = querryQueue.poll();

            if(cuv2.charAt(0) == '+') {
                finalSet.addAll(operations.doIntersection(cuv1,cuv2.replace("+","")));
            } else if(cuv2.charAt(0) == '-'){
                finalSet.addAll(operations.doDifference(cuv1, cuv2.replace("-","")));
            } else {
                finalSet.addAll(operations.doUnion(cuv1, cuv2));
            }

            if(querryQueue.size() != 0){
                finalSet = doOperation(finalSet, querryQueue, operations);
            }
        }
        return finalSet;
    }

    private static Set<String> doOperation(Set<String> set, Queue<String> queue, Operations operations){
        Set<String> finalSet = new HashSet<>();
        finalSet.addAll(set);
        while(queue.size() != 0){
            String cuv = queue.poll();
            if(cuv.charAt(0) == '+'){
                finalSet = operations.doIntersection(finalSet, cuv.replace("+",""));
            } else if (cuv.charAt(0) == '-'){
                finalSet = operations.doDifference(finalSet, cuv.replace("-",""));
            } else {
                finalSet = operations.doUnion(finalSet, cuv);
            }
        }
        return finalSet;
    }
}
